package com.everytrip.app.feature.artwork.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.feature.region.presentation.search.TourismImage
import com.everytrip.app.ui.theme.SecondaryText

@Composable
internal fun ArtworkSearchResults(
    state: ArtworkSearchUiState,
    onArtworkClick: (Int) -> Unit,
) {
    Column {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp,
            top = 22.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("검색 결과 총 ${state.total}개", color = ArtworkInk,
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = Color(0xFFE5E8EF))
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("검색 결과를 불러오지 못했어요.", color = SecondaryText)
            }
            state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (state.query.isBlank()) "검색어를 입력해주세요."
                    else "검색 결과가 없습니다.", color = SecondaryText)
            }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(state.items, key = { it.productId }) { artwork ->
                    ArtworkResultRow(artwork) { onArtworkClick(artwork.productId) }
                    HorizontalDivider(Modifier.padding(start = 116.dp, end = 20.dp),
                        color = Color(0xFFE5E8EF))
                }
            }
        }
    }
}

@Composable
private fun ArtworkResultRow(item: ArtworkSearchItem, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically) {
        TourismImage(item.posterUrl, item.title,
            Modifier.size(width = 80.dp, height = 112.dp).clip(RoundedCornerShape(6.dp)),
            placeholderText = "준비 중")
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = ArtworkInk, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp))
            Text(listOfNotNull(item.categoryLabel, item.year, item.genres)
                .joinToString(" · "), color = SecondaryText, fontSize = 13.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null,
            Modifier.size(22.dp), tint = SecondaryText)
    }
}
