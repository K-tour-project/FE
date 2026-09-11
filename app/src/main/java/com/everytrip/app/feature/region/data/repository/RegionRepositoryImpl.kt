package com.everytrip.app.feature.region.data.repository

import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.model.TourismPage
import com.everytrip.app.feature.region.data.model.TourismDetail
import com.everytrip.app.feature.region.data.model.PlaceDetail
import com.everytrip.app.feature.region.data.model.ContentDetail
import com.everytrip.app.feature.region.data.remote.RegionApi

class RegionRepositoryImpl(
    private val regionApi: RegionApi = RegionApi(),
) : RegionRepository {
    override suspend fun getTourismPlaces(regionId: Int, page: Int, size: Int): TourismPage =
        regionApi.getTourismPlaces(regionId, page, size)

    override suspend fun getTourismDetail(contentId: String): TourismDetail =
        regionApi.getTourismDetail(contentId)

    override suspend fun getPlaceDetail(placeId: Int): PlaceDetail = regionApi.getPlaceDetail(placeId)

    override suspend fun getContentDetail(productId: Int): ContentDetail =
        regionApi.getContentDetail(productId)

    override suspend fun getSidos(): List<Sido> = regionApi.getSidos()

    override suspend fun getSigungus(sidoId: Int): List<RegionOption> = regionApi.getSigungus(sidoId)

    override suspend fun getBoundary(regionId: Int): RegionBoundary = regionApi.getBoundary(regionId)
}
