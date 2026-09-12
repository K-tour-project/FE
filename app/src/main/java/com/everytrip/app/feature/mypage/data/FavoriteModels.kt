package com.everytrip.app.feature.mypage.data

data class FavoriteMutation(val isSaved: Boolean, val favoriteId: Long?, val favoritePlaceCount: Int, val savedProductCount: Int)
data class MyPageData(
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val favoritePlaceCount: Int = 0,
    val savedProductCount: Int = 0,
    val favoritePlaces: List<FavoritePlaceData> = emptyList(),
)
data class FavoritePlaceData(
    val favoriteId: Long,
    val placeId: Int?,
    val contentId: String?,
    val name: String,
    val description: String,
    val address: String,
    val imageUrl: String?,
    val detailPath: String,
)
data class SavedProductData(
    val productId: Int,
    val title: String,
    val category: String,
    val year: String,
    val overview: String,
    val locationSummary: String,
    val posterUrl: String?,
    val detailPath: String,
)
