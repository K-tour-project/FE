package com.everytrip.app.feature.mypage.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.mypage.data.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FavoriteRepository(application)
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val myPage = repository.getMyPage()
                myPage to repository.getSavedProducts()
            }.onSuccess { (myPage, products) ->
                    _uiState.value = MyPageUiState(
                        myPage = myPage,
                        savedProducts = products,
                        favoritePlaceIds = myPage.favoritePlaces.mapNotNull { it.placeId }.toSet(),
                        favoriteTourismIds = myPage.favoritePlaces.mapNotNull { it.contentId }.toSet(),
                        savedProductIds = products.map { it.productId }.toSet(),
                    )
                }.onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun loadMorePlaces() {
        val state = _uiState.value
        val places = state.myPage.favoritePlaces
        if (state.isLoading || state.isLoadingMorePlaces || state.loadMorePlacesFailed ||
            places.size >= state.myPage.favoritePlaceCount
        ) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMorePlaces = true) }
            runCatching { repository.getFavoritePlaces(offset = places.size) }
                .onSuccess { nextPage ->
                    _uiState.update { current ->
                        val merged = (current.myPage.favoritePlaces + nextPage)
                            .distinctBy { it.favoriteId }
                        current.copy(
                            myPage = current.myPage.copy(favoritePlaces = merged),
                            favoritePlaceIds = merged.mapNotNull { it.placeId }.toSet(),
                            favoriteTourismIds = merged.mapNotNull { it.contentId }.toSet(),
                            isLoadingMorePlaces = false,
                            loadMorePlacesFailed = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoadingMorePlaces = false, loadMorePlacesFailed = true)
                    }
                }
        }
    }

    fun loadMoreProducts() {
        val state = _uiState.value
        val products = state.savedProducts
        if (state.isLoading || state.isLoadingMoreProducts || state.loadMoreProductsFailed ||
            products.size >= state.myPage.savedProductCount
        ) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoreProducts = true) }
            runCatching { repository.getSavedProducts(offset = products.size) }
                .onSuccess { nextPage ->
                    _uiState.update { current ->
                        val merged = (current.savedProducts + nextPage).distinctBy { it.productId }
                        current.copy(
                            savedProducts = merged,
                            savedProductIds = merged.map { it.productId }.toSet(),
                            isLoadingMoreProducts = false,
                            loadMoreProductsFailed = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoadingMoreProducts = false, loadMoreProductsFailed = true)
                    }
                }
        }
    }

    fun retryLoadMorePlaces() {
        _uiState.update { it.copy(loadMorePlacesFailed = false) }
        loadMorePlaces()
    }

    fun retryLoadMoreProducts() {
        _uiState.update { it.copy(loadMoreProductsFailed = false) }
        loadMoreProducts()
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

    fun updateProfileImage(url: String?) = mutate {
        repository.updateProfileImage(url)
        refresh()
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }
}
