package com.everytrip.app.feature.region.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.everytrip.app.feature.region.presentation.search.RegionSearchUiState
import com.everytrip.app.feature.region.presentation.search.TourismImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDetailScreen(
    state: RegionSearchUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    key(state.selectedContentId) {
        ModalBottomSheet(onDismissRequest = onClose,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onClose) { Text("닫기") }
                if (state.isLoadingDetail) CircularProgressIndicator()
                state.detailErrorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRetry) { Text("다시 시도") }
                }
                state.placeDetail?.let { detail ->
                    Text(detail.name, style = MaterialTheme.typography.headlineSmall)
                    DetailField("설명", detail.overview)
                    DetailField("홈페이지", detail.homepage)
                    DetailField("전화번호", detail.tel)
                    DetailField("주소", listOfNotNull(detail.address, detail.addressDetail).joinToString(" "))
                    detail.images.forEach { image ->
                        TourismImage(image, detail.name, Modifier.fillMaxWidth().height(220.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String?) {
    val text = remember(value) {
        value?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim() }
            ?.takeIf { it.isNotBlank() } ?: "정보 없음"
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        androidx.compose.foundation.text.selection.SelectionContainer {
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
