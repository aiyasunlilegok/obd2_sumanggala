package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.navigation.NavigationManager
import com.example.navigation.NavigationState
import com.example.navigation.RouteDestination
import com.example.obd.BluetoothDeviceInfo
import com.example.obd.ObdBluetoothManager
import com.example.obd.ObdConnectionState
import com.example.obd.VehicleSimulator
import com.example.sound.AlertManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val simulator = VehicleSimulator(viewModelScope)
    val bluetoothManager = ObdBluetoothManager(application, viewModelScope)
    private val alertManager = AlertManager(application, viewModelScope)
    val navigationManager = NavigationManager(application, viewModelScope)

    // User Layout Preferences
    private val _splitMode = MutableStateFlow(SplitMode.SPLIT_VERTICAL)
    val splitMode: StateFlow<SplitMode> = _splitMode.asStateFlow()

    private val _gaugeStyle = MutableStateFlow(GaugeStyle.SPORTS_ARC)
    val gaugeStyle: StateFlow<GaugeStyle> = _gaugeStyle.asStateFlow()

    private val _theme = MutableStateFlow(ClusterTheme.NEON_CYAN)
    val theme: StateFlow<ClusterTheme> = _theme.asStateFlow()

    private val _speedUnit = MutableStateFlow(SpeedUnit.KMH)
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _tempUnit = MutableStateFlow(TempUnit.CELSIUS)
    val tempUnit: StateFlow<TempUnit> = _tempUnit.asStateFlow()

    private val _splitRatio = MutableStateFlow(0.5f)
    val splitRatio: StateFlow<Float> = _splitRatio.asStateFlow()

    private val _dayNightMode = MutableStateFlow(DayNightMode.AUTO_SYSTEM)
    val dayNightMode: StateFlow<DayNightMode> = _dayNightMode.asStateFlow()

    private val _secondaryPaneType = MutableStateFlow(SecondaryPaneType.GPS_MAP)
    val secondaryPaneType: StateFlow<SecondaryPaneType> = _secondaryPaneType.asStateFlow()

    // Safety Thresholds
    private val _thresholds = MutableStateFlow(SafetyThresholds())
    val thresholds: StateFlow<SafetyThresholds> = _thresholds.asStateFlow()

    // Active Safety Alerts
    val activeAlerts: StateFlow<List<SafetyAlert>> = alertManager.activeAlerts

    // Dismissed alerts tracking
    private val _dismissedAlertTypes = MutableStateFlow<Set<String>>(emptySet())

    private val _isSimulatorEnabled = MutableStateFlow(true)
    val isSimulatorEnabledState: StateFlow<Boolean> = _isSimulatorEnabled.asStateFlow()

    val packetsReceived: StateFlow<Int> = bluetoothManager.packetsReceived.asStateFlow()
    val lastRawPacket: StateFlow<String> = bluetoothManager.lastRawPacket.asStateFlow()

    // OBD Connection State
    val obdConnectionState: StateFlow<ObdConnectionState> = combine(
        bluetoothManager.connectionState,
        _isSimulatorEnabled
    ) { btState, simEnabled ->
        if (btState is ObdConnectionState.Connected || btState is ObdConnectionState.Connecting) {
            btState
        } else if (simEnabled) {
            ObdConnectionState.Simulating()
        } else {
            btState
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ObdConnectionState.Simulating())

    // Active Telemetry Flow (Real OBD-II hardware when connected, Simulator when enabled, Zeroed when offline)
    val currentTelemetry: StateFlow<VehicleTelemetry> = combine(
        bluetoothManager.connectionState,
        bluetoothManager.liveTelemetry,
        simulator.telemetry,
        _isSimulatorEnabled
    ) { btState, liveData, simData, simEnabled ->
        if (btState is ObdConnectionState.Connected) {
            liveData
        } else if (simEnabled) {
            simData
        } else {
            // Mode Nyata (Simulasi Mati): Semua meteran berada di posisi 0 hingga tersambung ke mobil
            VehicleTelemetry(
                speedKmh = 0f,
                rpm = 0,
                engineTempC = 0f,
                batteryVoltage = 0f,
                throttlePct = 0f,
                intakeTempC = 0f,
                fuelLevelPct = 0f,
                calculatedLoadPct = 0f,
                currentGear = "P",
                isEngineRunning = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, VehicleTelemetry())

    val navState: StateFlow<NavigationState> = navigationManager.navState

    init {
        // Continuously evaluate safety thresholds
        viewModelScope.launch {
            combine(currentTelemetry, thresholds) { telemetry, thresh ->
                alertManager.evaluate(telemetry, thresh)
            }.collect {}
        }
    }

    fun getPairedBluetoothDevices(): List<BluetoothDeviceInfo> {
        return bluetoothManager.getPairedDevices()
    }

    fun connectToObdDevice(address: String, name: String) {
        toggleSimulator(false)
        bluetoothManager.connectToDevice(address, name)
    }

    fun disconnectObd() {
        bluetoothManager.disconnect()
        // Do NOT restart simulator automatically - let user stay in real offline mode
    }

    fun toggleSimulator(enable: Boolean) {
        _isSimulatorEnabled.value = enable
        if (enable) {
            bluetoothManager.disconnect()
            simulator.start()
        } else {
            simulator.stop()
        }
    }

    val isSimulatorActive: Boolean
        get() = _isSimulatorEnabled.value

    fun setTestOverheat(active: Boolean) {
        simulator.toggleTestOverheat(active)
    }

    fun setTestRedline(active: Boolean) {
        simulator.toggleTestRedline(active)
    }

    fun setTestLowBattery(active: Boolean) {
        simulator.toggleTestLowBattery(active)
    }

    fun setTestSpeeding(active: Boolean) {
        simulator.toggleTestSpeeding(active)
    }

    fun updateThresholds(newThresholds: SafetyThresholds) {
        _thresholds.value = newThresholds
    }

    fun setSplitMode(mode: SplitMode) {
        _splitMode.value = mode
    }

    fun setGaugeStyle(style: GaugeStyle) {
        _gaugeStyle.value = style
    }

    fun setTheme(newTheme: ClusterTheme) {
        _theme.value = newTheme
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        _speedUnit.value = unit
    }

    fun setTempUnit(unit: TempUnit) {
        _tempUnit.value = unit
    }

    fun setSplitRatio(ratio: Float) {
        _splitRatio.value = ratio.coerceIn(0.25f, 0.75f)
    }

    fun setDayNightMode(mode: DayNightMode) {
        _dayNightMode.value = mode
    }

    fun setSecondaryPaneType(type: SecondaryPaneType) {
        _secondaryPaneType.value = type
    }

    fun selectDestination(dest: RouteDestination) {
        navigationManager.selectDestination(dest)
    }

    fun openExternalNavigation() {
        val destName = navigationManager.navState.value.activeDestination?.name ?: "Pom Bensin Terdekat"
        navigationManager.openExternalNavigation(destName)
    }

    fun dismissAlert(alert: SafetyAlert) {
        _dismissedAlertTypes.value = _dismissedAlertTypes.value + alert.type.name
    }

    override fun onCleared() {
        super.onCleared()
        simulator.stop()
        bluetoothManager.disconnect()
        alertManager.release()
        navigationManager.stopGpsUpdates()
    }
}
