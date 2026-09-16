package com.everytrip.app.feature.auth.presentation.legal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.everytrip.app.core.designsystem.component.AppTopBar

@Composable
fun TermsOfServiceScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(title1 = "이용 약관", title2 = "" ,onBackClick = onBackClick)
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
