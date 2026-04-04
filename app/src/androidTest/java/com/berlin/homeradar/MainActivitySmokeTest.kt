package com.berlin.homeradar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.berlin.homeradar.R
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndShowsTitle() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.onboarding_get_started)).assertIsDisplayed()
    }
}
