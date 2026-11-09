package com.example.obd

import com.example.model.VehicleTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * High-fidelity automotive physics simulator for OBD-II telemetry.
 * Simulates realistic engine RPM, gear shifting, speed acceleration/deceleration,
 * coolant warm-up, alternator voltage regulation, and allows testing safety limits.
 */
class VehicleSimulator(private val scope: CoroutineScope) {

    private val _telemetry = MutableStateFlow(
        VehicleTelemetry(
            speedKmh = 0f,
            rpm = 850,
            engineTempC = 86f,
            batteryVoltage = 14.1f,
            throttlePct = 8.5f,
            intakeTempC = 31f,
            fuelLevelPct = 68f,
            currentGear = "P"
        )
    )
    val telemetry: StateFlow<VehicleTelemetry> = _telemetry.asStateFlow()

    private var simulationJob: Job? = null
    var isEnabled: Boolean = false
        private set

    // Simulation drive states
    enum class DrivePhase {
        IDLE, ACCELERATING, CRUISING, DECELERATING
    }

    private var currentPhase = DrivePhase.ACCELERATING
    private var phaseTicks = 0
    private var targetSpeed = 70f
    private var currentSpeed = 0f
    private var currentRpm = 850
    private var engineTemp = 86f
    private var batteryVolt = 14.1f
    private var throttle = 12f
    private var currentGear = "1"
    private var tripKm = 0f
    private var maxSpeed = 0f

    // Manual test override triggers for safety evaluation
    private var overrideOverheat: Boolean = false
    private var overrideRedline: Boolean = false
    private var overrideLowBattery: Boolean = false
    private var overrideSpeeding: Boolean = false

    fun start() {
        if (simulationJob?.isActive == true) return
        isEnabled = true
        simulationJob = scope.launch {
            while (isActive) {
                tickPhysics()
                delay(80) // 12.5 updates per second for butter-smooth needles
            }
        }
    }

    fun stop() {
        isEnabled = false
        simulationJob?.cancel()
        simulationJob = null
    }

    fun toggleTestOverheat(active: Boolean) {
        overrideOverheat = active
    }

    fun toggleTestRedline(active: Boolean) {
        overrideRedline = active
    }

    fun toggleTestLowBattery(active: Boolean) {
        overrideLowBattery = active
    }

    fun toggleTestSpeeding(active: Boolean) {
        overrideSpeeding = active
    }

    private fun tickPhysics() {
        phaseTicks++

        // Handle overrides
        if (overrideOverheat) {
            engineTemp = min(112f, engineTemp + 0.6f)
        } else {
            // Normal fluctuation around 88 - 92 C
            val targetNormalTemp = 90f + Random.nextFloat() * 1.5f
            engineTemp += (targetNormalTemp - engineTemp) * 0.05f
        }

        if (overrideLowBattery) {
            batteryVolt = max(11.2f, batteryVolt - 0.1f)
        } else {
            // Alternator active voltage with small realistic ripple
            val targetVolt = 14.1f + (Random.nextFloat() - 0.5f) * 0.15f
            batteryVolt += (targetVolt - batteryVolt) * 0.1f
        }

        if (overrideSpeeding) {
            currentSpeed = min(135f, currentSpeed + 1.2f)
            throttle = 85f
            currentGear = "5"
            currentRpm = 4800
        } else if (overrideRedline) {
            currentRpm = min(6800, currentRpm + 180)
            throttle = 95f
        } else {
            // Normal drive cycle simulation
            when (currentPhase) {
                DrivePhase.IDLE -> {
                    throttle = 7.5f + Random.nextFloat() * 1.2f
                    currentSpeed = max(0f, currentSpeed - 0.8f)
                    currentRpm = 840 + (Random.nextFloat() * 30).roundToInt()
                    currentGear = if (currentSpeed < 2f) "P" else "1"
                    if (phaseTicks > 40) {
                        currentPhase = DrivePhase.ACCELERATING
                        phaseTicks = 0
                        targetSpeed = 45f + Random.nextFloat() * 55f
                    }
                }
                DrivePhase.ACCELERATING -> {
                    throttle = 35f + Random.nextFloat() * 15f
                    val accelRate = 0.5f + (throttle / 100f) * 0.8f
                    currentSpeed = min(targetSpeed, currentSpeed + accelRate)
                    calculateGearsAndRpm()
                    if (currentSpeed >= targetSpeed - 1f || phaseTicks > 90) {
                        currentPhase = DrivePhase.CRUISING
                        phaseTicks = 0
                    }
                }
                DrivePhase.CRUISING -> {
                    throttle = 18f + Random.nextFloat() * 5f
                    // Slight speed variations
                    currentSpeed += (Random.nextFloat() - 0.48f) * 0.4f
                    calculateGearsAndRpm()
                    if (phaseTicks > 120) {
                        currentPhase = DrivePhase.DECELERATING
                        phaseTicks = 0
                    }
                }
                DrivePhase.DECELERATING -> {
                    throttle = 0f
                    currentSpeed = max(0f, currentSpeed - 0.7f)
                    calculateGearsAndRpm()
                    if (currentSpeed <= 1f || phaseTicks > 80) {
                        currentPhase = DrivePhase.IDLE
                        phaseTicks = 0
                    }
                }
            }
        }

        if (currentSpeed > maxSpeed) {
            maxSpeed = currentSpeed
        }
        tripKm += (currentSpeed / 3600f) * 0.08f // speed km/h * delta time

        val intakeAir = 30f + (engineTemp * 0.05f) + (Random.nextFloat() * 0.8f)

        _telemetry.value = VehicleTelemetry(
            speedKmh = currentSpeed,
            rpm = currentRpm,
            engineTempC = engineTemp,
            batteryVoltage = batteryVolt,
            throttlePct = throttle,
            intakeTempC = intakeAir,
            fuelLevelPct = 65f - (tripKm * 0.1f),
            calculatedLoadPct = min(100f, throttle * 1.3f),
            currentGear = currentGear,
            maxSpeedKmh = maxSpeed,
            tripDistanceKm = tripKm,
            isEngineRunning = true,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun calculateGearsAndRpm() {
        when {
            currentSpeed < 1f -> {
                currentGear = "N"
                currentRpm = 850
            }
            currentSpeed < 25f -> {
                currentGear = "1"
                currentRpm = (850 + (currentSpeed / 25f) * 2400).roundToInt()
            }
            currentSpeed < 48f -> {
                currentGear = "2"
                currentRpm = (1500 + ((currentSpeed - 25f) / 23f) * 2200).roundToInt()
            }
            currentSpeed < 75f -> {
                currentGear = "3"
                currentRpm = (1800 + ((currentSpeed - 48f) / 27f) * 2100).roundToInt()
            }
            currentSpeed < 105f -> {
                currentGear = "4"
                currentRpm = (2000 + ((currentSpeed - 75f) / 30f) * 2000).roundToInt()
            }
            else -> {
                currentGear = "5"
                currentRpm = (2200 + ((currentSpeed - 105f) / 40f) * 2100).roundToInt()
            }
        }
        // Add subtle vibration
        currentRpm += (Random.nextFloat() * 30 - 15).roundToInt()
    }
}
