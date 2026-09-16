package com.everytrip.app.feature.artwork.presentation.search

data class ArtworkSearchItem(
    val productId: Int,
    val title: String,
    val category: String,
    val posterUrl: String?,
    val year: String?,
    val genres: String?,
) {
    val categoryLabel: String
        get() = when (category.uppercase()) {
            "MOVIE" -> "영화"
            "DRAMA" -> "드라마"
            "ENTERTAINMENT", "VARIETY" -> "예능"
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
