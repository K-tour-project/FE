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
    val placeIds: List<Int> = emptyList(),
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
    val imageCount: Int = images.size,
    val useTime: String? = null,
    val restDate: String? = null,
    val parking: String? = null,
    val petAllowed: String? = null,
    val contents: List<ContentSummary> = emptyList(),
)

data class ContentSummary(
    val productId: Int,
    val title: String,
    val category: String,
    val posterUrl: String?,
    val detailPath: String,
)

data class ContentDetail(
    val productId: Int,
    val title: String,
    val overview: String?,
    val firstAirDate: String?,
    val category: String,
    val productType: String?,
    val runtime: Int?,
    val posterUrl: String?,
    val genres: String?,
    val networks: String?,
    val episodeCount: Int?,
    val rating: Double?,
    val popularity: Double?,
    val leadActors: String?,
)

val ContentSummary.categoryLabel: String
    get() = category.toContentCategoryLabel()

val ContentDetail.categoryLabel: String
    get() = category.toContentCategoryLabel()

fun String.toContentCategoryLabel(): String = when (uppercase()) {
    "MOVIE" -> "영화"
    "DRAMA" -> "드라마"
    else -> this
}

fun ContentDetail.isMovie(): Boolean = category.equals("MOVIE", ignoreCase = true)

fun ContentDetail.isDrama(): Boolean = category.equals("DRAMA", ignoreCase = true)

data class PlaceTourDetail(
    val tourContentId: String,
    val title: String,
    val overview: String?,
    val tel: String?,
    val homepage: String?,
    val useTime: String?,
    val restDate: String?,
    val parking: String?,
    val petAllowed: String?,
    val images: List<String>,
    val imageCount: Int = images.size,
)

data class RelatedPlace(
    val relatedId: String,
    val contentId: String,
    val name: String,
    val sidoName: String?,
    val sigunguName: String?,
    val detailPath: String,
)

data class PlaceDetail(
    val placeId: Int,
    val name: String,
    val location: RegionLocation,
    val address: String?,
    val roadAddress: String?,
    val region: Map<String, String?>,
    val contents: List<ContentSummary>,
    val detail: PlaceTourDetail?,
    val relatedPlaces: List<RelatedPlace>,
)
