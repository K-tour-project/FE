package com.everytrip.app.feature.region.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.everytrip.app.feature.region.data.model.TourismPlace
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun RegionBottomSheet(
    state: RegionSearchUiState,
    places: LazyPagingItems<TourismPlace>,
    isExpanded: Boolean,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalListState = rememberLazyListState()
    val verticalListState = rememberLazyListState()
    LaunchedEffect(state.selectedRegionId) {
        horizontalListState.scrollToItem(0)
        verticalListState.scrollToItem(0)
    }

    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 3.dp)) {
            Text(
                listOfNotNull(state.selectedSido?.name, state.selectedSigungu?.name).joinToString(" "),
                color = NavyText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                buildAnnotatedString {
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
            places.itemCount == 0 && places.loadState.refresh is LoadState.Loading -> LoadingContent()
            places.itemCount == 0 && places.loadState.refresh is LoadState.Error -> ErrorContent(
                (places.loadState.refresh as LoadState.Error).error.message
                    ?: "관광지 목록을 불러오지 못했어요.",
                places::retry,
            )
            places.itemCount == 0 -> EmptyContent()
            else -> PlaceList(
                places,
                if (isExpanded) verticalListState else horizontalListState,
                isExpanded,
                onPlaceClick,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlaceList(
    places: LazyPagingItems<TourismPlace>,
    listState: LazyListState,
    expanded: Boolean,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier,
) {
    val content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit = {
        items(places.itemCount, key = { index -> places.peek(index)?.contentId ?: index }) { index ->
            places[index]?.let { place ->
                FilteredItem(
                    place = place.toFilteredPlace(),
                    layout = if (expanded) FilteredItemLayout.FullScreen else FilteredItemLayout.BottomTab,
                    onClick = { onPlaceClick(place.contentId) },
                )
            }
        }
        when (places.loadState.append) {
            is LoadState.Loading -> item { CircularProgressIndicator(Modifier.padding(20.dp)) }
            is LoadState.Error -> item {
                TextButton(onClick = places::retry) { Text("다시 시도") }
            }
            else -> Unit
        }
    }
    val padding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 28.dp)
    if (expanded) {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            state = listState,
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            state = listState,
            contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
private fun LoadingContent() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("다시 시도") }
    }
}

@Composable
private fun EmptyContent() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text("이 지역에 표시할 관광지가 없어요.")
}

private fun TourismPlace.toFilteredPlace() = FilteredPlace(
    name = name,
    playground = category,
    region = listOfNotNull(sidoName, sigunguName).joinToString(" "),
    thumbnailUrl = thumbnailUrl,
)
