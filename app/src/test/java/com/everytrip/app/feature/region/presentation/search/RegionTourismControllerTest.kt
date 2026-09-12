package com.everytrip.app.feature.region.presentation.search

import com.everytrip.app.feature.region.data.model.*
import com.everytrip.app.feature.region.data.repository.RegionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class RegionTourismControllerTest {
    private val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
    private val repository = FakeRepository()
    private val state = MutableStateFlow(RegionSearchUiState(selectedRegionId = 10))
    private val controller = RegionTourismController(repository, state, scope)

    @After fun tearDown() { scope.cancel() }

    @Test fun pagesAppendWithoutDuplicatesAndStopAtLastPage() {
        controller.loadMorePlaces()
        controller.loadMorePlaces()
        assertEquals(1, repository.pages.size)
        assertEquals(Triple(10, 1, 20), repository.pages[0].parameters)
        repository.pages[0].result.complete(TourismPage(listOf(place("1")), 3, 1, true))

        controller.loadMorePlaces()
        assertEquals(Triple(10, 2, 20), repository.pages[1].parameters)
        repository.pages[1].result.complete(TourismPage(listOf(place("1"), place("2")), 3, 2, false))
        assertEquals(listOf("1", "2"), state.value.places.map { it.contentId })
        assertNull(state.value.places[0].location)
        assertEquals(3, state.value.totalPlaces)
        controller.loadMorePlaces()
        assertEquals(2, repository.pages.size)
    }

    @Test fun regionChangeClearsPlacesAndIgnoresOldResponseEvenWhenReturningToSameRegion() {
        controller.loadMorePlaces()
        controller.resetPlaces()
        state.value = state.value.copy(selectedRegionId = 20)
        controller.loadMorePlaces()
        repository.pages[1].result.complete(TourismPage(listOf(place("20")), 1, 1, false))
        controller.selectPlace("20")
        controller.resetPlaces()
        assertTrue(state.value.places.isEmpty())
        assertNull(state.value.selectedContentId)
        assertEquals(0, state.value.totalPlaces)
        state.value = state.value.copy(selectedRegionId = 10)
        controller.loadMorePlaces()
        repository.pages[0].result.complete(TourismPage(listOf(place("old")), 1, 1, false))
        assertTrue(state.value.places.isEmpty())
        assertTrue(state.value.isLoadingPlaces)
        repository.details[0].second.complete(detail("20"))
        assertNull(state.value.placeDetail)
        repository.pages[2].result.complete(TourismPage(listOf(place("new")), 1, 1, false))
        assertEquals("new", state.value.places.single().contentId)
    }

    @Test fun failedNextPageKeepsPlacesAndRetryUsesSamePage() {
        controller.loadMorePlaces()
        repository.pages[0].result.complete(TourismPage(listOf(place("1")), 2, 1, true))
        controller.loadMorePlaces()
        repository.pages[1].result.completeExceptionally(IllegalStateException("offline"))
        assertEquals("1", state.value.places.single().contentId)
        assertEquals(1, state.value.placesPage)
        assertNotNull(state.value.placesErrorMessage)
        controller.loadMorePlaces()
        assertEquals(Triple(10, 2, 20), repository.pages[2].parameters)
        repository.pages[2].result.complete(TourismPage(listOf(place("2")), 2, 2, false))
        assertNull(state.value.placesErrorMessage)
        assertEquals(2, state.value.places.size)
    }

    @Test fun detailUsesListContentIdAndClosePreservesListWithoutRequest() {
        controller.loadMorePlaces()
        repository.pages[0].result.complete(TourismPage(listOf(place("000123")), 1, 1, false))
        controller.selectPlace("not-in-list")
        assertTrue(repository.details.isEmpty())
        controller.selectPlace("000123")
        assertEquals("000123", repository.details[0].first)
        repository.details[0].second.complete(detail("000123"))
        assertEquals("000123", state.value.placeDetail?.contentId)
        val before = state.value
        controller.closePlaceDetail()
        assertEquals(before.places, state.value.places)
        assertEquals(before.selectedRegionId, state.value.selectedRegionId)
        assertEquals(before.placesPage, state.value.placesPage)
        assertNull(state.value.placeDetail)
        assertNull(state.value.selectedContentId)
        assertEquals(1, repository.pages.size)
        assertEquals(1, repository.details.size)
    }

    @Test fun filmingPlaceUsesFirstPlaceIdInsteadOfContentId() {
        state.value = state.value.copy(
            places = listOf(TourismPlace("126121", "촬영지", null, null, null, null,
                "촬영지", placeIds = listOf(321, 322))),
        )

        controller.selectPlace("126121")

        assertEquals(listOf(321), repository.placeDetailIds)
        assertTrue(repository.details.isEmpty())
        assertEquals(321, state.value.selectedPlaceId)
    }

    @Test fun oldDetailCannotReplaceNewSelectionOrReopenClosedPanel() {
        controller.loadMorePlaces()
        repository.pages[0].result.complete(TourismPage(listOf(place("1"), place("2")), 2, 1, false))
        controller.selectPlace("1")
        controller.selectPlace("2")
        repository.details[0].second.complete(detail("1"))
        assertEquals("2", state.value.selectedContentId)
        assertNull(state.value.placeDetail)
        assertTrue(state.value.isLoadingDetail)
        controller.closePlaceDetail()
        repository.details[1].second.complete(detail("2"))
        assertNull(state.value.selectedContentId)
        assertNull(state.value.placeDetail)
        assertFalse(state.value.isLoadingDetail)
    }

    @Test fun detailFailureCanBeRetriedWithoutReloadingList() {
        controller.loadMorePlaces()
        repository.pages[0].result.complete(TourismPage(listOf(place("1")), 1, 1, false))
        controller.selectPlace("1")
        repository.details[0].second.completeExceptionally(IllegalStateException("offline"))
        assertNotNull(state.value.detailErrorMessage)
        assertFalse(state.value.isLoadingDetail)
        controller.selectPlace("1")
        repository.details[1].second.complete(detail("1"))
        assertNotNull(state.value.placeDetail)
        assertNull(state.value.detailErrorMessage)
        assertEquals(1, repository.pages.size)
    }

    private fun place(id: String) = TourismPlace(id, "장소", null, null, "서울", null, "관광지")
    private fun detail(id: String) = TourismDetail(id, "장소", null, null, null, null, null, emptyList())

    private class PageRequest(val parameters: Triple<Int, Int, Int>) {
        val result = CompletableDeferred<TourismPage>()
    }

    private class FakeRepository : RegionRepository {
        val pages = mutableListOf<PageRequest>()
        val details = mutableListOf<Pair<String, CompletableDeferred<TourismDetail>>>()
        val placeDetailIds = mutableListOf<Int>()
        override suspend fun getTourismPlaces(
            regionId: Int,
            page: Int,
            size: Int,
            productId: Int?,
        ): TourismPage {
            val request = PageRequest(Triple(regionId, page, size))
            pages += request
            return request.result.await()
        }
        override suspend fun getTourismDetail(contentId: String): TourismDetail {
            val response = CompletableDeferred<TourismDetail>()
            details += contentId to response
            return response.await()
        }
        override suspend fun getPlaceDetail(placeId: Int): PlaceDetail {
            placeDetailIds += placeId
            return PlaceDetail(placeId, "촬영지", RegionLocation(37.0, 127.0), null,
                null, emptyMap(), emptyList(), null, emptyList())
        }
        override suspend fun getSidos(): List<Sido> = error("Unexpected request")
        override suspend fun getSigungus(sidoId: Int): List<RegionOption> = error("Unexpected request")
        override suspend fun getBoundary(regionId: Int): RegionBoundary = error("Unexpected request")
    }
}
