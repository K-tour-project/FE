package com.everytrip.app.feature.mypage.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.everytrip.app.core.designsystem.component.DefaultProfileImage
import com.everytrip.app.core.designsystem.component.PasswordTextField
import com.everytrip.app.feature.region.presentation.search.loadTourismBitmap
import com.everytrip.app.ui.theme.BodyText
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.FavoritePink
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.ProjectTheme
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun SettingsRoute(
    viewModel: FavoriteViewModel,
    authProvider: String?,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        when {
            mimeType !in setOf("image/jpeg", "image/png", "image/webp") ->
                viewModel.showSettingsMessage("JPEG, PNG, WebP 이미지만 사용할 수 있습니다.")
            bytes == null -> viewModel.showSettingsMessage("이미지를 읽을 수 없습니다.")
            bytes.size > 5 * 1024 * 1024 -> viewModel.showSettingsMessage("이미지는 최대 5MB까지 가능합니다.")
            else -> viewModel.updateProfileImage(bytes, mimeType, "profile.${mimeType.substringAfter('/')}" )
        }
    }
    SettingsScreen(
        profileName = state.myPage.nickname.ifBlank { "사용자" },
        profileEmail = state.myPage.email,
        profileImageUrl = state.myPage.profileImageUrl,
        authProvider = authProvider,
        isLoading = state.isSettingsLoading,
        message = state.settingsMessage,
        onBackClick = onBackClick,
        onProfileEditClick = { imageLauncher.launch("image/*") },
        onRemoveProfileImage = viewModel::removeProfileImage,
        onNicknameChange = viewModel::updateNickname,
        onPasswordChange = { current, new -> viewModel.changePassword(current, new, onLogoutClick) },
        onDeleteAccount = { viewModel.deleteAccount(onLogoutClick) },
        onMessageDismiss = viewModel::clearSettingsMessage,
        onLogoutClick = onLogoutClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profileName: String = "lee neng",
    profileEmail: String = "leeneng@example.com",
    profileImageUrl: String? = null,
    authProvider: String? = "local",
    isLoading: Boolean = false,
    message: String? = null,
    onBackClick: () -> Unit = {},
    onProfileEditClick: () -> Unit = {},
    onRemoveProfileImage: () -> Unit = {},
    onNicknameChange: (String) -> Unit = {},
    onPasswordChange: (currentPassword: String, newPassword: String) -> Unit = { _, _ -> },
    onDeleteAccount: () -> Unit = {},
    onMessageDismiss: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    var passwordDialog by remember { mutableStateOf(false) }
    var nicknameDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var profileDialog by remember { mutableStateOf(false) }
    val isSocialAccount = authProvider
        ?.lowercase()
        ?.let { it !in setOf("local", "email", "password") }
        ?: false

    Column(Modifier.fillMaxSize().background(Color(0xFFFAFCFF))) {
        CenterAlignedTopAppBar(
            title = { Text("설정", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyText) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로가기", tint = NavyText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFCFF)),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SettingsProfileCard(profileName, profileEmail, profileImageUrl) { profileDialog = true } }
            item {
                SettingsSection("계정 관리", listOf(
                    SettingItem(Icons.Outlined.Edit, "닉네임 변경", onClick = { nicknameDialog = true }),
                    SettingItem(
                        icon = Icons.Outlined.Lock,
                        title = "비밀번호 변경",
                        onClick = { passwordDialog = true },
                    ),
                    SettingItem(
                        icon = Icons.Outlined.DeleteOutline,
                        title = "회원 탈퇴",
                        color = FavoritePink,
                        onClick = { deleteDialog = true },
                    ),
                ))
            }
            item {
                SettingsSection("기타", listOf(
                    SettingItem(Icons.Outlined.Description, "이용약관"),
                    SettingItem(Icons.Outlined.PrivacyTip, "개인정보 처리방침"),
                ))
            }
            item { LogoutButton(onLogoutClick) }
        }
    }

    if (passwordDialog) {
        if (isSocialAccount) {
            SocialAccountPasswordDialog(onDismiss = { passwordDialog = false })
        } else {
            PasswordChangeDialog(
                onDismiss = { passwordDialog = false },
                onChange = { current, new ->
                    onPasswordChange(current, new)
                    passwordDialog = false
                },
            )
        }
    }
    if (nicknameDialog) {
        NicknameChangeDialog(
            currentNickname = profileName,
            onDismiss = { nicknameDialog = false },
            onChange = { onNicknameChange(it); nicknameDialog = false },
        )
    }
    if (deleteDialog) {
        ConfirmDeleteDialog(
            onDismiss = { deleteDialog = false },
            onConfirm = { onDeleteAccount(); deleteDialog = false },
        )
    }
    if (profileDialog) {
        ProfileImageDialog(
            hasImage = profileImageUrl != null,
            onDismiss = { profileDialog = false },
            onChange = { profileDialog = false; onProfileEditClick() },
            onRemove = { profileDialog = false; onRemoveProfileImage() },
        )
    }
    if (isLoading) {
        Dialog(onDismissRequest = {}) {
            Box(Modifier.size(88.dp).clip(RoundedCornerShape(20.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
    message?.let { SettingsMessageDialog(it, onMessageDismiss) }
}

@Composable
private fun NicknameChangeDialog(currentNickname: String, onDismiss: () -> Unit, onChange: (String) -> Unit) {
    var nickname by remember { mutableStateOf(currentNickname) }
    val trimmed = nickname.trim()
    SettingsDialog(onDismiss) {
        Text("닉네임 변경", color = NavyText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        androidx.compose.material3.OutlinedTextField(
            value = nickname,
            onValueChange = { if (it.length <= 20) nickname = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("새 닉네임") },
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
        )
        Text("앞뒤 공백 제외 2~20자", Modifier.fillMaxWidth().padding(top = 8.dp), color = SecondaryText, fontSize = 14.sp)
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = { onChange(trimmed) },
            enabled = trimmed.length in 2..20 && trimmed != currentNickname,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp),
        ) { Text("변경하기", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ConfirmDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    SettingsDialog(onDismiss) {
        Icon(Icons.Outlined.DeleteOutline, null, tint = FavoritePink, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(14.dp))
        Text("회원 탈퇴", color = NavyText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Text("계정과 모든 데이터가 영구 삭제됩니다.\n정말 탈퇴하시겠어요?", color = SecondaryText, fontSize = 16.sp, lineHeight = 24.sp)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, Modifier.weight(1f).height(54.dp)) { Text("취소") }
            Button(onClick = onConfirm, Modifier.weight(1f).height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = FavoritePink)) { Text("탈퇴하기") }
        }
    }
}

@Composable
private fun ProfileImageDialog(hasImage: Boolean, onDismiss: () -> Unit, onChange: () -> Unit, onRemove: () -> Unit) {
    SettingsDialog(onDismiss) {
        Text("프로필 이미지", color = NavyText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onChange, Modifier.fillMaxWidth().height(52.dp)) { Text("이미지 변경") }
        if (hasImage) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onRemove, Modifier.fillMaxWidth().height(52.dp)) {
                Text("기본 이미지로 변경", color = FavoritePink)
            }
        }
    }
}

@Composable
private fun SettingsMessageDialog(message: String, onDismiss: () -> Unit) {
    SettingsDialog(onDismiss) {
        Text("안내", color = NavyText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        Text(message, color = BodyText, fontSize = 16.sp, lineHeight = 24.sp)
        Spacer(Modifier.height(22.dp))
        Button(onClick = onDismiss, Modifier.fillMaxWidth(0.58f).height(52.dp)) { Text("닫기") }
    }
}

@Composable
private fun PasswordChangeDialog(
    onDismiss: () -> Unit,
    onChange: (String, String) -> Unit,
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val isValid = newPassword.length >= 8 &&
        newPassword.length <= 72 && newPassword.toByteArray(Charsets.UTF_8).size <= 72 &&
        newPassword.any(Char::isLetter) && newPassword.any(Char::isDigit) &&
        currentPassword.isNotBlank() && currentPassword != newPassword

    SettingsDialog(onDismiss) {
        Text("비밀번호 변경", color = NavyText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        PasswordTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            placeholder = "현재 비밀번호",
            modifier = Modifier.height(64.dp),
            contentDescription = "현재 비밀번호",
        )
        Spacer(Modifier.height(14.dp))
        PasswordTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = "새 비밀번호",
            modifier = Modifier.height(64.dp),
            contentDescription = "새 비밀번호",
        )
        Text(
            "영문, 숫자 포함 8자 이상 · 최대 72바이트",
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            color = SecondaryText,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("취소", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            Button(
                onClick = { onChange(currentPassword, newPassword) },
                enabled = isValid,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            ) { Text("변경하기", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SocialAccountPasswordDialog(onDismiss: () -> Unit) {
    SettingsDialog(onDismiss) {
        Text("안내", color = NavyText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier.size(92.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Groups, null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
        }
        Text(
            "소셜 로그인 계정",
            modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(PrimaryBlue.copy(alpha = 0.10f))
                .padding(horizontal = 16.dp, vertical = 7.dp),
            color = PrimaryBlue,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            "현재 계정은 소셜 로그인 계정이에요.\n비밀번호는 앱에서 변경할 수 없어요.",
            color = NavyText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 27.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "로그인한 서비스에서 계정 정보를\n관리해 주세요.",
            color = SecondaryText,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(0.58f).height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        ) { Text("닫기", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun SettingsDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                ) { Icon(Icons.Outlined.Close, "닫기", tint = SecondaryText) }
            }
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FavoritePink),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text("로그아웃", fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsProfileCard(
    name: String,
    email: String,
    imageUrl: String?,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(76.dp).clip(CircleShape).background(Color(0xFFE6F1FF)),
                contentAlignment = Alignment.Center,
            ) {
                val bitmap by produceState<android.graphics.Bitmap?>(null, imageUrl) {
                    value = loadTourismBitmap(imageUrl)
                }
                bitmap?.let {
                    Image(it.asImageBitmap(), name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } ?: DefaultProfileImage(Modifier.fillMaxSize(), "$name 프로필 이미지")
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = NavyText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(email, color = SecondaryText, fontSize = 14.sp, maxLines = 1)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, PrimaryBlue, RoundedCornerShape(18.dp))
                    .clickable(onClick = onEditClick)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text("프로필 수정", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val value: String? = null,
    val color: Color = BodyText,
    val onClick: () -> Unit = {},
)

@Composable
private fun SettingsSection(title: String, items: List<SettingItem>) {
    Column {
        Text(
            text = title,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
            color = NavyText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(62.dp).clickable(onClick = item.onClick).padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(item.icon, null, tint = item.color, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(item.title, Modifier.weight(1f), color = item.color, fontSize = 16.sp)
                    item.value?.let {
                        Text(it, color = SecondaryText, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = SecondaryText, modifier = Modifier.size(24.dp))
                }
                if (index < items.lastIndex) {
                    Box(Modifier.fillMaxWidth().padding(start = 58.dp).height(1.dp).background(Border))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SettingsScreenPreview() {
    ProjectTheme { SettingsScreen() }
}
