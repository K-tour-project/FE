package com.everytrip.app.feature.mypage.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.core.designsystem.component.AppBottomNavigationBar
import com.everytrip.app.core.designsystem.component.MainTopBar
import com.everytrip.app.feature.region.presentation.search.TourismImage
import com.everytrip.app.ui.theme.BodyText
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.ProjectTheme
import com.everytrip.app.ui.theme.SecondaryText

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
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }

    Column(Modifier.fillMaxSize().background(Color(0xFFFAFCFF))) {
        MainTopBar(
            title = "MY",
            icon = Icons.Outlined.Settings,
            iconContentDescription = "설정",
            onIconClick = onSettingsClick,
            showActionBorder = true,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ProfileCard() }
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
            when (selectedTab) {
                MyPageTab.LikedPlaces -> items(likedPlaces, key = { "place-${it.id}" }) {
                    LikedPlaceCard(it, onClick = { onPlaceClick(it.id) })
                }
                MyPageTab.SavedWorks -> items(savedWorks, key = { "work-${it.id}" }) {
                    SavedWorkCard(it, onClick = { onWorkClick(it.id) })
                }
            }
        }
    }
}

@Composable
private fun ProfileCard() {
    SurfaceCard(height = 130.dp) {
        Row(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(92.dp).clip(CircleShape).background(Color(0xFFE6F1FF)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Person, null, tint = PrimaryBlue, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text("lee neng", fontSize = MyPageTextSize.profileName, fontWeight = FontWeight.Bold, color = BodyText)
                Spacer(Modifier.height(6.dp))
                Text("콘텐츠로 떠나는 여행을 저장해보세요.", fontSize = MyPageTextSize.profileMessage, color = SecondaryText)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(PrimaryBlue.copy(alpha = 0.25f)))
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
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = MyPageTextSize.summaryLabel, color = BodyText)
                Text(count.toString(), fontSize = MyPageTextSize.summaryCount, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = BodyText)
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
                    .then(
                        if (isSelected) Modifier.border(
                            1.dp,
                            PrimaryBlue.copy(alpha = 0.22f),
                            RoundedCornerShape(27.dp),
                        ) else Modifier
                    )
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
private fun LikedPlaceCard(place: LikedPlaceUi, onClick: () -> Unit) {
    ListCard(onClick) {
        TourismImage(place.imageUrl, place.name, Modifier.width(138.dp).fillMaxSize())
        Column(Modifier.weight(1f).padding(16.dp, 14.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(place.name, Modifier.weight(1f), color = BodyText, fontSize = MyPageTextSize.cardTitle, fontWeight = FontWeight.Bold, maxLines = 1)
                Icon(Icons.Filled.Favorite, "찜 해제", tint = PrimaryBlue)
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
private fun SavedWorkCard(work: SavedWorkUi, onClick: () -> Unit) {
    ListCard(onClick) {
        TourismImage(work.posterUrl, work.title, Modifier.width(124.dp).fillMaxSize())
        Column(Modifier.weight(1f).padding(16.dp, 14.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(work.title, Modifier.weight(1f), color = BodyText, fontSize = MyPageTextSize.cardTitle, fontWeight = FontWeight.Bold, maxLines = 1)
                Icon(Icons.Filled.Bookmark, "저장 해제", tint = PrimaryBlue)
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
