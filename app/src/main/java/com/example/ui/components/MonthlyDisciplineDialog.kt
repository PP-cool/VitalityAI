package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyMetricsEntity
import com.example.data.local.DailyTargetEntity
import com.example.data.local.DisciplineDayEntity
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantOrange
import com.example.ui.theme.VitalityBorder
import com.example.ui.theme.VitalityBorderSubtle
import com.example.ui.theme.VitalitySurface
import com.example.ui.theme.VitalitySurfaceVariant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DisciplineDay(
    val dayNumber: Int,
    val dateString: String,
    val completionPercent: Int, // 0 = missed, 1-99 = partial, 100 = full
    val isToday: Boolean = false,
    val isCheckedIn: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val summaryNotes: String = "",
    val waterDone: Boolean = false,
    val stepsDone: Boolean = false,
    val sleepDone: Boolean = false,
    val workoutDone: Boolean = false
)

@Composable
fun MonthlyDisciplineDialog(
    currentMetrics: DailyMetricsEntity,
    dailyTargets: List<DailyTargetEntity> = emptyList(),
    disciplineHistory: List<DisciplineDayEntity> = emptyList(),
    onCheckInToday: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val cal = remember { Calendar.getInstance() }
    val currentDay = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val maxDaysInMonth = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val monthName = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Map history records by date
    val historyMap = remember(disciplineHistory) {
        disciplineHistory.associateBy { it.date }
    }

    // Generate monthly days with permanently saved Room records
    val disciplineDays = remember(disciplineHistory, currentMetrics, dailyTargets) {
        val list = mutableListOf<DisciplineDay>()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        for (day in 1..maxDaysInMonth) {
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val dateStr = dateFormat.format(dayCal.time)
            val isToday = (day == currentDay)
            val savedRecord = historyMap[dateStr]

            val percent = if (savedRecord != null) {
                savedRecord.completionPercent
            } else if (isToday) {
                val targetsSum = dailyTargets.sumOf { (it.currentValue / it.targetValue.coerceAtLeast(0.1f)).coerceIn(0f, 1f).toDouble() }
                val stepRatio = (currentMetrics.stepCurrent.toFloat() / currentMetrics.stepTarget.coerceAtLeast(1)).coerceIn(0f, 1f).toDouble()
                val totalT = (dailyTargets.size + 1).coerceAtLeast(1)
                (((targetsSum + stepRatio) / totalT) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            val isCheckedIn = savedRecord?.isCheckedIn == true || (isToday && percent >= 100)

            list.add(
                DisciplineDay(
                    dayNumber = day,
                    dateString = dateStr,
                    completionPercent = percent,
                    isToday = isToday,
                    isCheckedIn = isCheckedIn,
                    completedCount = savedRecord?.completedTargetsCount ?: if (isToday) dailyTargets.count { it.isCompleted } + (if (currentMetrics.isStepsCompleted) 1 else 0) else 0,
                    totalCount = savedRecord?.totalTargetsCount ?: (dailyTargets.size + 1),
                    summaryNotes = savedRecord?.summaryNotes ?: if (isToday) "${dailyTargets.count { it.isCompleted }}/${dailyTargets.size} habit targets done" else "No activity logged",
                    waterDone = if (isToday) currentMetrics.isWaterCompleted else percent >= 50,
                    sleepDone = if (isToday) currentMetrics.isSleepCompleted else percent >= 50,
                    stepsDone = if (isToday) currentMetrics.isStepsCompleted else savedRecord?.stepsDone == true,
                    workoutDone = if (isToday) currentMetrics.isWorkoutCompleted else percent >= 100
                )
            )
        }
        list
    }

    var selectedDay by remember {
        mutableStateOf<DisciplineDay?>(disciplineDays.firstOrNull { it.isToday } ?: disciplineDays.firstOrNull())
    }

    val totalPastDays = remember(disciplineDays) {
        disciplineDays.filter { it.dayNumber <= currentDay }
    }

    val perfectDays = remember(totalPastDays) {
        totalPastDays.count { it.completionPercent >= 100 || it.isCheckedIn }
    }

    val activeDays = remember(totalPastDays) {
        totalPastDays.count { it.completionPercent > 0 || it.isCheckedIn }
    }

    val consistencyRate = remember(totalPastDays) {
        if (totalPastDays.isEmpty()) 0
        else (totalPastDays.sumOf { it.completionPercent } / totalPastDays.size).coerceIn(0, 100)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_discipline_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Monthly Discipline",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
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
                // Top Consistency & Streak Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Consistency Rate Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VitalitySurfaceVariant),
                        border = BorderStroke(1.dp, VitalityBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Consistency",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$consistencyRate%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                ),
                                color = EmeraldGreen
                            )
                            Text(
                                text = "$activeDays/${totalPastDays.size} active days",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    // Streak Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VitalitySurfaceVariant),
                        border = BorderStroke(1.dp, VitalityBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = VibrantOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Current Streak",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currentMetrics.streakDays} Days",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                ),
                                color = VibrantOrange
                            )
                            Text(
                                text = "$perfectDays perfect days",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Heatmap Color Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "30-Day Activity Heatmap",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeatmapLegendItem(color = EmeraldGreen, label = "100%")
                        HeatmapLegendItem(color = VibrantOrange, label = "Partial")
                        HeatmapLegendItem(color = VitalityBorderSubtle, label = "Missed")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 30-Day Heatmap Grid (6 columns x 5 rows)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = VitalitySurfaceVariant),
                    border = BorderStroke(1.dp, VitalityBorder)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        items(disciplineDays, key = { it.dayNumber }) { day ->
                            val isSelected = selectedDay?.dayNumber == day.dayNumber
                            val bgBrush = when {
                                day.completionPercent >= 100 -> Brush.linearGradient(listOf(EmeraldGreen, Color(0xFF00BFA5)))
                                day.completionPercent > 0 -> Brush.linearGradient(listOf(VibrantOrange, Color(0xFFFFB300)))
                                else -> Brush.linearGradient(listOf(VitalitySurface, VitalitySurface))
                            }
                            val borderColor = when {
                                isSelected -> Color.White
                                day.isToday -> ElectricCyan
                                day.completionPercent >= 100 -> EmeraldGreen.copy(alpha = 0.5f)
                                day.completionPercent > 0 -> VibrantOrange.copy(alpha = 0.5f)
                                else -> VitalityBorder
                            }

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bgBrush)
                                    .border(
                                        width = if (isSelected || day.isToday) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDay = day }
                                    .testTag("heatmap_day_${day.dayNumber}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${day.dayNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (day.completionPercent > 0) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (day.completionPercent > 0) Color(0xFF0D1B12) else TextMuted
                                    )
                                    if (day.completionPercent >= 100) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF0D1B12),
                                            modifier = Modifier.size(9.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selected Day Details Breakdown
                selectedDay?.let { day ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = VitalitySurfaceVariant,
                        border = BorderStroke(1.dp, VitalityBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (day.isToday) "Today (Day ${day.dayNumber})" else "Day ${day.dayNumber} Breakdown",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        day.completionPercent >= 100 -> EmeraldGreen.copy(alpha = 0.2f)
                                        day.completionPercent > 0 -> VibrantOrange.copy(alpha = 0.2f)
                                        else -> VitalityBorderSubtle
                                    }
                                ) {
                                    Text(
                                        text = "${day.completionPercent}% Completed",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        ),
                                        color = when {
                                            day.completionPercent >= 100 -> EmeraldGreen
                                            day.completionPercent > 0 -> VibrantOrange
                                            else -> TextMuted
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = day.summaryNotes,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = TextSecondary
                            )

                            if (day.isToday && !day.isCheckedIn) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onCheckInToday,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantOrange,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("check_in_today_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Complete Today's Check-in (+1 Streak)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else if (day.isToday && day.isCheckedIn) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Today is checked in & permanently saved to history!",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = EmeraldGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color(0xFF003817)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_discipline_dialog_button")
            ) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun HeatmapLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextSecondary
        )
    }
}

@Composable
fun MetricStatusPill(label: String, isDone: Boolean, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDone) color.copy(alpha = 0.15f) else VitalitySurface)
            .border(1.dp, if (isDone) color.copy(alpha = 0.4f) else VitalityBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isDone) color else TextMuted)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isDone) color else TextSecondary
        )
    }
}
