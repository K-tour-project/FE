package com.everytrip.app.feature.artwork.presentation.detail

import com.everytrip.app.feature.region.data.model.ContentDetail

data class ArtworkDetailUiState(
    val productId: Int? = null,
    val content: ContentDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
