package com.everytrip.app.feature.mypage.data

import com.everytrip.app.core.network.AuthToken
import com.google.gson.JsonElement
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

internal interface FavoriteService {
    @PUT("me/favorites/places/{placeId}") suspend fun favoritePlace(@Path("placeId") id: Int, @Tag token: AuthToken): JsonElement
    @DELETE("me/favorites/places/{placeId}") suspend fun unfavoritePlace(@Path("placeId") id: Int, @Tag token: AuthToken): JsonElement
    @PUT("me/favorites/tourism/{contentId}") suspend fun favoriteTourism(@Path("contentId") id: String, @Tag token: AuthToken): JsonElement
    @DELETE("me/favorites/tourism/{contentId}") suspend fun unfavoriteTourism(@Path("contentId") id: String, @Tag token: AuthToken): JsonElement
    @DELETE("me/favorite-places/{favoriteId}") suspend fun deleteFavorite(@Path("favoriteId") id: Long, @Tag token: AuthToken): JsonElement
    @PUT("me/saved-products/{productId}") suspend fun saveProduct(@Path("productId") id: Int, @Tag token: AuthToken): JsonElement
    @DELETE("me/saved-products/{productId}") suspend fun unsaveProduct(@Path("productId") id: Int, @Tag token: AuthToken): JsonElement
    @GET("me/saved-products") suspend fun savedProducts(@Query("limit") limit: Int, @Query("offset") offset: Int, @Tag token: AuthToken): JsonElement
    @GET("me/mypage") suspend fun myPage(@Query("limit") limit: Int, @Query("offset") offset: Int, @Tag token: AuthToken): JsonElement
}
