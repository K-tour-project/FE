package com.everytrip.app.feature.home.data

data class HomeData(
    val popularProducts: List<HomeProduct>,
    val popularTourismPlaces: List<HomeTourismPlace>,
)

data class HomeProduct(
    val title: String,
    val posterUrl: String?,
    val detailPath: String,
)

data class HomeTourismPlace(
    val name: String,
    val thumbnailUrl: String?,
    val detailPath: String,
)
