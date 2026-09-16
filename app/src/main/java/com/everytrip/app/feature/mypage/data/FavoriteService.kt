package com.everytrip.app.feature.mypage.data

import com.everytrip.app.core.network.AuthToken
import com.google.gson.JsonElement
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody

internal interface FavoriteService {
    @PUT("me/favorites/places/{placeId}") suspend fun favoritePlace(@Path("placeId") id: Int, @Tag token: AuthToken): JsonElement
    @DELETE("me/favorites/places/{placeId}") suspend fun unfavoritePlace(@Path("placeId") id: Int, @Tag token: AuthToken): JsonElement
    @PUT("me/favorites/tourism/{contentId}") suspend fun favoriteTourism(@Path("contentId") id: String, @Tag token: AuthToken): JsonElement
    @DELETE("me/favorites/tourism/{contentId}") suspend fun unfavoriteTourism(@Path("contentId") id: String, @Tag token: AuthToken): JsonElement
    @DELETE("me/favorite-places/{favoriteId}") suspend fun deleteFavorite(@Path("favoriteId") id: Long, @Tag token: AuthToken): JsonElement
    @GET("me/favorite-places") suspend fun favoritePlaces(@Query("limit") limit: Int, @Query("offset") offset: Int, @Tag token: AuthToken): JsonElement
    @PUT("me/saved-products/{productId}") suspend fun saveProduct(@Path("productId") id: Int, @Tag token: AuthToken): JsonElement
    @DELETE("me/saved-products/{productId}") suspend fun unsaveProduct(@Path("productId") id: Int, @Tag token: AuthToken): JsonElement
    @GET("me/saved-products") suspend fun savedProducts(@Query("limit") limit: Int, @Query("offset") offset: Int, @Tag token: AuthToken): JsonElement
    @GET("me/mypage") suspend fun myPage(@Query("limit") limit: Int, @Query("offset") offset: Int, @Tag token: AuthToken): JsonElement
    @PATCH("me/nickname") suspend fun updateNickname(@Body request: NicknameRequest, @Tag token: AuthToken): JsonElement
    @PATCH("me/password") suspend fun changePassword(@Body request: PasswordChangeRequest, @Tag token: AuthToken): JsonElement
    @DELETE("me/account") suspend fun deleteAccount(@Tag token: AuthToken): JsonElement
    @Multipart
    @PATCH("me/profile")
    suspend fun updateProfileImage(@Part image: MultipartBody.Part, @Tag token: AuthToken): JsonElement
    @Multipart
    @PATCH("me/profile")
    suspend fun removeProfileImage(@Part("remove_image") removeImage: RequestBody, @Tag token: AuthToken): JsonElement
}
