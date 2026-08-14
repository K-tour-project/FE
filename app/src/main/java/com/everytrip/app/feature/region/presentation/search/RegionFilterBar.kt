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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.SecondaryText

@Immutable
data class RegionFilterState(
    val province: String = "경기도",
    val city: String = "수원시",
    val district: String = "팔달구",
)

@Composable
fun RegionFilterBar(
    state: RegionFilterState,
    onProvinceSelected: (String) -> Unit,
    onCitySelected: (String) -> Unit,
    onDistrictSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    provinceOptions: List<String> = defaultProvinceOptions,
    cityOptions: List<String> = defaultCityOptions,
    districtOptions: List<String> = defaultDistrictOptions,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Text(
            text = "지역 필터",
            color = NavyText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RegionFilterDropdown(
                label = "도",
                selectedValue = state.province,
                options = provinceOptions,
                onOptionSelected = onProvinceSelected,
                modifier = Modifier.weight(1f),
            )
            RegionFilterDropdown(
                label = "시",
                selectedValue = state.city,
                options = cityOptions,
                onOptionSelected = onCitySelected,
                modifier = Modifier.weight(1f),
            )
            RegionFilterDropdown(
                label = "구",
                selectedValue = state.district,
                options = districtOptions,
                onOptionSelected = onDistrictSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RegionFilterDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            color = SecondaryText,
            fontSize = 18.sp,
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
                    .clip(FilterShape)
                    .border(width = 1.dp, color = Border, shape = FilterShape)
                    .clickable { expanded = true }
                    .padding(start = 22.dp, end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = selectedValue,
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

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = NavyText,
                                fontSize = 17.sp,
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

private val FilterShape = RoundedCornerShape(32.dp)

val defaultProvinceOptions = listOf("경기도", "서울특별시", "인천광역시")
val defaultCityOptions = listOf("수원시", "성남시", "고양시", "용인시")
val defaultDistrictOptions = listOf("팔달구", "영통구", "권선구", "장안구")

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 430)
@Composable
private fun RegionFilterBarPreview() {
    MaterialTheme {
        RegionFilterBar(
            state = RegionFilterState(),
            onProvinceSelected = {},
            onCitySelected = {},
            onDistrictSelected = {},
        )
    }
}
