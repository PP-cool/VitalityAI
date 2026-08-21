package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyMetricsEntity
import com.example.data.local.DailyTargetEntity
import com.example.data.local.HabitEntity
import com.example.data.local.WorkoutEntity
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantOrange
import com.example.ui.theme.VitalityBackground
import com.example.ui.theme.VitalityBorder
import com.example.ui.theme.VitalityBorderSubtle
import com.example.ui.theme.VitalitySurface
import com.example.ui.theme.VitalitySurfaceVariant
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

fun getWorkoutIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "stretch" -> Icons.Default.SelfImprovement
        "core" -> Icons.Default.FitnessCenter
        "cardio" -> Icons.AutoMirrored.Filled.DirectionsRun
        "walk" -> Icons.AutoMirrored.Filled.DirectionsRun
        "yoga" -> Icons.Default.SelfImprovement
        else -> Icons.Default.FitnessCenter
    }
}

fun getTargetIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "water", "hydration" -> Icons.Default.WaterDrop
        "sleep", "rest", "night" -> Icons.Default.Nightlight
        "workout", "fitness", "gym", "exercise" -> Icons.Default.FitnessCenter
        "meditation", "mind", "mindfulness", "peace" -> Icons.Default.SelfImprovement
        "book", "reading", "read" -> Icons.Default.AutoStories
        "sun", "outdoor", "walk" -> Icons.Default.WbSunny
        "flame", "calories", "burn" -> Icons.Default.LocalFireDepartment
        "food", "diet", "nutrition", "meal" -> Icons.Default.Restaurant
        "heart", "health" -> Icons.Default.Favorite
        "spa" -> Icons.Default.Spa
        else -> Icons.Default.AutoAwesome
    }
}

val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun DashboardSection(
    metrics: DailyMetricsEntity,
    dailyTargets: List<DailyTargetEntity> = emptyList(),
    habits: List<HabitEntity> = emptyList(),
    workouts: List<WorkoutEntity> = emptyList(),
    isSensorActive: Boolean = false,
    isRefreshingInsight: Boolean = false,
    onDailyTargetDelta: (Long, Float) -> Unit = { _, _ -> },
    onToggleDailyTargetCompleted: (Long) -> Unit = {},
    onAddDailyTarget: (String, Float, String, Float, String, Long) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateDailyTarget: (Long, String, Float, String, Float, String, Long) -> Unit = { _, _, _, _, _, _, _ -> },
    onDeleteDailyTarget: (Long) -> Unit = {},
    onUpdateStepTarget: (Int) -> Unit = {},
    onToggleWorkout: (Long) -> Unit = {},
    onAddWorkout: (String, Int, String, Long) -> Unit = { _, _, _, _ -> },
    onUpdateWorkout: (Long, String, Int, String, Long) -> Unit = { _, _, _, _, _ -> },
    onDeleteWorkout: (Long) -> Unit = {},
    onToggleHabit: (HabitEntity) -> Unit = {},
    onToggleHabitDay: (HabitEntity, Int) -> Unit = { _, _ -> },
    onAddCustomHabit: (String, String) -> Unit = { _, _ -> },
    onUpdateHabit: (Long, String, String) -> Unit = { _, _, _ -> },
    onDeleteHabit: (Long) -> Unit = {},
    onRefreshInsight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddTargetDialog by remember { mutableStateOf(false) }
    var editingDailyTarget by remember { mutableStateOf<DailyTargetEntity?>(null) }
    var deletingDailyTarget by remember { mutableStateOf<DailyTargetEntity?>(null) }
    var showEditStepGoalDialog by remember { mutableStateOf(false) }

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var editingWorkout by remember { mutableStateOf<WorkoutEntity?>(null) }
    var deletingWorkout by remember { mutableStateOf<WorkoutEntity?>(null) }

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var deletingHabit by remember { mutableStateOf<HabitEntity?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Step Progress calculation based strictly on mobile sensor steps
    val stepProgress = (metrics.stepCurrent.toFloat() / metrics.stepTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
    val isStepsDone = metrics.stepCurrent >= metrics.stepTarget

    // Calculate dynamic analytics from all custom daily targets
    val completedTargetCount = dailyTargets.count { it.isCompleted || (it.currentValue >= it.targetValue && it.targetValue > 0f) }
    val totalTargetsCount = dailyTargets.size
    val totalUnits = totalTargetsCount + 1 // +1 for the mobile step goal
    val targetsProgressSum = dailyTargets.sumOf {
        (it.currentValue / it.targetValue.coerceAtLeast(0.1f)).coerceIn(0f, 1f).toDouble()
    }.toFloat()
    val overallPercent = (((targetsProgressSum + stepProgress) / totalUnits.coerceAtLeast(1)) * 100).roundToInt()

    // Current day of week index (Mon=0..Sun=6)
    val todayIdx = remember {
        val cal = Calendar.getInstance()
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_section")
    ) {
        // Daily Score & Goal Focus Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = if (metrics.userGoal.isNotBlank()) "Goal: ${metrics.userGoal}" else "${dailyTargets.size} customizable habit targets active",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (overallPercent >= 100) EmeraldGreen.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                border = BorderStroke(1.dp, if (overallPercent >= 100) EmeraldGreen else VitalityBorder),
                modifier = Modifier.testTag("daily_score_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (overallPercent >= 100) EmeraldGreen else ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$overallPercent% Completed ($completedTargetCount/$totalTargetsCount)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp
                        ),
                        color = if (overallPercent >= 100) EmeraldGreen else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Editable Daily Habit Targets Carousel Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Daily Habit Targets",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = if (dailyTargets.size > 4) "Swipe to see all targets (${dailyTargets.size})" else "${dailyTargets.size} active targets",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricCyan.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showAddTargetDialog = true }
                    .testTag("add_daily_target_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Target",
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Add Target",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sliding Carousel for Daily Targets (4 per slide page)
        val chunkedTargets = remember(dailyTargets) { dailyTargets.chunked(4) }
        val pagerState = rememberPagerState(pageCount = { chunkedTargets.size.coerceAtLeast(1) })

        if (dailyTargets.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = VitalitySurfaceVariant,
                border = BorderStroke(1.dp, VitalityBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No habit targets added yet",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ Add Target' to create custom hydration, sleep, workout, or study goals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_targets_pager")
            ) { page ->
                val pageTargets = chunkedTargets.getOrNull(page) ?: emptyList()
                val targetRows = pageTargets.chunked(2)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    targetRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { target ->
                                DynamicTargetCard(
                                    target = target,
                                    onMinus = { onDailyTargetDelta(target.id, -target.stepDelta) },
                                    onPlus = { onDailyTargetDelta(target.id, target.stepDelta) },
                                    onToggleComplete = { onToggleDailyTargetCompleted(target.id) },
                                    onEdit = { editingDailyTarget = target },
                                    onDelete = { deletingDailyTarget = target },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Pagination Dots Indicator: ONLY shown when there are more than 1 slide page (>4 targets)
            if (chunkedTargets.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                        .testTag("pager_dots_indicator"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until chunkedTargets.size) {
                        val isSelected = pagerState.currentPage == i
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else 7.dp,
                            animationSpec = tween(300),
                            label = "pager_dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(7.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCyan else VitalityBorder)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Personalized Daily Insight Banner (AI Health Coach)
        AiInsightBanner(
            insightText = metrics.aiInsight.ifBlank { "Hydrate well and keep a steady pace to hit all your habit targets today!" },
            isLoading = isRefreshingInsight,
            onRefresh = onRefreshInsight
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dedicated Live Steps Tracker Card (Non-manual, hardware movement only)
        LiveMobileStepCard(
            currentSteps = metrics.stepCurrent,
            targetSteps = metrics.stepTarget,
            progress = stepProgress,
            isCompleted = isStepsDone,
            isSensorActive = isSensorActive,
            onEditGoal = { showEditStepGoalDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Mini-Workouts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = VibrantOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Quick Mini-Workouts",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = VibrantOrange.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, VibrantOrange.copy(alpha = 0.4f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showAddWorkoutDialog = true }
                    .testTag("add_workout_header_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Workout",
                        tint = VibrantOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Add",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = VibrantOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mini_workouts_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(workouts, key = { it.id }) { workout ->
                val isDone = workout.isCompletedToday
                val accentColor = Color(workout.colorHex)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDone) VitalitySurfaceVariant else VitalitySurface,
                    border = BorderStroke(
                        1.dp,
                        if (isDone) accentColor.copy(alpha = 0.6f) else VitalityBorder
                    ),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            onToggleWorkout(workout.id)
                        }
                        .testTag("mini_workout_${workout.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDone) accentColor else accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = getWorkoutIcon(workout.iconName),
                                    contentDescription = "Start",
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = workout.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isDone) TextSecondary else TextPrimary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "+${workout.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Edit / Delete button
                        IconButton(
                            onClick = { editingWorkout = workout },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("edit_workout_${workout.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Workout",
                                tint = TextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        IconButton(
                            onClick = { deletingWorkout = workout },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("delete_workout_${workout.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Workout",
                                tint = Color(0xFFFF5252).copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly Habit Trackers Section
        WeeklyHabitsSection(
            habits = habits,
            todayIndex = todayIdx,
            onToggleHabit = onToggleHabit,
            onToggleHabitDay = onToggleHabitDay,
            onAddHabitClick = { showAddHabitDialog = true },
            onEditHabit = { editingHabit = it },
            onDeleteHabit = { deletingHabit = it }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Dialogs
    if (showAddTargetDialog) {
        AddDailyTargetDialog(
            onAddTarget = { title, targetVal, unit, stepDelta, icon, colorHex ->
                onAddDailyTarget(title, targetVal, unit, stepDelta, icon, colorHex)
                showAddTargetDialog = false
            },
            onDismiss = { showAddTargetDialog = false }
        )
    }

    editingDailyTarget?.let { target ->
        EditDailyTargetDialog(
            target = target,
            onUpdate = { title, targetVal, unit, stepDelta, icon, colorHex ->
                onUpdateDailyTarget(target.id, title, targetVal, unit, stepDelta, icon, colorHex)
                editingDailyTarget = null
            },
            onDelete = {
                deletingDailyTarget = target
                editingDailyTarget = null
            },
            onDismiss = { editingDailyTarget = null }
        )
    }

    deletingDailyTarget?.let { target ->
        DeleteDailyTargetConfirmDialog(
            target = target,
            onConfirmDelete = {
                onDeleteDailyTarget(target.id)
                deletingDailyTarget = null
            },
            onDismiss = { deletingDailyTarget = null }
        )
    }

    if (showEditStepGoalDialog) {
        EditStepGoalDialog(
            currentGoal = metrics.stepTarget,
            onSaveGoal = { newGoal ->
                onUpdateStepTarget(newGoal)
                showEditStepGoalDialog = false
            },
            onDismiss = { showEditStepGoalDialog = false }
        )
    }

    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onAdd = { title, duration, category, colorHex ->
                onAddWorkout(title, duration, category, colorHex)
                showAddWorkoutDialog = false
            },
            onDismiss = { showAddWorkoutDialog = false }
        )
    }

    editingWorkout?.let { workout ->
        EditWorkoutDialog(
            workout = workout,
            onUpdate = { title, duration, category, colorHex ->
                onUpdateWorkout(workout.id, title, duration, category, colorHex)
                editingWorkout = null
            },
            onDelete = {
                deletingWorkout = workout
                editingWorkout = null
            },
            onDismiss = { editingWorkout = null }
        )
    }

    deletingWorkout?.let { workout ->
        DeleteWorkoutConfirmDialog(
            workout = workout,
            onConfirmDelete = {
                onDeleteWorkout(workout.id)
                deletingWorkout = null
            },
            onDismiss = { deletingWorkout = null }
        )
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onAdd = { title, category ->
                onAddCustomHabit(title, category)
                showAddHabitDialog = false
            },
            onDismiss = { showAddHabitDialog = false }
        )
    }

    editingHabit?.let { habit ->
        EditHabitDialog(
            habit = habit,
            onUpdate = { title, category ->
                onUpdateHabit(habit.id, title, category)
                editingHabit = null
            },
            onDelete = {
                deletingHabit = habit
                editingHabit = null
            },
            onDismiss = { editingHabit = null }
        )
    }

    deletingHabit?.let { habit ->
        DeleteHabitConfirmDialog(
            habit = habit,
            onConfirmDelete = {
                onDeleteHabit(habit.id)
                deletingHabit = null
            },
            onDismiss = { deletingHabit = null }
        )
    }
}

@Composable
fun LiveMobileStepCard(
    currentSteps: Int,
    targetSteps: Int,
    progress: Float,
    isCompleted: Boolean,
    isSensorActive: Boolean,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "step_progress"
    )
    val percentage = (progress * 100).roundToInt()
    val distanceKm = String.format(Locale.getDefault(), "%.2f", currentSteps * 0.00075f)
    val caloriesKcal = (currentSteps * 0.04f).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_live_steps"),
        colors = CardDefaults.cardColors(containerColor = VitalitySurface),
        border = BorderStroke(
            1.dp,
            if (isCompleted) EmeraldGreen.copy(alpha = 0.7f) else VitalityBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Sensor Icon, Live Badge, and Edit Goal Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = "Live Step Tracker",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Live Steps",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSensorActive) EmeraldGreen.copy(alpha = 0.18f) else VitalitySurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSensorActive) EmeraldGreen else TextSecondary)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isSensorActive) "Live Sensor" else "Mobile Movement",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSensorActive) EmeraldGreen else TextSecondary
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Tracks physical motion as you carry your phone",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VitalitySurfaceVariant,
                    border = BorderStroke(1.dp, VitalityBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onEditGoal)
                        .testTag("edit_step_goal_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Edit Step Target",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Goal",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Step Counts & Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currentSteps",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (isCompleted) EmeraldGreen else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "/ $targetSteps steps",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    color = if (isCompleted) EmeraldGreen else ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth Linear Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) EmeraldGreen else ElectricCyan,
                trackColor = VitalitySurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Estimated Distance & Calorie Burn Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VitalitySurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Distance: ~$distanceKm km",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VitalitySurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = VibrantOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Burn: ~$caloriesKcal kcal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicTargetCard(
    target: DailyTargetEntity,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(target.colorHex)
    val progress = (target.currentValue / target.targetValue.coerceAtLeast(0.1f)).coerceIn(0f, 1f)
    val isDone = target.isCompleted || progress >= 1f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "target_ring_${target.id}"
    )
    val percentage = (progress * 100).roundToInt()

    var showMenu by remember { mutableStateOf(false) }

    // Format current and target strings nicely
    val currentFormatted = if (target.currentValue % 1f == 0f) {
        target.currentValue.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", target.currentValue)
    }

    val targetFormatted = if (target.targetValue % 1f == 0f) {
        target.targetValue.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", target.targetValue)
    }

    val stepLabel = if (target.stepDelta % 1f == 0f) {
        target.stepDelta.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", target.stepDelta)
    }

    Card(
        modifier = modifier.testTag("target_card_${target.id}"),
        colors = CardDefaults.cardColors(containerColor = VitalitySurface),
        border = BorderStroke(
            1.dp,
            if (isDone) accentColor.copy(alpha = 0.6f) else VitalityBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Icon, Title, and Completion / Options buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTargetIcon(target.iconName),
                            contentDescription = target.title,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small More/Edit Dropdown Button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(22.dp)
                                .testTag("target_options_${target.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(VitalitySurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Target", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricCyan) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Target", color = Color(0xFFFF5252)) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFF5252)) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Completion Checkbox
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isDone) accentColor else VitalitySurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isDone) accentColor else VitalityBorderSubtle,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable(onClick = onToggleComplete)
                            .testTag("checkbox_target_${target.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color(0xFF003817),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // High-Contrast Progress Ring
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 7.dp.toPx()
                    drawCircle(
                        color = VitalitySurfaceVariant,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        ),
                        color = if (isDone) accentColor else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$currentFormatted ${target.unit}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                ),
                color = accentColor
            )

            Text(
                text = "goal $targetFormatted ${target.unit}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stepper buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VitalitySurfaceVariant,
                    border = BorderStroke(1.dp, VitalityBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onMinus)
                        .testTag("minus_target_${target.id}")
                ) {
                    Text(
                        text = "-$stepLabel",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onPlus)
                        .testTag("plus_target_${target.id}")
                ) {
                    Text(
                        text = "+$stepLabel",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiInsightBanner(
    insightText: String,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_insight_card"),
        colors = CardDefaults.cardColors(containerColor = VitalitySurface),
        border = BorderStroke(1.dp, VitalityBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricCyan.copy(alpha = 0.2f), EmeraldGreen.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Coach",
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "AI Health Coach",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp
                        ),
                        color = ElectricCyan
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Text(
                        text = "Real-time",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = insightText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("refresh_insight_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = ElectricCyan
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Insight",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyHabitsSection(
    habits: List<HabitEntity>,
    todayIndex: Int,
    onToggleHabit: (HabitEntity) -> Unit,
    onToggleHabitDay: (HabitEntity, Int) -> Unit,
    onAddHabitClick: () -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_habits_section"),
        colors = CardDefaults.cardColors(containerColor = VitalitySurface),
        border = BorderStroke(1.dp, VitalityBorder),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Weekly Habit Tracker",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddHabitClick)
                        .testTag("new_habit_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Habit",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Habit",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = EmeraldGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Column Header: "Habit" on left, Day of Week Labels (M T W T F S S) on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HABIT (TAP TO EDIT/DELETE)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.4.sp
                    ),
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )

                // 7 Day Labels (M T W T F S S)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dayLabels.forEachIndexed { idx, label ->
                        val isToday = idx == todayIndex
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isToday) EmeraldGreen.copy(alpha = 0.18f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 10.5.sp
                                ),
                                color = if (isToday) EmeraldGreen else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Habit Rows
            if (habits.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = VitalitySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No active weekly habits yet. Tap '+ New Habit' or ask your AI coach below!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    habits.forEach { habit ->
                        HabitWeeklyRow(
                            habit = habit,
                            todayIndex = todayIndex,
                            onToggleDay = { dayIdx -> onToggleHabitDay(habit, dayIdx) },
                            onEdit = { onEditHabit(habit) },
                            onDelete = { onDeleteHabit(habit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitWeeklyRow(
    habit: HabitEntity,
    todayIndex: Int,
    onToggleDay: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryGradients = when (habit.category.lowercase()) {
        "hydration" -> listOf(ElectricCyan, Color(0xFF00B0FF))
        "sleep" -> listOf(SoftPurple, Color(0xFF7C4DFF))
        "mindfulness" -> listOf(Color(0xFF80CBC4), Color(0xFF26A69A))
        "nutrition" -> listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
        "fitness" -> listOf(VibrantOrange, Color(0xFFFF5722))
        else -> listOf(EmeraldGreen, Color(0xFF00C853))
    }
    val primaryColor = categoryGradients.first()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = VitalitySurfaceVariant,
        border = BorderStroke(1.dp, VitalityBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_row_${habit.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Habit Name, Category & Edit affordance (Left)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = habit.category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_habit_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Habit",
                        tint = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 7 Multi-Day Interactive Checkbox Circles (Right)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (dayIdx in 0..6) {
                    val isChecked = habit.isDayChecked(dayIdx)
                    val isToday = dayIdx == todayIndex

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (isChecked) Brush.linearGradient(categoryGradients)
                                else Brush.linearGradient(listOf(VitalitySurface, VitalitySurface))
                            )
                            .border(
                                width = if (isToday) 1.5.dp else 1.dp,
                                color = if (isChecked) primaryColor.copy(alpha = 0.8f) else if (isToday) ElectricCyan else VitalityBorder,
                                shape = CircleShape
                            )
                            .clickable { onToggleDay(dayIdx) }
                            .testTag("habit_${habit.id}_day_$dayIdx"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Checked",
                                tint = Color(0xFF003817),
                                modifier = Modifier.size(13.dp)
                            )
                        } else if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Add Custom Daily Target
@Composable
fun AddDailyTargetDialog(
    onAddTarget: (title: String, targetVal: Float, unit: String, stepDelta: Float, icon: String, colorHex: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetValText by remember { mutableStateOf("100") }
    var unitText by remember { mutableStateOf("pts") }
    var stepDeltaText by remember { mutableStateOf("10") }
    var selectedIcon by remember { mutableStateOf("sparkle") }
    var selectedColorHex by remember { mutableStateOf(0xFF00E5FF) }

    val iconOptions = listOf(
        "water" to "Water",
        "sleep" to "Sleep",
        "workout" to "Workout",
        "meditation" to "Mind",
        "book" to "Reading",
        "sun" to "Outdoor",
        "flame" to "Calories",
        "food" to "Nutrition",
        "heart" to "Health",
        "spa" to "Spa"
    )

    val colorOptions = listOf(
        0xFF00E5FF to "Cyan",
        0xFF00E676 to "Emerald",
        0xFFB388FF to "Purple",
        0xFFFF7043 to "Orange",
        0xFFFFD54F to "Gold",
        0xFFFF4081 to "Pink",
        0xFF69F0AE to "Mint"
    )

    val unitPresets = listOf("ml", "hrs", "mins", "cups", "pts", "pages", "km", "kcal")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Add Daily Habit Target",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Target Name (e.g. Read Book, Protein)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_target_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetValText,
                        onValueChange = { targetValText = it },
                        label = { Text("Goal Amount") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = VitalityBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitText,
                        onValueChange = { unitText = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = VitalityBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Unit Presets
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(unitPresets) { u ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (unitText == u) ElectricCyan.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (unitText == u) ElectricCyan else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { unitText = u }
                        ) {
                            Text(
                                text = u,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (unitText == u) ElectricCyan else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = stepDeltaText,
                    onValueChange = { stepDeltaText = it },
                    label = { Text("Step Increment per tap (+/- amount)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Icon",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { (key, _) ->
                        val isSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else VitalitySurfaceVariant)
                                .border(1.dp, if (isSelected) ElectricCyan else VitalityBorder, CircleShape)
                                .clickable { selectedIcon = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getTargetIcon(key),
                                contentDescription = null,
                                tint = if (isSelected) ElectricCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions) { (hex, _) ->
                        val isSelected = selectedColorHex == hex
                        val clr = Color(hex)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(clr)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetFloat = targetValText.toFloatOrNull() ?: 100f
                    val stepFloat = stepDeltaText.toFloatOrNull() ?: 10f
                    onAddTarget(title.trim(), targetFloat, unitText.trim(), stepFloat, selectedIcon, selectedColorHex)
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("confirm_add_target_button")
            ) {
                Text("Add Target", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Edit Existing Daily Target
@Composable
fun EditDailyTargetDialog(
    target: DailyTargetEntity,
    onUpdate: (title: String, targetVal: Float, unit: String, stepDelta: Float, icon: String, colorHex: Long) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(target.title) }
    var targetValText by remember { mutableStateOf(if (target.targetValue % 1f == 0f) target.targetValue.toInt().toString() else target.targetValue.toString()) }
    var unitText by remember { mutableStateOf(target.unit) }
    var stepDeltaText by remember { mutableStateOf(if (target.stepDelta % 1f == 0f) target.stepDelta.toInt().toString() else target.stepDelta.toString()) }
    var selectedIcon by remember { mutableStateOf(target.iconName) }
    var selectedColorHex by remember { mutableStateOf(target.colorHex) }

    val iconOptions = listOf(
        "water" to "Water",
        "sleep" to "Sleep",
        "workout" to "Workout",
        "meditation" to "Mind",
        "book" to "Reading",
        "sun" to "Outdoor",
        "flame" to "Calories",
        "food" to "Nutrition",
        "heart" to "Health",
        "spa" to "Spa"
    )

    val colorOptions = listOf(
        0xFF00E5FF to "Cyan",
        0xFF00E676 to "Emerald",
        0xFFB388FF to "Purple",
        0xFFFF7043 to "Orange",
        0xFFFFD54F to "Gold",
        0xFFFF4081 to "Pink",
        0xFF69F0AE to "Mint"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Habit Target",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Target Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetValText,
                        onValueChange = { targetValText = it },
                        label = { Text("Goal Amount") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = VitalityBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitText,
                        onValueChange = { unitText = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = VitalityBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = stepDeltaText,
                    onValueChange = { stepDeltaText = it },
                    label = { Text("Step Increment per tap (+/-)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Icon",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { (key, _) ->
                        val isSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else VitalitySurfaceVariant)
                                .border(1.dp, if (isSelected) ElectricCyan else VitalityBorder, CircleShape)
                                .clickable { selectedIcon = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getTargetIcon(key),
                                contentDescription = null,
                                tint = if (isSelected) ElectricCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions) { (hex, _) ->
                        val isSelected = selectedColorHex == hex
                        val clr = Color(hex)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(clr)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetFloat = targetValText.toFloatOrNull() ?: target.targetValue
                    val stepFloat = stepDeltaText.toFloatOrNull() ?: target.stepDelta
                    onUpdate(title.trim(), targetFloat, unitText.trim(), stepFloat, selectedIcon, selectedColorHex)
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black
                )
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Delete Daily Target Confirmation
@Composable
fun DeleteDailyTargetConfirmDialog(
    target: DailyTargetEntity,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Remove Habit Target?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove \"${target.title}\"? You can always re-add it whenever you like.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                )
            ) {
                Text("Remove", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Edit Step Goal
@Composable
fun EditStepGoalDialog(
    currentGoal: Int,
    onSaveGoal: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var goalVal by remember { mutableFloatStateOf(currentGoal.toFloat()) }
    val stepPresets = listOf(5000, 7500, 10000, 12500, 15000, 20000)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Set Daily Step Goal",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Physical step movements are counted live by your device sensors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${goalVal.roundToInt()} steps / day",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = EmeraldGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = goalVal,
                    onValueChange = { goalVal = (it / 500).roundToInt() * 500f },
                    valueRange = 2000f..25000f,
                    steps = 45,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldGreen,
                        activeTrackColor = EmeraldGreen,
                        inactiveTrackColor = VitalityBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Presets
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stepPresets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (goalVal.toInt() == preset) EmeraldGreen.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (goalVal.toInt() == preset) EmeraldGreen else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { goalVal = preset.toFloat() }
                        ) {
                            Text(
                                text = "${preset / 1000}k",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (goalVal.toInt() == preset) EmeraldGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveGoal(goalVal.roundToInt()) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color(0xFF003817)
                )
            ) {
                Text("Save Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Add Custom Mini-Workout
@Composable
fun AddWorkoutDialog(
    onAdd: (title: String, duration: Int, category: String, colorHex: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(10) }
    var selectedCategory by remember { mutableStateOf("cardio") }

    val categories = listOf(
        Triple("cardio", "Cardio", 0xFF00E676),
        Triple("core", "Core", 0xFFFF7043),
        Triple("stretch", "Stretch", 0xFF26A69A),
        Triple("walk", "Walk", 0xFF00E5FF),
        Triple("fitness", "Full Body", 0xFFFFAB40),
        Triple("yoga", "Yoga", 0xFFB388FF)
    )

    val durationPresets = listOf(3, 5, 7, 10, 15, 20, 30)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Add Custom Workout",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Workout Name (e.g. 10-min Core)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantOrange,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_workout_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Duration: $duration minutes",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(durationPresets) { d ->
                        val isSelected = duration == d
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) VibrantOrange.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) VibrantOrange else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { duration = d }
                        ) {
                            Text(
                                text = "${d}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) VibrantOrange else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Workout Type",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { (catKey, label, colorHex) ->
                        val isSelected = selectedCategory == catKey
                        val clr = Color(colorHex)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) clr.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) clr else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = catKey }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getWorkoutIcon(catKey),
                                    contentDescription = null,
                                    tint = if (isSelected) clr else TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) clr else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val catObj = categories.firstOrNull { it.first == selectedCategory } ?: categories[0]
                    onAdd(title.trim(), duration, selectedCategory, catObj.third)
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantOrange,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("confirm_add_workout_button")
            ) {
                Text("Add Workout", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Edit Existing Mini-Workout
@Composable
fun EditWorkoutDialog(
    workout: WorkoutEntity,
    onUpdate: (title: String, duration: Int, category: String, colorHex: Long) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(workout.title) }
    var duration by remember { mutableIntStateOf(workout.durationMinutes) }
    var selectedCategory by remember { mutableStateOf(workout.iconName) }

    val categories = listOf(
        Triple("cardio", "Cardio", 0xFF00E676),
        Triple("core", "Core", 0xFFFF7043),
        Triple("stretch", "Stretch", 0xFF26A69A),
        Triple("walk", "Walk", 0xFF00E5FF),
        Triple("fitness", "Full Body", 0xFFFFAB40),
        Triple("yoga", "Yoga", 0xFFB388FF)
    )

    val durationPresets = listOf(3, 5, 7, 10, 15, 20, 30, 45)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Mini-Workout",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Workout Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantOrange,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Duration: $duration minutes",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(durationPresets) { d ->
                        val isSelected = duration == d
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) VibrantOrange.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) VibrantOrange else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { duration = d }
                        ) {
                            Text(
                                text = "${d}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) VibrantOrange else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Workout Type",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { (catKey, label, colorHex) ->
                        val isSelected = selectedCategory == catKey
                        val clr = Color(colorHex)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) clr.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) clr else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = catKey }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getWorkoutIcon(catKey),
                                    contentDescription = null,
                                    tint = if (isSelected) clr else TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) clr else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val catObj = categories.firstOrNull { it.first == selectedCategory } ?: categories[0]
                    onUpdate(title.trim(), duration, selectedCategory, catObj.third)
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantOrange,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("confirm_update_workout_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Delete Workout Confirmation
@Composable
fun DeleteWorkoutConfirmDialog(
    workout: WorkoutEntity,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Delete Workout?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove \"${workout.title}\" (${workout.durationMinutes}m)?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_delete_workout_button")
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Add Custom Weekly Habit
@Composable
fun AddHabitDialog(
    onAdd: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("mindfulness") }
    val categories = listOf("mindfulness", "nutrition", "fitness", "hydration", "sleep", "general")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Add Weekly Habit",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title (e.g. Morning Meditation)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_habit_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) EmeraldGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(title.trim(), selectedCategory) },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color(0xFF003817)
                ),
                modifier = Modifier.testTag("confirm_add_habit_button")
            ) {
                Text("Add Habit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Edit Existing Weekly Habit
@Composable
fun EditHabitDialog(
    habit: HabitEntity,
    onUpdate: (String, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(habit.title) }
    var selectedCategory by remember { mutableStateOf(habit.category) }
    val categories = listOf("mindfulness", "nutrition", "fitness", "hydration", "sleep", "general")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Habit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else VitalityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) EmeraldGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(title.trim(), selectedCategory) },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color(0xFF003817)
                )
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Dialog: Delete Habit Confirmation
@Composable
fun DeleteHabitConfirmDialog(
    habit: HabitEntity,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Delete Habit?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove \"${habit.title}\" and its weekly history?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                )
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
