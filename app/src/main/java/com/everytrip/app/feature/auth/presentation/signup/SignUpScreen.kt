package com.everytrip.app.feature.auth.presentation.signup

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

internal fun canSubmitSignUp(
    termsAccepted: Boolean,
    privacyAccepted: Boolean,
): Boolean = termsAccepted && privacyAccepted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    uiState: SignUpUiState = SignUpUiState(),
    onBackClick: () -> Unit = {},
    onSendCodeClick: (String) -> Unit = {},
    onVerifyCodeClick: (String, String) -> Unit = { _, _ -> },
    onSignUpClick: (String, String, String, String, String?) -> Unit = { _, _, _, _, _ -> },
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onMessageShown: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordCheck by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var termsAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var verificationTimeLeft by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.expiresIn, uiState.codeSent) {
        if (uiState.codeSent && uiState.expiresIn > 0) {
            verificationTimeLeft = uiState.expiresIn
        }
    }

    LaunchedEffect(verificationTimeLeft) {
        if (verificationTimeLeft > 0 && !uiState.emailVerified) {
            delay(1.seconds)
            verificationTimeLeft -= 1
        }
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageShown()
    }

    Scaffold(
        topBar = {
            AppTopBar(title1 = "Every", title2 = "Trip", onBackClick = onBackClick)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Every Trip에 오신 것을 환영합니다.\n이메일 인증 후 가입을 완료해 주세요.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = NavyText,
            )

            Spacer(modifier = Modifier.height(22.dp))
            ProfilePhotoPicker(
                selectedImageUri = profileImageUri,
                onDefaultImageSelected = { profileImageUri = null },
                onGalleryImageSelected = { profileImageUri = it },
            )
            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        verificationCode = ""
                    },
                    placeholder = "이메일 주소를 입력해 주세요",
                    leadingIcon = Icons.Outlined.Email,
                    contentDescription = "이메일",
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedActionButton(
                    text = when {
                        uiState.isSendingCode -> "발송 중"
                        verificationTimeLeft > 0 -> verificationTimeLeft.toTimerText()
                        uiState.codeSent -> "재전송"
                        else -> "코드 전송"
                    },
                    enabled = !uiState.isSendingCode && email.isNotBlank(),
                    onClick = { onSendCodeClick(email) },
                    modifier = Modifier.width(92.dp),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextField(
                    value = verificationCode,
                    onValueChange = { input ->
                        verificationCode = input.filter { it.isDigit() }.take(6)
                    },
                    placeholder = "인증코드 6자리",
                    leadingIcon = Icons.Outlined.VerifiedUser,
                    contentDescription = "인증코드",
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledActionButton(
                    text = if (uiState.emailVerified) "완료" else "확인",
                    enabled = uiState.codeSent &&
                        !uiState.emailVerified &&
                        !uiState.isVerifyingCode &&
                        verificationCode.length == 6,
                    onClick = { onVerifyCodeClick(email, verificationCode) },
                    modifier = Modifier.width(92.dp),
                )
            }

            if (uiState.codeSent && !uiState.emailVerified) {
                Text(
                    text = "인증코드가 이메일로 발송되었습니다.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 12.dp),
                    fontSize = 12.sp,
                    color = SecondaryText,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = nickname,
                onValueChange = { nickname = it },
                placeholder = "닉네임을 입력해 주세요",
                leadingIcon = Icons.Outlined.Person,
                contentDescription = "닉네임",
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "비밀번호를 입력해 주세요",
            )

            Text(
                text = "8자 이상, 영문과 숫자 포함, 최대 72바이트",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, start = 12.dp),
                fontSize = 12.sp,
                color = SecondaryText,
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordTextField(
                value = passwordCheck,
                onValueChange = { passwordCheck = it },
                placeholder = "비밀번호를 다시 입력해 주세요",
                contentDescription = "비밀번호 확인",
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = SecondaryText.copy(alpha = 0.35f))

            Spacer(modifier = Modifier.height(20.dp))

            LegalConsentRow(
                checked = termsAccepted,
                linkText = "이용약관",
                onCheckedChange = { termsAccepted = it },
                onLinkClick = onTermsClick,
            )
            LegalConsentRow(
                checked = privacyAccepted,
                linkText = "개인정보 처리방침",
                onCheckedChange = { privacyAccepted = it },
                onLinkClick = onPrivacyClick,
            )

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = if (uiState.isSigningUp) "가입 중" else "회원가입 완료",
                onClick = {
                    onSignUpClick(
                        email,
                        password,
                        passwordCheck,
                        nickname,
                        profileImageUri?.toString(),
                    )
                },
                enabled = canSubmitSignUp(termsAccepted, privacyAccepted) &&
                    uiState.emailVerified &&
                    !uiState.isSigningUp,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfilePhotoPicker(
    selectedImageUri: Uri?,
    onDefaultImageSelected: () -> Unit,
    onGalleryImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSourceDialog by remember { mutableStateOf(false) }
    val galleryLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let(onGalleryImageSelected)
    }
    val selectedBitmap by selectedImageBitmap(selectedImageUri)

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("프로필 사진 선택") },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onDefaultImageSelected()
                            showSourceDialog = false
                        },
                    ) { Text("기본 이미지") }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showSourceDialog = false
                            galleryLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        },
                    ) { Text("갤러리에서 선택") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceDialog = false }) { Text("취소") }
            },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(96.dp).clickable { showSourceDialog = true },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F7FF))
                    .border(1.5.dp, PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selectedBitmap != null) {
                    Image(
                        bitmap = selectedBitmap!!,
                        contentDescription = "선택한 프로필 사진",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFF9DBEFF),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "프로필 사진 선택",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "프로필 사진을 선택해 주세요",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavyText,
        )
    }
}

@Composable
private fun selectedImageBitmap(uri: Uri?): androidx.compose.runtime.State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = if (uri == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PrimaryBlue,
            disabledContainerColor = Color.White,
            disabledContentColor = SecondaryText,
        ),
        border = BorderStroke(1.dp, if (enabled) PrimaryBlue else SecondaryText.copy(alpha = 0.45f)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun FilledActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFDDEAFF),
            contentColor = PrimaryBlue,
            disabledContainerColor = Color(0xFFE8EEF7),
            disabledContentColor = SecondaryText,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Text(
            text = text,
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
    onLinkClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularCheckBox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = linkText,
                color = PrimaryBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onLinkClick),
            )
            Text(
                text = " 동의(필수)",
                color = NavyText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun CircularCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(21.dp)
            .clip(CircleShape)
            .background(if (checked) PrimaryBlue else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) PrimaryBlue else SecondaryText,
                shape = CircleShape,
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    SignUpScreen()
}
