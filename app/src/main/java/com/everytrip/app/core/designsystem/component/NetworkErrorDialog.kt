package com.everytrip.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.everytrip.app.R
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.ProjectTheme
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun NetworkErrorDialog(
    onExitClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        NetworkErrorDialogContent(
            onExitClick = onExitClick,
            onRetryClick = onRetryClick,
        )
    }
}

@Composable
private fun NetworkErrorDialogContent(
    onExitClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 24.dp,
                top = 36.dp,
                end = 24.dp,
                bottom = 24.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.network_error),
                contentDescription = null,
                modifier = Modifier.size(76.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "네트워크 연결 오류",
                color = NavyText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "인터넷 연결 상태를 확인한 뒤\n다시 시도해 주세요.",
                color = SecondaryText,
                fontSize = 17.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onExitClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF9FAFC),
                        contentColor = NavyText,
                    ),
                ) {
                    Text(
                        text = "앱 종료",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Button(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "재시도",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview(
    name = "네트워크 연결 오류 팝업",
    showBackground = true,
    backgroundColor = 0xFFE9EEF5,
    widthDp = 390,
    heightDp = 640,
)
@Composable
private fun NetworkErrorDialogPreview() {
    ProjectTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            NetworkErrorDialogContent(
                onExitClick = {},
                onRetryClick = {},
            )
        }
    }
}
