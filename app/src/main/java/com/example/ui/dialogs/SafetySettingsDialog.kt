package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.SafetyThresholds

@Composable
fun SafetySettingsDialog(
    currentThresholds: SafetyThresholds,
    onSaveThresholds: (SafetyThresholds) -> Unit,
    onDismiss: () -> Unit
) {
    var tempWarning by remember { mutableFloatStateOf(currentThresholds.engineTempWarningC) }
    var tempCritical by remember { mutableFloatStateOf(currentThresholds.engineTempCriticalC) }
    var rpmRedline by remember { mutableFloatStateOf(currentThresholds.rpmRedline.toFloat()) }
    var batteryMin by remember { mutableFloatStateOf(currentThresholds.batteryMinVoltage) }
    var speedLimit by remember { mutableFloatStateOf(currentThresholds.speedLimitKmh) }
    var soundEnabled by remember { mutableStateOf(currentThresholds.soundAlertEnabled) }
    var vibrateEnabled by remember { mutableStateOf(currentThresholds.vibrateAlertEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFFFB300)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pengaturan Batas Aman Kendaraan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Aplikasi akan membunyikan alarm dan bergetar otomatis jika parameter melampaui batas yang Anda tetapkan.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Suhu Mesin Kritis Slider
                ThresholdSliderItem(
                    label = "Batas Suhu Mesin Kritis (Overheat)",
                    valueText = "${tempCritical.toInt()}°C",
                    value = tempCritical,
                    valueRange = 90f..120f,
                    activeColor = Color(0xFFFF1744),
                    onValueChange = { tempCritical = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Suhu Mesin Peringatan Slider
                ThresholdSliderItem(
                    label = "Batas Suhu Mesin Waspada",
                    valueText = "${tempWarning.toInt()}°C",
                    value = tempWarning,
                    valueRange = 85f..105f,
                    activeColor = Color(0xFFFF9100),
                    onValueChange = { tempWarning = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // RPM Redline Slider
                ThresholdSliderItem(
                    label = "Batas RPM Redline Mesin",
                    valueText = "${rpmRedline.toInt()} RPM",
                    value = rpmRedline,
                    valueRange = 4000f..8500f,
                    activeColor = Color(0xFFFF1744),
                    onValueChange = { rpmRedline = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Aki Minimum Voltage
                ThresholdSliderItem(
                    label = "Tegangan Aki Minimum (Lemah)",
                    valueText = "${String.format("%.1f", batteryMin)} V",
                    value = batteryMin,
                    valueRange = 10.5f..12.5f,
                    activeColor = Color(0xFFFF9100),
                    onValueChange = { batteryMin = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Batas Kecepatan
                ThresholdSliderItem(
                    label = "Batas Kecepatan Maksimal",
                    valueText = "${speedLimit.toInt()} km/h",
                    value = speedLimit,
                    valueRange = 60f..180f,
                    activeColor = Color(0xFF00E5FF),
                    onValueChange = { speedLimit = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Alarm Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Bunyikan Alarm Beep",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Vibration Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Getar Perangkat Saat Bahaya",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                    }
                    Switch(
                        checked = vibrateEnabled,
                        onCheckedChange = { vibrateEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveThresholds(
                        SafetyThresholds(
                            engineTempWarningC = tempWarning,
                            engineTempCriticalC = tempCritical,
                            rpmRedline = rpmRedline.toInt(),
                            batteryMinVoltage = batteryMin,
                            batteryMaxVoltage = currentThresholds.batteryMaxVoltage,
                            speedLimitKmh = speedLimit,
                            soundAlertEnabled = soundEnabled,
                            vibrateAlertEnabled = vibrateEnabled
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_safety_settings_button")
            ) {
                Text("Simpan", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_safety_settings_button")
            ) {
                Text("Batal", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0B111A)
    )
}

@Composable
private fun ThresholdSliderItem(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1))
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = activeColor
                )
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
