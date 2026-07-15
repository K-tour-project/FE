package com.everytrip.app.feature.home.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.everytrip.app.ui.theme.ProjectTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun categoryExploreSectionUsesColumnWithTwoButtons() {
        composeTestRule.setContent {
            ProjectTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithTag("category_explore_column").assertExists()
        composeTestRule.onAllNodesWithTag("category_explore_button").assertCountEquals(2)
        composeTestRule.onNodeWithText("지??�� ?�색").assertExists()
        composeTestRule.onNodeWithText("?�마�??�색").assertExists()
    }
}
