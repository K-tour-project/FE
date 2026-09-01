package com.everytrip.app.feature.region.data.repository

import com.everytrip.app.feature.region.data.model.RegionBoundary
import com.everytrip.app.feature.region.data.model.RegionOption
import com.everytrip.app.feature.region.data.model.Sido

interface RegionRepository {
    suspend fun getSidos(): List<Sido>
    suspend fun getSigungus(sidoId: Int): List<RegionOption>
    suspend fun getBoundary(regionId: Int): RegionBoundary
}
