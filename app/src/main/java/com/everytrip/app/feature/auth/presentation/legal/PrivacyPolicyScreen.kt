package com.everytrip.app.feature.auth.presentation.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everytrip.app.ui.theme.NavyText
import com.everytrip.app.ui.theme.PrimaryBlue
import com.everytrip.app.ui.theme.ProjectTheme
import com.everytrip.app.ui.theme.SecondaryText
import kotlinx.coroutines.launch

private val pageBackground = Color(0xFFF6F9FE)
private val lineColor = Color(0xFFE2EAF5)
private const val documentTextSize = 16

@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit = {}) {
    LegalDocumentScreen(
        title = "개인정보 처리방침",
        kicker = "EVERY TRIP  ·  PRIVACY",
        headline = "여행의 순간을 안심하고 기록하세요.",
        introduction = "팀 ISFP는 Every Trip 서비스 이용자의 개인정보를 중요하게 여기며, 「개인정보 보호법」 등 관련 법령을 준수합니다. 이 방침은 수집·이용, 보관·파기 방법을 안내합니다.",
        sections = policySections,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LegalDocumentScreen(
    title: String,
    kicker: String,
    headline: String,
    introduction: String,
    sections: List<Pair<String, String>>,
    onBackClick: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { sections.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = pageBackground,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = NavyText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로가기", tint = NavyText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBackground),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("이전")
                }
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    enabled = pagerState.currentPage < sections.lastIndex,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                ) {
                    Text("다음")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(kicker, Modifier.padding(start = 22.dp, top = 8.dp), color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(headline, Modifier.padding(start = 22.dp, end = 22.dp, top = 5.dp), color = NavyText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                introduction,
                Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp),
                color = SecondaryText, fontSize = 13.sp, lineHeight = 19.sp,
            )
            Text("시행일  2026. 09. 21", Modifier.padding(start = 22.dp, top = 10.dp, bottom = 12.dp), color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1).toFloat() / sections.size },
                modifier = Modifier.fillMaxWidth().height(3.dp), color = PrimaryBlue, trackColor = lineColor,
            )
            Text("좌우로 넘겨 조항 보기", Modifier.padding(horizontal = 22.dp, vertical = 10.dp), color = SecondaryText, fontSize = 12.sp)
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                PolicyPage(page, documentTextSize, sections)
            }
        }
    }
}

@Composable
private fun PolicyPage(page: Int, textSize: Int, sections: List<Pair<String, String>>) {
    val (title, body) = sections[page]
    val blocks = remember(body) { parsePolicyBlocks(body) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("${(page + 1).toString().padStart(2, '0')} / ${sections.size}", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text(title, color = NavyText, fontSize = 21.sp, fontWeight = FontWeight.Bold, lineHeight = 29.sp)
                }
            }
        }
        items(blocks) { block -> PolicyBlockView(block, textSize) }
    }
}

private sealed interface PolicyBlock {
    data class Paragraph(val text: String) : PolicyBlock
    data class Heading(val text: String) : PolicyBlock
    data class Table(val rows: List<List<String>>) : PolicyBlock
    data class Note(val text: String) : PolicyBlock
}

private fun parsePolicyBlocks(body: String): List<PolicyBlock> {
    val lines = body.lines().map(String::trim).filter(String::isNotEmpty)
    val blocks = mutableListOf<PolicyBlock>()
    var index = 0
    while (index < lines.size) {
        val line = lines[index]
        when {
            line.startsWith("### ") -> blocks += PolicyBlock.Heading(line.removePrefix("### "))
            line.startsWith("※") -> blocks += PolicyBlock.Note(line)
            line.startsWith("|") -> {
                val rows = mutableListOf<List<String>>()
                while (index < lines.size && lines[index].startsWith("|")) {
                    rows += lines[index].trim('|').split('|').map(String::trim)
                    index++
                }
                blocks += PolicyBlock.Table(rows)
                continue
            }
            else -> blocks += PolicyBlock.Paragraph(line)
        }
        index++
    }
    return blocks
}

@Composable
private fun PolicyBlockView(block: PolicyBlock, textSize: Int) {
    val uriHandler = LocalUriHandler.current
    when (block) {
        is PolicyBlock.Paragraph -> Text(block.text, Modifier.fillMaxWidth().padding(horizontal = 5.dp), color = NavyText, fontSize = textSize.sp, lineHeight = (textSize * 1.55f).sp)
        is PolicyBlock.Heading -> Text(block.text, Modifier.fillMaxWidth().padding(top = 8.dp, start = 5.dp), color = PrimaryBlue, fontSize = (textSize + 2).sp, fontWeight = FontWeight.Bold)
        is PolicyBlock.Note -> Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E8)), shape = RoundedCornerShape(14.dp)) {
            Text(block.text, Modifier.padding(14.dp), color = NavyText, fontSize = textSize.sp, lineHeight = (textSize * 1.5f).sp)
        }
        is PolicyBlock.Table -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            block.rows.drop(1).forEach { row ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(13.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(row.firstOrNull().orEmpty(), color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = textSize.sp)
                        row.drop(1).forEachIndexed { column, value ->
                            if (row.size > 2) Text(block.rows.first().getOrNull(column + 1).orEmpty(), color = SecondaryText, fontSize = (textSize - 2).sp)
                            Text(
                                value,
                                modifier = if (value == "everytrip.smtp@gmail.com") Modifier.clickable {
                                    uriHandler.openUri("mailto:everytrip.smtp@gmail.com")
                                } else Modifier,
                                color = if (value == "everytrip.smtp@gmail.com") PrimaryBlue else NavyText,
                                fontSize = textSize.sp,
                                lineHeight = (textSize * 1.5f).sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun PrivacyPolicyPreview() {
    ProjectTheme { PrivacyPolicyScreen() }
}
