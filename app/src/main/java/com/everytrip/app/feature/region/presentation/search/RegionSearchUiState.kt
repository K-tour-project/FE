package com.everytrip.app.feature.region.presentation.search

import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.model.RegionOption

data class RegionSearchUiState(
    val isLocationPermissionGranted: Boolean = false,
    val isLoadingCurrentLocation: Boolean = false,
    val currentLocation: RegionCoordinate? = null,
    val sidos: List<Sido> = emptyList(),
    val sigungus: List<RegionOption> = emptyList(),
    val selectedSido: Sido? = null,
    val selectedSigungu: RegionOption? = null,
    val selectedRegionLocation: RegionCoordinate? = null,
    val isLoadingSidos: Boolean = false,
    val isLoadingSigungus: Boolean = false,
    val isLoadingRegionBounds: Boolean = false,
    val regionErrorMessage: String? = null,
)

data class RegionCoordinate(
    val latitude: Double,
    val longitude: Double,
)
