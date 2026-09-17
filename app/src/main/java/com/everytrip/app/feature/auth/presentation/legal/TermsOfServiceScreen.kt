package com.everytrip.app.feature.auth.presentation.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.everytrip.app.ui.theme.ProjectTheme

@Composable
fun TermsOfServiceScreen(onBackClick: () -> Unit = {}) {
    LegalDocumentScreen(
        title = "이용약관",
        kicker = "EVERY TRIP  ·  TERMS",
        headline = "Every Trip 이용 안내",
        introduction = "팀 ISFP가 제공하는 Every Trip 서비스의 이용조건과 운영팀·이용자의 권리 및 의무를 안내합니다.",
        sections = termsSections,
        onBackClick = onBackClick,
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun TermsOfServicePreview() {
    ProjectTheme { TermsOfServiceScreen() }
}
