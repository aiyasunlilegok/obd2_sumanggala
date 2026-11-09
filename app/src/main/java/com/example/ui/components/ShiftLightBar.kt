package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Formula 1 / GT3 style LED Shift Light Bar across the top of the tachometer.
 */
@Composable
fun ShiftLightBar(
    currentRpm: Int,
    maxRpm: Int = 7500,
    redlineRpm: Int = 6000,
    modifier: Modifier = Modifier
) {
    val totalLeds = 12
    val fraction = (currentRpm.toFloat() / maxRpm.toFloat()).coerceIn(0f, 1f)
    val isRedline = currentRpm >= redlineRpm

    val infiniteTransition = rememberInfiniteTransition(label = "shift_light_flash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalLeds) {
            val ledThreshold = 0.35f + (i.toFloat() / totalLeds) * 0.65f
            val isLit = fraction >= ledThreshold || isRedline

            val baseColor = when {
                i < 4 -> Color(0xFF00E676)  // Green (Early)
                i < 8 -> Color(0xFFFFD600)  // Yellow/Amber (Mid)
                else -> Color(0xFFFF1744)   // Red (Peak)
            }

            val ledColor = if (isLit) {
                if (isRedline) {
                    Color(0xFFFF1744).copy(alpha = flashAlpha)
                } else {
                    baseColor
                }
            } else {
                Color(0xFF1E293B)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ledColor)
            )
        }
    }
}
