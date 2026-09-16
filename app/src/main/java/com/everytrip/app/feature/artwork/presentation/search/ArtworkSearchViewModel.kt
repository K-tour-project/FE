package com.everytrip.app.feature.artwork.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.artwork.data.ArtworkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtworkSearchViewModel(
    private val repository: ArtworkRepository = ArtworkRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArtworkSearchUiState())
    val uiState = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun activate() {
        _uiState.update { it.copy(isActive = true) }
    }

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query, isActive = true) }
        search()
    }

    private fun search() {
        searchJob?.cancel()
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.update { it.copy(items = emptyList(), total = 0,
                isLoading = false, error = null) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        searchJob = viewModelScope.launch {
            delay(250)
            runCatching { repository.searchArtworks(query) }
                .onSuccess { items ->
                    _uiState.update { it.copy(items = items, total = items.size,
                        isLoading = false) }
                }
                .onFailure { error ->
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    _uiState.update { it.copy(items = emptyList(), total = 0,
                        isLoading = false, error = error.message ?: "검색 오류") }
                }
        }
    }
}
