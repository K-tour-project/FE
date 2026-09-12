package com.everytrip.app.feature.region.presentation.search

import com.everytrip.app.feature.region.data.repository.RegionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Owns tourism requests independently of Android location services. Called on the UI thread. */
internal class RegionTourismController(
    private val regionRepository: RegionRepository,
    private val _uiState: MutableStateFlow<RegionSearchUiState>,
    private val scope: CoroutineScope,
) {
    private var placesRequestId = 0
    private var detailRequestId = 0

    fun resetPlaces() {
        placesRequestId++
        closePlaceDetail()
        _uiState.update {
            it.copy(places = emptyList(), totalPlaces = 0, placesPage = 0,
                hasNextPlaces = false, isLoadingPlaces = false, placesErrorMessage = null)
        }
    }

    fun loadMorePlaces() {
        val state = _uiState.value
        val regionId = state.selectedRegionId ?: return
        if (state.isLoadingPlaces || (state.placesPage > 0 && !state.hasNextPlaces)) return
        val requestId = placesRequestId
        val page = state.placesPage + 1
        _uiState.update { it.copy(isLoadingPlaces = true, placesErrorMessage = null) }
        scope.launch {
            try {
                val result = regionRepository.getTourismPlaces(regionId, page)
                check(result.page == page) { "Unexpected tourism page" }
                if (requestId != placesRequestId) return@launch
                _uiState.update {
                    it.copy(places = (it.places + result.items).distinctBy { place -> place.contentId },
                        totalPlaces = result.total, placesPage = result.page,
                        hasNextPlaces = result.hasNext, isLoadingPlaces = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (requestId == placesRequestId) {
                    _uiState.update { it.copy(isLoadingPlaces = false,
                        placesErrorMessage = "관광지 목록을 불러오지 못했어요. 다시 시도해 주세요.") }
                }
            }
        }
    }

    fun selectPlace(contentId: String) {
        val place = _uiState.value.places.firstOrNull { it.contentId == contentId } ?: return
        selectRelatedPlace(place.contentId)
    }

    fun selectRelatedPlace(contentId: String) {
        if (contentId.isBlank()) return
        val requestId = ++detailRequestId
        _uiState.update { it.copy(selectedContentId = contentId, selectedPlaceId = null,
            selectedProductId = null, placeDetail = null, filmingPlaceDetail = null,
            contentDetail = null,
            isLoadingDetail = true, detailErrorMessage = null) }
        scope.launch {
            try {
                val detail = regionRepository.getTourismDetail(contentId)
                check(detail.contentId == contentId) { "Unexpected tourism content ID" }
                if (requestId != detailRequestId) return@launch
                _uiState.update { it.copy(placeDetail = detail, isLoadingDetail = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (requestId == detailRequestId) {
                    _uiState.update { it.copy(isLoadingDetail = false,
                        detailErrorMessage = "상세 정보를 불러오지 못했어요. 다시 시도해 주세요.") }
                }
            }
        }
    }

    fun selectFilmingPlace(placeId: Int) {
        if (placeId <= 0) return
        val requestId = ++detailRequestId
        _uiState.update { it.copy(selectedContentId = null, selectedPlaceId = placeId,
            selectedProductId = null, placeDetail = null, filmingPlaceDetail = null,
            contentDetail = null, isLoadingDetail = true, detailErrorMessage = null) }
        scope.launch {
            try {
                val detail = regionRepository.getPlaceDetail(placeId)
                check(detail.placeId == placeId) { "Unexpected place ID" }
                if (requestId != detailRequestId) return@launch
                _uiState.update { it.copy(filmingPlaceDetail = detail, isLoadingDetail = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (requestId == detailRequestId) _uiState.update { it.copy(
                    isLoadingDetail = false, detailErrorMessage = "장소 상세 정보를 불러오지 못했어요. 다시 시도해 주세요.") }
            }
        }
    }

    fun selectContent(productId: Int) {
        if (productId <= 0) return
        val requestId = ++detailRequestId
        _uiState.update { it.copy(selectedContentId = null, selectedPlaceId = null,
            selectedProductId = productId, placeDetail = null, filmingPlaceDetail = null,
            contentDetail = null, isLoadingDetail = true, detailErrorMessage = null) }
        scope.launch {
            try {
                val detail = regionRepository.getContentDetail(productId)
                check(detail.productId == productId) { "Unexpected product ID" }
                if (requestId != detailRequestId) return@launch
                _uiState.update { it.copy(contentDetail = detail, isLoadingDetail = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (requestId == detailRequestId) _uiState.update { it.copy(
                    isLoadingDetail = false, detailErrorMessage = "작품 상세 정보를 불러오지 못했어요. 다시 시도해 주세요.") }
            }
        }
    }

    fun closePlaceDetail() {
        detailRequestId++
        _uiState.update { it.copy(selectedContentId = null, selectedPlaceId = null,
            selectedProductId = null, placeDetail = null, filmingPlaceDetail = null,
            contentDetail = null,
            isLoadingDetail = false, detailErrorMessage = null) }
    }

}
