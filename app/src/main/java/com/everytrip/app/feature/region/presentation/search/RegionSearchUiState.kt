package com.everytrip.app.feature.region.presentation.search

data class RegionSearchUiState(
    val isLocationPermissionGranted: Boolean = false,
    val isLoadingCurrentLocation: Boolean = false,
    val currentLocation: RegionCoordinate? = null,
    val errorMessage: String? = null,
)

data class RegionCoordinate(
    val latitude: Double,
    val longitude: Double,
)
