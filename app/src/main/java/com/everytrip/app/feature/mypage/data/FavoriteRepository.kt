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

    suspend fun getMyPage(limit: Int = 20, offset: Int = 0): MyPageData {
        validatePage(limit, offset)
        return authenticated { service.myPage(limit, offset, it) }.toMyPage()
    }

    suspend fun getFavoritePlaces(limit: Int = 20, offset: Int = 0): List<FavoritePlaceData> {
        validatePage(limit, offset)
        return authenticated { service.favoritePlaces(limit, offset, it) }.items("favorite_places", "items")
            .mapNotNull { it.asObjectOrNull()?.toFavoritePlace() }
    }

    suspend fun getSavedProducts(limit: Int = 20, offset: Int = 0): List<SavedProductData> {
        validatePage(limit, offset)
        return authenticated { service.savedProducts(limit, offset, it) }.items("saved_products", "items")
            .mapNotNull { it.asObjectOrNull()?.toSavedProduct() }
    }

    suspend fun updateProfileImage(profileImageUrl: String?) = authenticated {
        service.updateProfile(ProfileImageRequest(profileImageUrl), it)
    }

    private suspend fun <T> authenticated(block: suspend (AuthToken) -> T): T =
        session.executeAuthenticated { token ->
            try {
                block(AuthToken(token))
            } catch (error: HttpException) {
                throw AuthHttpException(error.code(), error.response()?.errorBody()?.string().orEmpty())
            }
        }

    private fun validatePage(limit: Int, offset: Int) {
        require(limit in 1..50) { "limit must be between 1 and 50." }
        require(offset >= 0) { "offset must be at least 0." }
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
            email = profile.string("email"),
            profileImageUrl = profile.stringOrNull("profile_image_url"),
            favoritePlaceCount = root.obj("counts")?.int("favorite_place_count")
                ?: root.int("favorite_place_count", "favorite_count"),
            savedProductCount = root.obj("counts")?.int("saved_product_count")
                ?: root.int("saved_product_count"),
            favoritePlaces = root.items("favorite_places", "favorites", "items")
                .mapNotNull { it.asObjectOrNull()?.toFavoritePlace() },
        )
    }

    private fun JsonObject.toFavoritePlace(): FavoritePlaceData? {
        val value = obj("place") ?: obj("tourism") ?: obj("favorite_place") ?: this
        return FavoritePlaceData(
            favoriteId = longOrNull("favorite_id") ?: return null,
            placeId = intOrNull("place_id") ?: value.intOrNull("place_id") ?: value.intOrNull("id"),
            contentId = stringOrNull("content_id") ?: value.stringOrNull("content_id"),
            name = value.string("name", "place_name", "title"),
            description = value.string("overview", "description", "scene_desc"),
            address = value.string("address", "road_address", "addr1"),
            imageUrl = value.stringOrNull("thumbnail_url", "image_url", "poster_url", "first_image"),
            detailPath = value.string("detail_path"),
        )
    }

    private fun JsonObject.toSavedProduct(): SavedProductData? {
        val value = obj("product") ?: obj("content") ?: this
        return SavedProductData(
            productId = intOrNull("product_id") ?: value.intOrNull("product_id")
                ?: value.intOrNull("id") ?: return null,
            title = value.string("title"),
            category = value.string("category"),
            year = value.string("first_air_date", "release_date", "year").take(4),
            overview = value.string("overview"),
            locationSummary = value.string("filming_location_summary", "place_summary", "locations"),
            posterUrl = value.stringOrNull("poster_url"),
            detailPath = value.string("detail_path"),
        )
    }

    private fun JsonElement.items(vararg names: String): JsonArray {
        if (isJsonArray) return asJsonArray
        val root = asObjectOrNull() ?: return JsonArray()
        return names.firstNotNullOfOrNull { name ->
            root.get(name)?.let { value ->
                when {
                    value.isJsonArray -> value.asJsonArray
                    value.isJsonObject -> value.asJsonObject.get("items")
                        ?.takeIf(JsonElement::isJsonArray)?.asJsonArray
                    else -> null
                }
            }
        }
            ?: JsonArray()
    }

    private fun JsonElement.asObjectOrNull() = takeIf(JsonElement::isJsonObject)?.asJsonObject
    private fun JsonObject.obj(name: String) = get(name)?.asObjectOrNull()
    private fun JsonObject.string(vararg names: String) = stringOrNull(*names).orEmpty()
    private fun JsonObject.stringOrNull(vararg names: String) = names.firstNotNullOfOrNull { name ->
        get(name)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString?.takeIf(String::isNotBlank)
    }
    private fun JsonObject.int(vararg names: String) = names.firstNotNullOfOrNull { intOrNull(it) } ?: 0
    private fun JsonObject.intOrNull(name: String) = runCatching { get(name)?.takeUnless(JsonElement::isJsonNull)?.asInt }.getOrNull()
    private fun JsonObject.longOrNull(name: String) = runCatching { get(name)?.takeUnless(JsonElement::isJsonNull)?.asLong }.getOrNull()
    private fun JsonObject.bool(name: String) = runCatching { get(name)?.asBoolean }.getOrNull() ?: false
}
