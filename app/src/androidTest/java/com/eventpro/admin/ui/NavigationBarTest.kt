package com.eventpro.admin.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eventpro.admin.ui.components.BottomNavBar
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationBarTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Test
    fun allNavLabelsAreDisplayed() {
        composeTestRule.setContent {
            BottomNavBar(navController = androidx.navigation.testing.TestNavHostController(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
            ))
        }

        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Events").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clients").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inventory").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ledger").assertIsDisplayed()
    }
}
