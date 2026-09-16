package com.everytrip.app.feature.mypage.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.everytrip.app.feature.mypage.data.FavoritePlaceData
import com.everytrip.app.feature.mypage.data.SavedProductData
import com.everytrip.app.core.designsystem.component.AppBottomNavigationBar
import com.everytrip.app.core.designsystem.component.DefaultProfileImage
import com.everytrip.app.core.designsystem.component.MainTopBar
import com.everytrip.app.feature.region.presentation.search.TourismImage
import com.everytrip.app.feature.region.presentation.search.loadTourismBitmap
import com.everytrip.app.ui.theme.BodyText
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.FavoritePink
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.ProjectTheme
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun MyPageRoute(
    viewModel: FavoriteViewModel,
    onSettingsClick: () -> Unit = {},
    onPlaceClick: (String) -> Unit = {},
    onWorkClick: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val favoritePlaces = viewModel.favoritePlaces.collectAsLazyPagingItems()
    val savedProducts = viewModel.savedProducts.collectAsLazyPagingItems()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.refresh() }
    MyPageScreen(
        onSettingsClick = onSettingsClick,
        onPlaceClick = { id ->
            favoritePlaces.itemSnapshotList.items.firstOrNull { it.favoriteId == id }
                ?.detailPath?.let(onPlaceClick)
        },
        onWorkClick = { id ->
            savedProducts.itemSnapshotList.items.firstOrNull { it.productId.toLong() == id }
                ?.detailPath?.let(onWorkClick)
        },
        likedPlaces = favoritePlaces.itemSnapshotList.items.map {
            LikedPlaceUi(it.favoriteId, it.name, it.description, it.address, it.imageUrl)
        },
        savedWorks = savedProducts.itemSnapshotList.items.map {
            SavedWorkUi(it.productId.toLong(), it.title, it.category, it.year, it.overview, it.locationSummary, it.posterUrl)
        },
        likedPlaceCount = state.myPage.favoritePlaceCount,
        savedWorkCount = state.myPage.savedProductCount,
        profileName = state.myPage.nickname.ifBlank { "사용자" },
        profileEmail = state.myPage.email,
        profileImageUrl = state.myPage.profileImageUrl,
        profileImageRevision = state.profileImageRevision,
        isLoading = state.isLoading,
        isLoadingMorePlaces = state.isLoadingMorePlaces,
        isLoadingMoreWorks = state.isLoadingMoreProducts,
        hasMorePlaces = state.myPage.favoritePlaces.size < state.myPage.favoritePlaceCount,
        hasMoreWorks = state.savedProducts.size < state.myPage.savedProductCount,
        loadMorePlacesFailed = state.loadMorePlacesFailed,
        loadMoreWorksFailed = state.loadMoreProductsFailed,
        onUnlikePlace = viewModel::deleteFavorite,
        onUnsaveWork = { viewModel.toggleProduct(it.toInt()) },
        onLoadMorePlaces = viewModel::loadMorePlaces,
        onLoadMoreWorks = viewModel::loadMoreProducts,
        onRetryLoadMorePlaces = viewModel::retryLoadMorePlaces,
        onRetryLoadMoreWorks = viewModel::retryLoadMoreProducts,
        likedPaging = favoritePlaces,
        savedPaging = savedProducts,
    )
}

/** 마이페이지의 텍스트 크기는 여기서 한 번에 조정할 수 있습니다. */
object MyPageTextSize {
    val profileName = 24.sp
    val profileMessage = 16.sp
    val summaryLabel = 17.sp
    val summaryCount = 27.sp
    val tab = 17.sp
    val cardTitle = 20.sp
    val cardBody = 15.sp
    val cardMeta = 14.sp
}

enum class MyPageTab { LikedPlaces, SavedWorks }

data class LikedPlaceUi(
    val id: Long,
    val name: String,
    val description: String,
    val address: String,
    val imageUrl: String? = null,
)

data class SavedWorkUi(
    val id: Long,
    val title: String,
    val category: String,
    val year: String,
    val overview: String,
    val filmingLocationSummary: String,
    val posterUrl: String? = null,
)

@Composable
fun MyPageScreen(
    onSettingsClick: () -> Unit = {},
    onPlaceClick: (Long) -> Unit = {},
    onWorkClick: (Long) -> Unit = {},
    initialTab: MyPageTab = MyPageTab.LikedPlaces,
    likedPlaces: List<LikedPlaceUi> = previewLikedPlaces,
    savedWorks: List<SavedWorkUi> = previewSavedWorks,
    likedPlaceCount: Int = 12,
    savedWorkCount: Int = 6,
    profileName: String = "lee neng",
    profileEmail: String = "user@example.com",
    profileImageUrl: String? = null,
    profileImageRevision: Int = 0,
    isLoading: Boolean = false,
    isLoadingMorePlaces: Boolean = false,
    isLoadingMoreWorks: Boolean = false,
    hasMorePlaces: Boolean = false,
    hasMoreWorks: Boolean = false,
    loadMorePlacesFailed: Boolean = false,
    loadMoreWorksFailed: Boolean = false,
    onUnlikePlace: (Long) -> Unit = {},
    onUnsaveWork: (Long) -> Unit = {},
    onLoadMorePlaces: () -> Unit = {},
    onLoadMoreWorks: () -> Unit = {},
    onRetryLoadMorePlaces: () -> Unit = {},
    onRetryLoadMoreWorks: () -> Unit = {},
    likedPaging: LazyPagingItems<FavoritePlaceData>? = null,
    savedPaging: LazyPagingItems<SavedProductData>? = null,
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }

    Column(Modifier.fillMaxSize().background(Color(0xFFFAFCFF))) {
        MainTopBar(
            title = "MY",
            icon = Icons.Outlined.Settings,
            iconContentDescription = "설정",
            onIconClick = onSettingsClick,
            showActionBorder = false,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ProfileCard(profileName, profileEmail, profileImageUrl, profileImageRevision) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Outlined.FavoriteBorder, null, tint = PrimaryBlue) },
                        label = "찜한 장소",
                        count = likedPlaceCount,
                        onClick = { selectedTab = MyPageTab.LikedPlaces },
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Outlined.Movie, null, tint = PrimaryBlue) },
                        label = "저장한 작품",
                        count = savedWorkCount,
                        onClick = { selectedTab = MyPageTab.SavedWorks },
                    )
                }
            }
            item { MyPageTabSelector(selectedTab) { selectedTab = it } }
            if (isLoading && likedPlaces.isEmpty() && savedWorks.isEmpty()) {
                item { LoadingListContent() }
            } else when (selectedTab) {
                MyPageTab.LikedPlaces -> {
                    if (likedPaging != null) {
                        when {
                            likedPaging.itemCount == 0 && likedPaging.loadState.refresh is LoadState.Loading ->
                                item { LoadingListContent() }
                            likedPaging.itemCount == 0 && likedPaging.loadState.refresh is LoadState.Error ->
                                item { PagingErrorContent(likedPaging::retry) }
                            likedPaging.itemCount == 0 ->
                                item { EmptyListContent("아직 찜한 장소가 없어요.", "마음에 드는 여행 장소를 찜해보세요.") }
                            else -> {
                                items(likedPaging.itemCount, key = { index ->
                                    "place-${likedPaging.peek(index)?.favoriteId ?: index}"
                                }) { index ->
                                    likedPaging[index]?.let { data ->
                                        val place = LikedPlaceUi(data.favoriteId, data.name, data.description, data.address, data.imageUrl)
                                        LikedPlaceCard(place, { onPlaceClick(place.id) }, { onUnlikePlace(place.id) })
                                    }
                                }
                                pagingFooter(likedPaging.loadState.append, likedPaging::retry)
                            }
                        }
                    } else if (likedPlaces.isEmpty()) {
                        item { EmptyListContent("아직 찜한 장소가 없어요.", "마음에 드는 여행 장소를 찜해보세요.") }
                    } else {
                        items(likedPlaces, key = { "place-${it.id}" }) {
                            LikedPlaceCard(it, onClick = { onPlaceClick(it.id) }, onUnlike = { onUnlikePlace(it.id) })
                        }
                        if (hasMorePlaces || isLoadingMorePlaces || loadMorePlacesFailed) {
                            item {
                                PaginationFooter(
                                    isLoading = isLoadingMorePlaces,
                                    failed = loadMorePlacesFailed,
                                    onLoadMore = onLoadMorePlaces,
                                    onRetry = onRetryLoadMorePlaces,
                                )
                            }
                        }
                    }
                }
                MyPageTab.SavedWorks -> {
                    if (savedPaging != null) {
                        when {
                            savedPaging.itemCount == 0 && savedPaging.loadState.refresh is LoadState.Loading ->
                                item { LoadingListContent() }
                            savedPaging.itemCount == 0 && savedPaging.loadState.refresh is LoadState.Error ->
                                item { PagingErrorContent(savedPaging::retry) }
                            savedPaging.itemCount == 0 ->
                                item { EmptyListContent("아직 저장한 작품이 없어요.", "여행하고 싶은 작품을 저장해보세요.") }
                            else -> {
                                items(savedPaging.itemCount, key = { index ->
                                    "work-${savedPaging.peek(index)?.productId ?: index}"
                                }) { index ->
                                    savedPaging[index]?.let { data ->
                                        val work = SavedWorkUi(data.productId.toLong(), data.title, data.category, data.year, data.overview, data.locationSummary, data.posterUrl)
                                        SavedWorkCard(work, { onWorkClick(work.id) }, { onUnsaveWork(work.id) })
                                    }
                                }
                                pagingFooter(savedPaging.loadState.append, savedPaging::retry)
                            }
                        }
                    } else if (savedWorks.isEmpty()) {
                        item { EmptyListContent("아직 저장한 작품이 없어요.", "여행하고 싶은 작품을 저장해보세요.") }
                    } else {
                        items(savedWorks, key = { "work-${it.id}" }) {
                            SavedWorkCard(it, onClick = { onWorkClick(it.id) }, onUnsave = { onUnsaveWork(it.id) })
                        }
                        if (hasMoreWorks || isLoadingMoreWorks || loadMoreWorksFailed) {
                            item {
                                PaginationFooter(
                                    isLoading = isLoadingMoreWorks,
                                    failed = loadMoreWorksFailed,
                                    onLoadMore = onLoadMoreWorks,
                                    onRetry = onRetryLoadMoreWorks,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.pagingFooter(
    loadState: LoadState,
    retry: () -> Unit,
) {
    when (loadState) {
        is LoadState.Loading -> item { LoadingListContent() }
        is LoadState.Error -> item { PagingErrorContent(retry) }
        else -> Unit
    }
}

@Composable
private fun PagingErrorContent(onRetry: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
        TextButton(onClick = onRetry) { Text("다시 시도") }
    }
}

@Composable
private fun LoadingListContent() {
    Box(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = PrimaryBlue)
    }
}

@Composable
private fun EmptyListContent(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, color = BodyText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(description, color = SecondaryText, fontSize = 14.sp)
    }
}

@Composable
private fun PaginationFooter(
    isLoading: Boolean,
    failed: Boolean,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!isLoading && !failed) onLoadMore()
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            failed -> TextButton(onClick = onRetry) { Text("더 불러오기 재시도") }
            else -> CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = PrimaryBlue,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Composable
private fun ProfileCard(profileName: String, profileEmail: String, profileImageUrl: String?, imageRevision: Int) {
    SurfaceCard(height = 130.dp) {
        Row(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(92.dp).clip(CircleShape).background(Color(0xFFE6F1FF)),
                contentAlignment = Alignment.Center,
            ) {
                val profileBitmap by produceState<android.graphics.Bitmap?>(null, profileImageUrl, imageRevision) {
                    value = loadTourismBitmap(profileImageUrl)
                }
                profileBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = profileName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } ?: DefaultProfileImage(
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "$profileName 기본 프로필 이미지",
                )
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(profileName, fontSize = MyPageTextSize.profileName, fontWeight = FontWeight.Bold, color = BodyText)
                Spacer(Modifier.height(6.dp))
                Text(profileEmail, fontSize = MyPageTextSize.profileMessage, color = SecondaryText)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    icon: @Composable () -> Unit,
    label: String,
    count: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.height(92.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) { icon() }
                Spacer(Modifier.width(8.dp))
                Text(label, fontSize = MyPageTextSize.summaryLabel, color = BodyText)
            }
            Text(
                text = count.toString(),
                fontSize = MyPageTextSize.summaryCount,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
            )
        }
    }
}

@Composable
private fun MyPageTabSelector(selected: MyPageTab, onSelected: (MyPageTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(27.dp))
            .background(Color.White).border(1.dp, Border, RoundedCornerShape(27.dp)),
    ) {
        MyPageTab.entries.forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(27.dp))
                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.09f) else Color.Transparent)
                    .clickable { onSelected(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (tab == MyPageTab.LikedPlaces) "찜한 장소" else "저장한 작품",
                    fontSize = MyPageTextSize.tab,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PrimaryBlue else SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun LikedPlaceCard(place: LikedPlaceUi, onClick: () -> Unit, onUnlike: () -> Unit) {
    ListCard(onClick) {
        TourismImage(place.imageUrl, place.name, Modifier.width(138.dp).fillMaxSize())
        Column(Modifier.weight(1f).padding(16.dp, 14.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(place.name, Modifier.weight(1f), color = BodyText, fontSize = MyPageTextSize.cardTitle, fontWeight = FontWeight.Bold, maxLines = 1)
                Icon(Icons.Filled.Favorite, "찜 해제", tint = FavoritePink, modifier = Modifier.clickable(onClick = onUnlike))
                Spacer(Modifier.width(12.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(place.description, color = SecondaryText, fontSize = MyPageTextSize.cardBody, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(5.dp))
                Text(place.address, Modifier.weight(1f), color = SecondaryText, fontSize = MyPageTextSize.cardMeta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Outlined.ChevronRight, null, tint = BodyText)
            }
        }
    }
}

@Composable
private fun SavedWorkCard(work: SavedWorkUi, onClick: () -> Unit, onUnsave: () -> Unit) {
    ListCard(onClick) {
        TourismImage(work.posterUrl, work.title, Modifier.width(124.dp).fillMaxSize())
        Column(Modifier.weight(1f).padding(16.dp, 14.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(work.title, Modifier.weight(1f), color = BodyText, fontSize = MyPageTextSize.cardTitle, fontWeight = FontWeight.Bold, maxLines = 1)
                Icon(Icons.Filled.Bookmark, "저장 해제", tint = PrimaryBlue, modifier = Modifier.clickable(onClick = onUnsave))
                Spacer(Modifier.width(12.dp))
            }
            Text("${work.category}  |  ${work.year}", color = SecondaryText, fontSize = MyPageTextSize.cardMeta)
            Spacer(Modifier.height(5.dp))
            Text(work.overview, color = SecondaryText, fontSize = MyPageTextSize.cardBody, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(5.dp))
                Text(work.filmingLocationSummary, Modifier.weight(1f), color = SecondaryText, fontSize = MyPageTextSize.cardMeta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Outlined.ChevronRight, null, tint = BodyText)
            }
        }
    }
}

@Composable
private fun ListCard(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(132.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) { Row(Modifier.fillMaxSize(), content = content) }
}

@Composable
private fun SurfaceCard(height: Dp, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(height),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        content = { content() },
    )
}

private val previewLikedPlaces = listOf(
    LikedPlaceUi(1, "수원 화성", "역사와 전통이 살아있는 대표 명소", "경기 수원시 팔달구"),
    LikedPlaceUi(2, "정동진 해변", "바다와 기차가 만나는 감성 여행지", "강원 강릉시 강동면"),
    LikedPlaceUi(3, "행궁동 벽화마을", "예술이 살아있는 골목길", "경기 수원시 팔달구"),
    LikedPlaceUi(4, "청평호", "자연이 주는 힐링 스팟", "경기 가평군 청평면"),
)

private val previewSavedWorks = listOf(
    SavedWorkUi(101, "선재 업고 튀어", "드라마", "2024", "다시, 너에게로 달려가는 시간", "수원, 서울, 춘천 외 5곳"),
    SavedWorkUi(102, "도깨비", "드라마", "2016", "세상을 넘은, 두 사람의 이야기", "강릉, 주문진, 서울 외 4곳"),
    SavedWorkUi(103, "왕과 사는 남자", "영화", "2017", "운명을 넘어, 사람을 꿈꾼 이야기", "수원 화성, 서울, 문경 외 3곳"),
    SavedWorkUi(104, "미스터 션샤인", "드라마", "2018", "그 시대, 사랑의 이름", "인천, 서울, 강화 외 6곳"),
)

@Preview(name = "마이페이지 - 찜한 장소", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun LikedPlacesPreview() = MyPagePreview(MyPageTab.LikedPlaces)

@Preview(name = "마이페이지 - 저장한 작품", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SavedWorksPreview() = MyPagePreview(MyPageTab.SavedWorks)

@Composable
private fun MyPagePreview(initialTab: MyPageTab) {
    ProjectTheme {
        Scaffold(bottomBar = { AppBottomNavigationBar(selectedIndex = 4, onItemSelected = {}) }) { padding ->
            Box(Modifier.padding(padding)) { MyPageScreen(initialTab = initialTab) }
        }
    }
}
