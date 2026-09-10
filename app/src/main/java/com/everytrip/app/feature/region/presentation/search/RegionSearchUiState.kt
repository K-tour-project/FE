package com.everytrip.app.feature.region.presentation.search

import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.RegionPolygon
import com.everytrip.app.feature.region.data.model.TourismPlace
import com.everytrip.app.feature.region.data.model.TourismDetail

data class RegionSearchUiState(
    val isLocationPermissionGranted: Boolean = false,
    val isLoadingCurrentLocation: Boolean = false,
    val currentLocation: RegionCoordinate? = null,
    val sidos: List<Sido> = emptyList(),
    val sigungus: List<RegionOption> = emptyList(),
    val selectedSido: Sido? = null,
    val selectedSigungu: RegionOption? = null,
    val selectedRegionId: Int? = null,
    val selectedRegionLocation: RegionCoordinate? = null,
    val selectedRegionPolygons: List<RegionPolygon> = emptyList(),
    val isLoadingSidos: Boolean = false,
    val isLoadingSigungus: Boolean = false,
    val isLoadingRegionBounds: Boolean = false,
    val regionErrorMessage: String? = null,
    val places: List<TourismPlace> = emptyList(),
    val totalPlaces: Int = 0,
    val placesPage: Int = 0,
    val hasNextPlaces: Boolean = false,
    val isLoadingPlaces: Boolean = false,
    val placesErrorMessage: String? = null,
    val selectedContentId: String? = null,
    val placeDetail: TourismDetail? = null,
    val isLoadingDetail: Boolean = false,
    val detailErrorMessage: String? = null,
)

data class RegionCoordinate(
    val latitude: Double,
    val longitude: Double,
)
