package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediaControllerView(
    themeColor: Color,
    onEnterPiP: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var trackProgress by remember { mutableFloatStateOf(0.42f) }
    var currentTrackIndex by remember { mutableStateOf(0) }

    val playlist = listOf(
        Pair("Night Drive Synthwave - Track #01", "Automotive Cockpit Beats"),
        Pair("Tokyo Highway Drift Mix", "EDM & Bass Radio"),
        Pair("Cruising Sunset Acoustic", "Head Unit Streaming Audio"),
        Pair("High Octane Engine Symphony", "Sport Sound System")
    )

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .padding(14.dp)
            .testTag("media_controller_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PEMUTAR MUSIK & MEDIA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = themeColor,
                            letterSpacing = 1.sp
                        )
                    )
                }

                ElevatedButton(
                    onClick = onEnterPiP,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF00E5FF)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("pip_from_media_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPicture,
                        contentDescription = "Floating PiP",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Mini HUD PiP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vinyl / Track Visualizer Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131D2D))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spinning Vinyl Disc
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF070B12))
                        .border(2.dp, themeColor.copy(alpha = 0.5f), CircleShape)
                        .rotate(if (isPlaying) discRotation else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(themeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val currentSong = playlist[currentTrackIndex % playlist.size]
                    Text(
                        text = currentSong.first,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = currentSong.second,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8)
                        ),
                        maxLines = 1
                    )
                }
            }

            // Progress Slider
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Slider(
                    value = trackProgress,
                    onValueChange = { trackProgress = it },
                    colors = SliderDefaults.colors(
                        thumbColor = themeColor,
                        activeTrackColor = themeColor,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.height(28.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "01:45", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 10.sp))
                    Text(text = "03:50", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 10.sp))
                }
            }

            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentTrackIndex > 0) currentTrackIndex--
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Sebelumnya",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(themeColor),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Jeda" else "Putar",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        currentTrackIndex++
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Selanjutnya",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))

            // External App Launcher Shortcuts (YouTube, Spotify, Pemutar Video)
            Text(
                text = "Buka Bersamaan Aplikasi Head Unit Lain:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLauncherChip(
                    label = "YouTube",
                    packageName = "com.google.android.youtube",
                    color = Color(0xFFFF0000),
                    context = context,
                    modifier = Modifier.weight(1f)
                )
                AppLauncherChip(
                    label = "Spotify",
                    packageName = "com.spotify.music",
                    color = Color(0xFF1DB954),
                    context = context,
                    modifier = Modifier.weight(1f)
                )
                AppLauncherChip(
                    label = "Video Player",
                    packageName = "org.videolan.vlc",
                    color = Color(0xFFFF9800),
                    context = context,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AppLauncherChip(
    label: String,
    packageName: String,
    color: Color,
    context: Context,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = {
            launchExternalApp(context, packageName, label)
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

fun launchExternalApp(context: Context, packageName: String, appName: String) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Toast.makeText(context, "Membuka $appName...", Toast.LENGTH_SHORT).show()
        } else {
            // If specific package not found, open search or generic intent
            val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://search?q=$appName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (genericIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(genericIntent)
            } else {
                Toast.makeText(
                    context,
                    "Aplikasi $appName belum terpasang di Head Unit.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka $appName", Toast.LENGTH_SHORT).show()
    }
}
