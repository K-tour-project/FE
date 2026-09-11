package com.everytrip.app.feature.region.data.remote

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface RegionService {
    @GET("regions/sidos")
    suspend fun getSidos(): JsonElement

    @GET("regions/{sidoId}/children")
    suspend fun getSigungus(@Path("sidoId") sidoId: Int): JsonElement

    @GET("regions/{regionId}/boundary")
    suspend fun getBoundary(@Path("regionId") regionId: Int): JsonElement

    @GET("regions/{regionId}/tourism-places")
    suspend fun getTourismPlaces(
        @Path("regionId") regionId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): JsonElement

    @GET("tourism-places/{contentId}")
    suspend fun getTourismDetail(@Path("contentId") contentId: String): JsonElement

    @GET("places/{placeId}")
    suspend fun getPlaceDetail(@Path("placeId") placeId: Int): JsonElement

    @GET("contents/{productId}")
    suspend fun getContentDetail(@Path("productId") productId: Int): JsonElement
}
