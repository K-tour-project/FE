package com.everytrip.app.feature.auth.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.everytrip.app.core.designsystem.component.AppTextField
import com.everytrip.app.core.designsystem.component.AppTopBar
import com.everytrip.app.core.designsystem.component.GradientButton
import com.everytrip.app.core.designsystem.component.PasswordTextField
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private const val VerificationTimeoutSeconds = 180

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
    var verificationCode by remember { mutableStateOf("") }
    var verificationTimeLeft by remember { mutableIntStateOf(0) }
    var hasRequestedVerification by remember { mutableStateOf(false) }

    LaunchedEffect(verificationTimeLeft) {
        if (verificationTimeLeft > 0) {
            delay(1.seconds)
            verificationTimeLeft -= 1
        }
    }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Every Trip에 오신 것을 환영합니다.\n나만의 여행을 시작해보세요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = NavyText
            )

            Spacer(modifier = Modifier.height(22.dp))

            ProfilePhotoPicker()

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이메일 입력칸
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "이메일 주소를 입력해주세요",
                    leadingIcon = Icons.Outlined.Email,
                    contentDescription = "이메일",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedActionButton(
                    text = when {
                        verificationTimeLeft > 0 -> verificationTimeLeft.toTimerText()
                        hasRequestedVerification -> "재전송"
                        else -> "코드 전송"
                    },
                    onClick = {
                        hasRequestedVerification = true
                        verificationTimeLeft = VerificationTimeoutSeconds
                    },
                    modifier = Modifier.width(87.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it.take(6) },
                    placeholder = "인증코드 6자리",
                    leadingIcon = Icons.Outlined.VerifiedUser,
                    contentDescription = "인증번호",
                    modifier = Modifier.width(160.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                FilledActionButton(
                    onClick = {},
                    modifier = Modifier.width(87.dp)
                )
            }

            if (verificationTimeLeft > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "인증번호가 이메일로 발송되었습니다.",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 닉네임 입력칸
            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "닉네임을 입력해주세요",
                leadingIcon = Icons.Outlined.Person,
                contentDescription = "회원"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 입력칸
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "비밀번호를 입력해 주세요"
            )

            Text(
                text = "영문, 숫자, 특수문자 포함 8자 이상",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, start = 12.dp),
                fontSize = 12.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 확인 입력칸
            PasswordTextField(
                value = passwordCheck,
                onValueChange = { passwordCheck = it },
                placeholder = "비밀번호를 다시 입력해주세요",
                contentDescription = "비밀번호 확인"
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
private fun ProfilePhotoPicker(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F7FF))
                    .border(1.5.dp, PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color(0xFF9DBEFF)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "프로필 사진 선택",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "프로필 사진을 선택하세요",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyText
        )
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PrimaryBlue
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilledActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFDDEAFF),
            contentColor = PrimaryBlue
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            text = "확인",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun Int.toTimerText(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
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
            .height(35.dp),
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
            .size(21.dp)
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
