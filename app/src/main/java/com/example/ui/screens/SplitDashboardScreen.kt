package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.SafetyAlert
import com.example.model.SafetyThresholds
import com.example.model.SecondaryPaneType
import com.example.model.SpeedUnit
import com.example.model.SplitMode
import com.example.model.TempUnit
import com.example.model.VehicleTelemetry
import com.example.navigation.MapCanvasView
import com.example.navigation.NavigationState
import com.example.navigation.RouteDestination
import com.example.obd.BluetoothDeviceInfo
import com.example.obd.ObdConnectionState
import com.example.ui.components.AppLauncherSplitView
import com.example.ui.components.MediaControllerView
import com.example.ui.components.MiniTelemetryHud
import com.example.ui.components.SafetyAlertBanner
import com.example.ui.dialogs.BluetoothObdDialog
import com.example.ui.dialogs.LayoutCustomizeSheet
import com.example.ui.dialogs.SafetySettingsDialog
import com.example.ui.theme.CockpitColorScheme
import com.example.ui.theme.HeadUnitThemeColors

@Composable
fun SplitDashboardScreen(
    telemetry: VehicleTelemetry,
    navState: NavigationState,
    connectionState: ObdConnectionState,
    pairedDevices: List<BluetoothDeviceInfo>,
    isSimulatorActive: Boolean,
    activeAlerts: List<SafetyAlert>,
    thresholds: SafetyThresholds,
    splitMode: SplitMode,
    gaugeStyle: GaugeStyle,
    theme: ClusterTheme,
    dayNightMode: DayNightMode,
    secondaryPaneType: SecondaryPaneType,
    speedUnit: SpeedUnit,
    tempUnit: TempUnit,
    splitRatio: Float,
    packetsReceived: Int = 0,
    lastRawPacket: String = "",
    onConnectDevice: (String, String) -> Unit,
    onDisconnectObd: () -> Unit,
    onToggleSimulator: (Boolean) -> Unit,
    onTestOverheat: (Boolean) -> Unit,
    onTestRedline: (Boolean) -> Unit,
    onTestLowBattery: (Boolean) -> Unit,
    onTestSpeeding: (Boolean) -> Unit,
    onSaveThresholds: (SafetyThresholds) -> Unit,
    onSelectSplitMode: (SplitMode) -> Unit,
    onSelectGaugeStyle: (GaugeStyle) -> Unit,
    onSelectTheme: (ClusterTheme) -> Unit,
    onSelectDayNightMode: (DayNightMode) -> Unit,
    onSelectSecondaryPane: (SecondaryPaneType) -> Unit,
    onSelectSpeedUnit: (SpeedUnit) -> Unit,
    onSelectTempUnit: (TempUnit) -> Unit,
    onChangeSplitRatio: (Float) -> Unit,
    onSelectDestination: (RouteDestination) -> Unit,
    onOpenExternalMap: () -> Unit,
    onEnterPiPMode: () -> Unit,
    onDismissAlert: (SafetyAlert) -> Unit
) {
    var showObdDialog by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var showLayoutSheet by remember { mutableStateOf(false) }

    val isSystemDark = isSystemInDarkTheme()
    val isNightActive = when (dayNightMode) {
        DayNightMode.ALWAYS_DARK -> true
        DayNightMode.ALWAYS_LIGHT -> false
        DayNightMode.AUTO_SYSTEM -> isSystemDark || HeadUnitThemeColors.isNightTime()
    }
    val colorScheme = if (isNightActive) HeadUnitThemeColors.DarkScheme else HeadUnitThemeColors.LightScheme
    val primaryThemeColor = Color(theme.primaryHex)

    Scaffold(
        containerColor = colorScheme.screenBackground,
        topBar = {
            CockpitHeader(
                connectionState = connectionState,
                dayNightMode = dayNightMode,
                isNightActive = isNightActive,
                activeAlertsCount = activeAlerts.size,
                primaryColor = primaryThemeColor,
                colorScheme = colorScheme,
                onOpenObd = { showObdDialog = true },
                onOpenSafety = { showSafetyDialog = true },
                onOpenLayout = { showLayoutSheet = true },
                onEnterPiP = onEnterPiPMode,
                onToggleDayNight = {
                    val next = when (dayNightMode) {
                        DayNightMode.AUTO_SYSTEM -> DayNightMode.ALWAYS_DARK
                        DayNightMode.ALWAYS_DARK -> DayNightMode.ALWAYS_LIGHT
                        DayNightMode.ALWAYS_LIGHT -> DayNightMode.AUTO_SYSTEM
                    }
                    onSelectDayNightMode(next)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Safety Warning Banner
            SafetyAlertBanner(
                alerts = activeAlerts,
                onDismissAlert = onDismissAlert
            )

            // Dynamic Split Screen Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (splitMode) {
                    SplitMode.SPLIT_VERTICAL -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top: Dashboard Cluster
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(splitRatio)
                            ) {
                                DashboardClusterView(
                                    telemetry = telemetry,
                                    gaugeStyle = gaugeStyle,
                                    speedUnit = speedUnit,
                                    tempUnit = tempUnit,
                                    themeColor = primaryThemeColor,
                                    warningTempC = thresholds.engineTempWarningC,
                                    criticalTempC = thresholds.engineTempCriticalC,
                                    redlineRpm = thresholds.rpmRedline,
                                    minBatteryV = thresholds.batteryMinVoltage,
                                    maxSpeedLimitKmh = thresholds.speedLimitKmh,
                                    colorScheme = colorScheme
                                )
                            }

                            // Split divider bar with Secondary App Selector Chips
                            SecondaryPaneHeaderBar(
                                currentType = secondaryPaneType,
                                onSelectType = onSelectSecondaryPane,
                                primaryColor = primaryThemeColor,
                                colorScheme = colorScheme
                            )

                            // Bottom: Secondary Selected Pane (Map / Media / Apps)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f - splitRatio)
                            ) {
                                SecondaryPaneContent(
                                    paneType = secondaryPaneType,
                                    navState = navState,
                                    themeColor = primaryThemeColor,
                                    onSelectDestination = onSelectDestination,
                                    onOpenExternalMap = onOpenExternalMap,
                                    onEnterPiP = onEnterPiPMode
                                )
                            }
                        }
                    }

                    SplitMode.SPLIT_HORIZONTAL -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left: Dashboard Cluster
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(splitRatio)
                            ) {
                                DashboardClusterView(
                                    telemetry = telemetry,
                                    gaugeStyle = gaugeStyle,
                                    speedUnit = speedUnit,
                                    tempUnit = tempUnit,
                                    themeColor = primaryThemeColor,
                                    warningTempC = thresholds.engineTempWarningC,
                                    criticalTempC = thresholds.engineTempCriticalC,
                                    redlineRpm = thresholds.rpmRedline,
                                    minBatteryV = thresholds.batteryMinVoltage,
                                    maxSpeedLimitKmh = thresholds.speedLimitKmh,
                                    colorScheme = colorScheme
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(colorScheme.dividerColor)
                            )

                            // Right: Secondary Pane with Tabs
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f - splitRatio)
                            ) {
                                SecondaryPaneHeaderBar(
                                    currentType = secondaryPaneType,
                                    onSelectType = onSelectSecondaryPane,
                                    primaryColor = primaryThemeColor,
                                    colorScheme = colorScheme
                                )
                                Box(modifier = Modifier.fillMaxSize()) {
                                    SecondaryPaneContent(
                                        paneType = secondaryPaneType,
                                        navState = navState,
                                        themeColor = primaryThemeColor,
                                        onSelectDestination = onSelectDestination,
                                        onOpenExternalMap = onOpenExternalMap,
                                        onEnterPiP = onEnterPiPMode
                                    )
                                }
                            }
                        }
                    }

                    SplitMode.FULL_DASHBOARD -> {
                        DashboardClusterView(
                            telemetry = telemetry,
                            gaugeStyle = gaugeStyle,
                            speedUnit = speedUnit,
                            tempUnit = tempUnit,
                            themeColor = primaryThemeColor,
                            warningTempC = thresholds.engineTempWarningC,
                            criticalTempC = thresholds.engineTempCriticalC,
                            redlineRpm = thresholds.rpmRedline,
                            minBatteryV = thresholds.batteryMinVoltage,
                            maxSpeedLimitKmh = thresholds.speedLimitKmh,
                            colorScheme = colorScheme
                        )
                    }

                    SplitMode.FULL_MAP -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MapCanvasView(
                                navState = navState,
                                onSelectDestination = onSelectDestination,
                                onOpenExternalMap = onOpenExternalMap
                            )

                            // Floating mini HUD on top of map
                            MiniTelemetryHud(
                                telemetry = telemetry,
                                speedUnit = speedUnit,
                                themeColor = primaryThemeColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 68.dp)
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showObdDialog) {
        BluetoothObdDialog(
            connectionState = connectionState,
            pairedDevices = pairedDevices,
            isSimulatorActive = isSimulatorActive,
            packetsReceived = packetsReceived,
            lastRawPacket = lastRawPacket,
            onConnectDevice = onConnectDevice,
            onDisconnect = onDisconnectObd,
            onToggleSimulator = onToggleSimulator,
            onTestOverheat = onTestOverheat,
            onTestRedline = onTestRedline,
            onTestLowBattery = onTestLowBattery,
            onTestSpeeding = onTestSpeeding,
            onDismiss = { showObdDialog = false }
        )
    }

    if (showSafetyDialog) {
        SafetySettingsDialog(
            currentThresholds = thresholds,
            onSaveThresholds = onSaveThresholds,
            onDismiss = { showSafetyDialog = false }
        )
    }

    if (showLayoutSheet) {
        LayoutCustomizeSheet(
            currentSplitMode = splitMode,
            currentGaugeStyle = gaugeStyle,
            currentTheme = theme,
            currentDayNightMode = dayNightMode,
            currentSecondaryPane = secondaryPaneType,
            currentSpeedUnit = speedUnit,
            currentTempUnit = tempUnit,
            splitRatio = splitRatio,
            onSelectSplitMode = onSelectSplitMode,
            onSelectGaugeStyle = onSelectGaugeStyle,
            onSelectTheme = onSelectTheme,
            onSelectDayNightMode = onSelectDayNightMode,
            onSelectSecondaryPane = onSelectSecondaryPane,
            onSelectSpeedUnit = onSelectSpeedUnit,
            onSelectTempUnit = onSelectTempUnit,
            onChangeSplitRatio = onChangeSplitRatio,
            onEnterPiPMode = {
                showLayoutSheet = false
                onEnterPiPMode()
            },
            onDismiss = { showLayoutSheet = false }
        )
    }
}

@Composable
private fun SecondaryPaneHeaderBar(
    currentType: SecondaryPaneType,
    onSelectType: (SecondaryPaneType) -> Unit,
    primaryColor: Color,
    colorScheme: CockpitColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SecondaryPaneType.entries.forEach { type ->
            val isSelected = currentType == type
            FilterChip(
                selected = isSelected,
                onClick = { onSelectType(type) },
                label = {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                },
                leadingIcon = {
                    val icon = when (type) {
                        SecondaryPaneType.GPS_MAP -> Icons.Default.Map
                        SecondaryPaneType.MEDIA_PLAYER -> Icons.Default.Audiotrack
                        SecondaryPaneType.APP_LAUNCHER -> Icons.Default.Apps
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isSelected) primaryColor else colorScheme.textMuted
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                    selectedLabelColor = primaryColor,
                    containerColor = colorScheme.cardBackground,
                    labelColor = colorScheme.textMuted
                )
            )
        }
    }
}

@Composable
private fun SecondaryPaneContent(
    paneType: SecondaryPaneType,
    navState: NavigationState,
    themeColor: Color,
    onSelectDestination: (RouteDestination) -> Unit,
    onOpenExternalMap: () -> Unit,
    onEnterPiP: () -> Unit
) {
    when (paneType) {
        SecondaryPaneType.GPS_MAP -> {
            MapCanvasView(
                navState = navState,
                onSelectDestination = onSelectDestination,
                onOpenExternalMap = onOpenExternalMap
            )
        }
        SecondaryPaneType.MEDIA_PLAYER -> {
            MediaControllerView(
                themeColor = themeColor,
                onEnterPiP = onEnterPiP
            )
        }
        SecondaryPaneType.APP_LAUNCHER -> {
            AppLauncherSplitView(
                themeColor = themeColor,
                onEnterPiP = onEnterPiP
            )
        }
    }
}

@Composable
private fun CockpitHeader(
    connectionState: ObdConnectionState,
    dayNightMode: DayNightMode,
    isNightActive: Boolean,
    activeAlertsCount: Int,
    primaryColor: Color,
    colorScheme: CockpitColorScheme,
    onOpenObd: () -> Unit,
    onOpenSafety: () -> Unit,
    onOpenLayout: () -> Unit,
    onEnterPiP: () -> Unit,
    onToggleDayNight: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App branding & OBD Status Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onOpenObd() }
        ) {
            Text(
                text = "OBD2",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = primaryColor
                )
            )
            Text(
                text = "SPEEDO",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = colorScheme.textPrimary
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // OBD Status Pill
            val (pillColor, pillText) = when (connectionState) {
                is ObdConnectionState.Connected -> Color(0xFF00E676) to "OBD LIVE"
                is ObdConnectionState.Connecting -> Color(0xFFFFB300) to "CONNECTING..."
                is ObdConnectionState.Simulating -> Color(0xFF00E5FF) to "SIMULATOR"
                is ObdConnectionState.Error -> Color(0xFFFF1744) to "OBD ERROR"
                else -> Color(0xFFEF4444) to "OBD OFFLINE"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(pillColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(pillColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = pillText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = pillColor,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }

        // Action Icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Day / Night Auto Toggle button
            IconButton(
                onClick = onToggleDayNight,
                modifier = Modifier.testTag("toggle_day_night_header_button")
            ) {
                val icon = when (dayNightMode) {
                    DayNightMode.AUTO_SYSTEM -> Icons.Default.BrightnessAuto
                    DayNightMode.ALWAYS_DARK -> Icons.Default.DarkMode
                    DayNightMode.ALWAYS_LIGHT -> Icons.Default.LightMode
                }
                Icon(
                    imageVector = icon,
                    contentDescription = dayNightMode.label,
                    tint = if (dayNightMode == DayNightMode.AUTO_SYSTEM) primaryColor else colorScheme.textPrimary
                )
            }

            // Bluetooth OBD button
            IconButton(
                onClick = onOpenObd,
                modifier = Modifier.testTag("open_obd_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "OBD Bluetooth",
                    tint = if (connectionState is ObdConnectionState.Connected) Color(0xFF00E676) else colorScheme.textPrimary
                )
            }

            // Safety Warning Settings with active badge
            IconButton(
                onClick = onOpenSafety,
                modifier = Modifier.testTag("open_safety_button")
            ) {
                if (activeAlertsCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = Color(0xFFFF1744)) {
                                Text("$activeAlertsCount", color = Color.White)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Peringatan Otomatis",
                            tint = Color(0xFFFFB300)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Peringatan Otomatis",
                        tint = colorScheme.textPrimary
                    )
                }
            }

            // Layout Customize button
            IconButton(
                onClick = onOpenLayout,
                modifier = Modifier.testTag("open_layout_customizer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DashboardCustomize,
                    contentDescription = "Tata Letak",
                    tint = primaryColor
                )
            }

            // Picture-in-Picture button
            IconButton(
                onClick = onEnterPiP,
                modifier = Modifier.testTag("enter_pip_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureInPicture,
                    contentDescription = "Picture-in-Picture",
                    tint = colorScheme.textPrimary
                )
            }
        }
    }
}
