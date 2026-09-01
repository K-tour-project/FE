package com.everytrip.app.feature.region.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue

@Composable
fun RegionDropdownPopup(
    expanded: Boolean,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 220.dp,
    offset: IntOffset = IntOffset.Zero,
) {
    if (!expanded) return

    Popup(
        offset = offset,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        RegionDropdownPopupContent(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = onOptionSelected,
            modifier = modifier.width(width),
        )
    }
}

@Composable
private fun RegionDropdownPopupContent(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        LazyColumn(
            modifier = Modifier
                .heightIn(max = DropdownMaxHeight)
                .padding(vertical = 8.dp),
        ) {
            items(options) { option ->
                val isSelected = option == selectedOption

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) Color(0xFFF1F6FF) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onOptionSelected(option) }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.weight(1f),
                        color = if (isSelected) PrimaryBlue else NavyText,
                        fontSize = 17.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PrimaryBlue,
                        )
                    }
                }
            }
        }
    }
}

private val DropdownMaxHeight = 500.dp

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RegionDropdownPopupPreview() {
    MaterialTheme {
        RegionDropdownPopupContent(
            options = listOf(
                "수원시 팔달구",
                "수원시 영통구",
                "수원시 권선구",
                "수원시 장안구",
            ),
            selectedOption = "수원시 팔달구",
            onOptionSelected = {},
            modifier = Modifier.width(240.dp),
        )
    }
}
