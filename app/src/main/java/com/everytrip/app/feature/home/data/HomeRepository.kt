package com.everytrip.app.feature.home.data

import com.everytrip.app.core.network.NetworkProvider
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.HttpException

class HomeRepository(
    private val service: HomeService = NetworkProvider.create(HomeService::class.java),
) {
    suspend fun getHome(): HomeData = try {
        service.getHome().toHomeData()
    } catch (error: HttpException) {
        val body = error.response()?.errorBody()?.string().orEmpty()
        throw IllegalStateException("Home API request failed. code=${error.code()} body=$body", error)
    }

    private fun JsonElement.toHomeData(): HomeData {
        val response = takeIf(JsonElement::isJsonObject)?.asJsonObject ?: JsonObject()
        val root = response.objectOrNull("data") ?: response
        return HomeData(
            popularProducts = root.array("popular_products").take(10).mapNotNull { element ->
                val item = element.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return@mapNotNull null
                val detailPath = item.stringOrNull("detail_path") ?: return@mapNotNull null
                HomeProduct(
                    title = item.stringOrNull("title", "name").orEmpty(),
                    posterUrl = item.stringOrNull("poster_url", "thumbnail_url", "image_url"),
                    detailPath = detailPath,
                )
            },
            popularTourismPlaces = root.array("popular_tourism_places").take(10).mapNotNull { element ->
                val item = element.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return@mapNotNull null
                val detailPath = item.stringOrNull("detail_path") ?: return@mapNotNull null
                HomeTourismPlace(
                    name = item.stringOrNull("name", "title").orEmpty(),
                    thumbnailUrl = item.stringOrNull("thumbnail_url", "image_url", "first_image"),
                    detailPath = detailPath,
                )
            },
        )
    }

    private fun JsonObject.objectOrNull(name: String): JsonObject? =
        get(name)?.takeIf(JsonElement::isJsonObject)?.asJsonObject

    private fun JsonObject.array(name: String): List<JsonElement> =
        get(name)?.takeIf(JsonElement::isJsonArray)?.asJsonArray?.toList().orEmpty()

    private fun JsonObject.stringOrNull(vararg names: String): String? =
        names.firstNotNullOfOrNull { name ->
            runCatching { get(name)?.takeUnless(JsonElement::isJsonNull)?.asString }
                .getOrNull()
                ?.takeIf(String::isNotBlank)
        }
}
