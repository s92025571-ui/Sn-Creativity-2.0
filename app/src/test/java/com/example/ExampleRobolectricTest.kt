package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.util.WhatsAppSupportHelper
import com.example.util.WhatsAppSupportResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `package id and app name match SN Creativity AI specification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.sncreator.app", BuildConfig.APPLICATION_ID)
        assertEquals("SN Creativity AI", context.getString(R.string.app_name))
        assertEquals("Welcome to SN Creativity", context.getString(R.string.welcome_title))
    }

    @Test
    fun `dashboard renders four primary studio sections and navigates into Prompt Enhancer`() {
        composeTestRule.setContent {
            SNCreativityAppRoot(skipSplashForTest = true)
        }

        val dashboardList = composeTestRule.onNodeWithTag("dashboard_list")

        dashboardList.performScrollToNode(hasTestTag("studio_card_gaming_studio"))
        composeTestRule.onNodeWithTag("studio_card_gaming_studio").assertIsDisplayed()

        dashboardList.performScrollToNode(hasTestTag("studio_card_voice_script_studio"))
        composeTestRule.onNodeWithTag("studio_card_voice_script_studio").assertIsDisplayed()

        dashboardList.performScrollToNode(hasTestTag("studio_card_creator_toolkit"))
        composeTestRule.onNodeWithTag("studio_card_creator_toolkit").assertIsDisplayed()

        dashboardList.performScrollToNode(hasTestTag("studio_card_ai_prompt_studio"))
        composeTestRule.onNodeWithTag("studio_card_ai_prompt_studio")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("section_tools_list")
            .performScrollToNode(hasTestTag("tool_card_prompt_enhancer"))
        composeTestRule.onNodeWithTag("tool_card_prompt_enhancer")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Advanced Prompt Enhancer").assertIsDisplayed()
    }

    @Test
    fun `whatsapp helper returns NumberNotConfigured when owner has not supplied digits`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = WhatsAppSupportHelper.openOwnerSupportChat(context)
        assertEquals(WhatsAppSupportResult.NumberNotConfigured, result)
    }
}
