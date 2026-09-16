package com.everytrip.app.feature.artwork.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.everytrip.app.ui.theme.SecondaryText

internal val ArtworkInk = Color(0xFF171E2B)
internal val ArtworkPale = Color(0xFFF3F5F9)

@Composable
fun ArtworkSearchScreen(
    onCancelClick: () -> Unit = {},
    onArtworkClick: (Int) -> Unit = {},
    viewModel: ArtworkSearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    ArtworkSearchContent(
        state = state,
        onCancelClick = onCancelClick,
        onQueryChange = viewModel::setQuery,
        onFocus = viewModel::activate,
        onClear = { viewModel.setQuery("") },
        onArtworkClick = onArtworkClick,
    )
}

@Composable
private fun ArtworkSearchContent(
    state: ArtworkSearchUiState,
    onCancelClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFocus: () -> Unit,
    onClear: () -> Unit,
    onArtworkClick: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize().background(Color.White)
        .windowInsetsPadding(WindowInsets.statusBars)) {
        Row(Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancelClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로", tint = ArtworkInk)
            }
            Spacer(Modifier.width(12.dp))
            Text("작품 검색", color = ArtworkInk, fontSize = 21.sp,
                fontWeight = FontWeight.Bold)
        }

        ArtworkSearchField(
            query = state.query,
            onQueryChange = onQueryChange,
            onFocus = onFocus,
            onClear = onClear,
            onSearch = { focusManager.clearFocus() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )

        if (state.isActive) {
            ArtworkSearchResults(state, onArtworkClick)
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(82.dp).background(ArtworkPale, CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Search, null, Modifier.size(40.dp),
                            tint = ArtworkInk)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("어떤 작품을 찾고 계세요?", color = ArtworkInk,
                        fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("다양한 작품을 검색해보세요.",
                        color = SecondaryText, fontSize = 16.sp, lineHeight = 24.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 840)
@Composable
private fun ArtworkSearchPreview() {
    ArtworkSearchContent(
        state = ArtworkSearchUiState(),
        onCancelClick = {},
        onQueryChange = {},
        onFocus = {},
        onClear = {},
        onArtworkClick = {},
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 840)
@Composable
private fun ArtworkSearchResultsPreview() {
    ArtworkSearchContent(
        state = ArtworkSearchUiState(
            query = "왕",
            isActive = true,
            items = listOf(
                ArtworkSearchItem(1, "왕이 된 남자", "MOVIE", null, "2012", "사극"),
                ArtworkSearchItem(2, "왕과 사는 남자", "MOVIE", null, "2026", "사극"),
            ),
            total = 2,
        ),
        onCancelClick = {},
        onQueryChange = {},
        onFocus = {},
        onClear = {},
        onArtworkClick = {},
    )
}
