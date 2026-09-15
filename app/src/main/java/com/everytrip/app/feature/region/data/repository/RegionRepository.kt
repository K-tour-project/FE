package com.everytrip.app.feature.region.data.repository

import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.model.TourismPage
import com.everytrip.app.feature.region.data.model.TourismDetail
import com.everytrip.app.feature.region.data.model.PlaceDetail
import com.everytrip.app.feature.region.data.model.ContentDetail
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.everytrip.app.feature.region.data.model.TourismPlace
import com.everytrip.app.feature.region.data.paging.TourismPlacePagingSource
import kotlinx.coroutines.flow.Flow

interface RegionRepository {
    suspend fun getSidos(): List<Sido>
    suspend fun getSigungus(sidoId: Int): List<RegionOption>
    suspend fun getBoundary(regionId: Int): RegionBoundary
    suspend fun getTourismPlaces(
        regionId: Int,
        page: Int,
        size: Int = 20,
    ): TourismPage
    fun getTourismPlacesPaged(
        regionId: Int,
        onTotalChanged: (Int) -> Unit = {},
    ): Flow<PagingData<TourismPlace>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 20,
            prefetchDistance = 3,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { TourismPlacePagingSource(this, regionId, onTotalChanged) },
    ).flow
    suspend fun getTourismDetail(contentId: String): TourismDetail
    suspend fun getPlaceDetail(placeId: Int): PlaceDetail =
        throw UnsupportedOperationException("Place detail is not implemented")
    suspend fun getContentDetail(productId: Int): ContentDetail =
        throw UnsupportedOperationException("Content detail is not implemented")
}
