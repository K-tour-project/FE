package com.everytrip.app.feature.mypage.presentation

import com.everytrip.app.feature.mypage.data.MyPageData
import com.everytrip.app.feature.mypage.data.SavedProductData

data class MyPageUiState(
    val myPage: MyPageData = MyPageData(),
    val savedProducts: List<SavedProductData> = emptyList(),
    val favoritePlaceIds: Set<Int> = emptySet(),
    val favoriteTourismIds: Set<String> = emptySet(),
    val savedProductIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isLoadingMorePlaces: Boolean = false,
    val isLoadingMoreProducts: Boolean = false,
    val loadMorePlacesFailed: Boolean = false,
    val loadMoreProductsFailed: Boolean = false,
)
