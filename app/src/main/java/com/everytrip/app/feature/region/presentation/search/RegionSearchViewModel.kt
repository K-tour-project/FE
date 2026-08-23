package com.everytrip.app.feature.region.presentation.search

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegionSearchViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _uiState = MutableStateFlow(
        RegionSearchUiState(
            isLocationPermissionGranted = application.hasLocationPermission(),
        ),
    )
    val uiState: StateFlow<RegionSearchUiState> = _uiState.asStateFlow()

    private var currentLocationListener: LocationListener? = null

    init {
        if (_uiState.value.isLocationPermissionGranted) {
            loadCurrentLocation()
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update {
            it.copy(
                isLocationPermissionGranted = isGranted,
            )
        }

        if (isGranted) {
            loadCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun loadCurrentLocation() {
        val application = getApplication<Application>()
        if (!application.hasLocationPermission()) {
            onLocationPermissionResult(isGranted = false)
            return
        }

        _uiState.update {
            it.copy(
                isLocationPermissionGranted = true,
                isLoadingCurrentLocation = true,
            )
        }

        val lastKnownLocation = findBestLastKnownLocation()
        if (lastKnownLocation != null) {
            updateCurrentLocation(lastKnownLocation)
            return
        }

        val provider = findAvailableProvider()
        if (provider == null) {
            _uiState.update {
                it.copy(
                    isLoadingCurrentLocation = false,
                )
            }
            return
        }

        currentLocationListener?.let(locationManager::removeUpdates)
        currentLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocationListener?.let(locationManager::removeUpdates)
                currentLocationListener = null
                updateCurrentLocation(location)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) {
                _uiState.update {
                    it.copy(
                        isLoadingCurrentLocation = false,
                    )
                }
            }
        }

        runCatching {
            locationManager.requestLocationUpdates(
                provider,
                MIN_LOCATION_UPDATE_TIME_MS,
                MIN_LOCATION_UPDATE_DISTANCE_M,
                currentLocationListener as LocationListener,
                Looper.getMainLooper(),
            )
        }.onFailure {
            currentLocationListener = null
            _uiState.update {
                it.copy(
                    isLoadingCurrentLocation = false,
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun findBestLastKnownLocation(): Location? {
        return locationManager.getProviders(true)
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxWithOrNull(
                compareBy<Location> { it.time }
                    .thenBy { -it.accuracy },
            )
    }

    private fun findAvailableProvider(): String? {
        return when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                LocationManager.GPS_PROVIDER
            }

            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                LocationManager.NETWORK_PROVIDER
            }

            else -> null
        }
    }

    private fun updateCurrentLocation(location: Location) {
        _uiState.update {
            it.copy(
                isLoadingCurrentLocation = false,
                currentLocation = RegionCoordinate(
                    latitude = location.latitude,
                    longitude = location.longitude,
                ),
            )
        }
    }

    override fun onCleared() {
        currentLocationListener?.let(locationManager::removeUpdates)
        currentLocationListener = null
    }

    private fun Context.hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    private companion object {
        const val MIN_LOCATION_UPDATE_TIME_MS = 1_000L
        const val MIN_LOCATION_UPDATE_DISTANCE_M = 1f
    }
}
