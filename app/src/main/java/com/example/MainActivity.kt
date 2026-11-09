package com.example

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DayNightMode
import com.example.model.SecondaryPaneType
import com.example.ui.components.MiniTelemetryHud
import com.example.ui.screens.SplitDashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isInPiPMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on during dashboard driving use
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                // Request permissions for Bluetooth & GPS Navigation
                RequestRequiredPermissions(
                    onPermissionsGranted = {
                        viewModel.navigationManager.startGpsUpdates()
                    }
                )

                val telemetry by viewModel.currentTelemetry.collectAsStateWithLifecycle()
                val navState by viewModel.navState.collectAsStateWithLifecycle()
                val connectionState by viewModel.obdConnectionState.collectAsStateWithLifecycle()
                val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
                val thresholds by viewModel.thresholds.collectAsStateWithLifecycle()
                val splitMode by viewModel.splitMode.collectAsStateWithLifecycle()
                val gaugeStyle by viewModel.gaugeStyle.collectAsStateWithLifecycle()
                val theme by viewModel.theme.collectAsStateWithLifecycle()
                val dayNightMode by viewModel.dayNightMode.collectAsStateWithLifecycle()
                val secondaryPaneType by viewModel.secondaryPaneType.collectAsStateWithLifecycle()
                val speedUnit by viewModel.speedUnit.collectAsStateWithLifecycle()
                val tempUnit by viewModel.tempUnit.collectAsStateWithLifecycle()
                val splitRatio by viewModel.splitRatio.collectAsStateWithLifecycle()
                val packetsReceived by viewModel.packetsReceived.collectAsStateWithLifecycle()
                val lastRawPacket by viewModel.lastRawPacket.collectAsStateWithLifecycle()

                if (isInPiPMode) {
                    // Ultra-compact HUD for Picture-in-Picture floating over other apps (e.g. YouTube / Waze / Maps)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF070B12))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MiniTelemetryHud(
                            telemetry = telemetry,
                            speedUnit = speedUnit,
                            themeColor = Color(theme.primaryHex),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    SplitDashboardScreen(
                        telemetry = telemetry,
                        navState = navState,
                        connectionState = connectionState,
                        pairedDevices = viewModel.getPairedBluetoothDevices(),
                        isSimulatorActive = viewModel.isSimulatorActive,
                        activeAlerts = activeAlerts,
                        thresholds = thresholds,
                        splitMode = splitMode,
                        gaugeStyle = gaugeStyle,
                        theme = theme,
                        dayNightMode = dayNightMode,
                        secondaryPaneType = secondaryPaneType,
                        speedUnit = speedUnit,
                        tempUnit = tempUnit,
                        splitRatio = splitRatio,
                        packetsReceived = packetsReceived,
                        lastRawPacket = lastRawPacket,
                        onConnectDevice = { address, name -> viewModel.connectToObdDevice(address, name) },
                        onDisconnectObd = { viewModel.disconnectObd() },
                        onToggleSimulator = { viewModel.toggleSimulator(it) },
                        onTestOverheat = { viewModel.setTestOverheat(it) },
                        onTestRedline = { viewModel.setTestRedline(it) },
                        onTestLowBattery = { viewModel.setTestLowBattery(it) },
                        onTestSpeeding = { viewModel.setTestSpeeding(it) },
                        onSaveThresholds = { viewModel.updateThresholds(it) },
                        onSelectSplitMode = { viewModel.setSplitMode(it) },
                        onSelectGaugeStyle = { viewModel.setGaugeStyle(it) },
                        onSelectTheme = { viewModel.setTheme(it) },
                        onSelectDayNightMode = { viewModel.setDayNightMode(it) },
                        onSelectSecondaryPane = { viewModel.setSecondaryPaneType(it) },
                        onSelectSpeedUnit = { viewModel.setSpeedUnit(it) },
                        onSelectTempUnit = { viewModel.setTempUnit(it) },
                        onChangeSplitRatio = { viewModel.setSplitRatio(it) },
                        onSelectDestination = { viewModel.selectDestination(it) },
                        onOpenExternalMap = { viewModel.openExternalNavigation() },
                        onEnterPiPMode = { enterPiP() },
                        onDismissAlert = { viewModel.dismissAlert(it) }
                    )
                }
            }
        }
    }

    private fun enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically enter PiP if user presses Home while dashboard is running
        enterPiP()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPiPMode = isInPictureInPictureMode
    }
}

@Composable
private fun RequestRequiredPermissions(
    onPermissionsGranted: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onPermissionsGranted()
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}
