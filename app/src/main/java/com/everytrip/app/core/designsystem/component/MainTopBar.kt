package com.everytrip.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    onIconClick: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    showBadge: Boolean = false,
    showActionBorder: Boolean = false,
) {
    TopAppBar(
        modifier = Modifier.height(80.dp),
        title = {
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.clickable(onClick = onTitleClick),
            )
        },
        actions = {
            if (icon != null) Box {
                IconButton(
                    onClick = onIconClick,
                    modifier = Modifier
                        .size(48.dp)
                        .then(
                            if (showActionBorder) Modifier.border(
                                width = 1.dp,
                                color = PrimaryBlue,
                                shape = RoundedCornerShape(14.dp),
                            ) else Modifier
                        ),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }

                if (showBadge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 9.dp, end = 9.dp)
                            .size(7.dp)
                            .background(Color(0xFF2878E8), CircleShape)
                    )
                }
            }
        }
    )
}
