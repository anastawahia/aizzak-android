package android.aizzak.com

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.feature.auth.AuthActions
import android.aizzak.com.feature.auth.AuthError
import android.aizzak.com.feature.auth.AuthIdentity
import android.aizzak.com.feature.auth.AuthMessage
import android.aizzak.com.feature.auth.AuthPage
import android.aizzak.com.feature.auth.AuthScreen
import android.aizzak.com.feature.auth.AuthSessionScreen
import android.aizzak.com.feature.auth.AuthStage
import android.aizzak.com.feature.auth.AuthUiState
import android.aizzak.com.feature.settings.SettingsUiState
import android.aizzak.com.navigation.LocalFoundationApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Isolated screens and fake callbacks: no Firebase requests or real credentials. */
class AuthUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun signInSubmitsEnteredCredentialsAndShowsBusyState() {
        var submitted: Pair<String, String>? = null
        var calls = 0
        var state by mutableStateOf(AuthUiState(stage = AuthStage.SIGNED_OUT))
        val actions = fakeAuthActions(signIn = { email, password ->
            submitted = email to password
            calls++
            state = state.copy(busy = true)
        })
        compose.setEnglishContent {
            AizzakTheme { AuthScreen(AuthPage.SIGN_IN, state, actions, {}, {}, {}, {}) }
        }
        compose.onNodeWithTag("auth-email").performTextInput("screen@example.com")
        compose.onNodeWithTag("auth-password").performTextInput("sample-password")
        compose.onNodeWithTag("auth-submit").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("screen@example.com" to "sample-password", submitted); assertEquals(1, calls) }
        compose.onNodeWithTag("auth-submit").assertIsNotEnabled()
        compose.onNodeWithTag("auth-email").assertIsNotEnabled()
        compose.onAllNodesWithText("Please wait…").assertCountEquals(2)
    }

    @Test fun signInFailureIsVisibleAndCanBeRetriedWithSameInput() {
        var attempts = 0
        var state by mutableStateOf(AuthUiState(stage = AuthStage.SIGNED_OUT))
        val actions = fakeAuthActions(signIn = { _, _ ->
            attempts++
            state = state.copy(error = AuthError.NETWORK)
        })
        compose.setEnglishContent { AizzakTheme { AuthScreen(AuthPage.SIGN_IN, state, actions, {}, {}, {}, {}) } }
        compose.onNodeWithTag("auth-email").performTextInput("screen@example.com")
        compose.onNodeWithTag("auth-password").performTextInput("sample-password")
        compose.onNodeWithTag("auth-submit").performScrollTo().performClick()
        compose.onNodeWithText("Could not connect. Check your connection and try again.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("auth-submit").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(2, attempts) }
    }

    @Test fun signupSubmitsConfirmationAndResetShowsNeutralResult() {
        var submitted: Triple<String, String, String>? = null
        var resetEmail: String? = null
        var state by mutableStateOf(AuthUiState(stage = AuthStage.SIGNED_OUT))
        var page by mutableStateOf(AuthPage.SIGN_UP)
        val actions = fakeAuthActions(
            signUp = { email, password, confirmation -> submitted = Triple(email, password, confirmation) },
            resetPassword = { email -> resetEmail = email; state = state.copy(message = AuthMessage.RESET_REQUESTED) },
        )
        compose.setEnglishContent { AizzakTheme { AuthScreen(page, state, actions, {}, {}, {}, {}) } }
        compose.onNodeWithTag("auth-email").performTextInput("screen@example.com")
        compose.onNodeWithTag("auth-password").performTextInput("sample-password")
        compose.onNodeWithTag("auth-confirmation").performScrollTo().performTextInput("sample-password")
        compose.onNodeWithTag("auth-submit").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(Triple("screen@example.com", "sample-password", "sample-password"), submitted); page = AuthPage.RESET }
        compose.onNodeWithTag("auth-email").performScrollTo().performTextInput("reset@example.com")
        compose.onNodeWithTag("auth-submit").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("reset@example.com", resetEmail) }
        compose.onNodeWithText("If this email can receive a reset link, check your inbox.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("auth-password").assertDoesNotExist()
    }

    @Test fun verificationProvidesRealActionsWithoutOpeningHomeAndSignoutClearsSession() {
        var refreshed = 0
        var resent = 0
        var state by mutableStateOf(AuthUiState(stage = AuthStage.NEEDS_VERIFICATION,
            identity = AuthIdentity("fake-user", "screen@example.com", false)))
        val actions = fakeAuthActions(
            refreshIdentity = { refreshed++ }, resendVerification = { resent++ },
            signOut = { state = AuthUiState(stage = AuthStage.SIGNED_OUT) },
        )
        compose.setEnglishContent {
            AizzakTheme { LocalFoundationApp(SettingsUiState(loading = false, preferences = AppPreferences(localeInitialized = true)),
                "en", "1.0.0", {}, {}, {}, state, actions) }
        }
        compose.onNodeWithText("Verify your email").assertExists()
        compose.onNodeWithTag("auth-refresh").performScrollTo().performClick()
        compose.onNodeWithText("Resend verification email").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, refreshed); assertEquals(1, resent) }
        compose.onNodeWithText("Your assistants").assertDoesNotExist()
        compose.onNodeWithText("Sign out").performScrollTo().performClick()
        compose.onNodeWithTag("auth-submit").assertExists()
        compose.onNodeWithText("screen@example.com").assertDoesNotExist()
    }

    @Test fun verifiedIdentityStillRequiresAccountContextAndRetryDoesNotBypassIt() {
        var retries = 0
        val state = AuthUiState(stage = AuthStage.CONTEXT_UNAVAILABLE,
            identity = AuthIdentity("fake-user", "screen@example.com", true))
        val actions = fakeAuthActions(refreshIdentity = { retries++ })
        compose.setEnglishContent {
            AizzakTheme { LocalFoundationApp(SettingsUiState(loading = false, preferences = AppPreferences(localeInitialized = true)),
                "en", "1.0.0", {}, {}, {}, state, actions) }
        }
        compose.onNodeWithText("Account services unavailable").assertExists()
        compose.onNodeWithTag("auth-context-retry").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, retries) }
        compose.onNodeWithText("Your assistants").assertDoesNotExist()
        compose.onNodeWithTag("auth-submit").assertDoesNotExist()
        compose.onNodeWithText("Sign out").assertExists()
    }

    @Test fun sessionRestorationDoesNotOfferPrematureSignInOrHome() {
        compose.setEnglishContent { AizzakTheme { AuthSessionScreen(AuthUiState(), fakeAuthActions(), {}) } }
        compose.onNodeWithText("Restoring your session…").assertExists()
        compose.onNodeWithTag("auth-submit").assertDoesNotExist()
        compose.onNodeWithText("Your assistants").assertDoesNotExist()
    }
}

internal fun fakeAuthActions(
    signIn: (String, String) -> Unit = { _, _ -> },
    signUp: (String, String, String) -> Unit = { _, _, _ -> },
    resetPassword: (String) -> Unit = {},
    resendVerification: () -> Unit = {},
    refreshIdentity: () -> Unit = {},
    signOut: () -> Unit = {},
    clearFeedback: () -> Unit = {},
    changePassword: (String, String, String) -> Unit = { _, _, _ -> },
) = AuthActions(signIn, signUp, resetPassword, resendVerification, refreshIdentity, signOut, clearFeedback, changePassword)
