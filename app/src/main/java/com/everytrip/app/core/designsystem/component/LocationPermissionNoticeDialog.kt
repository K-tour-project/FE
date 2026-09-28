package com.everytrip.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun LocationPermissionNoticeDialog(
    onAllowClick: () -> Unit,
    onLaterClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFEAF2FF), CircleShape)
                        .padding(14.dp),
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "위치정보 이용 안내",
                    color = NavyText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Every Trip은 현재 위치를 기반으로 주변 관광지를 안내하기 위해 위치 권한을 사용합니다.",
                    color = SecondaryText,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "위치 권한은 선택 사항이며, 허용하지 않아도 위치 기반 기능을 제외한 서비스를 이용할 수 있습니다.",
                    color = SecondaryText,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onAllowClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "위치 권한 허용하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onLaterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF9FAFC),
                        contentColor = NavyText,
                    ),
                ) {
                    Text(
                        text = "다음에 하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
