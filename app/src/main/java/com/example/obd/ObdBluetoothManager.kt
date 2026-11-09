package com.example.obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.example.model.VehicleTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Manages physical Bluetooth SPP connection to ELM327 OBD-II hardware adapters.
 */
class ObdBluetoothManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "ObdBluetoothManager"
        // Standard Bluetooth Serial Port Profile (SPP) UUID used by ELM327 devices
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectionState = MutableStateFlow<ObdConnectionState>(ObdConnectionState.Disconnected)
    val connectionState: StateFlow<ObdConnectionState> = _connectionState.asStateFlow()

    private val _liveTelemetry = MutableStateFlow(VehicleTelemetry())
    val liveTelemetry: StateFlow<VehicleTelemetry> = _liveTelemetry.asStateFlow()

    val packetsReceived = MutableStateFlow(0)
    val lastRawPacket = MutableStateFlow("")

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var pollJob: Job? = null

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        val adapter = bluetoothAdapter ?: return emptyList()
        return try {
            adapter.bondedDevices?.map { device ->
                BluetoothDeviceInfo(
                    name = device.name ?: "Unknown OBD Device",
                    address = device.address,
                    isPaired = true
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching paired devices", e)
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String, deviceName: String) {
        disconnect()
        _connectionState.value = ObdConnectionState.Connecting(deviceName)

        scope.launch(Dispatchers.IO) {
            try {
                val adapter = bluetoothAdapter ?: throw IllegalStateException("Bluetooth tidak tersedia")
                val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)

                val btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                adapter.cancelDiscovery()
                btSocket.connect()

                socket = btSocket
                inputStream = btSocket.inputStream
                outputStream = btSocket.outputStream

                // Initialize ELM327
                initializeElm327()

                _connectionState.value = ObdConnectionState.Connected(deviceName)

                // Start polling loop
                startObdPollingLoop()
            } catch (e: Exception) {
                Log.e(TAG, "Koneksi OBD-II gagal: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = ObdConnectionState.Error("Koneksi gagal: ${e.localizedMessage ?: "Tidak dapat terhubung"}")
                }
                disconnect()
            }
        }
    }

    private suspend fun initializeElm327() = withContext(Dispatchers.IO) {
        // Broad compatibility init sequence for stubborn Suzuki ECUs
        val initCommands = listOf(
            "ATZ",      // Reset device
            "ATE0",     // Echo off
            "ATL0",     // Linefeed off
            "ATS0",     // Spaces off
            "ATH0",     // Headers off
            "ATCAF0",   // CAN Formatting Off (try to bypass strict filtering)
            "ATAL",     // Allow long messages
            "ATSP0"     // Auto-protocol detection
        )

        for (cmd in initCommands) {
            sendCommand(cmd)
            delay(200) // Increased delay for stability
        }
    }

    private suspend fun startObdPollingLoop() = withContext(Dispatchers.IO) {
        pollJob?.cancel()
        pollJob = scope.launch(Dispatchers.IO) {
            var currentSpeed = 0f
            var currentRpm = 0
            var engineTemp = 85f
            var batteryVolt = 13.8f
            var throttle = 0f
            var intakeTemp = 32f
            var fuelLevel = 65f
            var maxSpeed = 0f

            while (isActive) {
                try {
                    // 1. Read Speed (High priority)
                    val speedResp = sendCommand(ObdProtocol.PID_VEHICLE_SPEED)
                    ObdProtocol.parseSpeed(speedResp)?.let {
                        currentSpeed = it
                        if (it > maxSpeed) maxSpeed = it
                    }

                    // 2. Read RPM (High priority)
                    val rpmResp = sendCommand(ObdProtocol.PID_ENGINE_RPM)
                    ObdProtocol.parseRpm(rpmResp)?.let { currentRpm = it }

                    // 3. Read Throttle
                    val throttleResp = sendCommand(ObdProtocol.PID_THROTTLE_POS)
                    ObdProtocol.parseThrottle(throttleResp)?.let { throttle = it }

                    // 4. Read Engine Coolant Temp
                    val tempResp = sendCommand(ObdProtocol.PID_COOLANT_TEMP)
                    ObdProtocol.parseCoolantTemp(tempResp)?.let { engineTemp = it }

                    // 5. Read Battery Voltage
                    val voltResp = sendCommand(ObdProtocol.CMD_READ_VOLTAGE)
                    ObdProtocol.parseBatteryVoltage(voltResp)?.let { batteryVolt = it }

                    // 6. Read Intake Temp
                    val intakeResp = sendCommand(ObdProtocol.PID_INTAKE_TEMP)
                    ObdProtocol.parseIntakeTemp(intakeResp)?.let { intakeTemp = it }

                    packetsReceived.value += 1
                    lastRawPacket.value = if (rpmResp.isNotBlank()) rpmResp.trim() else speedResp.trim()

                    _liveTelemetry.value = VehicleTelemetry(
                        speedKmh = currentSpeed,
                        rpm = currentRpm,
                        engineTempC = engineTemp,
                        batteryVoltage = batteryVolt,
                        throttlePct = throttle,
                        intakeTempC = intakeTemp,
                        fuelLevelPct = fuelLevel,
                        maxSpeedKmh = maxSpeed,
                        isEngineRunning = currentRpm > 300,
                        timestamp = System.currentTimeMillis()
                    )

                    delay(80) // Fast 10-12 Hz refresh
                } catch (e: Exception) {
                    Log.e(TAG, "Error in OBD poll loop", e)
                    if (!isActive) break
                    delay(300)
                }
            }
        }
    }

    // Diagnostic mode
    var isDiagnosticModeEnabled: Boolean = true

    private fun sendCommand(cmd: String): String {
        val out = outputStream ?: return ""
        val inStream = inputStream ?: return ""

        return try {
            out.write((cmd + "\r").toByteArray(Charsets.US_ASCII))
            out.flush()

            val buffer = ByteArray(256)
            val responseBuilder = StringBuilder()
            var bytesRead: Int
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < 800) {
                if (inStream.available() > 0) {
                    bytesRead = inStream.read(buffer)
                    val chunk = String(buffer, 0, bytesRead, Charsets.US_ASCII)
                    responseBuilder.append(chunk)
                    if (chunk.contains(">")) {
                        break
                    }
                } else {
                    Thread.sleep(10)
                }
            }
            val response = responseBuilder.toString()
            
            if (isDiagnosticModeEnabled) {
                Log.d(TAG, "DIAG: CMD=$cmd, RAW=$response")
            }
            
            response
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send command: $cmd", e)
            ""
        }
    }

    fun disconnect() {
        pollJob?.cancel()
        pollJob = null
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Bluetooth socket", e)
        }
        inputStream = null
        outputStream = null
        socket = null
        _connectionState.value = ObdConnectionState.Disconnected
    }
}
