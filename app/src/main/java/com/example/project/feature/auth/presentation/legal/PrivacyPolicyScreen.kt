package com.example.project.feature.auth.presentation.legal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.project.core.designsystem.component.AppTopBar

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(title = "개인정보 처리방침", onBackClick = onBackClick)
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
