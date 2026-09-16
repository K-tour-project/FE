package com.everytrip.app.feature.artwork.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.SecondaryText

@Composable
internal fun ArtworkSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onFocus: () -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.height(48.dp).clip(RoundedCornerShape(20.dp))
        .background(ArtworkPale).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Search, "검색", Modifier.size(24.dp), tint = ArtworkInk)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text("작품명을 입력해주세요.", color = SecondaryText, fontSize = 15.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) onFocus() },
                singleLine = true,
                textStyle = TextStyle(color = ArtworkInk, fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            )
        }
        if (query.isNotEmpty()) {
            Icon(Icons.Default.Close, "검색어 지우기",
                Modifier.size(20.dp).clip(CircleShape)
                    .background(Color(0xFF9CA1AB))
                    .clickable(onClick = onClear).padding(3.dp),
                tint = Color.White)
        }
    }
}
