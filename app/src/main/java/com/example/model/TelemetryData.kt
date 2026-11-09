package com.example.model

/**
 * Real-time vehicle telemetry data model parsed from OBD-II protocol or drive simulator.
 */
data class VehicleTelemetry(
    val speedKmh: Float = 0f,
    val rpm: Int = 0,
    val engineTempC: Float = 85f,
    val batteryVoltage: Float = 13.8f,
    val throttlePct: Float = 0f,
    val intakeTempC: Float = 32f,
    val fuelLevelPct: Float = 65f,
    val calculatedLoadPct: Float = 28f,
    val currentGear: String = "P",
    val maxSpeedKmh: Float = 0f,
    val tripDistanceKm: Float = 0f,
    val isEngineRunning: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Types of safety alerts triggered by vehicle thresholds.
 */
enum class AlertType(val title: String, val unit: String) {
    ENGINE_OVERHEAT("Suhu Mesin Panas", "°C"),
    RPM_REDLINE("RPM Redline", "RPM"),
    BATTERY_LOW("Tegangan Aki Rendah", "V"),
    BATTERY_OVERVOLTAGE("Tegangan Aki Berlebih", "V"),
    SPEED_LIMIT("Batas Kecepatan Terlampaui", "km/h")
}

data class SafetyAlert(
    val type: AlertType,
    val currentValue: Float,
    val thresholdValue: Float,
    val isCritical: Boolean = false,
    val message: String,
    val triggerTime: Long = System.currentTimeMillis()
)

/**
 * User-configurable safety thresholds.
 */
data class SafetyThresholds(
    val engineTempWarningC: Float = 95f,
    val engineTempCriticalC: Float = 105f,
    val rpmRedline: Int = 6000,
    val batteryMinVoltage: Float = 11.8f,
    val batteryMaxVoltage: Float = 15.0f,
    val speedLimitKmh: Float = 100f,
    val soundAlertEnabled: Boolean = true,
    val vibrateAlertEnabled: Boolean = true
)

/**
 * Aesthetic color themes for the digital instrument cluster.
 */
enum class ClusterTheme(
    val id: String,
    val label: String,
    val primaryHex: Long,
    val accentHex: Long,
    val warningHex: Long = 0xFFFFB300,
    val criticalHex: Long = 0xFFFF1744
) {
    NEON_CYAN("cyan", "Cyberpunk Cyan", 0xFF00E5FF, 0xFF00B0FF),
    RACING_RED("red", "Racing Sport Red", 0xFFFF1744, 0xFFFF5252),
    ELECTRIC_AMBER("amber", "Electric Amber (JDM)", 0xFFFF9100, 0xFFFFB300),
    EMERALD_ECO("green", "Emerald Green Eco", 0xFF00E676, 0xFF69F0AE)
}

/**
 * Dashboard screen split and layout options.
 */
enum class SplitMode(val label: String) {
    SPLIT_VERTICAL("Split Vertikal (Atas/Bawah)"),
    SPLIT_HORIZONTAL("Split Horizontal (Kiri/Kanan)"),
    FULL_DASHBOARD("Dasbor Penuh"),
    FULL_MAP("Peta Penuh + Mini HUD")
}

/**
 * Secondary split content selector (Map, Music/Video Player, App Launcher).
 */
enum class SecondaryPaneType(val label: String) {
    GPS_MAP("Navigasi GPS"),
    MEDIA_PLAYER("Pemutar Musik & Media"),
    APP_LAUNCHER("Peluncur Multi-Aplikasi (YouTube/Lainnya)")
}

/**
 * Day/Night Theme mode for automotive head units.
 */
enum class DayNightMode(val label: String) {
    AUTO_SYSTEM("Otomatis (Ikuti Head Unit & Waktu)"),
    ALWAYS_DARK("Selalu Mode Malam (Dark Stealth)"),
    ALWAYS_LIGHT("Selalu Mode Siang (Light High-Contrast)")
}

/**
 * Visual gauge presentation styles.
 */
enum class GaugeStyle(val label: String) {
    SPORTS_ARC("Sport Arc Tachometer & Digital Speed"),
    DUAL_DIAL("Dual Dial (Speedometer & RPM)"),
    MINIMAL_HUD("Minimalist High-Contrast HUD"),
    MODULAR_GRID("Modular Grid Telemetry Cards")
}

/**
 * Units of measurement for speed and temperature.
 */
enum class SpeedUnit(val label: String, val factorToKmh: Float) {
    KMH("km/h", 1.0f),
    MPH("mph", 0.621371f)
}

enum class TempUnit(val label: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F")
}
