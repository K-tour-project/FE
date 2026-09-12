package com.everytrip.app.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.SecondaryText

private data class BottomNavigationItem(val label: String, val icon: ImageVector)

private val items = listOf(
    BottomNavigationItem("홈", Icons.Default.Home),
    BottomNavigationItem("챗봇", Icons.Default.SmartToy),
    BottomNavigationItem("작품 검색", Icons.Default.Movie),
    BottomNavigationItem("장소 검색", Icons.Default.Place),
    BottomNavigationItem("마이페이지", Icons.Default.Person),
)

@Composable
fun AppBottomNavigationBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = if (selectedIndex == index) {
                    { Text(item.label, fontSize = 14.sp) }
                } else {
                    null
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = SecondaryText,
                    unselectedTextColor = SecondaryText,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
