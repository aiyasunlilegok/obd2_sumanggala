package com.example.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapCanvasView(
    navState: NavigationState,
    onSelectDestination: (RouteDestination) -> Unit,
    onOpenExternalMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    // Pulsing aura animation for vehicle GPS location
    val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B111A))
            .testTag("map_canvas_container")
    ) {
        // 1. Tactical Automotive Map Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background grid / road matrix
            val gridColor = Color(0xFF131D2A)
            val gridSize = 45f * zoomLevel
            var x = 0f
            while (x < w) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
                x += gridSize
            }
            var y = 0f
            while (y < h) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += gridSize
            }

            // Primary arterial avenues (dark slate road lines)
            val roadColor = Color(0xFF1E293B)
            val roadBorder = Color(0xFF334155)

            // Horizontal expressway
            val hHighwayY = h * 0.45f
            drawRect(
                color = roadColor,
                topLeft = Offset(0f, hHighwayY - 24f),
                size = androidx.compose.ui.geometry.Size(w, 48f)
            )
            drawLine(roadBorder, Offset(0f, hHighwayY - 24f), Offset(w, hHighwayY - 24f), 2f)
            drawLine(roadBorder, Offset(0f, hHighwayY + 24f), Offset(w, hHighwayY + 24f), 2f)
            // Dashed center divider
            drawLine(
                color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                start = Offset(0f, hHighwayY),
                end = Offset(w, hHighwayY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )

            // Curved highway interchange
            val highwayPath = Path().apply {
                moveTo(w * 0.15f, h)
                cubicTo(
                    w * 0.25f, h * 0.65f,
                    w * 0.4f, h * 0.5f,
                    w * 0.55f, hHighwayY
                )
                cubicTo(
                    w * 0.7f, h * 0.4f,
                    w * 0.85f, h * 0.25f,
                    w * 0.95f, 0f
                )
            }
            drawPath(
                path = highwayPath,
                color = roadColor,
                style = Stroke(width = 40f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = highwayPath,
                color = roadBorder,
                style = Stroke(width = 44f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = highwayPath,
                color = roadColor,
                style = Stroke(width = 38f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Active GPS Route Line (Neon Cyan Glow)
            val routeGlow = Path().apply {
                moveTo(w * 0.5f, h * 0.72f)
                lineTo(w * 0.5f, hHighwayY)
                cubicTo(
                    w * 0.62f, hHighwayY,
                    w * 0.75f, h * 0.38f,
                    w * 0.82f, h * 0.22f
                )
            }

            // Route outer glow
            drawPath(
                path = routeGlow,
                color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Route solid line
            drawPath(
                path = routeGlow,
                color = Color(0xFF00E5FF),
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Destination pin on route
            val destX = w * 0.82f
            val destY = h * 0.22f
            drawCircle(
                color = Color(0xFFFF1744).copy(alpha = 0.4f),
                radius = 24f,
                center = Offset(destX, destY)
            )
            drawCircle(
                color = Color(0xFFFF1744),
                radius = 12f,
                center = Offset(destX, destY)
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(destX, destY)
            )

            // Current Vehicle Location (Center lower)
            val carX = w * 0.5f
            val carY = h * 0.72f

            // Pulse ring
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = Offset(carX, carY)
            )
            // Solid aura
            drawCircle(
                color = Color(0xFF00B0FF).copy(alpha = 0.3f),
                radius = 18f,
                center = Offset(carX, carY)
            )
            // Center puck
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 9f,
                center = Offset(carX, carY)
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(carX, carY)
            )

            // Heading pointer arrow
            val rad = Math.toRadians((navState.bearingDegrees - 90.0)).toFloat()
            val arrowLength = 22f
            val headX = carX + cos(rad) * arrowLength
            val headY = carY + sin(rad) * arrowLength
            drawLine(
                color = Color.White,
                start = Offset(carX, carY),
                end = Offset(headX, headY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // 2. Top Navigation Guidance Card (Turn-by-turn banner)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.94f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Turn Icon Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val turnIcon = when (navState.currentStep.action) {
                        NavigationAction.TURN_RIGHT -> Icons.Default.TurnRight
                        NavigationAction.TURN_LEFT -> Icons.Default.TurnLeft
                        NavigationAction.U_TURN -> Icons.AutoMirrored.Filled.ArrowBack
                        else -> Icons.Default.Straight
                    }
                    Icon(
                        imageVector = turnIcon,
                        contentDescription = navState.currentStep.action.label,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${navState.currentStep.distanceMeters} m",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = navState.currentStep.action.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF00E5FF)
                            )
                        )
                    }
                    Text(
                        text = navState.currentStep.streetName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8)
                        ),
                        maxLines = 1
                    )
                }

                // Speed Limit Badge (Standard automotive round sign with red rim)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${navState.speedLimitKmh}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }

        // 3. Bottom Controls & Destination Quick Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF0B111A).copy(alpha = 0.95f))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Quick preset destination chips (SPBU, Bengkel, Rest Area)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigationManager.PRESET_DESTINATIONS.forEach { dest ->
                    val isSelected = navState.activeDestination?.id == dest.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDestination(dest) },
                        label = {
                            Text(
                                text = "${dest.name} (${dest.distanceKm} km)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            val icon = when (dest.category) {
                                "SPBU / Pom Bensin" -> Icons.Default.LocalGasStation
                                "Bengkel & Servis" -> Icons.Default.Build
                                else -> Icons.Default.Place
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF00E5FF),
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action row: Open external Google Maps / Waze + GPS status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Compass Heading",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(navState.bearingDegrees)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Heading: ${navState.bearingDegrees.toInt()}° | Alt: ${navState.altitudeMeters.toInt()}m",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                    )
                }

                ElevatedButton(
                    onClick = onOpenExternalMap,
                    modifier = Modifier.testTag("open_external_map_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF00E5FF)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Buka Maps",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buka di Google Maps",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
