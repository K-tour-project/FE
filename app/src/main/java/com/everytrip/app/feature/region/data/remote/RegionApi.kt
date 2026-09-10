package com.everytrip.app.feature.region.data.remote

import com.everytrip.app.core.network.NetworkProvider
import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.model.TourismDetail
import com.everytrip.app.feature.region.data.model.TourismPage
import retrofit2.HttpException

class RegionApi private constructor(
    private val service: RegionService,
) {
    constructor() : this(NetworkProvider.create(RegionService::class.java))

    suspend fun getSidos(): List<Sido> = execute {
        RegionResponseMapper.toSidos(service.getSidos())
    }

    suspend fun getSigungus(sidoId: Int): List<RegionOption> = execute {
        RegionResponseMapper.toSigungus(service.getSigungus(sidoId))
    }

    suspend fun getBoundary(regionId: Int): RegionBoundary = execute {
        RegionResponseMapper.toBoundary(service.getBoundary(regionId))
    }

    suspend fun getTourismPlaces(regionId: Int, page: Int, size: Int): TourismPage = execute {
        RegionResponseMapper.toTourismPage(
            service.getTourismPlaces(regionId, page, size),
        )
    }

    suspend fun getTourismDetail(contentId: String): TourismDetail {
        require(contentId.isNotBlank())
        return execute {
            RegionResponseMapper.toTourismDetail(service.getTourismDetail(contentId))
        }
    }

    private suspend fun <T> execute(request: suspend () -> T): T {
        return try {
            request()
        } catch (error: HttpException) {
            val responseBody = error.response()?.errorBody()?.string().orEmpty()
            throw IllegalStateException(
                "Region API request failed. code=${error.code()} body=$responseBody",
                error,
            )
        }
    }
}
