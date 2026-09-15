package com.everytrip.app.feature.home.data

import com.google.gson.JsonElement
import retrofit2.http.GET

interface HomeService {
    @GET("home")
    suspend fun getHome(): JsonElement
}
