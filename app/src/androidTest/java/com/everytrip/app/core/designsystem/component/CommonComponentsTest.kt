package com.everytrip.app.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.everytrip.app.ui.theme.ProjectTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CommonComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun commonComponentsExposeTheirContentAndActions() {
        var backClicked = false
        var buttonClicked = false

        composeTestRule.setContent {
            ProjectTheme {
                Column {
                    AppTopBar(title = "Title", onBackClick = { backClicked = true })
                    AppTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = "Email",
                        leadingIcon = Icons.Outlined.Email,
                        contentDescription = "email"
                    )
                    PasswordTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = "Password"
                    )
                    GradientButton(text = "Continue", onClick = { buttonClicked = true })
                }
            }
        }

        composeTestRule.onNodeWithText("Title").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithContentDescription("?�로가�?).performClick()
        composeTestRule.onNodeWithText("Continue").performClick()

        assertTrue(backClicked)
        assertTrue(buttonClicked)
    }
}
