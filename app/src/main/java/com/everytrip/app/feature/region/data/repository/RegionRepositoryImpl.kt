package com.everytrip.app.feature.region.data.repository

import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.Sido
import com.everytrip.app.feature.region.data.remote.RegionApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegionRepositoryImpl(
    private val regionApi: RegionApi = RegionApi(),
) : RegionRepository {
    override suspend fun getSidos(): List<Sido> = withContext(Dispatchers.IO) {
        regionApi.getSidos()
    }

    override suspend fun getSigungus(sidoId: Int): List<RegionOption> = withContext(Dispatchers.IO) {
        regionApi.getSigungus(sidoId)
    }

    override suspend fun getBoundary(regionId: Int): RegionBoundary = withContext(Dispatchers.IO) {
        regionApi.getBoundary(regionId)
    }
}
