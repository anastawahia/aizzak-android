package android.aizzak.com

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.feature.auth.AuthStage
import android.aizzak.com.feature.auth.AuthUiState
import android.aizzak.com.feature.home.HomeScreen
import android.aizzak.com.feature.home.foundationAssistants
import android.aizzak.com.feature.settings.SettingsUiState
import android.aizzak.com.navigation.LocalFoundationApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LocalFoundationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun signedOutNavigationKeepsHomeGatedAndPreferencesReturnsToSource() {
        compose.setContent {
            AizzakTheme {
                LocalFoundationApp(SettingsUiState(loading = false, preferences = AppPreferences(localeInitialized = true)), "en", "1.0.0", {}, {}, {},
                    AuthUiState(stage = AuthStage.SIGNED_OUT), fakeAuthActions())
            }
        }
        compose.onNodeWithTag("auth-submit").assertIsEnabled()
        compose.onNodeWithText("Your assistants").assertDoesNotExist()
        compose.onNodeWithText("Create account").performScrollTo().performClick()
        compose.onNodeWithTag("auth-submit").assertIsEnabled()
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
                    onTheme = { preferences = preferences.copy(theme = it) }, onLanguage = { chosenLanguage = it }, onRetry = {},
                    authState = AuthUiState(stage = AuthStage.SIGNED_OUT), authActions = fakeAuthActions())
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
