package com.everytrip.app.feature.artwork.data

import com.everytrip.app.BuildConfig
import com.everytrip.app.core.network.NetworkProvider
import com.everytrip.app.feature.artwork.presentation.search.ArtworkSearchItem
import com.everytrip.app.feature.region.data.model.ContentDetail
import com.everytrip.app.feature.region.data.repository.RegionRepositoryImpl
import retrofit2.http.GET
import retrofit2.http.Query

internal interface ArtworkSearchService {
    @GET("search/works")
    suspend fun search(@Query("q") query: String): WorkSearchResponse
}

internal data class WorkSearchResponse(
    val query: String,
    val total: Int,
    val results: List<WorkSearchResult>,
)

internal data class WorkSearchResult(
    val productId: Int?,
    val workTitle: String,
    val workYear: String?,
    val tmdbType: String?,
    val genreNames: String?,
    val posterUrl: String?,
)

data class ArtworkSearchResponse(
    val total: Int,
    val items: List<ArtworkSearchItem>,
)

class ArtworkRepository(
    private val regionRepository: RegionRepositoryImpl = RegionRepositoryImpl(),
) {
    suspend fun getArtworkDetail(productId: Int): ContentDetail =
        regionRepository.getContentDetail(productId)

    suspend fun searchArtworks(query: String): ArtworkSearchResponse {
        val response = NetworkProvider.create(
            ArtworkSearchService::class.java,
            BuildConfig.CHAT_BASE_URL,
        ).search(query)
        return response.toArtworkSearchResponse()
    }
}

internal fun WorkSearchResponse.toArtworkSearchResponse(): ArtworkSearchResponse = ArtworkSearchResponse(
    total = total,
    items = results.map { result ->
        ArtworkSearchItem(
            productId = result.productId,
            title = result.workTitle,
            category = result.tmdbType.orEmpty(),
            year = result.workYear,
            genres = result.genreNames,
            posterUrl = result.posterUrl,
        )
    },
)
