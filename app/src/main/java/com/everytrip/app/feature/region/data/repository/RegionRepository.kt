package com.everytrip.app.feature.region.data.repository

import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.model.TourismPage
import com.everytrip.app.feature.region.data.model.TourismDetail

interface RegionRepository {
    suspend fun getSidos(): List<Sido>
    suspend fun getSigungus(sidoId: Int): List<RegionOption>
    suspend fun getBoundary(regionId: Int): RegionBoundary
    suspend fun getTourismPlaces(regionId: Int, page: Int, size: Int = 20): TourismPage
    suspend fun getTourismDetail(contentId: String): TourismDetail
}
