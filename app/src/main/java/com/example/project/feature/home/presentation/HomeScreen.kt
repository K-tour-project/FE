package com.example.project.feature.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryBlue = Color(0xFF0038FF)
val BackgroundGray = Color(0xFFF7F7F7)

data class Category(val name: String, val iconResId: Int?)
data class Destination(val name: String, val description: String, val imageResId: Int?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToDestination: (Destination) -> Unit = {}
) {
    val categories = listOf(
        Category("해변", null /* TODO: ic_beach 아이콘 추가 */),
        Category("도시", null /* TODO: ic_city 아이콘 추가 */),
        Category("자연", null /* TODO: ic_nature 아이콘 추가 */),
        Category("모험", null /* TODO: ic_adventure 아이콘 추가 */),
    )
    val destinations = listOf(
        Destination("파리, 프랑스", "예술과 낭만의 도시를 만끽하세요!", null /* TODO: img_paris 이미지 추가 */),
        Destination("몰디브", "청정 바다와 프라이빗 비치에서 휴식을...", null /* TODO: img_maldives 이미지 추가 */),
        Destination("미국 그랜드 캐년", "대자연의 웅장함을 직접 느껴보세요!", null /* TODO: img_grand_canyon 이미지 추가 */),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Every Trip",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                },
                actions = {
                    IconButton(onClick = { /* 알림 설정/확인 등 */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "알림", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. 검색 바
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HomeSearchBar(onClick = onNavigateToSearch)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. 카테고리 섹션
            item {
                SectionTitle("카테고리 탐색")
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(category) {
                            /* TODO: 해당 카테고리 여행지 목록으로 이동 */
                        }
                    }
                    item { Spacer(modifier = Modifier.width(16.dp)) } // 마지막 아이템 여백
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. 추천 여행지 섹션
            item {
                SectionTitle("추천 여행지")
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(destinations) { destination ->
                RecommendationCard(destination, onNavigateToDestination)
                Spacer(modifier = Modifier.height(16.dp))
            }
            item { Spacer(modifier = Modifier.height(16.dp)) } // 하단 여백
        }
    }
}

@Composable
fun HomeSearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "검색 아이콘", tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Text("여행지를 검색해보세요!", color = Color.Gray, fontSize = 16.sp)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 12.dp)
    )
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (category.iconResId != null) {
                Image(
                    painter = painterResource(id = category.iconResId),
                    contentDescription = category.name,
                    modifier = Modifier.size(40.dp)
                    /* TODO: 아이콘 크기, 간격 등 조정 */
                )
            } else {
                // TODO: 여기에 아이콘 이미지를 painterResource(id = ...)를 이용해 넣어주세요.
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(40.dp), tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(category.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun RecommendationCard(destination: Destination, onClick: (Destination) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick(destination) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (destination.imageResId != null) {
                Image(
                    painter = painterResource(id = destination.imageResId),
                    contentDescription = destination.name,
                    contentScale = ContentScale.Crop, // 이미지 자르기 방식
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            } else {
                // TODO: 여기에 여행지 이미지를 painterResource(id = ...)를 이용해 넣어주세요.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(PrimaryBlue.copy(alpha = 0.1f)) // 임시 색상
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("추천 여행지 이미지 자리", color = PrimaryBlue)
                }
            }
            // ---------------------------------------------
            Column(modifier = Modifier.padding(16.dp)) {
                Text(destination.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(destination.description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTravelHomeScreen() {
    HomeScreen()
}