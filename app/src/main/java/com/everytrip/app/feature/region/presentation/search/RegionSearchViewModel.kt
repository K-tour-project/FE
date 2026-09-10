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
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.repository.RegionRepository
import com.everytrip.app.feature.region.data.repository.RegionRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegionSearchViewModel(
    application: Application,
    private val regionRepository: RegionRepository = RegionRepositoryImpl(),
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        regionRepository = RegionRepositoryImpl(),
    )

    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _uiState = MutableStateFlow(
        RegionSearchUiState(
            isLocationPermissionGranted = application.hasLocationPermission(),
        ),
    )
    val uiState: StateFlow<RegionSearchUiState> = _uiState.asStateFlow()

    private var currentLocationListener: LocationListener? = null
    private var regionBoundaryRequestId = 0
    private var sigungusRequestId = 0
    private val tourism = RegionTourismController(regionRepository, _uiState, viewModelScope)

    private fun resetPlaces() = tourism.resetPlaces()
    fun loadMorePlaces() = tourism.loadMorePlaces()
    fun selectPlace(contentId: String) = tourism.selectPlace(contentId)
    fun closePlaceDetail() = tourism.closePlaceDetail()

    init {
        if (_uiState.value.isLocationPermissionGranted) {
            loadCurrentLocation()
        }
    }

    fun loadSidos() {
        if (_uiState.value.isLoadingSidos) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingSidos = true,
                    regionErrorMessage = null,
                )
            }
            runCatching {
                regionRepository.getSidos()
            }.onSuccess { sidos ->
                _uiState.update {
                    it.copy(
                        sidos = sidos,
                        isLoadingSidos = false,
                    )
                }
            }.onFailure { throwable ->
                Log.e(TAG, "Failed to load sidos", throwable)
                _uiState.update {
                    it.copy(
                        isLoadingSidos = false,
                        regionErrorMessage = throwable.message,
                    )
                }
            }
        }
    }

    fun onSidoSelected(sidoName: String) {
        val sido = _uiState.value.sidos.firstOrNull { it.name == sidoName } ?: return
        regionBoundaryRequestId++
        sigungusRequestId++
        resetPlaces()

        _uiState.update {
            it.copy(
                selectedSido = sido,
                selectedSigungu = null,
                selectedRegionId = null,
                selectedRegionLocation = null,
                selectedRegionPolygons = emptyList(),
                sigungus = emptyList(),
                isLoadingSigungus = false,
                isLoadingRegionBounds = false,
                regionErrorMessage = null,
            )
        }

        if (sido.hasChildren) {
            loadSigungus(sido.id)
        } else {
            loadRegionBoundary(sido.id)
        }
    }

    fun onSigunguSelected(sigungu: String) {
        val selectedSigungu = _uiState.value.sigungus.firstOrNull { it.name == sigungu } ?: return
        resetPlaces()
        _uiState.update {
            it.copy(
                selectedSigungu = selectedSigungu,
                selectedRegionId = selectedSigungu.regionId,
                selectedRegionLocation = null,
                selectedRegionPolygons = emptyList(),
                regionErrorMessage = null,
            )
        }
        loadRegionBoundary(selectedSigungu.regionId)
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

    private fun loadSigungus(sidoId: Int) {
        val requestId = ++sigungusRequestId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingSigungus = true,
                    regionErrorMessage = null,
                )
            }
            runCatching {
                regionRepository.getSigungus(sidoId)
            }.onSuccess { sigungus ->
                if (requestId != sigungusRequestId) return@onSuccess
                _uiState.update {
                    it.copy(
                        sigungus = sigungus,
                        isLoadingSigungus = false,
                    )
                }
            }.onFailure { throwable ->
                Log.e(TAG, "Failed to load sigungus", throwable)
                if (requestId != sigungusRequestId) return@onFailure
                _uiState.update {
                    it.copy(
                        isLoadingSigungus = false,
                        regionErrorMessage = throwable.message,
                    )
                }
            }
        }
    }

    private fun loadRegionBoundary(regionId: Int) {
        val requestId = ++regionBoundaryRequestId
        _uiState.update { it.copy(selectedRegionId = regionId) }
        loadMorePlaces()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedRegionId = regionId,
                    isLoadingRegionBounds = true,
                    regionErrorMessage = null,
                )
            }
            runCatching {
                regionRepository.getBoundary(regionId)
            }.onSuccess { boundary ->
                if (requestId != regionBoundaryRequestId) {
                    return@onSuccess
                }
                _uiState.update {
                    it.copy(
                        selectedRegionId = regionId,
                        selectedRegionLocation = boundary.toRegionCoordinate(),
                        selectedRegionPolygons = boundary.polygons,
                        isLoadingRegionBounds = false,
                    )
                }
            }.onFailure { throwable ->
                Log.e(TAG, "Failed to load region boundary", throwable)
                if (requestId != regionBoundaryRequestId) {
                    return@onFailure
                }
                _uiState.update {
                    it.copy(
                        isLoadingRegionBounds = false,
                        regionErrorMessage = throwable.message,
                    )
                }
            }
        }
    }

    private fun RegionBoundary.toRegionCoordinate(): RegionCoordinate? {
        val centroid = centroid ?: return null
        return RegionCoordinate(
            latitude = centroid.latitude,
            longitude = centroid.longitude,
        )
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
        const val TAG = "RegionSearchViewModel"
        const val MIN_LOCATION_UPDATE_TIME_MS = 1_000L
        const val MIN_LOCATION_UPDATE_DISTANCE_M = 1f
    }
}
