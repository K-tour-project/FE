package com.everytrip.app.feature.mypage.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.mypage.data.FavoriteRepository
import com.everytrip.app.feature.mypage.data.MyPageData
import com.everytrip.app.feature.mypage.data.SavedProductData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteUiState(
    val myPage: MyPageData = MyPageData(),
    val savedProducts: List<SavedProductData> = emptyList(),
    val favoritePlaceIds: Set<Int> = emptySet(),
    val favoriteTourismIds: Set<String> = emptySet(),
    val savedProductIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
)

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FavoriteRepository(application)
    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getMyPage() to repository.getSavedProducts() }
                .onSuccess { (myPage, products) ->
                    _uiState.value = FavoriteUiState(
                        myPage = myPage,
                        savedProducts = products,
                        favoritePlaceIds = myPage.favoritePlaces.mapNotNull { it.placeId }.toSet(),
                        favoriteTourismIds = myPage.favoritePlaces.mapNotNull { it.contentId }.toSet(),
                        savedProductIds = products.map { it.productId }.toSet(),
                    )
                }.onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun togglePlace(id: Int) = mutate {
        val save = id !in _uiState.value.favoritePlaceIds
        repository.setPlace(id, save)
        refresh()
    }

    fun toggleTourism(id: String) = mutate {
        val save = id !in _uiState.value.favoriteTourismIds
        repository.setTourism(id, save)
        refresh()
    }

    fun toggleProduct(id: Int) = mutate {
        val save = id !in _uiState.value.savedProductIds
        repository.setProduct(id, save)
        refresh()
    }

    fun deleteFavorite(id: Long) = mutate { repository.deleteFavorite(id); refresh() }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }
}
