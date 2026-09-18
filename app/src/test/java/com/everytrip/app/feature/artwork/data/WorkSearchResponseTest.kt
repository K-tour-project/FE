package com.everytrip.app.feature.artwork.data

import com.everytrip.app.core.network.NetworkProvider
import com.everytrip.app.feature.artwork.presentation.search.ArtworkSearchItem
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkSearchResponseTest {
    @Test
    fun searchResultsIncludeWorksWithoutProductIds() {
        val response = WorkSearchResponse(
            query = "왕",
            total = 3,
            results = listOf(
                WorkSearchResult(null, "첫 번째 작품", null, "movie", null, null),
                WorkSearchResult(null, "두 번째 작품", null, "movie", null, null),
                WorkSearchResult(123, "상세가 있는 작품", null, "drama", null, null),
            ),
        )

        val result = response.toArtworkSearchResponse()

        assertEquals(3, result.total)
        assertEquals(listOf("첫 번째 작품", "두 번째 작품", "상세가 있는 작품"), result.items.map { it.title })
        assertEquals(listOf(null, null, 123), result.items.map { it.productId })
    }

    @Test
    fun searchResponseUsesBackendFieldNamesAndKoreanTypeLabel() {
        val json = """
            {
              "query": "눈물의 여왕",
              "total": 1,
              "results": [{
                "product_id": 123,
                "work_title": "눈물의 여왕",
                "work_year": "2024",
                "tmdb_id": "123",
                "tmdb_type": "drama",
                "genre_names": "드라마, 로맨스",
                "overview": "작품 줄거리",
                "vote_average": 8.4,
                "poster_url": "https://example.com/poster.jpg",
                "place_count": 15,
                "regions": ["서울특별시 영등포구", "경기도 고양시"]
              }]
            }
        """.trimIndent()

        val response = NetworkProvider.gson.fromJson(json, WorkSearchResponse::class.java)
        val work = response.results.single()
        val item = ArtworkSearchItem(
            productId = work.productId,
            title = work.workTitle,
            category = work.tmdbType.orEmpty(),
            year = work.workYear,
            genres = work.genreNames,
            posterUrl = work.posterUrl,
        )

        assertEquals(1, response.total)
        assertEquals(123, item.productId)
        assertEquals("눈물의 여왕", item.title)
        assertEquals("2024", item.year)
        assertEquals("https://example.com/poster.jpg", item.posterUrl)
        assertEquals("드라마", item.categoryLabel)
        assertEquals("드라마, 로맨스", item.genres)
    }
}
