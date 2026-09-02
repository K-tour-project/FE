package com.everytrip.app.feature.auth.presentation.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.R
import com.everytrip.app.core.designsystem.component.AppTextField
import com.everytrip.app.core.designsystem.component.AppTopBar
import com.everytrip.app.core.designsystem.component.GradientButton
import com.everytrip.app.core.designsystem.component.PasswordTextField
import com.everytrip.app.ui.theme.BodyText
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.PrimaryBlueDeep
import com.everytrip.app.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {},
    onFindPasswordClick: () -> Unit = {},
    onKakaoLoginClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    onGuestLoginClick: () -> Unit = {}
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

            SocialLoginSection(
                onKakaoClick = onKakaoLoginClick,
                onGoogleClick = onGoogleLoginClick,
                onGuestClick = onGuestLoginClick
            )

            Spacer(modifier = Modifier.height(28.dp))

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
private fun SocialLoginSection(
    onKakaoClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onGuestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE1E6EF)
            )

            Text(
                text = "또는",
                modifier = Modifier.padding(horizontal = 20.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SecondaryText
            )

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE1E6EF)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        SocialLoginButton(
            text = "카카오로 계속하기",
            iconRes = R.drawable.kakao_logo,
            buttonColor = Color(0xFFFFFBED),
            borderColor = Color(0xFFF7E6A9),
            iconBackgroundColor = Color(0xFFFEE500),
            onClick = onKakaoClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        SocialLoginButton(
            text = "구글로 계속하기",
            iconRes = R.drawable.google_logo,
            buttonColor = Color.White,
            borderColor = Color(0xFFE1E6EF),
            iconBackgroundColor = Color.White,
            onClick = onGoogleClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        GuestLoginButton(onClick = onGuestClick)
    }
}

@Composable
private fun GuestLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = PrimaryBlue

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = borderColor,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                            0f
                        )
                    )
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PrimaryBlue
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "게스트 로그인",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(28.dp),
                tint = PrimaryBlue
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "게스트로 로그인",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBlue
                )

                Text(
                    text = "테스트를 위한 로그인 입니다.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SecondaryText
                )
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    @DrawableRes iconRes: Int,
    buttonColor: Color,
    borderColor: Color,
    iconBackgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(47.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color(0xFF111827)
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    modifier = Modifier.size(25.dp)
                )
            }

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}
