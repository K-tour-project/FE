package com.everytrip.app.feature.region.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.everytrip.app.feature.region.data.model.TourismPlace
import com.everytrip.app.feature.region.data.repository.RegionRepository

class TourismPlacePagingSource(
    private val repository: RegionRepository,
    private val regionId: Int,
    private val onTotalChanged: (Int) -> Unit = {},
) : PagingSource<Int, TourismPlace>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TourismPlace> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = repository.getTourismPlaces(regionId, page, params.loadSize)
            onTotalChanged(response.total)
            LoadResult.Page(
                data = response.items,
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (response.hasNext) page + 1 else null,
            )
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TourismPlace>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
