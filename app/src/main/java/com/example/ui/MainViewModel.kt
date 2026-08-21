package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DailyMetricsEntity
import com.example.data.local.DailyTargetEntity
import com.example.data.local.HabitEntity
import com.example.data.local.WorkoutEntity
import com.example.data.repository.HealthRepository
import com.example.sensor.StepSensorTracker
import com.example.util.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HealthUiState(
    val metrics: DailyMetricsEntity = DailyMetricsEntity(date = "Today"),
    val dailyTargets: List<DailyTargetEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val workouts: List<WorkoutEntity> = emptyList(),
    val disciplineHistory: List<com.example.data.local.DisciplineDayEntity> = emptyList(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val isSendingChat: Boolean = false,
    val isRefreshingInsight: Boolean = false,
    val isListeningSpeech: Boolean = false,
    val isSensorActive: Boolean = false,
    val statusMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HealthRepository
    private var stepSensorTracker: StepSensorTracker? = null

    private val _isSendingChat = MutableStateFlow(false)
    private val _isRefreshingInsight = MutableStateFlow(false)
    private val _isListeningSpeech = MutableStateFlow(false)
    private val _isSensorActive = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)

    init {
        val database = AppDatabase.getInstance(application)
        repository = HealthRepository(database.healthDao())
        viewModelScope.launch {
            repository.ensureTodayInitialized()
            repository.ensureWorkoutsInitialized()
            repository.ensureDailyTargetsInitialized()
        }

        // Initialize Real-time Step Hardware Sensor
        try {
            stepSensorTracker = StepSensorTracker(application) { stepDelta ->
                viewModelScope.launch {
                    repository.updateSteps(stepDelta)
                }
            }
            stepSensorTracker?.startTracking()
            _isSensorActive.value = stepSensorTracker?.isSensorAvailable() == true
        } catch (e: Exception) {
            _isSensorActive.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepSensorTracker?.stopTracking()
    }

    private data class CoreDataState(
        val metrics: DailyMetricsEntity?,
        val dailyTargets: List<DailyTargetEntity>,
        val habits: List<HabitEntity>,
        val disciplineHistory: List<com.example.data.local.DisciplineDayEntity>
    )

    private val coreDataFlow = combine(
        repository.latestMetrics,
        repository.allDailyTargets,
        repository.allHabits,
        repository.allDisciplineHistory
    ) { metrics, dailyTargets, habits, disciplineHistory ->
        CoreDataState(metrics, dailyTargets, habits, disciplineHistory)
    }

    private data class SecondaryDataState(
        val workouts: List<WorkoutEntity>,
        val chatMessages: List<ChatMessageEntity>
    )

    private val secondaryDataFlow = combine(
        repository.allWorkouts,
        repository.allChatMessages
    ) { workouts, messages ->
        SecondaryDataState(workouts, messages)
    }

    private data class UiSignals(
        val isSendingChat: Boolean,
        val isRefreshingInsight: Boolean,
        val isListeningSpeech: Boolean,
        val isSensorActive: Boolean,
        val statusMessage: String?
    )

    private val signalsFlow = combine(
        _isSendingChat,
        _isRefreshingInsight,
        _isListeningSpeech,
        _isSensorActive,
        _statusMessage
    ) { sendingChat, refreshingInsight, listeningSpeech, sensorActive, statusMsg ->
        UiSignals(sendingChat, refreshingInsight, listeningSpeech, sensorActive, statusMsg)
    }

    val uiState: StateFlow<HealthUiState> = combine(
        coreDataFlow,
        secondaryDataFlow,
        signalsFlow
    ) { core, secondary, signals ->
        val rawMetrics = core.metrics ?: DailyMetricsEntity(date = repository.getTodayDate())
        HealthUiState(
            metrics = rawMetrics,
            dailyTargets = core.dailyTargets,
            habits = core.habits,
            workouts = secondary.workouts,
            disciplineHistory = core.disciplineHistory,
            chatMessages = secondary.chatMessages,
            isSendingChat = signals.isSendingChat,
            isRefreshingInsight = signals.isRefreshingInsight,
            isListeningSpeech = signals.isListeningSpeech,
            isSensorActive = signals.isSensorActive,
            statusMessage = signals.statusMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HealthUiState()
    )

    fun onDailyTargetDelta(targetId: Long, delta: Float) {
        viewModelScope.launch {
            repository.updateDailyTargetValue(targetId, delta)
        }
    }

    fun onToggleDailyTargetCompleted(targetId: Long) {
        viewModelScope.launch {
            repository.toggleDailyTargetCompleted(targetId)
        }
    }

    fun onAddDailyTarget(
        title: String,
        targetValue: Float,
        unit: String = "pts",
        stepDelta: Float = 10f,
        iconName: String = "sparkle",
        colorHex: Long = 0xFF00E5FF
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addDailyTarget(title, targetValue, unit, stepDelta, iconName, colorHex)
            _statusMessage.value = "Target added: $title"
        }
    }

    fun onUpdateDailyTarget(
        id: Long,
        title: String,
        targetValue: Float,
        unit: String,
        stepDelta: Float,
        iconName: String,
        colorHex: Long
    ) {
        viewModelScope.launch {
            repository.updateDailyTargetDetails(id, title, targetValue, unit, stepDelta, iconName, colorHex)
            _statusMessage.value = "Target updated: $title"
        }
    }

    fun onDeleteDailyTarget(id: Long) {
        viewModelScope.launch {
            repository.deleteDailyTarget(id)
            _statusMessage.value = "Target removed"
        }
    }

    fun onUpdateStepTarget(newTarget: Int) {
        viewModelScope.launch {
            val current = uiState.value.metrics
            repository.updateMetrics(current.copy(stepTarget = newTarget.coerceAtLeast(1000)))
            _statusMessage.value = "Step target updated to $newTarget steps"
        }
    }

    fun onWaterDelta(amount: Int) {
        viewModelScope.launch {
            repository.updateWater(amount)
        }
    }

    fun onStepsDelta(amount: Int) {
        viewModelScope.launch {
            repository.updateSteps(amount)
        }
    }

    fun onWorkoutDelta(amount: Int) {
        viewModelScope.launch {
            repository.updateWorkout(amount)
        }
    }

    fun onSleepDelta(amount: Float) {
        viewModelScope.launch {
            repository.updateSleep(amount)
        }
    }

    fun onToggleMetricCompleted(metricType: String) {
        viewModelScope.launch {
            repository.toggleMetricCompletion(metricType)
        }
    }

    fun onToggleWorkout(workoutId: Long) {
        viewModelScope.launch {
            val isNowDone = repository.toggleWorkoutCompleted(workoutId)
            _statusMessage.value = if (isNowDone) "⚡ Workout checked in & minutes added!" else "↩️ Workout unchecked & minutes deducted"
        }
    }

    fun onAddCustomWorkout(title: String, durationMinutes: Int, iconName: String = "fitness", colorHex: Long = 0xFFFF7043) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addWorkout(title, durationMinutes, iconName, colorHex)
            _statusMessage.value = "New workout added: $title (${durationMinutes}m)"
        }
    }

    fun onUpdateWorkout(id: Long, title: String, durationMinutes: Int, iconName: String = "fitness", colorHex: Long = 0xFFFF7043) {
        viewModelScope.launch {
            repository.updateWorkoutDetails(id, title, durationMinutes, iconName, colorHex)
            _statusMessage.value = "Workout updated: $title"
        }
    }

    fun onDeleteWorkout(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
            _statusMessage.value = "Workout deleted"
        }
    }

    fun onLogMiniWorkout(name: String, durationMins: Int) {
        viewModelScope.launch {
            repository.updateWorkout(durationMins)
            _statusMessage.value = "⚡ Completed $name (+$durationMins mins)!"
        }
    }

    fun onToggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.toggleHabit(habit)
        }
    }

    fun onToggleHabitDay(habit: HabitEntity, dayIndex: Int) {
        viewModelScope.launch {
            repository.toggleHabitDay(habit, dayIndex)
        }
    }

    fun onAddCustomHabit(title: String, category: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addHabit(
                HabitEntity(
                    title = title.trim(),
                    category = category,
                    targetValue = 1,
                    currentValue = 0,
                    completed = false,
                    isAiGenerated = false
                )
            )
            _statusMessage.value = "New habit added to dashboard!"
        }
    }

    fun onDeleteHabit(id: Long) {
        viewModelScope.launch {
            repository.deleteHabit(id)
        }
    }

    fun onRefreshInsight() {
        if (_isRefreshingInsight.value) return
        viewModelScope.launch {
            _isRefreshingInsight.value = true
            try {
                repository.refreshAiInsight()
                _statusMessage.value = "AI Insight updated!"
            } catch (e: Exception) {
                _statusMessage.value = "Could not refresh insight: ${e.message}"
            } finally {
                _isRefreshingInsight.value = false
            }
        }
    }

    fun onSendMessage(text: String, onComplete: (() -> Unit)? = null) {
        val clean = text.trim()
        if (clean.isBlank() || _isSendingChat.value) return

        viewModelScope.launch {
            _isSendingChat.value = true
            try {
                val result = repository.sendUserChatMessage(clean)
                if (result.generatedHabits.isNotEmpty()) {
                    _statusMessage.value = "✨ Added ${result.generatedHabits.size} new micro-habits to your dashboard!"
                }
                onComplete?.invoke()
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isSendingChat.value = false
            }
        }
    }

    fun onSpeechResult(recognizedText: String, onComplete: (() -> Unit)? = null) {
        _isListeningSpeech.value = false
        if (recognizedText.isNotBlank()) {
            onSendMessage(recognizedText, onComplete)
        }
    }

    fun setListeningSpeech(isListening: Boolean) {
        _isListeningSpeech.value = isListening
    }

    fun onUpdateTargets(water: Int, sleep: Float, steps: Int, workout: Int) {
        viewModelScope.launch {
            repository.setTargets(water, sleep, steps, workout)
            _statusMessage.value = "Target goals updated successfully!"
        }
    }

    fun onUpdateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateReminderSettings(enabled, hour, minute)
            val context = getApplication<Application>().applicationContext
            if (enabled) {
                ReminderScheduler.scheduleDailyReminder(context, hour, minute)
                val formattedTime = String.format(
                    java.util.Locale.getDefault(),
                    "%02d:%02d %s",
                    if (hour == 0 || hour == 12) 12 else hour % 12,
                    minute,
                    if (hour < 12) "AM" else "PM"
                )
                _statusMessage.value = "Daily reminder scheduled for $formattedTime!"
            } else {
                ReminderScheduler.cancelDailyReminder(context)
                _statusMessage.value = "Daily reminders turned off."
            }
        }
    }

    fun onUpdateProfile(name: String, goal: String, avatarIndex: Int) {
        viewModelScope.launch {
            repository.updateProfile(name, goal, avatarIndex)
            _statusMessage.value = "Profile updated for $name!"
        }
    }

    fun onUpdateProfileAndTargets(
        name: String,
        goal: String,
        avatarIndex: Int,
        water: Int,
        sleep: Float,
        steps: Int,
        workout: Int
    ) {
        viewModelScope.launch {
            repository.updateProfileAndTargets(name, goal, avatarIndex, water, sleep, steps, workout)
            _statusMessage.value = "Profile & targets saved for $name!"
        }
    }

    fun onUpdateHabit(id: Long, title: String, category: String) {
        viewModelScope.launch {
            repository.updateHabitDetails(id, title, category)
            _statusMessage.value = "Habit updated: $title"
        }
    }

    fun onSendTestReminder() {
        val context = getApplication<Application>().applicationContext
        ReminderScheduler.sendTestNotification(context)
        _statusMessage.value = "Test notification sent! Check your status bar."
    }

    fun onCheckInDisciplineToday() {
        viewModelScope.launch {
            repository.checkInDisciplineToday()
            _statusMessage.value = "🔥 Discipline check-in saved! Streak updated!"
        }
    }

    fun onClearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _statusMessage.value = "Chat history cleared."
        }
    }

    fun onResetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            _statusMessage.value = "Metric progress reset to 0!"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
