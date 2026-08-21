package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.HabitEntity
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

@Composable
fun ProfileDialog(
    metrics: DailyMetricsEntity,
    habits: List<HabitEntity>,
    onSaveProfileAndTargets: (name: String, goal: String, avatarIndex: Int, water: Int, sleep: Float, steps: Int, workout: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var editedName by remember(metrics.userName) { mutableStateOf(metrics.userName) }
    var editedGoal by remember(metrics.userGoal) { mutableStateOf(metrics.userGoal) }
    var selectedAvatarIndex by remember(metrics.avatarIndex) { mutableIntStateOf(metrics.avatarIndex.coerceIn(0, avatarIcons.size - 1)) }

    // Target Goals
    var waterTarget by remember(metrics.waterTargetMl) { mutableIntStateOf(metrics.waterTargetMl) }
    var sleepTarget by remember(metrics.sleepTargetHours) { mutableFloatStateOf(metrics.sleepTargetHours) }
    var stepsTarget by remember(metrics.stepTarget) { mutableIntStateOf(metrics.stepTarget) }
    var workoutTarget by remember(metrics.workoutTargetMinutes) { mutableIntStateOf(metrics.workoutTargetMinutes) }

    val completedHabits = habits.count { it.completed }
    val totalHabits = habits.size

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_dialog"),
        containerColor = VitalitySurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Selected Avatar Preview
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(avatarGradients[selectedAvatarIndex])),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = avatarIcons[selectedAvatarIndex],
                        contentDescription = "Avatar Preview",
                        tint = Color.Black.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Edit Profile & Targets",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Customize profile, daily targets & goals",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
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
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 12.dp),
                    color = VitalityBorder
                )

                // Avatar Selector
                Text(
                    text = "Choose Profile Avatar",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(avatarIcons) { index, icon ->
                        val isSelected = index == selectedAvatarIndex
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(avatarGradients[index]))
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape)
                                    else Modifier.border(1.dp, VitalityBorderSubtle, CircleShape)
                                )
                                .clickable { selectedAvatarIndex = index }
                                .testTag("avatar_option_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Avatar $index",
                                tint = Color.Black.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name Input
                Text(
                    text = "Your Display Name",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    placeholder = { Text("e.g., Alex Morgan", color = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VitalitySurfaceVariant,
                        unfocusedContainerColor = VitalitySurfaceVariant,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Health Goals Input
                Text(
                    text = "Primary Focus / AI Goal",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editedGoal,
                    onValueChange = { editedGoal = it },
                    placeholder = { Text("e.g. 10k steps, marathon prep, sleep before 11pm", color = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_goal_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VitalitySurfaceVariant,
                        unfocusedContainerColor = VitalitySurfaceVariant,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = VitalityBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.LightGray
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Target Customization Section
                Text(
                    text = "Daily Target Goals",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Water Target
                    TargetStepperRow(
                        label = "Water Target",
                        valueText = "$waterTarget ml",
                        color = ElectricCyan,
                        onDecrement = { if (waterTarget > 500) waterTarget -= 250 },
                        onIncrement = { if (waterTarget < 6000) waterTarget += 250 }
                    )

                    // Sleep Target
                    TargetStepperRow(
                        label = "Sleep Target",
                        valueText = String.format("%.1f hrs", sleepTarget),
                        color = SoftPurple,
                        onDecrement = { if (sleepTarget > 4f) sleepTarget -= 0.5f },
                        onIncrement = { if (sleepTarget < 12f) sleepTarget += 0.5f }
                    )

                    // Steps Target
                    TargetStepperRow(
                        label = "Steps Target",
                        valueText = "$stepsTarget steps",
                        color = EmeraldGreen,
                        onDecrement = { if (stepsTarget > 2000) stepsTarget -= 1000 },
                        onIncrement = { if (stepsTarget < 30000) stepsTarget += 1000 }
                    )

                    // Workout Target
                    TargetStepperRow(
                        label = "Workout Target",
                        valueText = "$workoutTarget mins",
                        color = VibrantOrange,
                        onDecrement = { if (workoutTarget > 10) workoutTarget -= 5 },
                        onIncrement = { if (workoutTarget < 180) workoutTarget += 5 }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = VitalitySurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, VitalityBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = VibrantOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${metrics.streakDays} Days",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = VibrantOrange
                                )
                                Text(
                                    text = "Current Streak",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = VitalitySurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, VitalityBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Habits Done",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "$completedHabits / $totalHabits",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = "Habits Today",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = TextSecondary
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
                    onSaveProfileAndTargets(
                        editedName,
                        editedGoal,
                        selectedAvatarIndex,
                        waterTarget,
                        sleepTarget,
                        stepsTarget,
                        workoutTarget
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = Color(0xFF003817)
                ),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalityBorder),
                modifier = Modifier.testTag("cancel_profile_button")
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun TargetStepperRow(
    label: String,
    valueText: String,
    color: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = VitalitySurfaceVariant,
        border = BorderStroke(1.dp, VitalityBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(VitalitySurface)
                        .border(1.dp, VitalityBorder, CircleShape)
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = color,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(VitalitySurface)
                        .border(1.dp, VitalityBorder, CircleShape)
                        .clickable(onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
            }
        }
    }
}
