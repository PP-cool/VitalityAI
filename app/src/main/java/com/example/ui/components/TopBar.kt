package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantOrange
import com.example.ui.theme.VitalityBorder
import com.example.ui.theme.VitalitySurface
import com.example.ui.theme.VitalitySurfaceVariant

val avatarIcons = listOf(
    Icons.Default.Person,
    Icons.AutoMirrored.Filled.DirectionsRun,
    Icons.Default.SelfImprovement,
    Icons.Default.Spa,
    Icons.Default.Favorite,
    Icons.Default.LocalFireDepartment
)

val avatarGradients = listOf(
    listOf(EmeraldGreen, ElectricCyan),
    listOf(VibrantOrange, Color(0xFFFFD600)),
    listOf(SoftPurple, ElectricCyan),
    listOf(Color(0xFFFF4081), SoftPurple),
    listOf(ElectricCyan, Color(0xFF0072FF)),
    listOf(Color(0xFFFF5252), VibrantOrange)
)

@Composable
fun TopBar(
    userName: String,
    streakDays: Int,
    avatarIndex: Int,
    isReminderEnabled: Boolean = false,
    onProfileClick: () -> Unit,
    onDisciplineClick: () -> Unit,
    onReminderClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeAvatarIndex = avatarIndex.coerceIn(0, avatarIcons.size - 1)
    val selectedIcon = avatarIcons[safeAvatarIndex]
    val selectedGradient = avatarGradients[safeAvatarIndex]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 24.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
            .testTag("top_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Profile Pill & Monthly Discipline Icon Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Interactive Profile Pill
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = VitalitySurface,
                border = BorderStroke(1.dp, VitalityBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .clickable(onClick = onProfileClick)
                    .testTag("profile_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gradient Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(selectedGradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selectedIcon,
                            contentDescription = "User Profile Avatar",
                            tint = Color.Black.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = userName.ifBlank { "Alex M." },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Streak Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VibrantOrange.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = VibrantOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${streakDays}d",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp
                            ),
                            color = VibrantOrange
                        )
                    }
                }
            }

            // Monthly Discipline Calendar Button
            Surface(
                shape = CircleShape,
                color = VitalitySurface,
                border = BorderStroke(1.dp, VitalityBorder),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .testTag("discipline_calendar_button")
            ) {
                IconButton(
                    onClick = onDisciplineClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Monthly Discipline Heatmap",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Center / Right: App Branding
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Vitality",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp
                ),
                color = TextPrimary
            )
            Text(
                text = "AI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp
                ),
                color = EmeraldGreen
            )
        }

        // Top-Right: Notification Bell & Settings Gear Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Daily Habit Reminder Bell Button
            Surface(
                shape = CircleShape,
                color = VitalitySurface,
                border = BorderStroke(1.dp, if (isReminderEnabled) ElectricCyan.copy(alpha = 0.5f) else VitalityBorder),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
            ) {
                IconButton(
                    onClick = onReminderClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("notification_reminder_button")
                ) {
                    Icon(
                        imageVector = if (isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = "Daily Habit Reminder",
                        tint = if (isReminderEnabled) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Settings Gear Action
            Surface(
                shape = CircleShape,
                color = VitalitySurface,
                border = BorderStroke(1.dp, VitalityBorder),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
