package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance circular/arc gauge for RPM, Speed, and Automotive meters.
 */
@Composable
fun ArcGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    title: String,
    unit: String,
    gaugeColor: Color,
    dangerThreshold: Float? = null,
    dangerColor: Color = Color(0xFFFF1744),
    textColor: Color = Color.White,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    showTicks: Boolean = true,
    valueFormat: String = "%.0f"
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(minValue, maxValue),
        animationSpec = tween(durationMillis = 120),
        label = "gauge_val"
    )

    val progress = ((animatedValue - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val startAngle = 140f
    val sweepAngle = 260f
    val currentAngle = startAngle + (progress * sweepAngle)

    val isDanger = dangerThreshold != null && value >= dangerThreshold

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14f
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth * 2) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val arcRect = Size(radius * 2f, radius * 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)

            // 1. Background Arc Track
            drawArc(
                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcRect,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Danger threshold arc track
            if (dangerThreshold != null && dangerThreshold < maxValue) {
                val dangerStartNorm = (dangerThreshold - minValue) / (maxValue - minValue)
                val dangerStartAngle = startAngle + (dangerStartNorm * sweepAngle)
                val dangerSweep = (1f - dangerStartNorm) * sweepAngle
                drawArc(
                    color = dangerColor.copy(alpha = 0.35f),
                    startAngle = dangerStartAngle,
                    sweepAngle = dangerSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcRect,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 3. Active Value Progress Arc
            val activeColor = if (isDanger) dangerColor else gaugeColor
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        gaugeColor.copy(alpha = 0.4f),
                        activeColor,
                        if (isDanger) dangerColor else gaugeColor
                    ),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcRect,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 4. Subtle Dial Ticks
            if (showTicks) {
                val numTicks = 9
                for (i in 0..numTicks) {
                    val tickAngleDeg = startAngle + (i.toFloat() / numTicks) * sweepAngle
                    val tickRad = Math.toRadians(tickAngleDeg.toDouble())
                    val innerR = radius - strokeWidth - 4f
                    val outerR = radius - strokeWidth - 14f
                    val tickColor = if (dangerThreshold != null &&
                        (minValue + (i.toFloat() / numTicks) * (maxValue - minValue)) >= dangerThreshold
                    ) dangerColor else Color(0xFF64748B)

                    drawLine(
                        color = tickColor,
                        start = Offset(
                            center.x + innerR * cos(tickRad).toFloat(),
                            center.y + innerR * sin(tickRad).toFloat()
                        ),
                        end = Offset(
                            center.x + outerR * cos(tickRad).toFloat(),
                            center.y + outerR * sin(tickRad).toFloat()
                        ),
                        strokeWidth = if (i % 2 == 0) 2.5f else 1.5f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 5. Glowing Pointer Needle
            val needleRad = Math.toRadians(currentAngle.toDouble())
            val needleLength = radius - 8f
            val needleTip = Offset(
                center.x + needleLength * cos(needleRad).toFloat(),
                center.y + needleLength * sin(needleRad).toFloat()
            )
            // Needle glow
            drawLine(
                color = activeColor.copy(alpha = 0.4f),
                start = center,
                end = needleTip,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            // Core needle
            drawLine(
                color = activeColor,
                start = center,
                end = needleTip,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Center pivot knob
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 10f,
                center = center
            )
            drawCircle(
                color = activeColor,
                radius = 5f,
                center = center
            )
        }

        // Center Digital Numeric Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format(valueFormat, value),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (isDanger) dangerColor else textColor,
                    fontSize = if (size > 140.dp) 26.sp else 18.sp
                )
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDanger) dangerColor else gaugeColor,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            )
        }
    }
}
