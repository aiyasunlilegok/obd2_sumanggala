package com.example.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages GPS telemetry, route guidance, and external map app launching (Google Maps / Waze).
 */
class NavigationManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "NavigationManager"

        val PRESET_DESTINATIONS = listOf(
            RouteDestination("spbu_1", "SPBU Pertamina 31.129", "SPBU / Pom Bensin", 1.8f, 4, -6.2150, 106.8490),
            RouteDestination("bengkel_1", "Bengkel Otomotif Resmi 24 Jam", "Bengkel & Servis", 3.2f, 8, -6.2210, 106.8520),
            RouteDestination("rest_1", "Rest Area & SPKLU Fast Charging", "Rest Area", 6.5f, 12, -6.2340, 106.8610),
            RouteDestination("mall_1", "Grand Metropolitan Hub", "Pusat Kota / Mall", 5.0f, 11, -6.2280, 106.8400)
        )
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _navState = MutableStateFlow(
        NavigationState(
            activeDestination = PRESET_DESTINATIONS[0]
        )
    )
    val navState: StateFlow<NavigationState> = _navState.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isGpsTracking = false
    private var simulatedNavJob: Job? = null

    init {
    }

    @SuppressLint("MissingPermission")
    fun startGpsUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500)
                .setMinUpdateIntervalMillis(1000)
                .setMinUpdateDistanceMeters(2f)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location: Location = result.lastLocation ?: return
                    updateWithRealLocation(location)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
            isGpsTracking = true
        } catch (e: Exception) {
            Log.w(TAG, "Gagal request location updates (mungkin izin belum diberikan): ${e.message}")
        }
    }

    fun stopGpsUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        isGpsTracking = false
    }

    private fun updateWithRealLocation(loc: Location) {
        val current = _navState.value
        _navState.value = current.copy(
            currentLat = loc.latitude,
            currentLng = loc.longitude,
            bearingDegrees = if (loc.hasBearing()) loc.bearing else current.bearingDegrees,
            altitudeMeters = loc.altitude
        )
    }

    fun selectDestination(dest: RouteDestination) {
        _navState.value = _navState.value.copy(
            activeDestination = dest,
            currentStep = NavigationStep(
                action = NavigationAction.TURN_RIGHT,
                streetName = "Arah ${dest.name}",
                distanceMeters = (dest.distanceKm * 400).toInt()
            )
        )
    }

    /**
     * Smoothly updates vehicle navigation progression along route when moving.
     */
    private fun startSimulatedRouteTracking() {
        simulatedNavJob?.cancel()
        simulatedNavJob = scope.launch {
            val routeSteps = listOf(
                NavigationStep(NavigationAction.STRAIGHT, "Jl. Sudirman Boulevard", 500),
                NavigationStep(NavigationAction.TURN_RIGHT, "Masuk Pintu Tol Lingkar", 350),
                NavigationStep(NavigationAction.STRAIGHT, "Jalan Tol Layang KM 12", 2400),
                NavigationStep(NavigationAction.SLIGHT_LEFT, "Keluar Exit Gerbang Tol", 800),
                NavigationStep(NavigationAction.DESTINATION, "Tiba di Titik Tujuan", 150)
            )
            var stepIndex = 0
            var remainingDist = routeSteps[0].distanceMeters

            while (isActive) {
                delay(1200)
                remainingDist -= 45
                if (remainingDist <= 0) {
                    stepIndex = (stepIndex + 1) % routeSteps.size
                    val nextStep = routeSteps[stepIndex]
                    remainingDist = nextStep.distanceMeters

                    val nextNextStep = routeSteps[(stepIndex + 1) % routeSteps.size]

                    _navState.value = _navState.value.copy(
                        currentStep = nextStep.copy(distanceMeters = remainingDist),
                        nextStep = nextNextStep,
                        bearingDegrees = (_navState.value.bearingDegrees + when (nextStep.action) {
                            NavigationAction.TURN_RIGHT -> 45f
                            NavigationAction.TURN_LEFT -> -45f
                            NavigationAction.U_TURN -> 180f
                            else -> 0f
                        }) % 360f
                    )
                } else {
                    _navState.value = _navState.value.copy(
                        currentStep = _navState.value.currentStep.copy(distanceMeters = remainingDist)
                    )
                }
            }
        }
    }

    /**
     * Opens native Google Maps or external navigation apps with destination.
     */
    fun openExternalNavigation(destinationName: String = "Pom Bensin Terdekat") {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(destinationName)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Try specifically Google Maps first, or fallback to generic geo intent
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val genericIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(genericIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membuka aplikasi navigasi eksternal", e)
        }
    }
}
