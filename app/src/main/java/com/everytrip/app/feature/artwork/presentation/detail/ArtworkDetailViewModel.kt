package com.everytrip.app.feature.artwork.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.artwork.data.ArtworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtworkDetailViewModel(
    private val repository: ArtworkRepository = ArtworkRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArtworkDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun load(productId: Int) {
        if (_uiState.value.productId == productId && _uiState.value.content != null) return
        viewModelScope.launch {
            _uiState.value = ArtworkDetailUiState(productId = productId, isLoading = true)
            runCatching { repository.getArtworkDetail(productId) }
                .onSuccess { content ->
                    _uiState.value = ArtworkDetailUiState(productId = productId, content = content)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "작품 정보를 불러오지 못했어요.")
                    }
                }
        }
    }
}
