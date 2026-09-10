package com.everytrip.app.feature.region.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.feature.region.data.model.TourismPlace
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.SecondaryText
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun RegionBottomSheet(
    state: RegionSearchUiState,
    isExpanded: Boolean,
    onPlaceClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalListState = rememberLazyListState()
    val verticalListState = rememberLazyListState()
    val activeListState = if (isExpanded) verticalListState else horizontalListState

    LaunchedEffect(state.selectedRegionId) {
        horizontalListState.scrollToItem(0)
        verticalListState.scrollToItem(0)
    }
    LoadMoreWhenListEnds(
        listState = activeListState,
        placeCount = state.places.size,
        hasNext = state.hasNextPlaces,
        isLoading = state.isLoadingPlaces,
        errorMessage = state.placesErrorMessage,
        onLoadMore = onLoadMore,
    )

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 3.dp),
            ) {
                Text(
                    text = listOfNotNull(state.selectedSido?.name, state.selectedSigungu?.name)
                        .joinToString(" "),
                    color = NavyText,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = buildAnnotatedString {
                        append("선택한 지역에서 총 ")
                        withStyle(SpanStyle(color = NavyText, fontWeight = FontWeight.Bold)) {
                            append("${state.totalPlaces}곳")
                        }
                        append("의 관광지를 찾았어요")
                    },
                    color = SecondaryText,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                )
            }

            when {
                state.places.isEmpty() && state.isLoadingPlaces -> LoadingContent()
                state.places.isEmpty() && state.placesErrorMessage != null -> ErrorContent(
                    message = state.placesErrorMessage,
                    onRetry = onLoadMore,
                )
                state.places.isEmpty() -> EmptyContent()
                isExpanded -> ExpandedPlaceList(
                    places = state.places,
                    listState = verticalListState,
                    isLoading = state.isLoadingPlaces,
                    errorMessage = state.placesErrorMessage,
                    hasNext = state.hasNextPlaces,
                    onPlaceClick = onPlaceClick,
                    onLoadMore = onLoadMore,
                    modifier = Modifier.weight(1f),
                )
                else -> CollapsedPlaceList(
                    places = state.places,
                    listState = horizontalListState,
                    isLoading = state.isLoadingPlaces,
                    errorMessage = state.placesErrorMessage,
                    hasNext = state.hasNextPlaces,
                    onPlaceClick = onPlaceClick,
                    onLoadMore = onLoadMore,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CollapsedPlaceList(
    places: List<TourismPlace>,
    listState: LazyListState,
    isLoading: Boolean,
    errorMessage: String?,
    hasNext: Boolean,
    onPlaceClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 28.dp),
    ) {
        items(places, key = { it.contentId }) { place ->
            FilteredItem(
                place = place.toFilteredPlace(),
                layout = FilteredItemLayout.BottomTab,
                onClick = { onPlaceClick(place.contentId) },
            )
        }
        listFooter(
            isLoading = isLoading,
            errorMessage = errorMessage,
            hasNext = hasNext,
            onLoadMore = onLoadMore,
        )
    }
}

@Composable
private fun ExpandedPlaceList(
    places: List<TourismPlace>,
    listState: LazyListState,
    isLoading: Boolean,
    errorMessage: String?,
    hasNext: Boolean,
    onPlaceClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 28.dp),
    ) {
        items(places, key = { it.contentId }) { place ->
            FilteredItem(
                place = place.toFilteredPlace(),
                layout = FilteredItemLayout.FullScreen,
                onClick = { onPlaceClick(place.contentId) },
            )
        }
        listFooter(
            isLoading = isLoading,
            errorMessage = errorMessage,
            hasNext = hasNext,
            onLoadMore = onLoadMore,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.listFooter(
    isLoading: Boolean,
    errorMessage: String?,
    hasNext: Boolean,
    onLoadMore: () -> Unit,
) {
    when {
        isLoading -> item { CircularProgressIndicator(modifier = Modifier.padding(20.dp)) }
        errorMessage != null -> item {
            TextButton(onClick = onLoadMore) { Text("다시 시도") }
        }
        hasNext -> item {
            TextButton(onClick = onLoadMore) { Text("더 보기") }
        }
    }
}

@Composable
private fun LoadMoreWhenListEnds(
    listState: LazyListState,
    placeCount: Int,
    hasNext: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(listState, placeCount, hasNext, isLoading, errorMessage) {
        if (!hasNext || isLoading || errorMessage != null || placeCount == 0) {
            return@LaunchedEffect
        }
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?.let { it >= placeCount - 1 } == true
        }
            .distinctUntilChanged()
            .collect { atEnd -> if (atEnd) onLoadMore() }
    }
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("다시 시도") }
    }
}

@Composable
private fun EmptyContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("이 지역에 표시할 관광지가 없어요.")
    }
}

private fun TourismPlace.toFilteredPlace(): FilteredPlace = FilteredPlace(
    name = name,
    playground = category,
    region = listOfNotNull(sidoName, sigunguName).joinToString(" "),
    thumbnailUrl = thumbnailUrl,
)
