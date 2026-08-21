package com.example.data.repository

import com.example.data.api.GeminiCoachResult
import com.example.data.api.GeminiRepository
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DailyMetricsEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HealthDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HealthRepository(
    private val healthDao: HealthDao,
    private val geminiRepository: GeminiRepository = GeminiRepository()
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    fun getTodayDate(): String = dateFormat.format(Date())
    fun getTodayMonthYear(): String = monthYearFormat.format(Date())

    val latestMetrics: Flow<DailyMetricsEntity?> = healthDao.getLatestMetrics()
    val allHabits: Flow<List<HabitEntity>> = healthDao.getAllHabits()
    val allChatMessages: Flow<List<ChatMessageEntity>> = healthDao.getAllMessages()
    val allWorkouts: Flow<List<com.example.data.local.WorkoutEntity>> = healthDao.getAllWorkouts()
    val allDailyTargets: Flow<List<com.example.data.local.DailyTargetEntity>> = healthDao.getAllDailyTargets()
    val userProfile: Flow<com.example.data.local.UserProfileEntity?> = healthDao.getUserProfile()
    val allDisciplineHistory: Flow<List<com.example.data.local.DisciplineDayEntity>> = healthDao.getAllDisciplineHistory()

    fun getDisciplineForMonth(monthYear: String): Flow<List<com.example.data.local.DisciplineDayEntity>> {
        return healthDao.getDisciplineForMonth(monthYear)
    }

    suspend fun ensureUserProfileInitialized(): com.example.data.local.UserProfileEntity {
        val existing = healthDao.getUserProfile().firstOrNull()
        if (existing != null) return existing

        val defaultProfile = com.example.data.local.UserProfileEntity(
            id = 1,
            userName = "Alex Morgan",
            userGoal = "Daily fitness, hydration & 8h sleep",
            avatarIndex = 0,
            waterTargetMl = 2500,
            sleepTargetHours = 8.0f,
            stepTarget = 10000,
            workoutTargetMinutes = 45,
            isReminderEnabled = false,
            reminderHour = 20,
            reminderMinute = 0,
            lastActiveDate = getTodayDate()
        )
        healthDao.insertOrUpdateUserProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun ensureDailyTargetsInitialized(): List<com.example.data.local.DailyTargetEntity> {
        val today = getTodayDate()
        val existing = healthDao.getAllDailyTargets().firstOrNull() ?: emptyList()
        if (existing.isNotEmpty()) {
            // Auto-refresh daily targets progress if new day started
            healthDao.resetDailyTargetsIfNotToday(today)
            return existing
        }

        val defaults = listOf(
            com.example.data.local.DailyTargetEntity(
                title = "Water Intake",
                currentValue = 0f,
                targetValue = 2500f,
                unit = "ml",
                stepDelta = 250f,
                iconName = "water",
                colorHex = 0xFF00E5FF,
                orderIndex = 0,
                lastUpdatedDate = today
            ),
            com.example.data.local.DailyTargetEntity(
                title = "Sleep Rest",
                currentValue = 0f,
                targetValue = 8.0f,
                unit = "hrs",
                stepDelta = 0.5f,
                iconName = "sleep",
                colorHex = 0xFFB388FF,
                orderIndex = 1,
                lastUpdatedDate = today
            ),
            com.example.data.local.DailyTargetEntity(
                title = "Workouts",
                currentValue = 0f,
                targetValue = 45f,
                unit = "mins",
                stepDelta = 15f,
                iconName = "workout",
                colorHex = 0xFFFF7043,
                orderIndex = 2,
                lastUpdatedDate = today
            ),
            com.example.data.local.DailyTargetEntity(
                title = "Meditation",
                currentValue = 0f,
                targetValue = 15f,
                unit = "mins",
                stepDelta = 5f,
                iconName = "meditation",
                colorHex = 0xFF00E676,
                orderIndex = 3,
                lastUpdatedDate = today
            )
        )
        healthDao.insertDailyTargets(defaults)
        return defaults
    }

    suspend fun addDailyTarget(
        title: String,
        targetValue: Float,
        unit: String = "pts",
        stepDelta: Float = 10f,
        iconName: String = "sparkle",
        colorHex: Long = 0xFF00E5FF
    ): Long {
        val today = getTodayDate()
        val existing = healthDao.getAllDailyTargets().firstOrNull() ?: emptyList()
        val id = healthDao.insertDailyTarget(
            com.example.data.local.DailyTargetEntity(
                title = title.trim(),
                currentValue = 0f,
                targetValue = targetValue.coerceAtLeast(1f),
                unit = unit.trim(),
                stepDelta = stepDelta.coerceAtLeast(0.1f),
                iconName = iconName,
                colorHex = colorHex,
                orderIndex = existing.size,
                lastUpdatedDate = today
            )
        )
        syncTodayDiscipline()
        return id
    }

    suspend fun updateDailyTargetDetails(
        id: Long,
        title: String,
        targetValue: Float,
        unit: String,
        stepDelta: Float,
        iconName: String,
        colorHex: Long
    ) {
        val all = healthDao.getAllDailyTargets().firstOrNull() ?: return
        val target = all.firstOrNull { it.id == id } ?: return
        val updated = target.copy(
            title = title.trim().ifEmpty { target.title },
            targetValue = targetValue.coerceAtLeast(1f),
            unit = unit.trim().ifEmpty { target.unit },
            stepDelta = stepDelta.coerceAtLeast(0.1f),
            iconName = iconName,
            colorHex = colorHex
        )
        healthDao.updateDailyTarget(updated)
        syncTodayDiscipline()
    }

    suspend fun updateDailyTargetValue(id: Long, delta: Float) {
        val today = getTodayDate()
        val all = healthDao.getAllDailyTargets().firstOrNull() ?: return
        val target = all.firstOrNull { it.id == id } ?: return
        val baseVal = if (target.lastUpdatedDate != today) 0f else target.currentValue
        val newVal = (((baseVal + delta) * 10).toInt() / 10f).coerceAtLeast(0f)
        val isDone = newVal >= target.targetValue
        healthDao.updateDailyTarget(
            target.copy(
                currentValue = newVal,
                isCompleted = isDone,
                lastUpdatedDate = today
            )
        )
        syncTodayDiscipline()
    }

    suspend fun toggleDailyTargetCompleted(id: Long): Boolean {
        val today = getTodayDate()
        val all = healthDao.getAllDailyTargets().firstOrNull() ?: return false
        val target = all.firstOrNull { it.id == id } ?: return false
        val baseCompleted = if (target.lastUpdatedDate != today) false else target.isCompleted
        val baseValue = if (target.lastUpdatedDate != today) 0f else target.currentValue
        val nextCompleted = !baseCompleted
        val newVal = if (nextCompleted && baseValue < target.targetValue) {
            target.targetValue
        } else if (!nextCompleted && baseValue >= target.targetValue) {
            0f
        } else {
            baseValue
        }
        healthDao.updateDailyTarget(
            target.copy(
                isCompleted = nextCompleted,
                currentValue = newVal,
                lastUpdatedDate = today
            )
        )
        syncTodayDiscipline()
        return nextCompleted
    }

    suspend fun deleteDailyTarget(id: Long) {
        healthDao.deleteDailyTarget(id)
        syncTodayDiscipline()
    }

    suspend fun ensureWorkoutsInitialized(): List<com.example.data.local.WorkoutEntity> {
        val today = getTodayDate()
        val existing = healthDao.getAllWorkouts().firstOrNull() ?: emptyList()
        if (existing.isNotEmpty()) {
            // Auto-refresh workouts completion state if new day started
            healthDao.resetWorkoutsIfNotToday(today)
            return existing
        }

        val defaults = listOf(
            com.example.data.local.WorkoutEntity(title = "5-min Stretch", durationMinutes = 5, iconName = "stretch", colorHex = 0xFF26A69A, lastCompletedDate = today),
            com.example.data.local.WorkoutEntity(title = "10-min Core", durationMinutes = 10, iconName = "core", colorHex = 0xFFFF7043, lastCompletedDate = today),
            com.example.data.local.WorkoutEntity(title = "15-min Cardio", durationMinutes = 15, iconName = "cardio", colorHex = 0xFF00E676, lastCompletedDate = today),
            com.example.data.local.WorkoutEntity(title = "10-min Walk", durationMinutes = 10, iconName = "walk", colorHex = 0xFF00E5FF, lastCompletedDate = today),
            com.example.data.local.WorkoutEntity(title = "7-min Full Body", durationMinutes = 7, iconName = "fitness", colorHex = 0xFFFFAB40, lastCompletedDate = today)
        )
        healthDao.insertWorkouts(defaults)
        return defaults
    }

    suspend fun addWorkout(title: String, durationMinutes: Int, iconName: String = "fitness", colorHex: Long = 0xFFFF7043): Long {
        val today = getTodayDate()
        return healthDao.insertWorkout(
            com.example.data.local.WorkoutEntity(
                title = title.trim(),
                durationMinutes = durationMinutes.coerceAtLeast(1),
                iconName = iconName,
                colorHex = colorHex,
                lastCompletedDate = today
            )
        )
    }

    suspend fun updateWorkoutDetails(id: Long, title: String, durationMinutes: Int, iconName: String, colorHex: Long) {
        val all = healthDao.getAllWorkouts().firstOrNull() ?: return
        val target = all.firstOrNull { it.id == id } ?: return
        val updated = target.copy(
            title = title.trim().ifEmpty { target.title },
            durationMinutes = durationMinutes.coerceAtLeast(1),
            iconName = iconName,
            colorHex = colorHex
        )
        healthDao.updateWorkout(updated)
    }

    suspend fun deleteWorkout(id: Long) {
        healthDao.deleteWorkout(id)
    }

    suspend fun toggleWorkoutCompleted(id: Long): Boolean {
        val today = getTodayDate()
        val all = healthDao.getAllWorkouts().firstOrNull() ?: return false
        val target = all.firstOrNull { it.id == id } ?: return false
        val baseCompleted = if (target.lastCompletedDate != today) false else target.isCompletedToday
        val nextCompleted = !baseCompleted
        healthDao.setWorkoutCompleted(id, nextCompleted, today)
        // Automatically adjust daily workout minutes: +duration when checked, -duration when unchecked!
        updateWorkout(if (nextCompleted) target.durationMinutes else -target.durationMinutes)
        syncTodayDiscipline()
        return nextCompleted
    }

    suspend fun ensureTodayInitialized(): DailyMetricsEntity {
        val today = getTodayDate()
        val profile = ensureUserProfileInitialized()
        
        // Auto-refresh daily targets and workouts for new day
        healthDao.resetDailyTargetsIfNotToday(today)
        healthDao.resetWorkoutsIfNotToday(today)

        val existing = healthDao.getDailyMetrics(today).firstOrNull()
        if (existing != null) {
            // Keep user profile permanently in sync if profile was updated
            if (existing.userName != profile.userName || existing.userGoal != profile.userGoal || existing.avatarIndex != profile.avatarIndex) {
                val synced = existing.copy(
                    userName = profile.userName,
                    userGoal = profile.userGoal,
                    avatarIndex = profile.avatarIndex,
                    waterTargetMl = profile.waterTargetMl,
                    sleepTargetHours = profile.sleepTargetHours,
                    stepTarget = profile.stepTarget,
                    workoutTargetMinutes = profile.workoutTargetMinutes,
                    isReminderEnabled = profile.isReminderEnabled,
                    reminderHour = profile.reminderHour,
                    reminderMinute = profile.reminderMinute
                )
                healthDao.insertOrUpdateMetrics(synced)
                return synced
            }
            return existing
        }

        // Calculate accurate streak from historical discipline days
        val calculatedStreak = calculateStreakFromHistory(today)

        // ZERO OUT daily metric progress rings for the new day while PERMANENTLY retaining User Profile & Goals
        val defaultMetrics = DailyMetricsEntity(
            date = today,
            waterCurrentMl = 0,
            waterTargetMl = profile.waterTargetMl,
            sleepHours = 0.0f,
            sleepTargetHours = profile.sleepTargetHours,
            stepCurrent = 0,
            stepTarget = profile.stepTarget,
            workoutMinutes = 0,
            workoutTargetMinutes = profile.workoutTargetMinutes,
            aiInsight = "Welcome to your day! Start logging your water, steps, sleep, and workouts. Your AI Coach is ready to support your daily goals.",
            streakDays = calculatedStreak,
            userName = profile.userName,
            userGoal = profile.userGoal,
            avatarIndex = profile.avatarIndex,
            isWaterCompleted = false,
            isSleepCompleted = false,
            isStepsCompleted = false,
            isWorkoutCompleted = false,
            isReminderEnabled = profile.isReminderEnabled,
            reminderHour = profile.reminderHour,
            reminderMinute = profile.reminderMinute
        )
        healthDao.insertOrUpdateMetrics(defaultMetrics)

        // Seed initial starter habits if empty (all uncompleted 0000000 by default)
        val currentHabits = healthDao.getAllHabits().firstOrNull() ?: emptyList()
        if (currentHabits.isEmpty()) {
            val starterHabits = listOf(
                HabitEntity(title = "Morning hydration (500ml)", category = "hydration", targetValue = 1, currentValue = 0, completed = false, weeklyDaysMask = "0000000", isAiGenerated = false),
                HabitEntity(title = "15-min afternoon brisk walk", category = "fitness", targetValue = 1, currentValue = 0, completed = false, weeklyDaysMask = "0000000", isAiGenerated = true, originGoal = "Daily step target"),
                HabitEntity(title = "Unplug screens by 10:30 PM", category = "sleep", targetValue = 1, currentValue = 0, completed = false, weeklyDaysMask = "0000000", isAiGenerated = false),
                HabitEntity(title = "10-min mindful meditation", category = "mindfulness", targetValue = 1, currentValue = 0, completed = false, weeklyDaysMask = "0000000", isAiGenerated = false)
            )
            healthDao.insertHabits(starterHabits)
        }

        // Seed welcome chat message if chat is empty
        val currentMessages = healthDao.getAllMessages().firstOrNull() ?: emptyList()
        if (currentMessages.isEmpty()) {
            healthDao.insertMessage(
                ChatMessageEntity(
                    sender = "gemini",
                    text = "Hello! I'm your AI Health & Habit Coach. All daily metric trackers are reset for today. Ask me for recommendations or state any goal (like 'Help me run a 5k next month' or 'Sleep by 10 PM') to generate tailored micro-habits!",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Ensure today's discipline history record exists
        syncTodayDiscipline()

        return defaultMetrics
    }

    suspend fun syncTodayDiscipline(forcedCheckIn: Boolean? = null) {
        val today = getTodayDate()
        val cal = Calendar.getInstance()
        val dayNum = cal.get(Calendar.DAY_OF_MONTH)
        val monthYear = getTodayMonthYear()

        val dailyTargets = healthDao.getAllDailyTargets().firstOrNull() ?: emptyList()
        val metrics = healthDao.getDailyMetrics(today).firstOrNull()
        val existingDiscipline = healthDao.getDisciplineForDate(today)

        val totalTargets = dailyTargets.size + 1 // +1 for Live Steps
        val completedTargets = dailyTargets.count { it.isCompleted } + if ((metrics?.stepCurrent ?: 0) >= (metrics?.stepTarget ?: 10000)) 1 else 0
        
        val progressRatio = if (totalTargets > 0) {
            val targetsSum = dailyTargets.sumOf { (it.currentValue / it.targetValue.coerceAtLeast(0.1f)).coerceIn(0f, 1f).toDouble() }
            val stepRatio = ((metrics?.stepCurrent ?: 0).toFloat() / (metrics?.stepTarget ?: 10000).coerceAtLeast(1)).coerceIn(0f, 1f).toDouble()
            ((targetsSum + stepRatio) / totalTargets * 100).toInt()
        } else {
            0
        }

        val isCheckedIn = forcedCheckIn ?: (existingDiscipline?.isCheckedIn == true || progressRatio >= 50 || completedTargets > 0)
        val finalPercent = if (forcedCheckIn == true) 100 else progressRatio.coerceIn(0, 100)

        val updatedDiscipline = com.example.data.local.DisciplineDayEntity(
            date = today,
            dayNumber = dayNum,
            monthYear = monthYear,
            completionPercent = finalPercent,
            isCheckedIn = isCheckedIn,
            completedTargetsCount = completedTargets,
            totalTargetsCount = totalTargets,
            stepsDone = (metrics?.stepCurrent ?: 0) >= (metrics?.stepTarget ?: 10000),
            summaryNotes = "$completedTargets/$totalTargets habit targets completed",
            updatedAt = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateDisciplineDay(updatedDiscipline)

        // Update streak in daily metrics
        val streak = calculateStreakFromHistory(today)
        metrics?.let {
            healthDao.insertOrUpdateMetrics(it.copy(streakDays = streak))
        }
    }

    suspend fun checkInDisciplineToday() {
        syncTodayDiscipline(forcedCheckIn = true)
    }

    suspend fun calculateStreakFromHistory(todayDate: String): Int {
        val history = healthDao.getAllDisciplineHistory().firstOrNull() ?: emptyList()
        val historyMap = history.associateBy { it.date }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        val todayRecord = historyMap[todayDate]
        val isTodayDone = (todayRecord?.isCheckedIn == true || (todayRecord?.completionPercent ?: 0) >= 50)

        // Check yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)
        val yesterdayRecord = historyMap[yesterdayStr]
        val isYesterdayDone = (yesterdayRecord?.isCheckedIn == true || (yesterdayRecord?.completionPercent ?: 0) >= 50)

        // If user missed yesterday and hasn't completed today, streak starts from 0 again!
        if (!isYesterdayDone && !isTodayDone) {
            return 0
        }

        var streak = if (isTodayDone) 1 else 0
        // If yesterday was done, count backward consecutively
        if (isYesterdayDone) {
            if (!isTodayDone) {
                streak = 1 // Active streak from yesterday pending today's check-in
            }
            while (true) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val prevDateStr = sdf.format(cal.time)
                val prevRecord = historyMap[prevDateStr]
                val isPrevDone = (prevRecord?.isCheckedIn == true || (prevRecord?.completionPercent ?: 0) >= 50)
                if (isPrevDone) {
                    streak++
                } else {
                    // Missed day breaks streak!
                    break
                }
            }
        }
        return streak
    }

    suspend fun updateMetrics(metrics: DailyMetricsEntity) {
        healthDao.insertOrUpdateMetrics(metrics.copy(lastUpdated = System.currentTimeMillis()))
        syncTodayDiscipline()
    }

    suspend fun updateWater(deltaMl: Int) {
        val today = getTodayDate()
        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val newWater = (current.waterCurrentMl + deltaMl).coerceAtLeast(0)
        val isCompleted = newWater >= current.waterTargetMl || (current.isWaterCompleted && newWater > 0)
        val updated = current.copy(
            waterCurrentMl = newWater,
            isWaterCompleted = isCompleted
        )
        healthDao.insertOrUpdateMetrics(updated)
        syncTodayDiscipline()
    }

    suspend fun updateSteps(deltaSteps: Int) {
        val today = getTodayDate()
        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val newSteps = (current.stepCurrent + deltaSteps).coerceAtLeast(0)
        val isCompleted = newSteps >= current.stepTarget || (current.isStepsCompleted && newSteps > 0)
        val updated = current.copy(
            stepCurrent = newSteps,
            isStepsCompleted = isCompleted
        )
        healthDao.insertOrUpdateMetrics(updated)
        syncTodayDiscipline()
    }

    suspend fun updateWorkout(deltaMins: Int) {
        val today = getTodayDate()
        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val newWorkout = (current.workoutMinutes + deltaMins).coerceAtLeast(0)
        val isCompleted = newWorkout >= current.workoutTargetMinutes || (current.isWorkoutCompleted && newWorkout > 0)
        val updated = current.copy(
            workoutMinutes = newWorkout,
            isWorkoutCompleted = isCompleted
        )
        healthDao.insertOrUpdateMetrics(updated)
        syncTodayDiscipline()
    }

    suspend fun updateSleep(deltaHours: Float) {
        val today = getTodayDate()
        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val newSleep = (((current.sleepHours + deltaHours) * 10).toInt() / 10f).coerceAtLeast(0.0f)
        val isCompleted = newSleep >= current.sleepTargetHours || (current.isSleepCompleted && newSleep > 0f)
        val updated = current.copy(
            sleepHours = newSleep,
            isSleepCompleted = isCompleted
        )
        healthDao.insertOrUpdateMetrics(updated)
        syncTodayDiscipline()
    }

    suspend fun toggleMetricCompletion(metricType: String) {
        val today = getTodayDate()
        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val updated = when (metricType) {
            "water" -> {
                val nextCompleted = !current.isWaterCompleted
                current.copy(
                    isWaterCompleted = nextCompleted,
                    waterCurrentMl = if (nextCompleted && current.waterCurrentMl < current.waterTargetMl) current.waterTargetMl else if (!nextCompleted && current.waterCurrentMl >= current.waterTargetMl) 0 else current.waterCurrentMl
                )
            }
            "sleep" -> {
                val nextCompleted = !current.isSleepCompleted
                current.copy(
                    isSleepCompleted = nextCompleted,
                    sleepHours = if (nextCompleted && current.sleepHours < current.sleepTargetHours) current.sleepTargetHours else if (!nextCompleted && current.sleepHours >= current.sleepTargetHours) 0.0f else current.sleepHours
                )
            }
            "steps" -> {
                val nextCompleted = !current.isStepsCompleted
                current.copy(
                    isStepsCompleted = nextCompleted,
                    stepCurrent = if (nextCompleted && current.stepCurrent < current.stepTarget) current.stepTarget else if (!nextCompleted && current.stepCurrent >= current.stepTarget) 0 else current.stepCurrent
                )
            }
            "workout" -> {
                val nextCompleted = !current.isWorkoutCompleted
                current.copy(
                    isWorkoutCompleted = nextCompleted,
                    workoutMinutes = if (nextCompleted && current.workoutMinutes < current.workoutTargetMinutes) current.workoutTargetMinutes else if (!nextCompleted && current.workoutMinutes >= current.workoutTargetMinutes) 0 else current.workoutMinutes
                )
            }
            else -> current
        }
        healthDao.insertOrUpdateMetrics(updated)
        syncTodayDiscipline()
    }

    suspend fun setTargets(water: Int, sleep: Float, steps: Int, workout: Int) {
        val today = getTodayDate()
        val profile = ensureUserProfileInitialized()
        val updatedProfile = profile.copy(
            waterTargetMl = water,
            sleepTargetHours = sleep,
            stepTarget = steps,
            workoutTargetMinutes = workout,
            updatedAt = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateUserProfile(updatedProfile)

        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val updated = current.copy(
            waterTargetMl = water,
            sleepTargetHours = sleep,
            stepTarget = steps,
            workoutTargetMinutes = workout,
            isWaterCompleted = current.waterCurrentMl >= water,
            isSleepCompleted = current.sleepHours >= sleep,
            isStepsCompleted = current.stepCurrent >= steps,
            isWorkoutCompleted = current.workoutMinutes >= workout,
            lastUpdated = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateMetrics(updated)
        syncTodayDiscipline()
    }

    suspend fun updateProfile(name: String, goal: String, avatarIndex: Int) {
        val today = getTodayDate()
        val profile = ensureUserProfileInitialized()
        val updatedProfile = profile.copy(
            userName = name.trim().ifEmpty { "Alex Morgan" },
            userGoal = goal.trim(),
            avatarIndex = avatarIndex,
            updatedAt = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateUserProfile(updatedProfile)

        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val updated = current.copy(
            userName = updatedProfile.userName,
            userGoal = updatedProfile.userGoal,
            avatarIndex = avatarIndex,
            lastUpdated = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateMetrics(updated)
    }

    suspend fun updateProfileAndTargets(
        name: String,
        goal: String,
        avatarIndex: Int,
        water: Int,
        sleep: Float,
        steps: Int,
        workout: Int
    ) {
        val today = getTodayDate()
        val profile = ensureUserProfileInitialized()
        val updatedProfile = profile.copy(
            userName = name.trim().ifEmpty { "Alex Morgan" },
            userGoal = goal.trim(),
            avatarIndex = avatarIndex,
            waterTargetMl = water,
            sleepTargetHours = sleep,
            stepTarget = steps,
            workoutTargetMinutes = workout,
            updatedAt = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateUserProfile(updatedProfile)

        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val updated = current.copy(
            userName = updatedProfile.userName,
            userGoal = updatedProfile.userGoal,
            avatarIndex = avatarIndex,
            waterTargetMl = water,
            sleepTargetHours = sleep,
            stepTarget = steps,
            workoutTargetMinutes = workout,
            isWaterCompleted = current.waterCurrentMl >= water,
            isSleepCompleted = current.sleepHours >= sleep,
            isStepsCompleted = current.stepCurrent >= steps,
            isWorkoutCompleted = current.workoutMinutes >= workout,
            lastUpdated = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateMetrics(updated)
        syncTodayDiscipline()
    }

    suspend fun toggleHabit(habit: HabitEntity) {
        val cal = Calendar.getInstance()
        val todayIdx = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        val updatedHabit = habit.toggleDay(todayIdx)
        healthDao.updateHabit(updatedHabit)
    }

    suspend fun toggleHabitDay(habit: HabitEntity, dayIndex: Int) {
        val updatedHabit = habit.toggleDay(dayIndex)
        healthDao.updateHabit(updatedHabit)
    }

    suspend fun addHabit(habit: HabitEntity): Long {
        return healthDao.insertHabit(habit)
    }

    suspend fun updateHabitDetails(id: Long, title: String, category: String) {
        val all = healthDao.getAllHabits().firstOrNull() ?: return
        val target = all.firstOrNull { it.id == id } ?: return
        val updated = target.copy(
            title = title.trim().ifEmpty { target.title },
            category = category.trim().ifEmpty { target.category }
        )
        healthDao.updateHabit(updated)
    }

    suspend fun deleteHabit(id: Long) {
        healthDao.deleteHabit(id)
    }

    suspend fun refreshAiInsight(): String {
        val today = getTodayDate()
        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val habits = healthDao.getAllHabits().firstOrNull() ?: emptyList()
        val newInsight = geminiRepository.generateDailyInsight(current, habits)
        healthDao.insertOrUpdateMetrics(current.copy(aiInsight = newInsight))
        return newInsight
    }

    suspend fun sendUserChatMessage(userText: String): GeminiCoachResult {
        healthDao.insertMessage(
            ChatMessageEntity(
                sender = "user",
                text = userText,
                timestamp = System.currentTimeMillis()
            )
        )

        val today = getTodayDate()
        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val habits = healthDao.getAllHabits().firstOrNull() ?: emptyList()

        val aiResult = geminiRepository.sendChatMessage(userText, current, habits)

        if (aiResult.generatedHabits.isNotEmpty()) {
            val newHabits = aiResult.generatedHabits.map {
                HabitEntity(
                    title = it.title,
                    category = it.category,
                    targetValue = it.targetValue,
                    unit = it.unit,
                    completed = false,
                    isAiGenerated = true,
                    originGoal = userText
                )
            }
            healthDao.insertHabits(newHabits)
        }

        healthDao.insertMessage(
            ChatMessageEntity(
                sender = "gemini",
                text = aiResult.replyText,
                timestamp = System.currentTimeMillis(),
                habitsCreatedCount = aiResult.generatedHabits.size
            )
        )

        return aiResult
    }

    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        val today = getTodayDate()
        val profile = ensureUserProfileInitialized()
        val updatedProfile = profile.copy(
            isReminderEnabled = enabled,
            reminderHour = hour,
            reminderMinute = minute,
            updatedAt = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateUserProfile(updatedProfile)

        val current = healthDao.getDailyMetrics(today).firstOrNull() ?: ensureTodayInitialized()
        val updated = current.copy(
            isReminderEnabled = enabled,
            reminderHour = hour,
            reminderMinute = minute,
            lastUpdated = System.currentTimeMillis()
        )
        healthDao.insertOrUpdateMetrics(updated)
    }

    suspend fun clearChatHistory() {
        healthDao.clearChat()
    }

    suspend fun resetDemoData() {
        healthDao.clearHabits()
        healthDao.clearChat()
        val today = getTodayDate()
        val profile = ensureUserProfileInitialized()
        
        val defaultMetrics = DailyMetricsEntity(
            date = today,
            waterCurrentMl = 0,
            waterTargetMl = profile.waterTargetMl,
            sleepHours = 0.0f,
            sleepTargetHours = profile.sleepTargetHours,
            stepCurrent = 0,
            stepTarget = profile.stepTarget,
            workoutMinutes = 0,
            workoutTargetMinutes = profile.workoutTargetMinutes,
            aiInsight = "Progress reset. All trackers are at zero. Tap the increment buttons or ask Gemini to start fresh!",
            streakDays = 0,
            userName = profile.userName,
            userGoal = profile.userGoal,
            avatarIndex = profile.avatarIndex,
            isWaterCompleted = false,
            isSleepCompleted = false,
            isStepsCompleted = false,
            isWorkoutCompleted = false,
            isReminderEnabled = profile.isReminderEnabled,
            reminderHour = profile.reminderHour,
            reminderMinute = profile.reminderMinute
        )
        healthDao.insertOrUpdateMetrics(defaultMetrics)

        val starterHabits = listOf(
            HabitEntity(title = "Morning hydration (500ml)", category = "hydration", targetValue = 1, currentValue = 0, completed = false, weeklyDaysMask = "0000000", isAiGenerated = false),
            HabitEntity(title = "15-min afternoon brisk walk", category = "fitness", targetValue = 1, currentValue = 0, completed = false, weeklyDaysMask = "0000000", isAiGenerated = true, originGoal = "Daily step target"),
            HabitEntity(title = "Unplug screens by 10:30 PM", category = "sleep", targetValue = 1, currentValue = 0, completed = false, weeklyDaysMask = "0000000", isAiGenerated = false)
        )
        healthDao.insertHabits(starterHabits)

        healthDao.insertMessage(
            ChatMessageEntity(
                sender = "gemini",
                text = "Hello! I'm your AI Health & Habit Coach. All daily metric trackers are reset for today. Ask me for recommendations or state your goal to generate tailored micro-habits!",
                timestamp = System.currentTimeMillis()
            )
        )
        
        syncTodayDiscipline()
    }
}
