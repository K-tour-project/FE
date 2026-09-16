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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonParser
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

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

    fun favoritePlacesPaged(): Flow<PagingData<FavoritePlaceData>> = Pager(
        config = pagingConfig(),
        pagingSourceFactory = { FavoritePlacePagingSource(this) },
    ).flow

    fun savedProductsPaged(): Flow<PagingData<SavedProductData>> = Pager(
        config = pagingConfig(),
        pagingSourceFactory = { SavedProductPagingSource(this) },
    ).flow

    suspend fun getMyPage(limit: Int = 20, offset: Int = 0): MyPageData {
        validatePage(limit, offset)
        return authenticated { service.myPage(limit, offset, it) }.toMyPage()
    }

    suspend fun getFavoritePlaces(limit: Int = 20, offset: Int = 0): List<FavoritePlaceData> =
        getFavoritePlacesPage(limit, offset).items

    suspend fun getFavoritePlacesPage(limit: Int = 20, offset: Int = 0): PageResult<FavoritePlaceData> {
        validatePage(limit, offset)
        val response = authenticated { service.favoritePlaces(limit, offset, it) }
        return PageResult(
            items = response.items("favorite_places", "items")
                .mapNotNull { it.asObjectOrNull()?.toFavoritePlace() },
            total = response.pageTotal("favorite_places"),
        )
    }

    suspend fun getSavedProducts(limit: Int = 20, offset: Int = 0): List<SavedProductData> =
        getSavedProductsPage(limit, offset).items

    suspend fun getSavedProductsPage(limit: Int = 20, offset: Int = 0): PageResult<SavedProductData> {
        validatePage(limit, offset)
        val response = authenticated { service.savedProducts(limit, offset, it) }
        return PageResult(
            items = response.items("saved_products", "items")
                .mapNotNull { it.asObjectOrNull()?.toSavedProduct() },
            total = response.pageTotal("saved_products"),
        )
    }

    suspend fun updateNickname(nickname: String): UpdatedProfile = authenticated {
        service.updateNickname(NicknameRequest(nickname.trim()), it)
    }.toUpdatedProfile()

    suspend fun changePassword(currentPassword: String, newPassword: String) = authenticated {
        service.changePassword(PasswordChangeRequest(currentPassword, newPassword), it)
    }

    suspend fun deleteAccount() = authenticated { service.deleteAccount(it) }

    suspend fun updateProfileImage(bytes: ByteArray, mimeType: String, fileName: String): UpdatedProfile {
        val body = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("profile_image", fileName, body)
        return authenticated { service.updateProfileImage(part, it) }.toUpdatedProfile()
    }

    suspend fun removeProfileImage(): UpdatedProfile = authenticated {
        service.removeProfileImage("true".toRequestBody("text/plain".toMediaType()), it)
    }.toUpdatedProfile()

    private suspend fun <T> authenticated(block: suspend (AuthToken) -> T): T =
        session.executeAuthenticated { token ->
            try {
                block(AuthToken(token))
            } catch (error: HttpException) {
                val body = error.response()?.errorBody()?.string().orEmpty()
                val detail = runCatching {
                    JsonParser.parseString(body).asJsonObject.get("detail")?.asString
                }.getOrNull() ?: body
                throw AuthHttpException(error.code(), detail)
            }
        }

    private fun validatePage(limit: Int, offset: Int) {
        require(limit in 1..50) { "limit must be between 1 and 50." }
        require(offset >= 0) { "offset must be at least 0." }
    }

    private fun pagingConfig() = PagingConfig(
        pageSize = 20,
        initialLoadSize = 20,
        prefetchDistance = 3,
        enablePlaceholders = false,
    )

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
            userId = profile.intOrNull("user_id"),
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

    private fun JsonElement.toUpdatedProfile(): UpdatedProfile {
        val root = asObjectOrNull() ?: error("Profile response is not an object")
        val profile = root.obj("user") ?: root.obj("profile") ?: root
        return UpdatedProfile(
            userId = profile.intOrNull("user_id") ?: error("Missing user_id"),
            nickname = profile.stringOrNull("nickname") ?: error("Missing nickname"),
            email = profile.stringOrNull("email") ?: error("Missing email"),
            profileImageUrl = profile.stringOrNull("profile_image_url"),
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

    private fun JsonElement.pageTotal(containerName: String): Int? {
        val root = asObjectOrNull() ?: return null
        return root.intOrNull("total")
            ?: root.obj(containerName)?.intOrNull("total")
            ?: root.obj("counts")?.intOrNull(
                if (containerName == "favorite_places") "favorite_place_count"
                else "saved_product_count",
            )
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
