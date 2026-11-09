package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SafetyAlert

@Composable
fun SafetyAlertBanner(
    alerts: List<SafetyAlert>,
    onDismissAlert: (SafetyAlert) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = alerts.isNotEmpty(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        val topAlert = alerts.firstOrNull() ?: return@AnimatedVisibility

        val infiniteTransition = rememberInfiniteTransition(label = "banner_flash")
        val flashAlpha by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(300),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alertAlpha"
        )

        val bannerBg = if (topAlert.isCritical) {
            Color(0xFFB71C1C).copy(alpha = flashAlpha)
        } else {
            Color(0xFFE65100).copy(alpha = flashAlpha)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bannerBg)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
                .testTag("safety_alert_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan Bahaya",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (topAlert.isCritical) "BAHAYA KRITIS!" else "PERINGATAN AMAN!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.Yellow,
                                    fontSize = 11.sp
                                )
                            )
                            if (alerts.size > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(+${alerts.size - 1} peringatan lain)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = topAlert.message,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { onDismissAlert(topAlert) },
                    modifier = Modifier.testTag("dismiss_alert_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Peringatan",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
