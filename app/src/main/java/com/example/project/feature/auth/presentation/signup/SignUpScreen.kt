package com.example.project.feature.auth.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.core.designsystem.component.AppTextField
import com.example.project.core.designsystem.component.AppTopBar
import com.example.project.core.designsystem.component.GradientButton
import com.example.project.core.designsystem.component.PasswordTextField
import com.example.project.ui.theme.NavyText
import com.example.project.ui.theme.PrimaryBlue
import com.example.project.ui.theme.SecondaryText

internal fun canSubmitSignUp(
    termsAccepted: Boolean,
    privacyAccepted: Boolean
): Boolean = termsAccepted && privacyAccepted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordCheck by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(title = "(아이콘)", onBackClick = onBackClick)
        },
        containerColor = Color.White
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Every Trip에 오신 것을 환영합니다.\n나만의 여행을 시작해보세요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = NavyText
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 이메일 입력칸
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "이메일을 입력해주세요",
                leadingIcon = Icons.Outlined.Email,
                contentDescription = "이메일"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 입력칸
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "비밀번호를 입력해 주세요"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 확인 입력칸
            PasswordTextField(
                value = passwordCheck,
                onValueChange = { passwordCheck = it },
                placeholder = "비밀번호를 다시 입력해주세요",
                contentDescription = "비밀번호 확인"
            )

            Spacer(modifier = Modifier.height(25.dp))

            // 닉네임 입력칸
            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "닉네임을 입력해주세요",
                leadingIcon = Icons.Outlined.Person,
                contentDescription = "회원"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 구분선
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 약관 동의
            LegalConsentRow(
                checked = termsAccepted,
                linkText = "이용약관",
                onCheckedChange = { termsAccepted = it },
                onLinkClick = onTermsClick
            )
            LegalConsentRow(
                checked = privacyAccepted,
                linkText = "개인정보 처리방침",
                onCheckedChange = { privacyAccepted = it },
                onLinkClick = onPrivacyClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = "여행을 시작하기",
                onClick = {
                    onLoginClick(email, password)
                },
                enabled = canSubmitSignUp(termsAccepted, privacyAccepted),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LegalConsentRow(
    checked: Boolean,
    linkText: String,
    onCheckedChange: (Boolean) -> Unit,
    onLinkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularCheckBox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = linkText,
                color = PrimaryBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onLinkClick)
            )

            Text(
                text = " 동의(필수)",
                color = NavyText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CircularCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (checked) PrimaryBlue
                else Color.Transparent
            )
            .border(
                width = 1.5.dp,
                color = if (checked) PrimaryBlue else SecondaryText,
                shape = CircleShape
            )
            .clickable {onCheckedChange(!checked)},
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    SignUpScreen()
}
