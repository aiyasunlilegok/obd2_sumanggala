package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedUnit
import com.example.model.VehicleTelemetry

@Composable
fun MiniTelemetryHud(
    telemetry: VehicleTelemetry,
    speedUnit: SpeedUnit,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    val displaySpeed = telemetry.speedKmh * speedUnit.factorToKmh

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.92f))
            .border(1.dp, themeColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("mini_telemetry_hud")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed HUD
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${displaySpeed.toInt()}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = speedUnit.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = themeColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF334155))
            )

            // RPM
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${telemetry.rpm}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (telemetry.rpm > 6000) Color(0xFFFF1744) else Color.White
                    )
                )
                Text(
                    text = "RPM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                )
            }

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF334155))
            )

            // Engine Temp
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${telemetry.engineTempC.toInt()}°C",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (telemetry.engineTempC >= 95f) Color(0xFFFF1744) else Color.White
                    )
                )
                Text(
                    text = "SUHU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                )
            }

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFF334155))
            )

            // Battery
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${String.format("%.1f", telemetry.batteryVoltage)}V",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (telemetry.batteryVoltage < 11.8f) Color(0xFFFF1744) else Color.White
                    )
                )
                Text(
                    text = "AKI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}
