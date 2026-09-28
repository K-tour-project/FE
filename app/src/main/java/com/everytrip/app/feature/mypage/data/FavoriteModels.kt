package com.everytrip.app.feature.mypage.data

data class PageResult<T>(val items: List<T>, val total: Int?)

data class FavoriteMutation(val isSaved: Boolean, val favoriteId: Long?, val favoritePlaceCount: Int, val savedProductCount: Int)
data class NicknameRequest(val nickname: String)
data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
)
data class UpdatedProfile(
    val userId: Int,
    val nickname: String,
    val email: String,
    val profileImageUrl: String?,
)
data class MyPageData(
    val userId: Int? = null,
    val nickname: String = "",
    val email: String = "",
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
    val sidoName: String = "",
    val sigunguName: String = "",
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
