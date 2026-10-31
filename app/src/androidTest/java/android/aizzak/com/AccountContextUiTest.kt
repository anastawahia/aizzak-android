package android.aizzak.com

import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.feature.auth.AccountContext
import android.aizzak.com.feature.auth.AccountContextError
import android.aizzak.com.feature.auth.AuthAction
import android.aizzak.com.feature.auth.AuthActions
import android.aizzak.com.feature.auth.AuthIdentity
import android.aizzak.com.feature.auth.AuthMessage
import android.aizzak.com.feature.auth.AuthStage
import android.aizzak.com.feature.auth.AuthUiState
import android.aizzak.com.feature.settings.SettingsUiState
import android.aizzak.com.navigation.LocalFoundationApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Fake context/callbacks only: these tests never authenticate or change a real password. */
class AccountContextUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loadingAndDeniedContextNeverExposeProtectedNavigation() {
        var retries = 0
        var state by mutableStateOf(ready().copy(stage = AuthStage.LOADING_CONTEXT, context = null))
        show({ state }, fakeAuthActions(refreshIdentity = { retries++ }))
        compose.onNodeWithTag("auth-context-loading").assertExists()
        compose.onNodeWithTag("nav-home").assertDoesNotExist()
        compose.runOnIdle { state = state.copy(stage = AuthStage.ACCESS_DENIED, contextError = AccountContextError.FORBIDDEN) }
        compose.onNodeWithText("Account access denied").assertExists()
        compose.onNodeWithTag("auth-context-retry").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, retries) }
        compose.onNodeWithText("Your assistants").assertDoesNotExist()
    }

    @Test fun readyWithoutContextRemainsClosedUntilContextExists() {
        var state by mutableStateOf(ready().copy(context = null))
        show({ state })
        compose.onNodeWithText("Account services unavailable").assertExists()
        compose.onNodeWithTag("nav-home").assertDoesNotExist()
        compose.runOnIdle { state = ready() }
        compose.onNodeWithText("Your assistants").assertExists()
        compose.onNodeWithTag("nav-account").assertExists()
    }

    @Test fun authorizedNavigationAndSignoutClearAccountContent() {
        var state by mutableStateOf(ready())
        show({ state }, fakeAuthActions(signOut = { state = AuthUiState(stage = AuthStage.SIGNED_OUT) }))
        compose.onNodeWithTag("nav-account").performClick()
        compose.onNodeWithText("account-a@example.com").assertExists()
        compose.onNodeWithTag("nav-settings").performClick()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Your assistants").assertExists()
        compose.onNodeWithTag("nav-account").performClick()
        compose.onNodeWithTag("account-sign-out").performScrollTo().performClick()
        compose.onNodeWithTag("auth-submit").assertExists()
        compose.onNodeWithTag("nav-account").assertDoesNotExist()
        compose.onNodeWithText("account-a@example.com").assertDoesNotExist()
    }

    @Test fun passwordSubmissionWaitsForActualSuccessBeforeReturningToAccount() {
        var state by mutableStateOf(ready())
        var submitted: Triple<String, String, String>? = null
        val actions = fakeAuthActions(changePassword = { current, password, confirmation ->
            submitted = Triple(current, password, confirmation)
            state = state.copy(busy = true, action = AuthAction.CHANGE_PASSWORD)
        })
        show({ state }, actions)
        openPassword()
        compose.onNodeWithTag("password-current").performTextInput("old-sample")
        compose.onNodeWithTag("password-new").performScrollTo().performTextInput("new-sample")
        compose.onNodeWithTag("password-confirmation").performScrollTo().performTextInput("new-sample")
        compose.onNodeWithTag("password-save").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(Triple("old-sample", "new-sample", "new-sample"), submitted) }
        compose.onNodeWithTag("password-save").assertIsNotEnabled()
        compose.onNodeWithTag("account-change-password").assertDoesNotExist()
        compose.runOnIdle { state = state.copy(busy = false, action = null, message = AuthMessage.PASSWORD_CHANGED) }
        compose.onNodeWithTag("password-current").assertDoesNotExist()
        compose.onNodeWithTag("account-change-password").assertExists()
        compose.onNodeWithText("Your password was changed.").assertExists()
    }

    @Test fun leavingPasswordFormDiscardsItsSecrets() {
        val state = ready()
        show({ state })
        openPassword()
        compose.onNodeWithTag("password-current").performTextInput("old-sample")
        compose.onNodeWithTag("password-new").performScrollTo().performTextInput("new-sample")
        compose.onNodeWithTag("password-back").performScrollTo().performClick()
        compose.onNodeWithTag("account-change-password").performScrollTo().performClick()
        compose.onNodeWithTag("password-current").assert(emptyPassword())
        compose.onNodeWithTag("password-new").assert(emptyPassword())
    }

    @Test fun accountSwitchDisposesPasswordRouteAndStartsAtHome() {
        var state by mutableStateOf(ready())
        show({ state })
        openPassword()
        compose.onNodeWithTag("password-current").performTextInput("old-sample")
        compose.runOnIdle { state = ready("account-b") }
        compose.onNodeWithText("Your assistants").assertExists()
        compose.onNodeWithTag("password-current").assertDoesNotExist()
        compose.onNodeWithTag("nav-account").performClick()
        compose.onNodeWithText("account-b@example.com").assertExists()
        compose.onNodeWithText("account-a@example.com").assertDoesNotExist()
        compose.onNodeWithTag("account-change-password").performScrollTo().performClick()
        compose.onNodeWithTag("password-current").assert(emptyPassword())
    }

    private fun show(state: () -> AuthUiState, actions: AuthActions = fakeAuthActions()) {
        compose.setEnglishContent {
            AizzakTheme {
                LocalFoundationApp(SettingsUiState(loading = false, preferences = AppPreferences(localeInitialized = true)),
                    "en", "1.0.0", {}, {}, {}, state(), actions)
            }
        }
    }

    private fun openPassword() {
        compose.onNodeWithTag("nav-account").performClick()
        compose.onNodeWithTag("account-change-password").performScrollTo().performClick()
    }

    private fun emptyPassword() = SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(""))

    private fun ready(uid: String = "account-a") = AuthUiState(stage = AuthStage.READY,
        identity = AuthIdentity(uid, "$uid@example.com", true),
        context = AccountContext("11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222", emptySet(), emptySet()))
}
