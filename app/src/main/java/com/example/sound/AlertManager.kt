package com.example.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.model.AlertType
import com.example.model.SafetyAlert
import com.example.model.SafetyThresholds
import com.example.model.VehicleTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors vehicle telemetry in real-time and sounds automated audio & visual alerts
 * when parameters exceed safe thresholds.
 */
class AlertManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "AlertManager"
        private const val SOUND_COOLDOWN_MS = 3200L
    }

    private val _activeAlerts = MutableStateFlow<List<SafetyAlert>>(emptyList())
    val activeAlerts: StateFlow<List<SafetyAlert>> = _activeAlerts.asStateFlow()

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_ALARM, 85)
    } catch (e: Exception) {
        Log.w(TAG, "ToneGenerator could not be initialized", e)
        null
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var lastSoundTimestamp: Long = 0L

    fun evaluate(telemetry: VehicleTelemetry, thresholds: SafetyThresholds) {
        val alerts = mutableListOf<SafetyAlert>()

        // 1. Engine Coolant Temperature Check
        if (telemetry.engineTempC >= thresholds.engineTempCriticalC) {
            alerts.add(
                SafetyAlert(
                    type = AlertType.ENGINE_OVERHEAT,
                    currentValue = telemetry.engineTempC,
                    thresholdValue = thresholds.engineTempCriticalC,
                    isCritical = true,
                    message = "KRITIS: Suhu mesin mencapai ${String.format("%.1f", telemetry.engineTempC)}°C! Segera tepikan kendaraan."
                )
            )
        } else if (telemetry.engineTempC >= thresholds.engineTempWarningC) {
            alerts.add(
                SafetyAlert(
                    type = AlertType.ENGINE_OVERHEAT,
                    currentValue = telemetry.engineTempC,
                    thresholdValue = thresholds.engineTempWarningC,
                    isCritical = false,
                    message = "WASPADA: Suhu mesin tinggi (${String.format("%.1f", telemetry.engineTempC)}°C). Batas aman: ${thresholds.engineTempWarningC.toInt()}°C."
                )
            )
        }

        // 2. RPM Redline Check
        if (telemetry.rpm >= thresholds.rpmRedline) {
            alerts.add(
                SafetyAlert(
                    type = AlertType.RPM_REDLINE,
                    currentValue = telemetry.rpm.toFloat(),
                    thresholdValue = thresholds.rpmRedline.toFloat(),
                    isCritical = telemetry.rpm >= thresholds.rpmRedline + 500,
                    message = "REDLINE: Putaran mesin ${telemetry.rpm} RPM melebihi batas aman (${thresholds.rpmRedline} RPM)!"
                )
            )
        }

        // 3. Battery Voltage Checks
        if (telemetry.batteryVoltage > 0.5f && telemetry.batteryVoltage < thresholds.batteryMinVoltage) {
            alerts.add(
                SafetyAlert(
                    type = AlertType.BATTERY_LOW,
                    currentValue = telemetry.batteryVoltage,
                    thresholdValue = thresholds.batteryMinVoltage,
                    isCritical = telemetry.batteryVoltage < 11.5f,
                    message = "AKI LEMAH: Tegangan aki turun ke ${String.format("%.1f", telemetry.batteryVoltage)}V (Min: ${thresholds.batteryMinVoltage}V)!"
                )
            )
        } else if (telemetry.batteryVoltage > thresholds.batteryMaxVoltage) {
            alerts.add(
                SafetyAlert(
                    type = AlertType.BATTERY_OVERVOLTAGE,
                    currentValue = telemetry.batteryVoltage,
                    thresholdValue = thresholds.batteryMaxVoltage,
                    isCritical = true,
                    message = "OVERVOLT: Tegangan pengisian ${String.format("%.1f", telemetry.batteryVoltage)}V berlebih (Maks: ${thresholds.batteryMaxVoltage}V)!"
                )
            )
        }

        // 4. Speed Limit Check
        if (telemetry.speedKmh > thresholds.speedLimitKmh) {
            alerts.add(
                SafetyAlert(
                    type = AlertType.SPEED_LIMIT,
                    currentValue = telemetry.speedKmh,
                    thresholdValue = thresholds.speedLimitKmh,
                    isCritical = telemetry.speedKmh > thresholds.speedLimitKmh + 20f,
                    message = "KECEPATAN TINGGI: ${telemetry.speedKmh.toInt()} km/h melampaui batas kecepatan (${thresholds.speedLimitKmh.toInt()} km/h)!"
                )
            )
        }

        _activeAlerts.value = alerts

        // Sound/Haptic feedback if alerts are present
        if (alerts.isNotEmpty()) {
            val now = System.currentTimeMillis()
            if (now - lastSoundTimestamp >= SOUND_COOLDOWN_MS) {
                lastSoundTimestamp = now
                val hasCritical = alerts.any { it.isCritical }

                if (thresholds.soundAlertEnabled) {
                    playAlertTone(isCritical = hasCritical)
                }
                if (thresholds.vibrateAlertEnabled) {
                    triggerVibration(isCritical = hasCritical)
                }
            }
        }
    }

    private fun playAlertTone(isCritical: Boolean) {
        try {
            val toneType = if (isCritical) {
                ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
            } else {
                ToneGenerator.TONE_PROP_BEEP2
            }
            toneGenerator?.startTone(toneType, if (isCritical) 500 else 250)
        } catch (e: Exception) {
            Log.w(TAG, "Failed playing alert tone", e)
        }
    }

    private fun triggerVibration(isCritical: Boolean) {
        try {
            val vib = vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = if (isCritical) longArrayOf(0, 200, 100, 200) else longArrayOf(0, 150)
                val amplitudes = if (isCritical) intArrayOf(0, 255, 0, 255) else intArrayOf(0, 180)
                vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(if (isCritical) 400L else 200L)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
