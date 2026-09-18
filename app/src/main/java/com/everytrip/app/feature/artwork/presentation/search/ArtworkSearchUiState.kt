package com.everytrip.app.feature.artwork.presentation.search

data class ArtworkSearchItem(
    val productId: Int?,
    val title: String,
    val category: String,
    val year: String?,
    val genres: String?,
    val posterUrl: String?,
) {
    val categoryLabel: String
        get() = when (category.trim().lowercase()) {
            "movie" -> "영화"
            "drama", "tv" -> "드라마"
            else -> category
        }
}

data class ArtworkSearchUiState(
    val query: String = "",
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val items: List<ArtworkSearchItem> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
)
