package com.everytrip.app.feature.region.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.PrimaryBlue

@Immutable
data class RegionPlaceMarkerUiModel(
    val name: String,
    val imageResId: Int? = null,
)

@Composable
fun RegionPlaceMarker(
    place: RegionPlaceMarkerUiModel,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val borderColor = if (selected) PrimaryBlue else Color(0xFFE3EAF4)
    val borderWidth = if (selected) 2.dp else 1.dp
    val markerShape = RegionPlaceMarkerShape

    Column(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .width(246.dp)
                .height(112.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = markerShape,
                    ambientColor = Color.Black.copy(alpha = 0.16f),
                    spotColor = Color.Black.copy(alpha = 0.16f),
                ),
            shape = markerShape,
            color = Color.White,
            border = BorderStroke(borderWidth, borderColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MarkerThumbnail(
                    place = place,
                    modifier = Modifier.size(72.dp),
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = place.name,
                    color = Color(0xFF111827),
                    fontSize = 24.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(30.dp)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
            )
        }
    }
}

@Composable
private fun MarkerThumbnail(
    place: RegionPlaceMarkerUiModel,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)

    if (place.imageResId != null) {
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
                            PrimaryBlue.copy(alpha = 0.20f),
                            Color(0xFFE8F2FF),
                            Color(0xFFF8FAFC),
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.70f), shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = place.name.markerFallbackText(),
                color = PrimaryBlue,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

private fun String.markerFallbackText(): String {
    val compact = filterNot { it.isWhitespace() }
    return compact.take(2).ifBlank { "?" }
}

private object RegionPlaceMarkerShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val corner = with(density) { 20.dp.toPx() }
        val tailHeight = with(density) { 20.dp.toPx() }
        val tailHalfWidth = with(density) { 12.dp.toPx() }
        val rectBottom = size.height - tailHeight
        val centerX = size.width / 2f

        return Outline.Generic(
            Path().apply {
                moveTo(corner, 0f)
                lineTo(size.width - corner, 0f)
                quadraticTo(size.width, 0f, size.width, corner)
                lineTo(size.width, rectBottom - corner)
                quadraticTo(size.width, rectBottom, size.width - corner, rectBottom)
                lineTo(centerX + tailHalfWidth, rectBottom)
                lineTo(centerX, size.height)
                lineTo(centerX - tailHalfWidth, rectBottom)
                lineTo(corner, rectBottom)
                quadraticTo(0f, rectBottom, 0f, rectBottom - corner)
                lineTo(0f, corner)
                quadraticTo(0f, 0f, corner, 0f)
                close()
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FC, widthDp = 340)
@Composable
private fun RegionPlaceMarkerSelectedPreview() {
    RegionPlaceMarker(
        place = RegionPlaceMarkerUiModel(
            name = "노벰버"
        ),
        selected = true,
        modifier = Modifier.padding(24.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FC, widthDp = 340)
@Composable
private fun RegionPlaceMarkerDefaultPreview() {
    RegionPlaceMarker(
        place = RegionPlaceMarkerUiModel(
            name = "기흥역 공영 주차장"
        ),
        modifier = Modifier.padding(24.dp),
    )
}
