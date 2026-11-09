package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClusterTheme
import com.example.model.DayNightMode
import com.example.model.GaugeStyle
import com.example.model.SecondaryPaneType
import com.example.model.SpeedUnit
import com.example.model.SplitMode
import com.example.model.TempUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutCustomizeSheet(
    currentSplitMode: SplitMode,
    currentGaugeStyle: GaugeStyle,
    currentTheme: ClusterTheme,
    currentDayNightMode: DayNightMode,
    currentSecondaryPane: SecondaryPaneType,
    currentSpeedUnit: SpeedUnit,
    currentTempUnit: TempUnit,
    splitRatio: Float,
    onSelectSplitMode: (SplitMode) -> Unit,
    onSelectGaugeStyle: (GaugeStyle) -> Unit,
    onSelectTheme: (ClusterTheme) -> Unit,
    onSelectDayNightMode: (DayNightMode) -> Unit,
    onSelectSecondaryPane: (SecondaryPaneType) -> Unit,
    onSelectSpeedUnit: (SpeedUnit) -> Unit,
    onSelectTempUnit: (TempUnit) -> Unit,
    onChangeSplitRatio: (Float) -> Unit,
    onEnterPiPMode: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DashboardCustomize,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Kustomisasi Tata Letak & Dasbor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Sesuaikan tampilan layar, posisi split, dan gaya meteran",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Split Screen Mode Selector
            Text(
                text = "1. MODE TAMPILAN LAYAR (SPLIT SCREEN)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SplitMode.entries.forEach { mode ->
                    val isSelected = currentSplitMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2E))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectSplitMode(mode) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (mode) {
                                SplitMode.SPLIT_VERTICAL -> Icons.Default.ViewStream
                                SplitMode.SPLIT_HORIZONTAL -> Icons.Default.ViewAgenda
                                SplitMode.FULL_DASHBOARD -> Icons.Default.Speed
                                SplitMode.FULL_MAP -> Icons.Default.ViewCompact
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                )
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Terpilih",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Split Ratio selection (only relevant for split modes)
            if (currentSplitMode in listOf(SplitMode.SPLIT_VERTICAL, SplitMode.SPLIT_HORIZONTAL)) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Proporsi Pembagian Layar:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val ratios = listOf(0.4f to "40 / 60", 0.5f to "50 / 50 (Seimbang)", 0.6f to "60 / 40")
                    ratios.forEach { (ratio, label) ->
                        val isSelected = (splitRatio - ratio) in -0.05f..0.05f
                        FilterChip(
                            selected = isSelected,
                            onClick = { onChangeSplitRatio(ratio) },
                            label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFF00E5FF),
                                containerColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Konten Layar Kedua (Aplikasi Pasangan Split):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SecondaryPaneType.entries.forEach { pane ->
                        val isSelected = currentSecondaryPane == pane
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2E))
                                .clickable { onSelectSecondaryPane(pane) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = pane.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCBD5E1)
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(14.dp))

            // 2. Gauge Presentation Style
            Text(
                text = "2. GAYA DESAIN METERAN KOKPIT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GaugeStyle.entries.forEach { style ->
                    val isSelected = currentGaugeStyle == style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2E))
                            .clickable { onSelectGaugeStyle(style) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCBD5E1)
                            )
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(14.dp))

            // 3. Cockpit Color Themes
            Text(
                text = "3. TEMA WARNA GLOW KOKPIT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClusterTheme.entries.forEach { theme ->
                    val isSelected = currentTheme == theme
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2E))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) Color(theme.primaryHex) else Color(0xFF334155),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(theme.primaryHex))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = theme.label.split(" ")[0],
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(14.dp))

            // 4. Day / Night Mode for Automotive Head Units
            Text(
                text = "4. MODE TEMA HEAD UNIT (SIANG / MALAM)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DayNightMode.entries.forEach { mode ->
                    val isSelected = currentDayNightMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2E))
                            .clickable { onSelectDayNightMode(mode) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCBD5E1)
                            )
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(14.dp))

            // 5. Units (Speed & Temp)
            Text(
                text = "5. SATUAN PENGUKURAN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Satuan Kecepatan",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpeedUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = currentSpeedUnit == unit,
                            onClick = { onSelectSpeedUnit(unit) },
                            label = { Text(unit.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFF00E5FF),
                                containerColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Satuan Suhu",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TempUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = currentTempUnit == unit,
                            onClick = { onSelectTempUnit(unit) },
                            label = { Text(unit.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFF00E5FF),
                                containerColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(14.dp))

            // 6. Picture-in-Picture / Floating Multi-Window Mode
            Text(
                text = "6. JALANKAN BERSAMAAN APLIKASI LAIN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Buka Picture-in-Picture (PiP) atau Multi-Window agar spedometer tetap tampil melayang saat Anda membuka aplikasi lain (seperti Waze atau Google Maps).",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ElevatedButton(
                onClick = onEnterPiPMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enter_pip_mode_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF00E5FF)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PictureInPicture,
                    contentDescription = "Masuk Mode PiP",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Aktifkan Mode Floating PiP (Melayang)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
