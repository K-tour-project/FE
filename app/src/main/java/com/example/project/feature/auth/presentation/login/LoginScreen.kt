package com.example.project.feature.auth.presentation.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.R
import com.example.project.core.designsystem.component.AppTextField
import com.example.project.core.designsystem.component.AppTopBar
import com.example.project.core.designsystem.component.GradientButton
import com.example.project.core.designsystem.component.PasswordTextField
import com.example.project.ui.theme.BodyText
import com.example.project.ui.theme.NavyText
import com.example.project.ui.theme.PrimaryBlue
import com.example.project.ui.theme.PrimaryBlueDeep
import com.example.project.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {},
    onFindPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                text = "Every Trip에 오신 것을 환영합니다.\n나만의 여행을 이어가세요.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = NavyText
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 소셜 로그인 영역: 로그인 화면 전용
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LoginSocialIcon("카카오", Color(0xFFFEE500), R.drawable.kakao_logo, 32.dp)
                LoginSocialIcon("네이버", Color(0xFF03C75A), R.drawable.naver_logo, 22.dp)
                LoginSocialIcon("구글", Color.White, R.drawable.google_logo, 30.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "또는" 구분선: 로그인 화면 전용
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = SecondaryText
                )

                Text(
                    text = "또는",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = BodyText
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 이메일 입력칸
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "이메일을 입력해 주세요",
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "비밀번호 찾기",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable(onClick = onFindPasswordClick)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton(
                text = "로그인",
                onClick = {
                    onLoginClick(email, password)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "아직 계정이 없으신가요? ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BodyText
                )

                Text(
                    text = "회원가입",
                    fontSize = 14.sp,
                    color = PrimaryBlueDeep,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToSignUp)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LoginSocialIcon(
    name: String,
    backgroundColor: Color,
    @DrawableRes iconRes: Int,
    iconSize: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = 1.5.dp,
                    color = Color.LightGray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "$name 로그인",
                modifier = Modifier.size(iconSize)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = BodyText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}
