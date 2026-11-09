package com.example.navigation

enum class NavigationAction(val label: String, val iconName: String) {
    STRAIGHT("Lurus terus", "straight"),
    TURN_RIGHT("Belok Kanan", "turn_right"),
    TURN_LEFT("Belok Kiri", "turn_left"),
    SLIGHT_RIGHT("Serong Kanan", "slight_right"),
    SLIGHT_LEFT("Serong Kiri", "slight_left"),
    U_TURN("Putar Balik", "u_turn"),
    DESTINATION("Tiba di Tujuan", "destination")
}

data class NavigationStep(
    val action: NavigationAction,
    val streetName: String,
    val distanceMeters: Int
)

data class RouteDestination(
    val id: String,
    val name: String,
    val category: String,
    val distanceKm: Float,
    val etaMinutes: Int,
    val destinationLat: Double,
    val destinationLng: Double
)

data class NavigationState(
    val currentLat: Double = -6.2088,
    val currentLng: Double = 106.8456,
    val bearingDegrees: Float = 45f,
    val altitudeMeters: Double = 24.0,
    val activeDestination: RouteDestination? = null,
    val currentStep: NavigationStep = NavigationStep(
        action = NavigationAction.STRAIGHT,
        streetName = "Jl. Sudirman Boulevard",
        distanceMeters = 450
    ),
    val nextStep: NavigationStep = NavigationStep(
        action = NavigationAction.TURN_RIGHT,
        streetName = "Menuju Tol Lingkar Luar",
        distanceMeters = 1200
    ),
    val speedLimitKmh: Int = 80,
    val isNavigating: Boolean = true
)
