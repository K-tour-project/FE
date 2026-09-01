package com.everytrip.app.feature.region.data.model

data class Sido(
    val id: Int,
    val name: String,
    val hasChildren: Boolean,
)

data class RegionOption(
    val regionId: Int,
    val name: String,
    val fullName: String,
    val level: String,
    val parentRegionId: Int?,
    val hasChildren: Boolean,
    val centroid: RegionLocation?,
)

data class RegionLocation(
    val latitude: Double,
    val longitude: Double,
)

data class RegionBoundary(
    val regionId: Int,
    val name: String,
    val fullName: String,
    val centroid: RegionLocation?,
)
