package com.everytrip.app.feature.chatbot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.core.designsystem.component.MainTopBar
import com.everytrip.app.core.designsystem.modifier.dismissKeyboardOnTap
import com.everytrip.app.R
import com.everytrip.app.feature.chatbot.data.ChatCourse
import com.everytrip.app.feature.chatbot.data.ChatCourseStop
import com.everytrip.app.feature.chatbot.data.ChatPlace
import com.everytrip.app.feature.chatbot.data.ChatWork
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
        modifier = Modifier.imePadding(),
        topBar = { MainTopBar(title = "Every Trip AI") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                enabled = !uiState.isLoading,
                onInputChanged = { inputText = it },
                onSendClicked = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
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
            modifier = Modifier.fillMaxSize().dismissKeyboardOnTap().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(uiState.messages) { message ->
                when (message) {
                    is ChatMessage.User -> UserMessageBubble(message.text)
                    is ChatMessage.Bot -> BotMessageGroup(message, onPlaceClick)
                }
            }
            if (uiState.isLoading) {
                item {
                    BotLoadingMessage()
                }
            }
        }
    }
}

@Composable
private fun BotMessageGroup(message: ChatMessage.Bot, onPlaceClick: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        BotProfileImage()
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BotMessageBubble(message.text)
            if (message.intent == "course_recommendation" && message.course != null) {
                ChatCourseCard(message.course, onPlaceClick)
            } else {
                if (message.works.isNotEmpty()) {
                    Text("관련 작품", color = BodyText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    message.works.forEach { work -> ChatWorkCard(work) }
                }
                if (message.places.isNotEmpty()) {
                    Text("관련 촬영지", color = BodyText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    message.places.forEach { place ->
                        ChatPlaceCard(place, onClick = { onPlaceClick(place.placeId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatWorkCard(work: ChatWork) {
    Row(
        modifier = Modifier.fillMaxWidth().height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        TourismImage(
            work.posterUrl,
            work.title,
            Modifier.width(94.dp).fillMaxSize(),
            placeholderText = "포스터 없음",
        )
        Column(
            modifier = Modifier.weight(1f).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                work.title,
                color = BodyText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val details = listOfNotNull(
                work.type?.let { if (it == "MOVIE") "영화" else if (it == "DRAMA") "드라마" else it },
                work.releaseDate?.take(4),
                work.genres?.takeIf { it.isNotEmpty() }?.joinToString(", "),
            )
            if (details.isNotEmpty()) {
                Text(
                    details.joinToString(" · "),
                    color = SecondaryText,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            work.rating?.let { rating ->
                Text("평점 $rating", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            work.overview?.takeIf(String::isNotBlank)?.let { overview ->
                Text(
                    overview,
                    color = SecondaryText,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ChatCourseCard(course: ChatCourse, onPlaceClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("추천 여행 코스", color = BodyText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            course.description?.takeIf(String::isNotBlank)?.let {
                Text(it, color = BodyText, fontSize = 14.sp, lineHeight = 20.sp)
            }
            if (course.description.isNullOrBlank()) {
                val summary = listOfNotNull(
                    course.totalPlaces?.let { "총 ${it}개 장소" },
                    course.estimatedMinutes?.let { "예상 ${it}분" },
                    course.transport?.takeIf(String::isNotBlank),
                )
                if (summary.isNotEmpty()) Text(summary.joinToString(" · "), color = BodyText, fontSize = 14.sp)
            }
            course.stops.orEmpty().sortedBy(ChatCourseStop::order).forEach { stop ->
                ChatCourseStopRow(stop, onClick = { onPlaceClick(stop.placeId) })
            }
        }
    }
}

@Composable
private fun ChatCourseStopRow(stop: ChatCourseStop, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(PrimaryBlue, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(stop.order.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stop.placeName, color = BodyText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            stop.workTitle?.takeIf(String::isNotBlank)?.let {
                Text(it, color = PrimaryBlue, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            stop.address?.takeIf(String::isNotBlank)?.let {
                Text(it, color = SecondaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val timing = listOfNotNull(
                stop.travelMinutesFromPrev?.takeIf { it > 0 }?.let { "이동 ${it}분" },
                stop.stayMinutes?.let { "체류 ${it}분" },
            )
            if (timing.isNotEmpty()) Text(timing.joinToString(" · "), color = SecondaryText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun BotProfileImage() {
    Image(
        painter = painterResource(R.drawable.chatbot),
        contentDescription = "Every Trip AI 프로필",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(40.dp).clip(CircleShape).background(Chat),
    )
}

@Composable
private fun BotMessageBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            Modifier.wrapContentWidth()
                .widthIn(max = 292.dp)
                .background(Chat, RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(text, color = BodyText, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun BotLoadingMessage() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        BotProfileImage()
        Spacer(Modifier.width(9.dp))
        Row(
            modifier = Modifier.background(Chat, RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = PrimaryBlue)
            Spacer(Modifier.width(9.dp))
            Text("답변을 찾고 있어요…", color = SecondaryText, fontSize = 14.sp)
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
