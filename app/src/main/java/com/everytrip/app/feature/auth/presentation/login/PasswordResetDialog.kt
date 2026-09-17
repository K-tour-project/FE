package com.everytrip.app.feature.auth.presentation.login

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.everytrip.app.core.designsystem.component.AppTextField
import com.everytrip.app.core.designsystem.component.PasswordTextField
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun PasswordResetDialog(
    state: PasswordResetUiState,
    onEmailChanged: (String) -> Unit,
    onSendCode: (String) -> Unit,
    onVerifyCode: (String, String) -> Unit,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordCheck by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(state.resendAvailableAt, state.codeExpiresAt) {
        while (now < state.resendAvailableAt || now < state.codeExpiresAt) {
            delay(1.seconds)
            now = SystemClock.elapsedRealtime()
        }
    }
    val resendSeconds = ((state.resendAvailableAt - now + 999) / 1000).coerceAtLeast(0)
    val codeSeconds = ((state.codeExpiresAt - now + 999) / 1000).coerceAtLeast(0)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).widthIn(max = 420.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                Text("비밀번호 찾기", color = NavyText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("가입한 이메일을 인증하고 새 비밀번호를 입력해 주세요.", color = SecondaryText, fontSize = 14.sp)
                Spacer(Modifier.height(22.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AppTextField(
                        value = email,
                        onValueChange = { email = it; code = ""; onEmailChanged(it) },
                        placeholder = "이메일 주소를 입력해 주세요",
                        leadingIcon = Icons.Outlined.Email,
                        contentDescription = "이메일",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    ResetCodeButton(
                        if (state.isSending) "발송 중" else if (resendSeconds > 0) "${resendSeconds}초" else if (state.codeSent) "재전송" else "코드 전송",
                        enabled = email.isNotBlank() && !state.isSending && resendSeconds == 0L && !state.isConfirming,
                        filled = false,
                    ) {
                        onSendCode(email)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AppTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                        placeholder = "인증코드 6자리",
                        leadingIcon = Icons.Outlined.VerifiedUser,
                        contentDescription = "인증코드",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    ResetCodeButton(if (state.verified) "인증 완료" else if (state.isVerifying) "확인 중" else "인증 확인",
                        enabled = state.codeSent && !state.verified && !state.isVerifying &&
                            codeSeconds > 0 && code.length == 6,
                        filled = true,
                    ) {
                        onVerifyCode(email, code)
                    }
                }
                if (state.codeSent && !state.verified) {
                    Text("인증코드 유효 시간 ${codeSeconds / 60}:${"%02d".format(codeSeconds % 60)}",
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp), color = SecondaryText, fontSize = 12.sp)
                }
                Spacer(Modifier.height(16.dp))
                PasswordTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "새 비밀번호",
                    contentDescription = "새 비밀번호",
                )
                Text(
                    "8자 이상, 영문과 숫자 포함, 최대 72바이트",
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                    color = SecondaryText,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(10.dp))
                PasswordTextField(
                    value = passwordCheck,
                    onValueChange = { passwordCheck = it },
                    placeholder = "새 비밀번호 확인",
                    contentDescription = "새 비밀번호 확인",
                )
                if (state.message != null) {
                    Text(state.message, modifier = Modifier.padding(top = 12.dp), color = PrimaryBlue, fontSize = 13.sp)
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(13.dp),
                        border = BorderStroke(1.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                    ) { Text("취소", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { onConfirm(newPassword, passwordCheck) },
                        enabled = state.verified && !state.isConfirming && newPassword.isNotBlank(),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    ) { Text(if (state.isConfirming) "변경 중" else "확인", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ResetCodeButton(text: String, enabled: Boolean, filled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.width(88.dp).height(52.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) Color(0xFFDDEAFF) else Color.White,
            contentColor = PrimaryBlue,
            disabledContainerColor = if (filled) Color(0xFFE8EEF7) else Color.White,
            disabledContentColor = SecondaryText,
        ),
        border = if (filled) null else BorderStroke(1.dp, if (enabled) PrimaryBlue else SecondaryText.copy(alpha = 0.45f)),
        contentPadding = PaddingValues(horizontal = 4.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
}
