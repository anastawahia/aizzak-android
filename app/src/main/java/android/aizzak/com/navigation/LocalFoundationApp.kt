package android.aizzak.com.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.feature.auth.AuthPage
import android.aizzak.com.feature.auth.AuthActions
import android.aizzak.com.feature.auth.AuthScreen
import android.aizzak.com.feature.auth.AuthSessionScreen
import android.aizzak.com.feature.auth.AuthStage
import android.aizzak.com.feature.auth.AuthUiState
import android.aizzak.com.feature.settings.SettingsScreen
import android.aizzak.com.feature.settings.SettingsUiState
import kotlinx.serialization.Serializable

@Serializable private data object SignIn : NavKey
@Serializable private data object SignUp : NavKey
@Serializable private data object Reset : NavKey
@Serializable private data object Preferences : NavKey

/** Firebase identity alone never grants access to authenticated backend destinations. */
@Composable
fun LocalFoundationApp(
    state: SettingsUiState,
    language: String,
    version: String,
    onTheme: (ThemePreference) -> Unit,
    onLanguage: (String) -> Unit,
    onRetry: () -> Unit,
    authState: AuthUiState,
    authActions: AuthActions,
) {
    val backStack = rememberNavBackStack(SignIn)
    var previousStage by remember { mutableStateOf(authState.stage) }
    LaunchedEffect(authState.stage) {
        // Drop obsolete form destinations when identity changes, including after signout.
        if (previousStage != authState.stage) {
            backStack.clear()
            backStack.add(SignIn)
        }
        previousStage = authState.stage
    }
    fun signIn() { authActions.clearFeedback(); backStack.clear(); backStack.add(SignIn) }
    fun signUp() { authActions.clearFeedback(); backStack.add(SignUp) }
    fun reset() { authActions.clearFeedback(); backStack.add(Reset) }
    fun preferences() { if (backStack.lastOrNull() != Preferences) backStack.add(Preferences) }
    @Composable fun authPage(page: AuthPage) {
        if (authState.stage == AuthStage.SIGNED_OUT) {
            AuthScreen(page, authState, authActions, ::signIn, ::signUp, ::reset, ::preferences)
        } else {
            AuthSessionScreen(authState, authActions, ::preferences)
        }
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        Surface(Modifier.fillMaxSize().padding(insets), color = MaterialTheme.colorScheme.background) {
            NavDisplay(backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                entryProvider = entryProvider {
                    entry<SignIn> { authPage(AuthPage.SIGN_IN) }
                    entry<SignUp> { authPage(AuthPage.SIGN_UP) }
                    entry<Reset> { authPage(AuthPage.RESET) }
                    entry<Preferences> {
                        SettingsScreen(state, language, version, onTheme, onLanguage, onRetry,
                            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) })
                    }
                })
        }
    }
}
