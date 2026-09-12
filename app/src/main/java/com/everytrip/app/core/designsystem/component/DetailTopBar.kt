package com.everytrip.app.core.designsystem.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = NavyText,
    actionIcon: ImageVector = Icons.Outlined.BookmarkBorder,
    selectedActionIcon: ImageVector = Icons.Filled.Bookmark,
    selectedActionColor: Color = PrimaryBlue,
    actionContentDescription: String = "저장",
    selectedActionContentDescription: String = "저장 취소",
) {
    var isSaved by rememberSaveable(title) { androidx.compose.runtime.mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier = modifier.height(64.dp),
        title = {
            Text(
                text = title,
                fontSize = 21.sp,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "상세 닫기",
                    tint = NavyText,
                )
            }
        },
        actions = {
            IconButton(onClick = { isSaved = !isSaved }) {
                Icon(
                    imageVector = if (isSaved) selectedActionIcon else actionIcon,
                    contentDescription = if (isSaved) {
                        selectedActionContentDescription
                    } else {
                        actionContentDescription
                    },
                    tint = if (isSaved) selectedActionColor else NavyText,
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
    )
}
