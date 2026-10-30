package com.aizzak.android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.aizzak.android.core.designsystem.AizzakTheme
import com.aizzak.android.core.model.AppPreferences
import com.aizzak.android.core.model.ThemePreference
import com.aizzak.android.feature.home.HomeScreen
import com.aizzak.android.feature.home.foundationAssistants
import com.aizzak.android.feature.settings.SettingsUiState
import com.aizzak.android.navigation.LocalFoundationApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LocalFoundationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun unconfiguredAuthenticationCannotEnterHomeAndPreferencesReturnsToSource() {
        compose.setContent {
            AizzakTheme {
                LocalFoundationApp(SettingsUiState(loading = false, preferences = AppPreferences(localeInitialized = true)), "en", "1.0.0", {}, {}, {})
            }
        }
        compose.onNode(hasText("Sign in") and hasClickAction()).assertIsNotEnabled()
        compose.onNodeWithText("Your assistants").assertDoesNotExist()
        compose.onNodeWithText("Create account").performScrollTo().performClick()
        compose.onNode(hasText("Create account") and hasClickAction()).assertIsNotEnabled()
        compose.onNodeWithText("Preferences").performScrollTo().performClick()
        compose.onNodeWithText("Settings").assertIsDisplayed()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Confirm password").assertExists()
    }

    @Test fun preferenceSelectionDispatchesThemeAndLanguageWithoutAuthentication() {
        var chosenLanguage = "en"
        var preferences by mutableStateOf(AppPreferences(localeInitialized = true))
        compose.setContent {
            AizzakTheme(preferences.theme) {
                LocalFoundationApp(SettingsUiState(loading = false, preferences = preferences), "en", "1.0.0",
                    onTheme = { preferences = preferences.copy(theme = it) }, onLanguage = { chosenLanguage = it }, onRetry = {})
            }
        }
        compose.onNodeWithText("Preferences").performScrollTo().performClick()
        compose.onNodeWithText("Dark").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(ThemePreference.DARK, preferences.theme) }
        compose.onNodeWithText("العربية").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("ar", chosenLanguage) }
    }

    @Test fun deferredAgentsAreReadableContentWithNoClickActions() {
        compose.setContent { AizzakTheme { HomeScreen(foundationAssistants) } }
        foundationAssistants.forEach { assistant ->
            compose.onNodeWithTag("assistant-${assistant.id}").performScrollTo().assertHasNoClickAction()
        }
        compose.onAllNodesWithText("Coming later", substring = true).assertCountEquals(3)
    }
}
