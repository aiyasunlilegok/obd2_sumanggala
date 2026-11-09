package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BatteryGauge(
    voltage: Float,
    minThreshold: Float = 11.8f,
    maxThreshold: Float = 15.0f,
    themeColor: Color,
    cardBg: Color = Color(0xFF131D2D),
    textColor: Color = Color.White,
    strokeColor: Color = Color(0xFF1E293B),
    modifier: Modifier = Modifier
) {
    val isLowBattery = voltage < minThreshold
    val isOverVoltage = voltage > maxThreshold
    val isAbnormal = isLowBattery || isOverVoltage

    val infiniteTransition = rememberInfiniteTransition(label = "volt_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450),
            repeatMode = RepeatMode.Reverse
        ),
        label = "volt_glow"
    )

    val (statusLabel, statusColor) = when {
        isLowBattery -> "AKI LEMAH / DROP" to Color(0xFFFF1744)
        isOverVoltage -> "OVERVOLT!" to Color(0xFFFF1744)
        voltage >= 13.5f -> "Alternator Mengisi" to Color(0xFF00E5FF)
        voltage >= 12.2f -> "Aki Sehat" to Color(0xFF00E676)
        else -> "Tegangan Rendah" to Color(0xFFFFB300)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = if (isAbnormal) 2.dp else 1.dp,
                color = if (isAbnormal) Color(0xFFFF1744).copy(alpha = pulseAlpha) else strokeColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("battery_gauge")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAbnormal) Icons.Default.BatteryAlert else Icons.Default.BatteryChargingFull,
                            contentDescription = "Tegangan Aki",
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "TEGANGAN AKI / BATTERY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Text(
                    text = "${String.format("%.2f", voltage)} V",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isAbnormal) Color(0xFFFF1744) else textColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Voltage Range Bar (10.0V to 16.0V scale)
            val minV = 10.0f
            val maxV = 16.0f
            val progress = ((voltage - minV) / (maxV - minV)).coerceIn(0f, 1f)

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = Color(0xFF1E293B),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "10.0V",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
                )
                Text(
                    text = "12.6V STANDBY",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
                )
                Text(
                    text = "14.2V CHARGE",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00E5FF), fontSize = 9.sp)
                )
                Text(
                    text = "16.0V",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
                )
            }
        }
    }
}
