package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HeadUnitAppItem(
    val name: String,
    val description: String,
    val packageName: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun AppLauncherSplitView(
    themeColor: Color,
    onEnterPiP: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val appList = listOf(
        HeadUnitAppItem(
            name = "YouTube",
            description = "Video streaming & musik",
            packageName = "com.google.android.youtube",
            icon = Icons.Default.SmartDisplay,
            iconColor = Color(0xFFFF0000)
        ),
        HeadUnitAppItem(
            name = "Spotify",
            description = "Streaming musik & podcast",
            packageName = "com.spotify.music",
            icon = Icons.Default.MusicNote,
            iconColor = Color(0xFF1DB954)
        ),
        HeadUnitAppItem(
            name = "Pemutar Video VLC / MX",
            description = "Video USB & file film",
            packageName = "org.videolan.vlc",
            icon = Icons.Default.VideoLibrary,
            iconColor = Color(0xFFFF9800)
        ),
        HeadUnitAppItem(
            name = "Google Maps",
            description = "Navigasi peta online",
            packageName = "com.google.android.apps.maps",
            icon = Icons.Default.Map,
            iconColor = Color(0xFF4285F4)
        ),
        HeadUnitAppItem(
            name = "Waze GPS",
            description = "Peta & info lalu lintas",
            packageName = "com.waze",
            icon = Icons.Default.Map,
            iconColor = Color(0xFF00E5FF)
        ),
        HeadUnitAppItem(
            name = "Radio / Audio Head Unit",
            description = "Pemutar audio lokal",
            packageName = "com.android.music",
            icon = Icons.Default.Radio,
            iconColor = Color(0xFFE91E63)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .padding(14.dp)
            .testTag("app_launcher_split_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PELUNCUR APLIKASI HEAD UNIT",
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
                    modifier = Modifier.testTag("pip_from_apps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPicture,
                        contentDescription = "Floating PiP",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Floating PiP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Split Screen Info Card for Car Head Units (7"-12")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131D2D))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Cara Split Layar di Head Unit Android (7\" - 12\"):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Tekan tombol Recents / Kotak Navigasi Head Unit -> pilih ikon Split Screen pada OBD2 Speedo -> pilih YouTube atau Pemutar Musik untuk separuh layar kedua.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Grid List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                appList.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                launchExternalApp(context, app.packageName, app.name)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(app.iconColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = app.icon,
                                    contentDescription = app.name,
                                    tint = app.iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = app.description,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Buka",
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
}
