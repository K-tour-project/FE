package com.everytrip.app.feature.region.presentation.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.everytrip.app.core.designsystem.component.DetailTopBar
import com.everytrip.app.feature.region.data.model.ContentSummary
import com.everytrip.app.feature.region.data.model.categoryLabel
import com.everytrip.app.feature.region.data.model.PlaceDetail
import com.everytrip.app.feature.region.data.model.PlaceTourDetail
import com.everytrip.app.feature.region.data.model.RelatedPlace
import com.everytrip.app.feature.region.data.model.RegionLocation
import com.everytrip.app.feature.region.data.model.TourismDetail
import com.everytrip.app.feature.region.presentation.search.RegionSearchUiState
import com.everytrip.app.feature.region.presentation.search.TourismImage
import com.everytrip.app.feature.artwork.presentation.detail.ArtworkDetailScreen
import com.everytrip.app.ui.theme.BodyText
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText
import com.everytrip.app.ui.theme.ProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDetailScreen(
    state: RegionSearchUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onContentClick: (Int) -> Unit,
    onFilmingPlaceClick: (Int) -> Unit,
    onRelatedPlaceClick: (String) -> Unit,
    favoritePlaceIds: Set<Int> = emptySet(),
    favoriteTourismIds: Set<String> = emptySet(),
    savedProductIds: Set<Int> = emptySet(),
    onTogglePlace: (Int) -> Unit = {},
    onToggleTourism: (String) -> Unit = {},
    onToggleProduct: (Int) -> Unit = {},
) {
    key(state.selectedContentId, state.selectedPlaceId, state.selectedProductId) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null,
            containerColor = Color.White,
            sheetGesturesEnabled = false,
        ) {
            Box(Modifier.fillMaxWidth().fillMaxHeight(0.96f)) {
                when {
                    state.isLoadingDetail -> CircularProgressIndicator(
                        Modifier.align(Alignment.Center), color = PrimaryBlue,
                    )
                    state.detailErrorMessage != null -> ErrorDetail(
                        message = state.detailErrorMessage,
                        onClose = onClose,
                        onRetry = onRetry,
                    )
                    state.filmingPlaceDetail != null -> FilmingPlaceContent(
                        place = state.filmingPlaceDetail,
                        onClose = onClose,
                        onContentClick = onContentClick,
                        onRelatedPlaceClick = onRelatedPlaceClick,
                        isFavorite = state.filmingPlaceDetail.placeId in favoritePlaceIds,
                        onFavoriteClick = { onTogglePlace(state.filmingPlaceDetail.placeId) },
                    )
                    state.placeDetail != null -> TourismPlaceContent(
                        detail = state.placeDetail,
                        onClose = onClose,
                        onContentClick = onContentClick,
                        onRelatedPlaceClick = onRelatedPlaceClick,
                        isFavorite = state.placeDetail.contentId in favoriteTourismIds,
                        onFavoriteClick = { onToggleTourism(state.placeDetail.contentId) },
                    )
                    state.contentDetail != null -> ArtworkDetailScreen(
                        content = state.contentDetail,
                        onBackClick = onClose,
                        onFilmingLocationClick = { location ->
                            location.detailPath.substringAfterLast('/').toIntOrNull()
                                ?.let(onFilmingPlaceClick)
                        },
                        onRelatedProductClick = { product ->
                            product.detailPath.substringAfterLast('/').toIntOrNull()
                                ?.let(onContentClick)
                        },
                        isSaved = state.contentDetail.productId in savedProductIds,
                        onSaveClick = { onToggleProduct(state.contentDetail.productId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilmingPlaceContent(
    place: PlaceDetail,
    onClose: () -> Unit,
    onContentClick: (Int) -> Unit,
    onRelatedPlaceClick: (String) -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
) {
    val tour = place.detail
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailTopBar(place.name, onClose, isActionSelected = isFavorite, onActionClick = onFavoriteClick)
        ImageCarousel(tour?.images.orEmpty(), tour?.imageCount ?: 0, place.name)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            tour?.overview?.let { BodyDescription(it) }
            SectionTitle("기본 정보")
            BasicInfoCard(
                address = place.roadAddress ?: place.address,
                homepage = tour?.homepage,
                tel = tour?.tel,
            )
            FacilityCard(tour?.petAllowed, tour?.parking, tour?.useTime)
            ContentCarousel(place.contents, onContentClick)
            RelatedPlaceCarousel(place.relatedPlaces, onRelatedPlaceClick)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TourismPlaceContent(
    detail: TourismDetail,
    onClose: () -> Unit,
    onContentClick: (Int) -> Unit,
    onRelatedPlaceClick: (String) -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailTopBar(detail.name, onClose, isActionSelected = isFavorite, onActionClick = onFavoriteClick)
        ImageCarousel(detail.images, detail.imageCount, detail.name)
        Column(Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            detail.overview?.let { BodyDescription(it) }
            SectionTitle("기본 정보")
            BasicInfoCard(
                address = listOfNotNull(detail.address, detail.addressDetail).joinToString(" "),
                homepage = detail.homepage,
                tel = detail.tel,
            )
            FacilityCard(detail.petAllowed, detail.parking, detail.useTime)
            ContentCarousel(detail.contents, onContentClick)
            RelatedPlaceCarousel(detail.relatedPlaces, onRelatedPlaceClick)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImageCarousel(images: List<String>, totalImageCount: Int, name: String) {
    if (images.isEmpty()) {
        TourismImage(null, name, Modifier.fillMaxWidth().height(240.dp).padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(18.dp)))
        return
    }
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box(Modifier.fillMaxWidth().height(240.dp).padding(horizontal = 12.dp)
        .clip(RoundedCornerShape(18.dp))) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            TourismImage(images[page], name, Modifier.fillMaxSize())
        }
        Text(
            text = "${pagerState.currentPage + 1}/${totalImageCount.coerceAtLeast(images.size)}",
            modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp)
                .clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.58f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun BodyDescription(value: String) {
    var expanded by rememberSaveable(value) { androidx.compose.runtime.mutableStateOf(false) }
    var hasOverflow by rememberSaveable(value) { androidx.compose.runtime.mutableStateOf(false) }
    Column {
        Text(
            htmlText(value),
            color = BodyText,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            fontSize = 17.sp,
            onTextLayout = { if (!expanded) hasOverflow = it.hasVisualOverflow },
        )
        if (hasOverflow) {
            Text(
                text = if (expanded) "접기" else "더보기",
                color = PrimaryBlue,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = NavyText,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun BasicInfoCard(address: String?, homepage: String?, tel: String?) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        InfoRow(Icons.Outlined.LocationOn, "주소", address)
        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Border)
        InfoRow(
            icon = Icons.Outlined.Home,
            label = "홈페이지",
            value = homepage,
            maxLines = 1,
            onClick = homepage?.takeIf { it.isNotBlank() }?.let { url ->
                { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url.withHttpScheme()))) }
            },
        )
        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Border)
        InfoRow(Icons.Outlined.Phone, "전화번호", tel)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String?,
    maxLines: Int = 2,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
        Text(label, Modifier.padding(start = 6.dp), color = NavyText, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(value?.takeIf { it.isNotBlank() } ?: "정보 없음", Modifier.padding(start = 16.dp),
            color = if (onClick != null) PrimaryBlue else SecondaryText,
            maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun FacilityCard(pet: String?, parking: String?, useTime: String?) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Facility(Icons.Outlined.Pets, pet, Modifier.weight(1f))
            Facility(Icons.Outlined.LocalParking, parking, Modifier.weight(1f))
            Facility(Icons.Outlined.AccessTime, useTime, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Facility(icon: ImageVector, value: String?, modifier: Modifier) {
    Column(modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(30.dp))
        Text(value?.takeIf { it.isNotBlank() } ?: "정보 없음", color = NavyText, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ContentCarousel(contents: List<ContentSummary>, onClick: (Int) -> Unit) {
    if (contents.isEmpty()) return
    SectionTitle("촬영 작품")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(contents, key = { it.productId }) { content ->
            Card(
                Modifier.width(150.dp).clickable { onClick(content.productId) },
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            ) {
                TourismImage(content.posterUrl, content.title, Modifier.fillMaxWidth().height(205.dp))
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(content.title, color = NavyText, fontWeight = FontWeight.Bold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(content.categoryLabel, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun RelatedPlaceCarousel(places: List<RelatedPlace>, onClick: (String) -> Unit) {
    if (places.isEmpty()) return
    SectionTitle("연관 관광지 추천")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(places, key = { it.relatedId }) { place ->
            Card(
                Modifier.width(192.dp).height(98.dp).clickable { onClick(place.contentId) },
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                    Text(place.name, color = NavyText, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(listOfNotNull(place.sidoName, place.sigunguName).joinToString(" "),
                        color = SecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ErrorDetail(message: String, onClose: () -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Row { TextButton(onClick = onClose) { Text("닫기") }; TextButton(onClick = onRetry) { Text("다시 시도") } }
    }
}

private fun htmlText(value: String): String =
    HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()

private fun String.withHttpScheme(): String = when {
    startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true) -> this
    else -> "https://$this"
}

@Preview(showBackground = true)
@Composable
private fun RegionDetailScreenPreview() {
    ProjectTheme {
        FilmingPlaceContent(
            place = PlaceDetail(
                    placeId = 123,
                    name = "행궁동 벽화마을",
                    location = RegionLocation(latitude = 37.285, longitude = 127.014),
                    address = "경기도 수원시 팔달구 화서문로 16번길 일대",
                    roadAddress = "경기도 수원시 팔달구 화서문로 16번길 일대",
                    region = mapOf("sido_name" to "경기도", "sigungu_name" to "수원시 팔달구"),
                    contents = listOf(
                        ContentSummary(
                            productId = 10,
                            title = "선재 업고 튀어",
                            category = "드라마 · 로맨스",
                            posterUrl = null,
                            detailPath = "/contents/10",
                        ),
                        ContentSummary(
                            productId = 11,
                            title = "스물다섯 스물하나",
                            category = "드라마",
                            posterUrl = null,
                            detailPath = "/contents/11",
                        ),
                    ),
                    detail = PlaceTourDetail(
                        tourContentId = "126121",
                        title = "행궁동 벽화마을",
                        overview = "드라마 촬영지로 알려진 골목으로, 아기자기한 벽화와 고즈넉한 분위기가 매력적인 마을입니다. 골목을 따라 다양한 벽화와 작은 공방, 카페를 만날 수 있습니다. 천천히 산책하며 사진을 남기기 좋고 주변 관광지와 함께 둘러보기에도 좋은 장소입니다.",
                        tel = "031-228-4672",
                        homepage = "www.suwon.go.kr",
                        useTime = "상시 개방",
                        restDate = null,
                        parking = "주차 가능",
                        petAllowed = "반려동물 동반 가능",
                        images = emptyList(),
                    ),
                    relatedPlaces = listOf(
                        RelatedPlace("r1", "654321", "수원화성", "경기도", "수원시 팔달구", "/tourism-places/654321"),
                        RelatedPlace("r2", "654322", "화성행궁", "경기도", "수원시 팔달구", "/tourism-places/654322"),
                        RelatedPlace("r3", "654323", "행리단길", "경기도", "수원시 팔달구", "/tourism-places/654323"),
                    ),
            ),
            onClose = {},
            onContentClick = {},
            onRelatedPlaceClick = {},
        )
    }
}
