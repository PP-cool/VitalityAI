package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DailyTargetEntity
import com.example.ui.components.AddDailyTargetDialog
import com.example.ui.components.ChatHistorySheet
import com.example.ui.components.DashboardSection
import com.example.ui.components.EditDailyTargetDialog
import com.example.ui.components.FloatingAiBar
import com.example.ui.components.MonthlyDisciplineDialog
import com.example.ui.components.ProfileDialog
import com.example.ui.components.ReminderDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopBar
import com.example.ui.theme.VitalityBackground
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showProfileDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSettingsAddTarget by remember { mutableStateOf(false) }
    var showSettingsEditTarget by remember { mutableStateOf<DailyTargetEntity?>(null) }
    var showDisciplineDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showChatSheet by remember { mutableStateOf(false) }

    // Speech-To-Text Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.getOrNull(0)
            if (!spokenText.isNullOrBlank()) {
                viewModel.onSpeechResult(spokenText) {
                    showChatSheet = true
                }
            } else {
                viewModel.setListeningSpeech(false)
            }
        } else {
            viewModel.setListeningSpeech(false)
        }
    }

    fun launchSpeechRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "State your health goal or ask your AI coach...")
            }
            viewModel.setListeningSpeech(true)
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.setListeningSpeech(false)
            Toast.makeText(context, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = VitalityBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopBar(
                userName = uiState.metrics.userName,
                avatarIndex = uiState.metrics.avatarIndex,
                streakDays = uiState.metrics.streakDays,
                isReminderEnabled = uiState.metrics.isReminderEnabled,
                onProfileClick = { showProfileDialog = true },
                onDisciplineClick = { showDisciplineDialog = true },
                onReminderClick = { showReminderDialog = true },
                onSettingsClick = { showSettingsDialog = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content: 80% Focus on Analytics Dashboard & Mini-Workouts
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 135.dp) // Balanced spacing so weekly habits scroll with clean separation above floating AI suggestions
            ) {
                DashboardSection(
                    metrics = uiState.metrics,
                    dailyTargets = uiState.dailyTargets,
                    habits = uiState.habits,
                    workouts = uiState.workouts,
                    isSensorActive = uiState.isSensorActive,
                    isRefreshingInsight = uiState.isRefreshingInsight,
                    onDailyTargetDelta = viewModel::onDailyTargetDelta,
                    onToggleDailyTargetCompleted = viewModel::onToggleDailyTargetCompleted,
                    onAddDailyTarget = viewModel::onAddDailyTarget,
                    onUpdateDailyTarget = viewModel::onUpdateDailyTarget,
                    onDeleteDailyTarget = viewModel::onDeleteDailyTarget,
                    onUpdateStepTarget = viewModel::onUpdateStepTarget,
                    onToggleWorkout = viewModel::onToggleWorkout,
                    onAddWorkout = viewModel::onAddCustomWorkout,
                    onUpdateWorkout = viewModel::onUpdateWorkout,
                    onDeleteWorkout = viewModel::onDeleteWorkout,
                    onToggleHabit = viewModel::onToggleHabit,
                    onToggleHabitDay = viewModel::onToggleHabitDay,
                    onAddCustomHabit = viewModel::onAddCustomHabit,
                    onUpdateHabit = viewModel::onUpdateHabit,
                    onDeleteHabit = viewModel::onDeleteHabit,
                    onRefreshInsight = viewModel::onRefreshInsight
                )
            }

            // Sleek Collapsible Floating AI Input Bar sitting cleanly at the bottom above keyboard & nav bar
            FloatingAiBar(
                messages = uiState.chatMessages,
                isSending = uiState.isSendingChat,
                isListeningSpeech = uiState.isListeningSpeech,
                onSendMessage = { text ->
                    viewModel.onSendMessage(text) {
                        showChatSheet = true
                    }
                },
                onMicClick = { launchSpeechRecognition() },
                onExpandChat = { showChatSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    }

    // Expandable Chat History Modal Sheet
    if (showChatSheet) {
        ChatHistorySheet(
            messages = uiState.chatMessages,
            isSending = uiState.isSendingChat,
            onSendMessage = viewModel::onSendMessage,
            onMicClick = { launchSpeechRecognition() },
            onClearChat = viewModel::onClearChat,
            onDismiss = { showChatSheet = false }
        )
    }

    // Interactive Profile & Goals Dialog
    if (showProfileDialog) {
        ProfileDialog(
            metrics = uiState.metrics,
            habits = uiState.habits,
            onSaveProfileAndTargets = viewModel::onUpdateProfileAndTargets,
            onDismiss = { showProfileDialog = false }
        )
    }

    // Monthly Discipline 30-Day Heatmap Dialog
    if (showDisciplineDialog) {
        MonthlyDisciplineDialog(
            currentMetrics = uiState.metrics,
            dailyTargets = uiState.dailyTargets,
            disciplineHistory = uiState.disciplineHistory,
            onCheckInToday = viewModel::onCheckInDisciplineToday,
            onDismiss = { showDisciplineDialog = false }
        )
    }

    // Target Goals & Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentMetrics = uiState.metrics,
            dailyTargets = uiState.dailyTargets,
            onSaveStepTarget = viewModel::onUpdateStepTarget,
            onAddTarget = { showSettingsAddTarget = true },
            onEditTarget = { target -> showSettingsEditTarget = target },
            onDeleteTarget = { target -> viewModel.onDeleteDailyTarget(target.id) },
            onUpdateReminder = viewModel::onUpdateReminder,
            onOpenReminderDialog = { showReminderDialog = true },
            onClearChat = viewModel::onClearChat,
            onResetDemoData = viewModel::onResetDemoData,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showSettingsAddTarget) {
        AddDailyTargetDialog(
            onAddTarget = { title, targetVal, unit, stepDelta, icon, colorHex ->
                viewModel.onAddDailyTarget(title, targetVal, unit, stepDelta, icon, colorHex)
                showSettingsAddTarget = false
                showSettingsDialog = true
            },
            onDismiss = {
                showSettingsAddTarget = false
                showSettingsDialog = true
            }
        )
    }

    showSettingsEditTarget?.let { target ->
        EditDailyTargetDialog(
            target = target,
            onUpdate = { title, targetVal, unit, stepDelta, icon, colorHex ->
                viewModel.onUpdateDailyTarget(target.id, title, targetVal, unit, stepDelta, icon, colorHex)
                showSettingsEditTarget = null
                showSettingsDialog = true
            },
            onDelete = {
                viewModel.onDeleteDailyTarget(target.id)
                showSettingsEditTarget = null
                showSettingsDialog = true
            },
            onDismiss = {
                showSettingsEditTarget = null
                showSettingsDialog = true
            }
        )
    }

    // Daily Habit Reminder Time Picker Dialog
    if (showReminderDialog) {
        ReminderDialog(
            isReminderEnabled = uiState.metrics.isReminderEnabled,
            reminderHour = uiState.metrics.reminderHour,
            reminderMinute = uiState.metrics.reminderMinute,
            onSaveReminder = viewModel::onUpdateReminder,
            onSendTestNotification = viewModel::onSendTestReminder,
            onDismiss = { showReminderDialog = false }
        )
    }
}
