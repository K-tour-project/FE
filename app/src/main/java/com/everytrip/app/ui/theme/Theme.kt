package com.everytrip.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)

private val LightColorScheme = lightColorScheme(
    primary = NavyText,
    onPrimary = Color.White,

    secondary = SecondaryText,

    background = Color.White,
    onBackground = NavyText,

    surface = Color.White,
    onSurface = NavyText,

    surfaceVariant = Color(0xFFF5F7FA),
    onSurfaceVariant = SecondaryText,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,

    outline = Border,
)

@Composable
fun ProjectTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
