package com.example.project.feature.chatbot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatbotScreen() {
    var inputText by remember { mutableStateOf("") }
    val primaryBlue = Color(0xFF003399) // 앱 메인 파란색
    val lightBlueBg = Color(0xFFE8F0FE) // 챗봇 말풍선 배경색

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "에브리 트립 도우미",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* 뒤로가기 액션 */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 닫기 액션 */ }) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onInputChanged = { inputText = it },
                onSendClicked = { /* 전송 액션 */ },
                primaryBlue = primaryBlue
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 첫 번째 챗봇 인사말
            item {
                BotMessageBubble(
                    text = "안녕하세요! 어떤 여행 계획을 도와드릴까요?",
                    backgroundColor = lightBlueBg
                )
            }

            // 두 번째 챗봇 질문
            item {
                BotMessageBubble(
                    text = "다음 중 무엇을 도와드릴까요?",
                    backgroundColor = lightBlueBg
                )
            }

            // 추천 옵션 리스트 (Chip 형태)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    SuggestionChipItem(text = "이 지역의 촬영지 1박 2일 코스 추천")
                    SuggestionChipItem(text = "서울 벚꽃 명소 추천")
                    SuggestionChipItem(text = "혼자서 걷는 코스 추천")
                    SuggestionChipItem(text = "직접 장소 검색")
                }
            }

            // 코스 추천하기 버튼
            item {
                Button(
                    onClick = { /* 코스 추천 액션 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(25.dp),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, lightBlueBg, RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "코스 추천하기",
                            color = primaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BotMessageBubble(text: String, backgroundColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 4.dp // 말풍선 꼬리 디테일
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun SuggestionChipItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
            .clickable { /* 옵션 클릭 액션 */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.DarkGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    primaryBlue: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(), // 하단 네비게이션 바 영역 확보
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChanged,
            placeholder = {
                Text(
                    "원하는 여행을 입력해 주세요.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 전송 버튼 (플로팅 액션 버튼 스타일)
        IconButton(
            onClick = onSendClicked,
            modifier = Modifier
                .size(50.dp)
                .background(primaryBlue, CircleShape)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "전송",
                tint = Color.White,
                modifier = Modifier.padding(start = 4.dp) // 화살표 시각적 중심 보정
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AiChatbotScreenPreview() {
    AiChatbotScreen()
}