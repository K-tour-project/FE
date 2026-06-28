package com.example.project.feature.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryBlue = Color(0xFF0038FF)
val BackgroundGray = Color(0xFFF7F7F7)

data class Category(val name: String, val description: String, val iconResId: Int?, val gradientColors: List<Color>)
data class Destination(val name: String, val imageResId: Int?)
data class Work(val name: String, val imageResId: Int?)

enum class RecommendationCardType(val aspectRatio: Float) {
    PLACE(1f),
    WORK(2f / 3f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToDestination: (Destination) -> Unit = {},
    onNavigateToWork: (Work) -> Unit = {}
) {
    val categories = listOf(
        Category("작품으로 검색", "작품 속 관광지", null /* TODO: ic_location 아이콘 추가 */, listOf(Color(0xFF3C4AC9), Color(0xFF2439B7))),
        Category("지역으로 검색", "지역 주변 촬영지", null /* TODO: ic_theme 아이콘 추가 */, listOf(Color(0xFF1A73D1), Color(0xFF124ECC))),
    )
    val destinations = listOf(
        Destination("여기는 어디인가", null /* TODO: 이미지 추가 */),
        Destination("노벰버라운지", null /* TODO: 이미지 추가 */),
        Destination("수원 화성", null /* TODO: 이미지 추가 */),
    )
    val works = listOf(
        Work("빠빠라빠삐코", null /* 이미지 추가 */),
        Work("바라밤: 바밤바의 역습", null /* TODO: 이미지 추가 */),
        Work("죠스바", null /* TODO: 이미지 추가 */),
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
                        Icon(Icons.Default.Notifications, contentDescription = "알림", tint = Color.DarkGray)
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
            item {
                Text("어디로 떠나볼까요? ✨",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 23.sp)
            }

            // 1. 검색 바
            item {
                Spacer(modifier = Modifier.height(10.dp))
                HomeSearchBar(onClick = onNavigateToSearch)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. 카테고리 섹션
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        CategoryItem(
                            category = category,
                            modifier = Modifier.weight(1f)
                        ) {
                            /* TODO: 해당 카테고리 여행지 목록으로 이동 */
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. 인기 관광지 섹션
            item {
                Spacer(modifier = Modifier.height(15.dp))
                SectionTitle("인기 관광지 Top 5")
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(destinations) { destination ->
                        RecommendationCard(
                            name = destination.name,
                            imageResId = destination.imageResId,
                            type = RecommendationCardType.PLACE,
                            onClick = { onNavigateToDestination(destination) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }

            // 추천 작품 섹션
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("추천 작품")
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(works) { work ->
                        RecommendationCard(
                            name = work.name,
                            imageResId = work.imageResId,
                            type = RecommendationCardType.WORK,
                            onClick = { onNavigateToWork(work) }
                        )
                    }
                }
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
            .padding(horizontal = 16.dp),
        color = Color(0xFFF7F9FC),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.Gray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "검색 아이콘", tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Text("영화, 드라마, 장소를 검색해보세요", color = Color.Gray, fontSize = 15.sp)
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
fun CategoryItem(category: Category, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(3f / 1.5f)
            .testTag("category_explore_button")
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = category.gradientColors))
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: 여기에 아이콘 이미지를 painterResource(id = ...)를 이용해 넣어주세요.
            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(45.dp), tint = PrimaryBlue)
            Spacer(modifier = Modifier.width(3.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(category.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(category.description, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

        }
    }
}

@Composable
fun RecommendationCard(
    name: String,
    imageResId: Int?,
    type: RecommendationCardType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .aspectRatio(type.aspectRatio)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = name,
                    contentScale = ContentScale.Crop, // 이미지 자르기 방식
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // TODO: 여기에 대체 이미지를 painterResource(id = ...)를 이용해 넣어주세요.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("대체 이미지 자리", color = PrimaryBlue)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp)) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTravelHomeScreen() {
    HomeScreen()
}
