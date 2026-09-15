package com.everytrip.app.feature.mypage.data

import androidx.paging.PagingSource
import androidx.paging.PagingState

internal abstract class OffsetPagingSource<T : Any> : PagingSource<Int, T>() {
    protected abstract suspend fun request(limit: Int, offset: Int): PageResult<T>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val offset = params.key ?: 0
        val limit = params.loadSize
        return try {
            val response = request(limit, offset)
            val nextOffset = offset + response.items.size
            val reachedEnd = response.items.isEmpty() ||
                response.total?.let { nextOffset >= it } == true ||
                (response.total == null && response.items.size < limit)
            LoadResult.Page(
                data = response.items,
                prevKey = if (offset == 0) null else maxOf(0, offset - limit),
                nextKey = if (reachedEnd) null else nextOffset,
            )
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.let { it + state.config.pageSize }
            ?: page.nextKey?.let { maxOf(0, it - state.config.pageSize) }
    }
}

internal class FavoritePlacePagingSource(
    private val repository: FavoriteRepository,
) : OffsetPagingSource<FavoritePlaceData>() {
    override suspend fun request(limit: Int, offset: Int) =
        repository.getFavoritePlacesPage(limit, offset)
}

internal class SavedProductPagingSource(
    private val repository: FavoriteRepository,
) : OffsetPagingSource<SavedProductData>() {
    override suspend fun request(limit: Int, offset: Int) =
        repository.getSavedProductsPage(limit, offset)
}
