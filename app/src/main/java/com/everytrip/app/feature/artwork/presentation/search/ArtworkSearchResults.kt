package com.everytrip.app.feature.artwork.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "검색 결과 총 ${state.total}개",
                color = ArtworkInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
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
                Text(
                    if (state.query.isBlank()) "검색어를 입력해주세요." else "검색 결과가 없습니다.",
                    color = SecondaryText,
                )
            }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(state.items) { _, artwork ->
                    ArtworkResultRow(artwork, artwork.productId?.let { productId ->
                        { onArtworkClick(productId) }
                    })
                    HorizontalDivider(
                        Modifier.padding(horizontal = 20.dp),
                        color = Color(0xFFE5E8EF),
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtworkResultRow(item: ArtworkSearchItem, onClick: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TourismImage(
            url = item.posterUrl,
            name = item.title,
            modifier = Modifier.size(width = 80.dp, height = 112.dp)
                .clip(RoundedCornerShape(6.dp)),
            placeholderText = "포스터 없음",
        )
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                color = ArtworkInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val details = listOfNotNull(
                item.year?.takeIf(String::isNotBlank),
                item.categoryLabel.takeIf(String::isNotBlank),
                item.genres?.takeIf(String::isNotBlank),
            )
            if (details.isNotEmpty()) {
                Text(
                    details.joinToString(" · "),
                    color = SecondaryText,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (onClick != null) Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = SecondaryText,
        )
    }
}
