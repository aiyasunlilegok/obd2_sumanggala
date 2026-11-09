package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedUnit

@Composable
fun SpeedometerCard(
    speedKmh: Float,
    speedUnit: SpeedUnit,
    currentGear: String,
    maxSpeedKmh: Float,
    tripDistanceKm: Float,
    themeColor: Color,
    isSpeedWarning: Boolean,
    cardBg: Color = Color(0xFF131B2E),
    textColor: Color = Color.White,
    strokeColor: Color = Color(0xFF1E293B),
    modifier: Modifier = Modifier
) {
    val displaySpeed = speedKmh * speedUnit.factorToKmh
    val animatedSpeed by animateFloatAsState(
        targetValue = displaySpeed,
        animationSpec = tween(100),
        label = "speed_anim"
    )

    val activeColor = if (isSpeedWarning) Color(0xFFFF1744) else themeColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(
                width = if (isSpeedWarning) 2.dp else 1.dp,
                color = if (isSpeedWarning) Color(0xFFFF1744) else strokeColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .testTag("speedometer_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gear selector display row (P R N D 1 2 3)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val gears = listOf("P", "R", "N", "D", "1", "2", "3", "4", "5")
                gears.forEach { gear ->
                    val isActive = currentGear == gear || (currentGear in listOf("1", "2", "3", "4", "5") && gear == "D")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isActive) activeColor.copy(alpha = 0.25f) else Color.Transparent
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = gear,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isActive) activeColor else Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Large Digital Speed Number
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${animatedSpeed.toInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = textColor,
                        fontSize = 72.sp,
                        letterSpacing = (-2).sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = speedUnit.label.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = activeColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-metrics: Trip & Max Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TRIP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${String.format("%.1f", tripDistanceKm)} km",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(Color(0xFF334155))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MAX SPEED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${(maxSpeedKmh * speedUnit.factorToKmh).toInt()} ${speedUnit.label}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                    )
                }
            }
        }
    }
}
