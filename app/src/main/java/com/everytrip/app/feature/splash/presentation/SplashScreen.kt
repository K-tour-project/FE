package com.everytrip.app.feature.splash.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.R
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue

@Composable
fun SplashScreen() {
    val taglineStart = stringResource(R.string.splash_tagline_start)
    val taglineMiddle = stringResource(R.string.splash_tagline_middle)
    val taglineEnd = stringResource(R.string.splash_tagline_end)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val logoSize = (maxWidth * 0.36f).coerceAtMost(148.dp)

        Image(
            painter = painterResource(R.drawable.start_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * 0.21f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(logoSize),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = NavyText)) { append("Every ") }
                    withStyle(SpanStyle(color = PrimaryBlue)) { append("Trip") }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.splash_brand_korean),
                color = Color(0xFF7B8798),
                fontSize = 17.sp,
            )
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                    append(taglineStart)
                }
                withStyle(SpanStyle(color = NavyText)) {
                    append(taglineMiddle)
                }
                withStyle(SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                    append(taglineEnd)
                }
            },
            fontSize = 19.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * 0.56f),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                color = PrimaryBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(30.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.splash_loading),
                color = Color(0xFF8A96A8),
                fontSize = 15.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
