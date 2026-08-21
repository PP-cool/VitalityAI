package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VitalityBackground
import com.example.ui.theme.VitalityBorder
import com.example.ui.theme.VitalitySurface
import com.example.ui.theme.VitalitySurfaceVariant
import java.util.Locale

data class QuickTimePreset(
    val label: String,
    val emoji: String,
    val hour24: Int,
    val minute: Int
)

@Composable
fun ReminderDialog(
    isReminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onSaveReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onSendTestNotification: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(isReminderEnabled) }

    // Internal 24-hour time state
    var selectedHour24 by remember { mutableIntStateOf(reminderHour.coerceIn(0, 23)) }
    var selectedMinute by remember { mutableIntStateOf(reminderMinute.coerceIn(0, 59)) }

    // Derived 12-hour values
    val isPm = selectedHour24 >= 12
    val displayHour12 = when {
        selectedHour24 == 0 -> 12
        selectedHour24 > 12 -> selectedHour24 - 12
        else -> selectedHour24
    }

    // Active edit mode: "none", "hour", "minute"
    var activePicker by remember { mutableStateOf("none") }

    val presets = remember {
        listOf(
            QuickTimePreset("Morning", "🌅", 7, 0),
            QuickTimePreset("Afternoon", "☀️", 13, 0),
            QuickTimePreset("Evening", "🌇", 19, 30),
            QuickTimePreset("Night", "🌙", 21, 30)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isEnabled = true
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isReminderEnabled) {
            val check = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (check != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VitalitySurface,
            border = BorderStroke(1.dp, VitalityBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 12.dp)
                .testTag("reminder_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with bell icon & close button
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
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricCyan.copy(alpha = 0.25f), EmeraldGreen.copy(alpha = 0.25f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = "Reminder Icon",
                                tint = if (isEnabled) ElectricCyan else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daily Reminder",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = if (isEnabled) "Active notifications" else "Disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) EmeraldGreen else TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_reminder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ON/OFF Switch Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isEnabled) EmeraldGreen.copy(alpha = 0.12f) else VitalitySurfaceVariant,
                    border = BorderStroke(1.dp, if (isEnabled) EmeraldGreen.copy(alpha = 0.35f) else VitalityBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnabled) "🔔 Reminder notifications ON" else "🔕 Reminder notifications OFF",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isEnabled) TextPrimary else TextSecondary
                        )

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val check = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                    if (check != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        isEnabled = true
                                    }
                                } else {
                                    isEnabled = checked
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldGreen,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = VitalitySurface
                            ),
                            modifier = Modifier.testTag("reminder_toggle_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direct Touch Digital Clock Display (NO KEYBOARD NEEDED)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = VitalityBackground,
                    border = BorderStroke(1.dp, VitalityBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TAP TO CHANGE TIME (NO KEYBOARD NEEDED)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Digital Clock Digits & AM/PM Selectors
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Hour Box (+ / - and Tap)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        var newH12 = displayHour12 + 1
                                        if (newH12 > 12) newH12 = 1
                                        selectedHour24 = if (isPm) {
                                            if (newH12 == 12) 12 else newH12 + 12
                                        } else {
                                            if (newH12 == 12) 0 else newH12
                                        }
                                    },
                                    modifier = Modifier.size(28.dp).testTag("hour_plus_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Hour +", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (activePicker == "hour") ElectricCyan.copy(alpha = 0.22f) else VitalitySurfaceVariant,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (activePicker == "hour") ElectricCyan else VitalityBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { activePicker = if (activePicker == "hour") "none" else "hour" }
                                        .testTag("hour_display_card")
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", displayHour12),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 32.sp
                                        ),
                                        color = if (activePicker == "hour") ElectricCyan else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        var newH12 = displayHour12 - 1
                                        if (newH12 < 1) newH12 = 12
                                        selectedHour24 = if (isPm) {
                                            if (newH12 == 12) 12 else newH12 + 12
                                        } else {
                                            if (newH12 == 12) 0 else newH12
                                        }
                                    },
                                    modifier = Modifier.size(28.dp).testTag("hour_minus_button")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Hour -", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = ":",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp
                                ),
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Minute Box (+ / - and Tap)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        selectedMinute = (selectedMinute + 5) % 60
                                    },
                                    modifier = Modifier.size(28.dp).testTag("minute_plus_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Minute +", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (activePicker == "minute") EmeraldGreen.copy(alpha = 0.22f) else VitalitySurfaceVariant,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (activePicker == "minute") EmeraldGreen else VitalityBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { activePicker = if (activePicker == "minute") "none" else "minute" }
                                        .testTag("minute_display_card")
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", selectedMinute),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 32.sp
                                        ),
                                        color = if (activePicker == "minute") EmeraldGreen else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        selectedMinute = if (selectedMinute - 5 < 0) 55 else selectedMinute - 5
                                    },
                                    modifier = Modifier.size(28.dp).testTag("minute_minus_button")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Minute -", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // AM / PM Toggle Pills
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!isPm) ElectricCyan.copy(alpha = 0.25f) else VitalitySurfaceVariant,
                                    border = BorderStroke(1.dp, if (!isPm) ElectricCyan else VitalityBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (isPm) {
                                                selectedHour24 = if (displayHour12 == 12) 0 else displayHour12
                                            }
                                        }
                                        .testTag("toggle_am_button")
                                ) {
                                    Text(
                                        text = "AM",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (!isPm) ElectricCyan else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPm) SoftPurple.copy(alpha = 0.25f) else VitalitySurfaceVariant,
                                    border = BorderStroke(1.dp, if (isPm) SoftPurple else VitalityBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (!isPm) {
                                                selectedHour24 = if (displayHour12 == 12) 12 else displayHour12 + 12
                                            }
                                        }
                                        .testTag("toggle_pm_button")
                                ) {
                                    Text(
                                        text = "PM",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (isPm) SoftPurple else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Direct 1-Tap Quick Number Grids (when Hour or Minute clicked)
                        if (activePicker == "hour") {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items((1..12).toList()) { h ->
                                    val isSelected = displayHour12 == h
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) ElectricCyan else VitalitySurfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) ElectricCyan else VitalityBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedHour24 = if (isPm) {
                                                    if (h == 12) 12 else h + 12
                                                } else {
                                                    if (h == 12) 0 else h
                                                }
                                                activePicker = "none"
                                            }
                                    ) {
                                        Text(
                                            text = "$h",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        } else if (activePicker == "minute") {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)) { m ->
                                    val isSelected = selectedMinute == m
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) EmeraldGreen else VitalitySurfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else VitalityBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedMinute = m
                                                activePicker = "none"
                                            }
                                    ) {
                                        Text(
                                            text = String.format(Locale.getDefault(), "%02d", m),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick 1-Tap Preset Time Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presets.forEach { p ->
                        val isCurrent = selectedHour24 == p.hour24 && selectedMinute == p.minute
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) EmeraldGreen.copy(alpha = 0.2f) else VitalitySurfaceVariant,
                            border = BorderStroke(1.dp, if (isCurrent) EmeraldGreen else VitalityBorder),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedHour24 = p.hour24
                                    selectedMinute = p.minute
                                    activePicker = "none"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = p.emoji, fontSize = 13.sp)
                                Text(
                                    text = p.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    ),
                                    color = if (isCurrent) EmeraldGreen else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Cancel and Save Reminder (ALWAYS VISIBLE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, VitalityBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_reminder_button")
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val check = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                if (check != PackageManager.PERMISSION_GRANTED) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            onSaveReminder(isEnabled, selectedHour24, selectedMinute)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("save_reminder_button")
                    ) {
                        Text(
                            text = "Save Reminder",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
