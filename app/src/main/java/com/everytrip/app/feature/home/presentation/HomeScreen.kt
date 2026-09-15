package com.everytrip.app.feature.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.core.designsystem.component.MainTopBar
import com.everytrip.app.feature.region.presentation.search.TourismImage
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText

val BackgroundGray = Color(0xFFF7F7F7)

enum class RecommendationCardType(val aspectRatio: Float) { PLACE(1f), WORK(2f / 3f) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState = HomeUiState(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRegionSearch: () -> Unit = {},
    onNavigateToDestination: (String) -> Unit = {},
    onNavigateToWork: (String) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Scaffold(topBar = { MainTopBar(title = "Every Trip") }, containerColor = BackgroundGray) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
            item { Spacer(Modifier.height(10.dp)); AISearchBar(onNavigateToSearch); Spacer(Modifier.height(10.dp)) }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryItem(Modifier.weight(1f), "작품으로 찾기", Icons.Default.Movie, onNavigateToSearch)
                    CategoryItem(Modifier.weight(1f), "지역으로 찾기", Icons.Default.Place, onNavigateToRegionSearch)
                }
                Spacer(Modifier.height(24.dp))
            }
            item { PromotionBanner() }
            item { Spacer(Modifier.height(15.dp)); SectionTitle("인기 관광지") }
            item {
                RecommendationRow(
                    uiState.popularTourismPlaces.map { Triple(it.name, it.thumbnailUrl, it.detailPath) },
                    RecommendationCardType.PLACE,
                    onNavigateToDestination,
                )
            }
            item { Spacer(Modifier.height(24.dp)); SectionTitle("인기 작품") }
            item {
                RecommendationRow(
                    uiState.popularProducts.map { Triple(it.title, it.posterUrl, it.detailPath) },
                    RecommendationCardType.WORK,
                    onNavigateToWork,
                )
            }
            when {
                uiState.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                uiState.errorMessage != null && uiState.popularTourismPlaces.isEmpty() && uiState.popularProducts.isEmpty() -> item {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("홈 정보를 불러오지 못했어요.", color = SecondaryText)
                        TextButton(onClick = onRetry) { Text("다시 시도") }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationRow(items: List<Triple<String, String?, String>>, type: RecommendationCardType, onClick: (String) -> Unit) {
    LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { it.third }) { item -> RecommendationCard(item.first, item.second, type) { onClick(item.third) } }
    }
}

@Composable
fun AISearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(53.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp), color = Color.White,
        border = BorderStroke(1.dp, PrimaryBlue), shadowElevation = 4.dp,
    ) {
        Row(Modifier.fillMaxSize().padding(start = 18.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = PrimaryBlue, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(14.dp)); VerticalDivider(Modifier.height(28.dp), color = Color(0xFFE3E7EF)); Spacer(Modifier.width(17.dp))
            Text("어떤 촬영지를 찾고 있나요?", Modifier.weight(1f), color = SecondaryText, fontSize = 15.sp, maxLines = 1)
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE4EFFF)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.NearMe, "검색", tint = PrimaryBlue, modifier = Modifier.size(25.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) = Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 12.dp))

@Composable
fun CategoryItem(modifier: Modifier, label: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Surface(modifier.height(50.dp).clickable(onClick = onClick), shape = RoundedCornerShape(15.dp), color = Color.White, border = BorderStroke(1.dp, Border)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(5.dp))
            Text(label, Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Default.ChevronRight, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PromotionBanner() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(132.dp).background(Brush.horizontalGradient(listOf(Color(0xFFEAF5FF), Color(0xFFC8E6F8), Color(0xFF7CB4D9))), RoundedCornerShape(18.dp))) {
        Column(Modifier.align(Alignment.CenterStart).padding(start = 22.dp)) {
            Text("콘텐츠로 떠나는 여행", color = Color(0xFF102B4C), fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp)); Text("영화·드라마 촬영지와 여행 코스를 한 번에", color = Color(0xFF4C6178), fontSize = 13.sp)
        }
        Icon(Icons.Default.Place, null, tint = Color.White.copy(alpha = .76f), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 22.dp).size(72.dp))
    }
}

@Composable
fun RecommendationCard(name: String, imageUrl: String?, type: RecommendationCardType, onClick: () -> Unit) {
    Card(Modifier.width(140.dp).aspectRatio(type.aspectRatio).clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(Modifier.fillMaxSize()) {
            TourismImage(imageUrl, name, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = .85f)))))
            Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTravelHomeScreen() = HomeScreen()
