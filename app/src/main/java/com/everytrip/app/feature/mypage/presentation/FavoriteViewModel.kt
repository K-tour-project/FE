package com.everytrip.app.feature.mypage.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.everytrip.app.feature.mypage.data.FavoriteRepository
import com.everytrip.app.feature.mypage.data.UpdatedProfile
import com.everytrip.app.feature.region.presentation.search.invalidateTourismBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import com.everytrip.app.feature.auth.data.remote.AuthHttpException
import com.everytrip.app.feature.auth.data.remote.AuthSessionManager

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FavoriteRepository(application)
    private val session = AuthSessionManager.get(application)
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()
    private val pagingGeneration = MutableStateFlow(0)
    private val placeFavoriteOverrides = mutableMapOf<Int, Boolean>()
    private val tourismFavoriteOverrides = mutableMapOf<String, Boolean>()
    private val pendingPlaceIds = mutableSetOf<Int>()
    private val pendingTourismIds = mutableSetOf<String>()
    val favoritePlaces = pagingGeneration
        .flatMapLatest { repository.favoritePlacesPaged() }
        .cachedIn(viewModelScope)
    val savedProducts = pagingGeneration
        .flatMapLatest { repository.savedProductsPaged() }
        .cachedIn(viewModelScope)

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
                        favoritePlaceIds = withPlaceOverrides(myPage.favoritePlaces.mapNotNull { it.placeId }.toSet()),
                        favoriteTourismIds = withTourismOverrides(myPage.favoritePlaces.mapNotNull { it.contentId }.toSet()),
                        savedProductIds = products.map { it.productId }.toSet(),
                    )
                }.onFailure { _uiState.update { it.copy(isLoading = false) } }
            pagingGeneration.value++
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
                            favoritePlaceIds = withPlaceOverrides(merged.mapNotNull { it.placeId }.toSet()),
                            favoriteTourismIds = withTourismOverrides(merged.mapNotNull { it.contentId }.toSet()),
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

    fun togglePlace(id: Int) {
        if (!pendingPlaceIds.add(id)) return
        val save = id !in _uiState.value.favoritePlaceIds
        val previousOverride = placeFavoriteOverrides.put(id, save)
        _uiState.update { state ->
            state.copy(favoritePlaceIds = if (save) state.favoritePlaceIds + id else state.favoritePlaceIds - id)
        }
        viewModelScope.launch {
            try {
                repository.setPlace(id, save)
                refresh()
            } catch (_: Exception) {
                if (previousOverride == null) placeFavoriteOverrides.remove(id)
                else placeFavoriteOverrides[id] = previousOverride
                _uiState.update { state ->
                    state.copy(favoritePlaceIds = if (save) state.favoritePlaceIds - id else state.favoritePlaceIds + id)
                }
            } finally {
                pendingPlaceIds.remove(id)
            }
        }
    }

    fun toggleTourism(id: String) {
        if (!pendingTourismIds.add(id)) return
        val save = id !in _uiState.value.favoriteTourismIds
        val previousOverride = tourismFavoriteOverrides.put(id, save)
        _uiState.update { state ->
            state.copy(favoriteTourismIds = if (save) state.favoriteTourismIds + id else state.favoriteTourismIds - id)
        }
        viewModelScope.launch {
            try {
                repository.setTourism(id, save)
                refresh()
            } catch (_: Exception) {
                if (previousOverride == null) tourismFavoriteOverrides.remove(id)
                else tourismFavoriteOverrides[id] = previousOverride
                _uiState.update { state ->
                    state.copy(favoriteTourismIds = if (save) state.favoriteTourismIds - id else state.favoriteTourismIds + id)
                }
            } finally {
                pendingTourismIds.remove(id)
            }
        }
    }

    private fun withPlaceOverrides(ids: Set<Int>): Set<Int> =
        placeFavoriteOverrides.entries.fold(ids) { current, (id, saved) ->
            if (saved) current + id else current - id
        }

    private fun withTourismOverrides(ids: Set<String>): Set<String> =
        tourismFavoriteOverrides.entries.fold(ids) { current, (id, saved) ->
            if (saved) current + id else current - id
        }

    fun setPlaceFavorite(id: Int, saved: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.setPlace(id, saved) }
                .onSuccess { mutation ->
                    placeFavoriteOverrides[id] = mutation.isSaved
                    _uiState.update { state ->
                        state.copy(favoritePlaceIds = if (mutation.isSaved) state.favoritePlaceIds + id else state.favoritePlaceIds - id)
                    }
                    refresh()
                    onComplete(true)
                }
                .onFailure { onComplete(false) }
        }
    }

    fun setTourismFavorite(id: String, saved: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.setTourism(id, saved) }
                .onSuccess { mutation ->
                    tourismFavoriteOverrides[id] = mutation.isSaved
                    _uiState.update { state ->
                        state.copy(favoriteTourismIds = if (mutation.isSaved) state.favoriteTourismIds + id else state.favoriteTourismIds - id)
                    }
                    refresh()
                    onComplete(true)
                }
                .onFailure { onComplete(false) }
        }
    }

    fun toggleProduct(id: Int) = mutate {
        val save = id !in _uiState.value.savedProductIds
        repository.setProduct(id, save)
        refresh()
    }

    fun deleteFavorite(id: Long) = mutate { repository.deleteFavorite(id); refresh() }

    fun updateNickname(nickname: String) = settingsMutation {
        val updated = repository.updateNickname(nickname)
        applyUpdatedProfile(updated)
        "닉네임이 변경되었습니다."
    }

    fun changePassword(currentPassword: String, newPassword: String, onSessionEnded: () -> Unit) = settingsMutation {
        repository.changePassword(currentPassword, newPassword)
        session.clearTokens()
        onSessionEnded()
        "비밀번호가 변경되었습니다. 다시 로그인해 주세요."
    }

    fun deleteAccount(onSessionEnded: () -> Unit) = settingsMutation {
        repository.deleteAccount()
        session.clearTokens()
        onSessionEnded()
        "회원 탈퇴가 완료되었습니다."
    }

    fun updateProfileImage(bytes: ByteArray, mimeType: String, fileName: String) = settingsMutation {
        val updated = repository.updateProfileImage(bytes, mimeType, fileName)
        invalidateTourismBitmap(_uiState.value.myPage.profileImageUrl)
        invalidateTourismBitmap(updated.profileImageUrl)
        applyUpdatedProfile(updated, imageChanged = true)
        "프로필 이미지가 변경되었습니다."
    }

    fun removeProfileImage() = settingsMutation {
        val updated = repository.removeProfileImage()
        invalidateTourismBitmap(_uiState.value.myPage.profileImageUrl)
        applyUpdatedProfile(updated, imageChanged = true)
        "프로필 이미지가 삭제되었습니다."
    }

    fun clearSettingsMessage() = _uiState.update { it.copy(settingsMessage = null) }
    fun showSettingsMessage(message: String) = _uiState.update { it.copy(settingsMessage = message) }

    private fun applyUpdatedProfile(updated: UpdatedProfile, imageChanged: Boolean = false) {
        _uiState.update { state ->
            state.copy(
                myPage = state.myPage.copy(
                    userId = updated.userId,
                    nickname = updated.nickname,
                    email = updated.email,
                    profileImageUrl = updated.profileImageUrl,
                ),
                profileImageRevision = state.profileImageRevision + if (imageChanged) 1 else 0,
            )
        }
    }

    private fun settingsMutation(block: suspend () -> String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSettingsLoading = true, settingsMessage = null) }
            runCatching { block() }
                .onSuccess { message -> _uiState.update { it.copy(isSettingsLoading = false, settingsMessage = message) } }
                .onFailure { error ->
                    val message = (error as? AuthHttpException)?.detail
                        ?: error.message ?: "요청을 처리하지 못했습니다."
                    _uiState.update { it.copy(isSettingsLoading = false, settingsMessage = message) }
                }
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }
}
