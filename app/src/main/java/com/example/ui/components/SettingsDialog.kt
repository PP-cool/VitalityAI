package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyMetricsEntity
import com.example.data.local.DailyTargetEntity
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VitalityBorder
import com.example.ui.theme.VitalitySurface
import com.example.ui.theme.VitalitySurfaceVariant
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(
    currentMetrics: DailyMetricsEntity,
    dailyTargets: List<DailyTargetEntity> = emptyList(),
    onSaveStepTarget: (Int) -> Unit = {},
    onAddTarget: () -> Unit = {},
    onEditTarget: (DailyTargetEntity) -> Unit = {},
    onDeleteTarget: (DailyTargetEntity) -> Unit = {},
    onUpdateReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit = { _, _, _ -> },
    onOpenReminderDialog: () -> Unit = {},
    onClearChat: () -> Unit,
    onResetDemoData: () -> Unit,
    onDismiss: () -> Unit
) {
    var stepsTarget by remember { mutableFloatStateOf(currentMetrics.stepTarget.toFloat()) }
    var isReminderOn by remember { mutableStateOf(currentMetrics.isReminderEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("settings_dialog"),
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Target Goals & Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Configure your daily step target and custom habit goals. All metrics sync in real-time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Steps Goal Section
                Text(
                    text = "Live Step Goal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Step Goal: ${stepsTarget.roundToInt()} steps",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
                Slider(
                    value = stepsTarget,
                    onValueChange = { stepsTarget = (it / 500).roundToInt() * 500f },
                    valueRange = 2000f..25000f,
                    steps = 45,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldGreen,
                        activeTrackColor = EmeraldGreen,
                        inactiveTrackColor = VitalityBorder
                    )
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = VitalityBorder
                )

                // Manage Daily Targets Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Habit Targets (${dailyTargets.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ElectricCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onDismiss()
                                onAddTarget()
                            }
                            .testTag("settings_add_target_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Target",
                                tint = ElectricCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+ Add",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (dailyTargets.isEmpty()) {
                    Text(
                        text = "No custom targets created yet. Tap '+ Add' to create one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    dailyTargets.forEach { target ->
                        val targetColor = Color(target.colorHex)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, VitalityBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(targetColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getTargetIcon(target.iconName),
                                            contentDescription = null,
                                            tint = targetColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = target.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.5.sp
                                            ),
                                            color = TextPrimary
                                        )
                                        val formattedTarget = if (target.targetValue % 1f == 0f) {
                                            target.targetValue.toInt().toString()
                                        } else {
                                            String.format(Locale.getDefault(), "%.1f", target.targetValue)
                                        }
                                        Text(
                                            text = "Target: $formattedTarget ${target.unit} (step: ${target.stepDelta})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            onDismiss()
                                            onEditTarget(target)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Target",
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            onDismiss()
                                            onDeleteTarget(target)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Target",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = VitalityBorder
                )

                // Daily Notification & Reminder Section
                Text(
                    text = "Daily Habit Reminders",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isReminderOn) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isReminderOn) EmeraldGreen else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Push Reminders",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = TextPrimary
                                    )
                                    val formattedTime = String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d %s",
                                        if (currentMetrics.reminderHour == 0 || currentMetrics.reminderHour == 12) 12 else currentMetrics.reminderHour % 12,
                                        currentMetrics.reminderMinute,
                                        if (currentMetrics.reminderHour < 12) "AM" else "PM"
                                    )
                                    Text(
                                        text = if (isReminderOn) "Scheduled daily at $formattedTime" else "Turned Off",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = if (isReminderOn) ElectricCyan else TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isReminderOn,
                                onCheckedChange = { checked ->
                                    isReminderOn = checked
                                    onUpdateReminder(
                                        checked,
                                        currentMetrics.reminderHour,
                                        currentMetrics.reminderMinute
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = EmeraldGreen,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = VitalitySurface
                                ),
                                modifier = Modifier.testTag("settings_reminder_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenReminderDialog()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, VitalityBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Set Custom Reminder Time",
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = VitalityBorder
                )

                // Data Actions
                Text(
                    text = "Data Controls",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onClearChat()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_chat_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VitalityBorder)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear AI Chat History", color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onResetDemoData()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_demo_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCF6679).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFCF6679)
                    )
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Progress to 0")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveStepTarget(stepsTarget.roundToInt())
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color(0xFF003817)
                ),
                modifier = Modifier.testTag("save_targets_button")
            ) {
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder)
            ) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

