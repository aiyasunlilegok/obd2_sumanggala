package com.example.ui.dialogs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.obd.BluetoothDeviceInfo
import com.example.obd.ObdConnectionState

@Composable
fun BluetoothObdDialog(
    connectionState: ObdConnectionState,
    pairedDevices: List<BluetoothDeviceInfo>,
    isSimulatorActive: Boolean,
    packetsReceived: Int = 0,
    lastRawPacket: String = "",
    onConnectDevice: (String, String) -> Unit,
    onDisconnect: () -> Unit,
    onToggleSimulator: (Boolean) -> Unit,
    onTestOverheat: (Boolean) -> Unit,
    onTestRedline: (Boolean) -> Unit,
    onTestLowBattery: (Boolean) -> Unit,
    onTestSpeeding: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var testOverheatActive by remember { mutableStateOf(false) }
    var testRedlineActive by remember { mutableStateOf(false) }
    var testLowBatteryActive by remember { mutableStateOf(false) }
    var testSpeedingActive by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Koneksi OBD-II ELM327",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Current Connection Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (dotColor, statusTitle, statusSub) = when (connectionState) {
                            is ObdConnectionState.Connected -> Triple(
                                Color(0xFF00E676),
                                "OBD-II Terhubung: ${connectionState.deviceName}",
                                if (packetsReceived > 0) "Data Masuk: $packetsReceived paket | PID: $lastRawPacket" else "Protokol: ${connectionState.protocol} (Membaca sensor...)"
                            )
                            is ObdConnectionState.Connecting -> Triple(
                                Color(0xFFFFB300),
                                "Menghubungkan ke ${connectionState.deviceName}...",
                                "Menginisialisasi AT commands (ATZ, ATSP0)..."
                            )
                            is ObdConnectionState.Simulating -> Triple(
                                Color(0xFF00E5FF),
                                "Mode Simulasi Aktif (Demo)",
                                "Angka bergerak otomatis. Matikan sakelar simulasi di bawah jika ingin membaca data asli mobil."
                            )
                            is ObdConnectionState.Error -> Triple(
                                Color(0xFFFF1744),
                                "Koneksi Gagal",
                                connectionState.message
                            )
                            else -> Triple(
                                Color(0xFF94A3B8),
                                "OBD-II Terputus (Mode Nyata)",
                                "Simulasi mati. Jarum berada di 0. Pilih perangkat ELM327 di bawah untuk membaca data asli mobil."
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = statusSub,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle: Realistic Drive Simulator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Simulasi Realistis",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Gunakan saat tidak berada di dalam mobil",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        )
                    }
                    Switch(
                        checked = isSimulatorActive,
                        onCheckedChange = { onToggleSimulator(it) },
                        modifier = Modifier.testTag("toggle_simulator_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Safety Alert Test Scenarios Section
                Text(
                    text = "Uji Skenario Peringatan Otomatis:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            testOverheatActive = !testOverheatActive
                            onTestOverheat(testOverheatActive)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_overheat_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (testOverheatActive) Color(0xFFFF1744) else Color(0xFF1E293B),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (testOverheatActive) "Overheat!" else "Tes Suhu",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            testRedlineActive = !testRedlineActive
                            onTestRedline(testRedlineActive)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_redline_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (testRedlineActive) Color(0xFFFF9100) else Color(0xFF1E293B),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (testRedlineActive) "Redline!" else "Tes RPM",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            testLowBatteryActive = !testLowBatteryActive
                            onTestLowBattery(testLowBatteryActive)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_battery_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (testLowBatteryActive) Color(0xFFFF1744) else Color(0xFF1E293B),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (testLowBatteryActive) "Aki Low!" else "Tes Aki",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(8.dp))

                // Paired Bluetooth ELM327 Devices list
                Text(
                    text = "Daftar Perangkat Bluetooth Terpasang:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (pairedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada ELM327 yang terpasang.\nPasangkan dongle OBD-II Anda di menu Bluetooth Android atau gunakan Mode Simulasi.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(pairedDevices) { dev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable { onConnectDevice(dev.address, dev.name) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = dev.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = dev.address,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF64748B),
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "Hubungkan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_obd_dialog_button")
            ) {
                Text("Tutup", color = Color(0xFF00E5FF))
            }
        },
        dismissButton = {
            if (connectionState is ObdConnectionState.Connected) {
                TextButton(
                    onClick = onDisconnect,
                    modifier = Modifier.testTag("disconnect_obd_button")
                ) {
                    Text("Putuskan", color = Color(0xFFFF1744))
                }
            }
        },
        containerColor = Color(0xFF0B111A)
    )
}
