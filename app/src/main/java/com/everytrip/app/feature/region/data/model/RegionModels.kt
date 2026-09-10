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
)

data class RegionLocation(
    val latitude: Double,
    val longitude: Double,
)

data class RegionPolygon(
    val outerBoundary: List<RegionLocation>,
    val holes: List<List<RegionLocation>> = emptyList(),
)

data class RegionBoundary(
    val regionId: Int,
    val name: String,
    val fullName: String,
    val centroid: RegionLocation?,
    val polygons: List<RegionPolygon>,
)

data class TourismPlace(
    val contentId: String,
    val name: String,
    val thumbnailUrl: String?,
    val location: RegionLocation?,
    val sidoName: String?,
    val sigunguName: String?,
    val category: String,
)

data class TourismPage(
    val items: List<TourismPlace>,
    val total: Int,
    val page: Int,
    val hasNext: Boolean,
)

data class TourismDetail(
    val contentId: String,
    val name: String,
    val overview: String?,
    val homepage: String?,
    val tel: String?,
    val address: String?,
    val addressDetail: String?,
    val images: List<String>,
)
