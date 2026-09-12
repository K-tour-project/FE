package com.everytrip.app.feature.mypage.data

import android.content.Context
import com.everytrip.app.core.network.AuthToken
import com.everytrip.app.core.network.NetworkProvider
import com.everytrip.app.feature.auth.data.remote.AuthHttpException
import com.everytrip.app.feature.auth.data.remote.AuthSessionManager
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.HttpException

class FavoriteRepository(context: Context) {
    private val service = NetworkProvider.create(FavoriteService::class.java)
    private val session = AuthSessionManager.get(context)

    suspend fun setPlace(id: Int, saved: Boolean) = authenticated {
        if (saved) service.favoritePlace(id, it) else service.unfavoritePlace(id, it)
    }.toMutation()

    suspend fun setTourism(id: String, saved: Boolean) = authenticated {
        if (saved) service.favoriteTourism(id, it) else service.unfavoriteTourism(id, it)
    }.toMutation()

    suspend fun deleteFavorite(id: Long) = authenticated { service.deleteFavorite(id, it) }.toMutation()

    suspend fun setProduct(id: Int, saved: Boolean) = authenticated {
        if (saved) service.saveProduct(id, it) else service.unsaveProduct(id, it)
    }.toMutation()

    suspend fun getMyPage(limit: Int = 20, offset: Int = 0): MyPageData =
        authenticated { service.myPage(limit, offset, it) }.toMyPage()

    suspend fun getSavedProducts(limit: Int = 20, offset: Int = 0): List<SavedProductData> =
        authenticated { service.savedProducts(limit, offset, it) }.items("saved_products", "items")
            .mapNotNull { it.asObjectOrNull()?.toSavedProduct() }

    private suspend fun authenticated(block: suspend (AuthToken) -> JsonElement): JsonElement =
        session.executeAuthenticated { token ->
            try {
                block(AuthToken(token))
            } catch (error: HttpException) {
                throw AuthHttpException(error.code(), error.response()?.errorBody()?.string().orEmpty())
            }
        }

    private fun JsonElement.toMutation(): FavoriteMutation {
        val value = asObjectOrNull() ?: JsonObject()
        return FavoriteMutation(
            isSaved = value.bool("is_saved"),
            favoriteId = value.longOrNull("favorite_id"),
            favoritePlaceCount = value.int("favorite_place_count"),
            savedProductCount = value.int("saved_product_count"),
        )
    }

    private fun JsonElement.toMyPage(): MyPageData {
        val root = asObjectOrNull() ?: JsonObject()
        val profile = root.obj("user") ?: root.obj("profile") ?: root
        return MyPageData(
            nickname = profile.string("nickname"),
            profileImageUrl = profile.stringOrNull("profile_image_url"),
            favoritePlaceCount = root.int("favorite_place_count", "favorite_count"),
            savedProductCount = root.int("saved_product_count"),
            favoritePlaces = root.items("favorite_places", "favorites", "items")
                .mapNotNull { it.asObjectOrNull()?.toFavoritePlace() },
        )
    }

    private fun JsonObject.toFavoritePlace() = FavoritePlaceData(
        favoriteId = longOrNull("favorite_id") ?: return null,
        placeId = intOrNull("place_id"),
        contentId = stringOrNull("content_id"),
        name = string("name", "place_name", "title"),
        description = string("overview", "description", "scene_desc"),
        address = string("address", "road_address"),
        imageUrl = stringOrNull("thumbnail_url", "image_url", "poster_url"),
        detailPath = string("detail_path"),
    )

    private fun JsonObject.toSavedProduct() = SavedProductData(
        productId = intOrNull("product_id") ?: return null,
        title = string("title"),
        category = string("category"),
        year = string("first_air_date", "year").take(4),
        overview = string("overview"),
        locationSummary = string("filming_location_summary", "place_summary", "locations"),
        posterUrl = stringOrNull("poster_url"),
        detailPath = string("detail_path"),
    )

    private fun JsonElement.items(vararg names: String): JsonArray {
        if (isJsonArray) return asJsonArray
        val root = asObjectOrNull() ?: return JsonArray()
        return names.firstNotNullOfOrNull { root.get(it)?.takeIf(JsonElement::isJsonArray)?.asJsonArray }
            ?: JsonArray()
    }

    private fun JsonElement.asObjectOrNull() = takeIf(JsonElement::isJsonObject)?.asJsonObject
    private fun JsonObject.obj(name: String) = get(name)?.asObjectOrNull()
    private fun JsonObject.string(vararg names: String) = stringOrNull(*names).orEmpty()
    private fun JsonObject.stringOrNull(vararg names: String) = names.firstNotNullOfOrNull { name ->
        get(name)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString?.takeIf(String::isNotBlank)
    }
    private fun JsonObject.int(vararg names: String) = names.firstNotNullOfOrNull(::intOrNull) ?: 0
    private fun JsonObject.intOrNull(name: String) = runCatching { get(name)?.takeUnless(JsonElement::isJsonNull)?.asInt }.getOrNull()
    private fun JsonObject.longOrNull(name: String) = runCatching { get(name)?.takeUnless(JsonElement::isJsonNull)?.asLong }.getOrNull()
    private fun JsonObject.bool(name: String) = runCatching { get(name)?.asBoolean }.getOrDefault(false)
}
