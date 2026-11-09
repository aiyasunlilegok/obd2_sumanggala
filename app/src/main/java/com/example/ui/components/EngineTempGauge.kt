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
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
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
import com.example.model.TempUnit

@Composable
fun EngineTempGauge(
    tempC: Float,
    tempUnit: TempUnit,
    warningThresholdC: Float = 95f,
    criticalThresholdC: Float = 105f,
    themeColor: Color,
    cardBg: Color = Color(0xFF131D2D),
    textColor: Color = Color.White,
    strokeColor: Color = Color(0xFF1E293B),
    modifier: Modifier = Modifier
) {
    val displayTemp = if (tempUnit == TempUnit.FAHRENHEIT) {
        (tempC * 9f / 5f) + 32f
    } else {
        tempC
    }

    val isOverheat = tempC >= criticalThresholdC
    val isWarning = tempC >= warningThresholdC && !isOverheat

    val infiniteTransition = rememberInfiniteTransition(label = "temp_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "temp_glow"
    )

    val (statusLabel, statusColor) = when {
        isOverheat -> "OVERHEAT KRITIS!" to Color(0xFFFF1744)
        isWarning -> "SUHU PANAS" to Color(0xFFFFB300)
        tempC < 70f -> "Pemanasan Mesin" to Color(0xFF64B5F6)
        else -> "Suhu Optimal" to Color(0xFF00E676)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = if (isOverheat) 2.dp else 1.dp,
                color = if (isOverheat) Color(0xFFFF1744).copy(alpha = pulseAlpha)
                else if (isWarning) Color(0xFFFFB300)
                else strokeColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("engine_temp_gauge")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
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
                            imageVector = if (isOverheat) Icons.Default.Warning else Icons.Default.Thermostat,
                            contentDescription = "Suhu Mesin",
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SUHU MESIN",
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

                // Numeric readout
                Text(
                    text = "${String.format("%.1f", displayTemp)}${tempUnit.label}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isOverheat) Color(0xFFFF1744) else textColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Progress Bar (40°C to 120°C scale)
            val minTemp = 40f
            val maxTemp = 120f
            val progress = ((tempC - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 1f)

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

            // Reference tick labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "40°C C",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
                )
                Text(
                    text = "90°C NORMAL",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00E676), fontSize = 9.sp)
                )
                Text(
                    text = "${criticalThresholdC.toInt()}°C H",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFF1744), fontSize = 9.sp)
                )
            }
        }
    }
}
