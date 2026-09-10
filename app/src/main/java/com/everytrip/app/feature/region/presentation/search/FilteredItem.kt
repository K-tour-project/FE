package com.everytrip.app.feature.region.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText

data class FilteredPlace(
    val name: String,
    val playground: String,
    val region: String,
    val imageResId: Int? = null,
    val thumbnailUrl: String? = null,
)

enum class FilteredItemLayout {
    BottomTab,
    FullScreen
}

@Composable
fun FilteredItem(
    place: FilteredPlace,
    layout: FilteredItemLayout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (layout) {
        FilteredItemLayout.BottomTab -> BottomTabFilteredItem(
            place = place,
            onClick = onClick,
            modifier = modifier,
        )

        FilteredItemLayout.FullScreen -> FullScreenFilteredItem(
            place = place,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun BottomTabFilteredItem(
    place: FilteredPlace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(132.dp)
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PlaceImage(
                place = place,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = place.name,
                    color = Color(0xFF101828),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = place.playground,
                    color = SecondaryText,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = place.region,
                    color = SecondaryText,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FullScreenFilteredItem(
    place: FilteredPlace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            PlaceImage(
                place = place,
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = place.name,
                    color = Color(0xFF101828),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = place.playground,
                    color = SecondaryText,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = place.region,
                    color = SecondaryText,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlaceImage(
    place: FilteredPlace,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    if (!place.thumbnailUrl.isNullOrBlank()) {
        TourismImage(
            url = place.thumbnailUrl,
            name = place.name,
            modifier = modifier.clip(shape),
        )
    } else if (place.imageResId != null) {
        Image(
            painter = painterResource(id = place.imageResId),
            contentDescription = place.name,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            PrimaryBlue.copy(alpha = 0.18f),
                            Color(0xFFE8F2FF),
                            Color(0xFFF8FAFC),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomTabFilteredItemPreview() {
    FilteredItem(
        place = FilteredPlace(
            name = "수원화성",
            playground = "촬영지",
            region = "팔달구",
        ),
        layout = FilteredItemLayout.BottomTab,
        onClick = {},
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
private fun FullScreenFilteredItemPreview() {
    FilteredItem(
        place = FilteredPlace(
            name = "행궁동 벽화마을",
            playground = "관광지",
            region = "팔달구",
        ),
        layout = FilteredItemLayout.FullScreen,
        onClick = {},
        modifier = Modifier.padding(16.dp),
    )
}
