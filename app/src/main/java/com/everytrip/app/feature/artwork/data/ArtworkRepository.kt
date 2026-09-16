package com.everytrip.app.feature.artwork.data

import com.everytrip.app.feature.region.data.model.ContentDetail
import com.everytrip.app.feature.region.data.repository.RegionRepositoryImpl
import com.everytrip.app.core.network.NetworkProvider
import com.everytrip.app.feature.artwork.presentation.search.ArtworkSearchItem
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

internal interface ArtworkSearchService {
    @GET("contents")
    suspend fun search(@Query("search") query: String): JsonElement
}

/** 작품 데이터를 화면의 진입 경로와 무관하게 제공하는 단일 진입점입니다. */
class ArtworkRepository(
    private val regionRepository: RegionRepositoryImpl = RegionRepositoryImpl(),
) {
    suspend fun getArtworkDetail(productId: Int): ContentDetail =
        regionRepository.getContentDetail(productId)

    suspend fun searchArtworks(query: String): List<ArtworkSearchItem> {
        val response = NetworkProvider.create(ArtworkSearchService::class.java).search(query)
        val root = response.asObjectOrNull() ?: return emptyList()
        val data = root.get("data") ?: response
        val payload = data.asObjectOrNull()
        val array = when {
            data.isJsonArray -> data.asJsonArray
            else -> sequenceOf("items", "results", "contents", "products")
                .mapNotNull { payload?.get(it)?.takeIf(JsonElement::isJsonArray)?.asJsonArray }
                .firstOrNull()
        } ?: return emptyList()
        return array.mapNotNull { element ->
            val item = element.asObjectOrNull() ?: return@mapNotNull null
            val id = item.get("product_id")?.intOrNull()
                ?: item.get("id")?.intOrNull() ?: return@mapNotNull null
            val title = item.string("title", "name") ?: return@mapNotNull null
            ArtworkSearchItem(
                productId = id,
                title = title,
                category = item.string("category") ?: "",
                posterUrl = item.string("poster_url", "thumbnail_url"),
                year = item.string("first_air_date", "release_date")?.take(4)
                    ?: item.string("year"),
                genres = item.string("genres")?.replace("|", ", "),
            )
        }
    }
}

private fun JsonElement.asObjectOrNull(): JsonObject? =
    takeIf(JsonElement::isJsonObject)?.asJsonObject

private fun JsonElement.intOrNull(): Int? = runCatching { asInt }.getOrNull()

private fun JsonObject.string(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        runCatching { get(key)?.takeUnless(JsonElement::isJsonNull)?.asString }
            .getOrNull()?.takeIf(String::isNotBlank)
    }
