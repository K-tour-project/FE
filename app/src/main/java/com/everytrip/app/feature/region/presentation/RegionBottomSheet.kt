package com.everytrip.app.feature.region.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.SecondaryText

@Immutable
data class RegionBottomSheetState(
    val regionName: String = "경기도 수원시 팔달구",
    val places: List<FilteredPlace> = defaultRegionPlaces,
)

@Composable
fun RegionBottomSheet(
    state: RegionBottomSheetState,
    onPlaceClick: (FilteredPlace) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                clip = false,
            )
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 28.dp),
        ) {
            SheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Text(
                    text = state.regionName,
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
                            append("${state.places.size}곳")
                        }
                        append("의 촬영지를 찾았어요")
                    },
                    color = SecondaryText,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                items(state.places) { place ->
                    FilteredItem(
                        place = place,
                        layout = FilteredItemLayout.BottomTab,
                        onClick = { onPlaceClick(place) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(56.dp)
            .height(6.dp),
        shape = RoundedCornerShape(100.dp),
        color = Color(0xFFD7D7D7),
    ) {}
}

val defaultRegionPlaces = listOf(
    FilteredPlace(name = "수원화성", distanceText = "0.8km", region = "팔달구"),
    FilteredPlace(name = "행궁동 벽화마을", distanceText = "1.2km", region = "팔달구"),
    FilteredPlace(name = "카페거리", distanceText = "1.6km", region = "팔달구"),
    FilteredPlace(name = "통닭거리", distanceText = "1.1km", region = "팔달구"),
    FilteredPlace(name = "광교호수공원", distanceText = "2.3km", region = "영통구"),
)

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC, widthDp = 430)
@Composable
private fun RegionBottomSheetPreview() {
    MaterialTheme {
        RegionBottomSheet(
            state = RegionBottomSheetState(),
            onPlaceClick = {},
        )
    }
}
