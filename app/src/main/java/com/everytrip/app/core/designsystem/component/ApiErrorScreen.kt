package com.everytrip.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.R
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun ApiErrorScreen(
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.api_error),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(210.dp),
        )

        Spacer(Modifier.height(36.dp))

        Text(
            text = "화면을 불러오지 못했어요",
            color = NavyText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "인터넷 연결이 불안정하거나 서버 응답이\n지연되고 있어요. 잠시 후 다시 시도해주세요.",
            color = SecondaryText,
            fontSize = 17.sp,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onRetryClick,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                ),
            ) {
                Text("다시 시도", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
            ) {
                Text("뒤로 가기", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
