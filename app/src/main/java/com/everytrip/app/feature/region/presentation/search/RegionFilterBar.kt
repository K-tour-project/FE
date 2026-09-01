package com.everytrip.app.feature.region.presentation.search

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.SecondaryText

@Immutable
data class RegionFilterState(
    val province: String = "",
    val city: String = "",
)

@Composable
fun RegionFilterBar(
    state: RegionFilterState,
    onProvinceSelected: (String) -> Unit,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    provinceOptions: List<String> = emptyList(),
    cityOptions: List<String> = emptyList(),
    isCityEnabled: Boolean = true,
    isProvinceLoading: Boolean = false,
    isCityLoading: Boolean = false,
    onProvinceDropdownClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Text(
            text = "지역 필터",
            color = NavyText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RegionFilterDropdown(
                label = "시/도",
                selectedValue = state.province.ifBlank { "시/도" },
                options = provinceOptions,
                isEnabled = true,
                isLoading = isProvinceLoading,
                onDropdownClick = onProvinceDropdownClick,
                onOptionSelected = onProvinceSelected,
                modifier = Modifier.weight(0.9f),
            )
            RegionFilterDropdown(
                label = "시/군/구",
                selectedValue = state.city.ifBlank {
                    if (isCityEnabled) "시/군/구" else "시/군/구 없음"
                },
                options = cityOptions,
                isEnabled = isCityEnabled,
                isLoading = isCityLoading,
                onOptionSelected = onCitySelected,
                modifier = Modifier.weight(1.1f),
            )
        }
    }
}

@Composable
private fun RegionFilterDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    isEnabled: Boolean,
    isLoading: Boolean,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDropdownClick: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var popupOffset by remember { mutableStateOf(IntOffset.Zero) }
    var popupWidth by remember { mutableStateOf(220.dp) }
    val density = LocalDensity.current

    Column(modifier = modifier) {
        Text(
            text = label,
            color = SecondaryText,
            fontSize = 16.sp,
            lineHeight = 28.sp,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .alpha(if (isEnabled) 1f else 0.45f)
                    .clip(FilterShape)
                    .border(width = 1.dp, color = Border, shape = FilterShape)
                    .clickable(enabled = isEnabled) {
                        onDropdownClick()
                        expanded = true
                    }
                    .onGloballyPositioned { coordinates ->
                        val popupGap = with(density) { 6.dp.roundToPx() }

                        popupOffset = IntOffset(
                            x = 0,
                            y = coordinates.size.height + popupGap,
                        )
                        popupWidth = with(density) {
                            coordinates.size.width.toDp()
                        }
                    }
                    .padding(start = 22.dp, end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = if (isLoading) "불러오는 중" else selectedValue,
                    modifier = Modifier.weight(1f),
                    color = NavyText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF52627D),
                )
            }

            RegionDropdownPopup(
                expanded = expanded && isEnabled,
                options = options,
                selectedOption = selectedValue,
                onOptionSelected = { option ->
                    onOptionSelected(option)
                    expanded = false
                },
                onDismissRequest = {
                    expanded = false
                },
                width = popupWidth,
                offset = popupOffset,
            )
        }
    }
}

private val FilterShape = RoundedCornerShape(32.dp)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 430)
@Composable
private fun RegionFilterBarPreview() {
    MaterialTheme {
        RegionFilterBar(
            state = RegionFilterState(),
            onProvinceSelected = {},
            onCitySelected = {}
        )
    }
}
