package com.everytrip.app.feature.chatbot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.core.designsystem.component.MainTopBar
import com.everytrip.app.feature.chatbot.data.ChatPlace
import com.everytrip.app.feature.region.presentation.search.TourismImage
import com.everytrip.app.ui.theme.BodyText
import com.everytrip.app.ui.theme.Border
import com.everytrip.app.ui.theme.Chat
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.PrimaryBlueDeep
import com.everytrip.app.ui.theme.ProjectTheme
import com.everytrip.app.ui.theme.SecondaryText

@Composable
fun AiChatbotScreen(
    viewModel: ChatViewModel,
    onPlaceClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    AiChatbotContent(
        uiState = uiState,
        onSendMessage = viewModel::sendMessage,
        onPlaceClick = onPlaceClick,
        onErrorShown = viewModel::consumeError,
    )
}

@Composable
private fun AiChatbotContent(
    uiState: ChatUiState,
    onSendMessage: (String) -> Unit,
    onPlaceClick: (String) -> Unit,
    onErrorShown: () -> Unit,
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        topBar = { MainTopBar(title = "Every Trip AI") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                enabled = !uiState.isLoading,
                onInputChanged = { inputText = it },
                onSendClicked = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
            )
        },
        containerColor = Color.White,
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(uiState.messages) { message ->
                when (message) {
                    is ChatMessage.User -> UserMessageBubble(message.text)
                    is ChatMessage.Bot -> {
                        BotMessageBubble(message.text)
                        if (message.places.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "관련 촬영지",
                                color = BodyText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            message.places.forEach { place ->
                                ChatPlaceCard(place, onClick = { onPlaceClick(place.placeId) })
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            if (uiState.isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryBlue)
                        Spacer(Modifier.width(10.dp))
                        Text("답변을 찾고 있어요…", color = SecondaryText, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BotMessageBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            Modifier.wrapContentWidth()
                .widthIn(max = 340.dp)
                .background(Chat, RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(text, color = BodyText, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun UserMessageBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier.wrapContentWidth()
                .widthIn(max = 340.dp)
                .background(PrimaryBlue, RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(text, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun ChatPlaceCard(place: ChatPlace, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(116.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(Color.White),
    ) {
        TourismImage(place.posterUrl, place.placeName, Modifier.width(112.dp).fillMaxSize())
        Column(
            modifier = Modifier.weight(1f).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                place.placeName,
                color = BodyText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            place.workTitle?.takeIf(String::isNotBlank)?.let {
                Text(it, color = PrimaryBlue, fontSize = 13.sp, maxLines = 1)
            }
            place.sceneDesc?.takeIf(String::isNotBlank)?.let {
                Text(it, color = SecondaryText, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.weight(1f))
            val address = place.address?.takeIf(String::isNotBlank)
                ?: listOfNotNull(place.sido, place.sigungu).joinToString(" ")
            if (address.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = SecondaryText, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(address, color = SecondaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    enabled: Boolean,
    onInputChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .border(width = 1.dp, color = Border.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChanged,
            enabled = enabled,
            placeholder = { Text("작품이나 촬영지를 물어보세요", color = SecondaryText, fontSize = 14.sp) },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp, max = 140.dp),
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Chat,
                unfocusedContainerColor = Chat,
            ),
            singleLine = false,
            minLines = 1,
            maxLines = 5,
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSendClicked,
            enabled = enabled && inputText.isNotBlank(),
            modifier = Modifier.size(48.dp).clip(CircleShape)
                .background(if (enabled && inputText.isNotBlank()) PrimaryBlueDeep else Border),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "전송", tint = Color.White)
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 820)
@Composable
private fun AiChatbotScreenPreview() {
    ProjectTheme {
        AiChatbotContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage.Bot("안녕하세요! 작품 촬영지와 여행 정보를 물어보세요."),
                    ChatMessage.User("눈물의 여왕 촬영지 알려줘"),
                    ChatMessage.Bot(
                        text = "관련 촬영지 정보를 찾았습니다.",
                        intent = "place_search",
                        places = listOf(
                            ChatPlace(
                                placeId = "596",
                                placeName = "여의도순복음교회",
                                workTitle = "눈물의 여왕",
                                address = "서울 영등포구 국회대로76길 68",
                                sceneDesc = "촬영 장면 설명",
                            ),
                        ),
                    ),
                ),
            ),
            onSendMessage = {},
            onPlaceClick = {},
            onErrorShown = {},
        )
    }
}
