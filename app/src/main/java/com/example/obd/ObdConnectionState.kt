package com.example.obd

sealed interface ObdConnectionState {
    data object Disconnected : ObdConnectionState
    data object Scanning : ObdConnectionState
    data class Connecting(val deviceName: String) : ObdConnectionState
    data class Connected(val deviceName: String, val protocol: String = "ISO 15765-4 (CAN)") : ObdConnectionState
    data class Simulating(val profileName: String = "Simulasi Kendaraan Realistis") : ObdConnectionState
    data class Error(val message: String) : ObdConnectionState
}

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isPaired: Boolean = true
)
