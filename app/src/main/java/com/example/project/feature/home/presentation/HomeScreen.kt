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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryBlue = Color(0xFF0038FF)
val BackgroundGray = Color(0xFFF7F7F7)

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
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { /* 알림 설정/확인 등 */ }) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = "알림",
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 9.dp, end = 9.dp)
                                .size(8.dp)
                                .background(Color(0xFF2878E8), CircleShape)
                        )
                    }
                }
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
                Spacer(modifier = Modifier.height(10.dp))
                HomeSearchBar(onClick = onNavigateToSearch)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. 카테고리 섹션
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryItem(
                        modifier = Modifier.weight(1f),
                        label = "작품으로 찾기",
                        icon = Icons.Default.Movie
                    )
                    CategoryItem(
                        modifier = Modifier.weight(1f),
                        label = "작품으로 찾기",
                        icon = Icons.Default.Movie
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. 광고 또는 온보딩 컴포넌트
            item {
                PromotionBanner()
            }

            // 4. 인기 관광지 섹션
            item {
                Spacer(modifier = Modifier.height(15.dp))
                SectionTitle("인기 관광지 Top 5")
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
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

            // 5. 추천 작품 섹션
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("추천 작품")
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
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
        }
    }
}

@Composable
fun HomeSearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE4E8EE)),
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "검색 아이콘", tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Text("작품 또는 지역을 검색해보세요", color = Color.Gray, fontSize = 15.sp)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 12.dp)
    )
}

@Composable
fun CategoryItem(modifier: Modifier, label: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PromotionBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(132.dp)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFEAF5FF), Color(0xFFC8E6F8), Color(0xFF7CB4D9))
                ),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 22.dp)
        ) {
            Text(
                text = "콘텐츠로 떠나는 여행",
                color = Color(0xFF102B4C),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "영화·드라마 촬영지와 여행 코스를 한 번에",
                color = Color(0xFF4C6178),
                fontSize = 13.sp
            )
        }
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.76f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 22.dp)
                .size(72.dp)
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(width = 14.dp, height = 5.dp).background(PrimaryBlue, CircleShape))
            Box(Modifier.size(5.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
            Box(Modifier.size(5.dp).background(Color.White.copy(alpha = 0.8f), CircleShape))
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
                Text(name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTravelHomeScreen() {
    HomeScreen()
}
