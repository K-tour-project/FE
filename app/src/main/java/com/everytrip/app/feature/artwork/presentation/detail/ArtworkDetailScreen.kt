package com.everytrip.app.feature.artwork.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.core.designsystem.component.DetailTopBar
import com.everytrip.app.feature.region.data.model.ContentDetail
import com.everytrip.app.feature.region.data.model.ContentSummary
import com.everytrip.app.feature.region.data.model.FilmingLocation
import com.everytrip.app.feature.region.data.model.categoryLabel
import com.everytrip.app.feature.region.data.model.isDrama
import com.everytrip.app.feature.region.data.model.isMovie
import com.everytrip.app.feature.region.presentation.search.TourismImage
import com.everytrip.app.ui.theme.BodyText
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.FavoritePink
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.ProjectTheme
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun ArtworkDetailScreen(
    content: ContentDetail,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFilmingLocationClick: (FilmingLocation) -> Unit = {},
    onRelatedProductClick: (ContentSummary) -> Unit = {},
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailTopBar(
            title = "Every Trip",
            onBackClick = onBackClick,
            titleColor = PrimaryBlue,
            actionIcon = Icons.Outlined.FavoriteBorder,
            selectedActionIcon = Icons.Filled.Favorite,
            selectedActionColor = FavoritePink,
            actionContentDescription = "좋아요",
            selectedActionContentDescription = "좋아요 취소",
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TourismImage(
                url = content.posterUrl,
                name = content.title,
                modifier = Modifier.width(138.dp).height(206.dp).clip(RoundedCornerShape(16.dp)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(content.title, color = NavyText, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                content.genres?.takeIf(String::isNotBlank)?.let {
                    Text(it.replace("|", " · "), color = SecondaryText,
                        style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(content.categoryLabel)
                    if (content.isDrama()) {
                        content.networks?.takeIf(String::isNotBlank)?.let { NetworkChip(it) }
                    }
                }
                content.overview?.let { ExpandableOverview(it) }
            }
        }
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)) {
            ContentMetaCard(content)
            if (content.isDrama()) {
                ActorSection(content.leadActors)
            }
            FilmingLocationCarousel(content.filmingLocations, onFilmingLocationClick)
            RelatedProductCarousel(content.relatedProducts, onRelatedProductClick)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExpandableOverview(value: String) {
    var expanded by rememberSaveable(value) { androidx.compose.runtime.mutableStateOf(false) }
    var hasOverflow by rememberSaveable(value) { androidx.compose.runtime.mutableStateOf(false) }
    Column {
        Text(value, color = BodyText, style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) hasOverflow = it.hasVisualOverflow })
        if (hasOverflow || expanded) {
            Text(if (expanded) "접기" else "더보기", color = PrimaryBlue,
                modifier = Modifier.clickable { expanded = !expanded }.padding(top = 4.dp))
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(PrimaryBlue)
        .padding(horizontal = 15.dp, vertical = 7.dp)) {
        Text(category, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NetworkChip(network: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEAF3FF))
            .padding(horizontal = 15.dp, vertical = 7.dp),
    ) {
        Text(
            text = network,
            color = Color(0xFF3578C9),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ContentMetaCard(content: ContentDetail) {
    val secondValue = when {
        content.isMovie() -> content.runtime?.let { "${it}분" }
        content.isDrama() -> content.episodeCount?.let { "${it}부작" }
        else -> null
    }
    val secondLabel = if (content.isMovie()) "상영 시간" else "에피소드"
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp),
        CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            MetaItem(Icons.Outlined.CalendarMonth, content.firstAirDate?.take(4), "공개 연도", Modifier.weight(1f))
            MetaDivider()
            MetaItem(if (content.isDrama()) Icons.Outlined.LiveTv else Icons.Outlined.AccessTime,
                secondValue, secondLabel, Modifier.weight(1f))
            MetaDivider()
            MetaItem(Icons.Outlined.StarOutline, content.rating?.toString(), "평점", Modifier.weight(1f))
        }
    }
}

@Composable private fun MetaDivider() = Box(Modifier.width(1.dp).height(48.dp).background(Border))

@Composable
private fun MetaItem(icon: ImageVector, value: String?, label: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = NavyText, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(5.dp))
            Text(value?.takeIf(String::isNotBlank) ?: "정보 없음", color = NavyText,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(label, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ActorSection(leadActors: String?) {
    val actors = leadActors?.split('|')?.map(String::trim)?.filter(String::isNotBlank).orEmpty()
    if (actors.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("출연 배우", color = NavyText, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(actors, key = { it }) { actor ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White),
                    border = BorderStroke(1.dp, Border)) {
                    Text(actor, Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        color = NavyText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FilmingLocationCarousel(
    locations: List<FilmingLocation>,
    onClick: (FilmingLocation) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("촬영지", color = NavyText, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        if (locations.isEmpty()) {
            Text("관광지로 확인된 촬영지가 없습니다.", color = SecondaryText)
            return@Column
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(locations, key = { it.placeId }) { place ->
                Card(
                    modifier = Modifier.width(210.dp).height(112.dp).clickable { onClick(place) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    border = BorderStroke(1.dp, Border),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center) {
                        Text(place.name, color = NavyText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(listOfNotNull(place.sidoName, place.sigunguName).joinToString(" "),
                            color = SecondaryText, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedProductCarousel(
    products: List<ContentSummary>,
    onClick: (ContentSummary) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("비슷한 작품", color = NavyText, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        if (products.isEmpty()) {
            Text("비슷한 작품이 없습니다.", color = SecondaryText)
            return@Column
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products, key = { it.productId }) { product ->
                Card(
                    modifier = Modifier.width(150.dp).clickable { onClick(product) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    border = BorderStroke(1.dp, Border),
                ) {
                    TourismImage(product.posterUrl, product.title,
                        Modifier.fillMaxWidth().height(205.dp))
                    Column(Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(product.title, color = NavyText, fontWeight = FontWeight.Bold,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(product.categoryLabel, color = SecondaryText,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 412, heightDp = 892)
@Composable
private fun ArtworkDetailScreenPreview() {
    ProjectTheme {
        ArtworkDetailScreen(
            content = ContentDetail(
                productId = 139,
                title = "선재 업고 튀어",
                overview = "자신의 최애를 살리기 위해 과거로 돌아간 시계를 거스른 운명적인 사랑과 성장 이야기. 수원을 배경으로 펼쳐지는 설렘 가득한 청춘 로맨스.",
                firstAirDate = "2024-04-08",
                category = "DRAMA",
                productType = "미니시리즈",
                runtime = null,
                posterUrl = null,
                genres = "드라마|로맨스|청춘",
                networks = "tvN",
                episodeCount = 16,
                rating = 8.7,
                popularity = 152.0,
                leadActors = "변우석|김혜윤|송건희|이승협",
                filmingLocations = listOf(
                    FilmingLocation(434, "경복궁", "서울특별시", "종로구", "/places/434"),
                    FilmingLocation(4621, "도구해수욕장", "경상북도", "포항시", "/places/4621"),
                ),
                relatedProducts = listOf(
                    ContentSummary(140, "스물다섯 스물하나", "DRAMA", null, "/contents/140"),
                    ContentSummary(141, "그 해 우리는", "DRAMA", null, "/contents/141"),
                ),
            ),
            onBackClick = {},
        )
    }
}
