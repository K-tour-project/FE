package com.everytrip.app.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.everytrip.app.ui.theme.ProjectTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LocationPermissionNoticeDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysLocationUsageNoticeAndHandlesActions() {
        var allowClicked = false
        var laterClicked = false

        composeTestRule.setContent {
            ProjectTheme {
                LocationPermissionNoticeDialog(
                    onAllowClick = { allowClicked = true },
                    onLaterClick = { laterClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("위치정보 이용 안내").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Every Trip은 현재 위치를 기반으로 주변 관광지를 안내하기 위해 위치 권한을 사용합니다.",
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "위치 권한은 선택 사항이며, 허용하지 않아도 위치 기반 기능을 제외한 서비스를 이용할 수 있습니다.",
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText("위치 권한 허용하기").performClick()
        composeTestRule.runOnIdle { assertTrue(allowClicked) }

        composeTestRule.onNodeWithText("다음에 하기").performClick()
        composeTestRule.runOnIdle { assertTrue(laterClicked) }
    }
}
