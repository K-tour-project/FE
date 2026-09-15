package com.everytrip.app.feature.artwork.data

import com.everytrip.app.feature.region.data.model.ContentDetail
import com.everytrip.app.feature.region.data.repository.RegionRepositoryImpl

/** 작품 데이터를 화면의 진입 경로와 무관하게 제공하는 단일 진입점입니다. */
class ArtworkRepository(
    private val regionRepository: RegionRepositoryImpl = RegionRepositoryImpl(),
) {
    suspend fun getArtworkDetail(productId: Int): ContentDetail =
        regionRepository.getContentDetail(productId)
}
