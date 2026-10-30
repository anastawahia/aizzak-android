package com.aizzak.android.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.aizzak.android.core.model.ThemePreference
import com.aizzak.android.feature.auth.AuthPage
import com.aizzak.android.feature.auth.AuthScreen
import com.aizzak.android.feature.settings.SettingsScreen
import com.aizzak.android.feature.settings.SettingsUiState
import kotlinx.serialization.Serializable

@Serializable private data object SignIn : NavKey
@Serializable private data object SignUp : NavKey
@Serializable private data object Reset : NavKey
@Serializable private data object Preferences : NavKey

/** No authenticated destination is registered until the real session boundary exists. */
@Composable
fun LocalFoundationApp(state: SettingsUiState, language: String, version: String, onTheme: (ThemePreference) -> Unit, onLanguage: (String) -> Unit, onRetry: () -> Unit) {
    val backStack = rememberNavBackStack(SignIn)
    fun signIn() { backStack.clear(); backStack.add(SignIn) }
    fun preferences() { if (backStack.lastOrNull() != Preferences) backStack.add(Preferences) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        Surface(Modifier.fillMaxSize().padding(insets), color = MaterialTheme.colorScheme.background) {
            NavDisplay(backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                entryProvider = entryProvider {
                    entry<SignIn> { AuthScreen(AuthPage.SIGN_IN, ::signIn, { backStack.add(SignUp) }, { backStack.add(Reset) }, ::preferences) }
                    entry<SignUp> { AuthScreen(AuthPage.SIGN_UP, ::signIn, {}, {}, ::preferences) }
                    entry<Reset> { AuthScreen(AuthPage.RESET, ::signIn, {}, {}, ::preferences) }
                    entry<Preferences> {
                        SettingsScreen(state, language, version, onTheme, onLanguage, onRetry,
                            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) })
                    }
                })
        }
    }
}
