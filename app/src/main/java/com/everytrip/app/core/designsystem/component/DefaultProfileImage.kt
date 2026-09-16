package com.everytrip.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.everytrip.app.ui.theme.PrimaryBlue

@Composable
fun DefaultProfileImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = "기본 프로필 이미지",
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFF3F7FF))
            .border(1.5.dp, PrimaryBlue, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(0.64f),
            tint = Color(0xFF9DBEFF),
        )
    }
}
