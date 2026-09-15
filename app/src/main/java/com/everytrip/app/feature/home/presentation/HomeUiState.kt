package com.everytrip.app.feature.home.presentation

import com.everytrip.app.feature.home.data.HomeProduct
import com.everytrip.app.feature.home.data.HomeTourismPlace

data class HomeUiState(
    val popularProducts: List<HomeProduct> = emptyList(),
    val popularTourismPlaces: List<HomeTourismPlace> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
