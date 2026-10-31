package android.aizzak.com.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.R
import android.aizzak.com.feature.account.AccountScreen
import android.aizzak.com.feature.account.PasswordScreen
import android.aizzak.com.feature.auth.AuthMessage
import android.aizzak.com.feature.auth.AuthPage
import android.aizzak.com.feature.auth.AuthActions
import android.aizzak.com.feature.auth.AuthScreen
import android.aizzak.com.feature.auth.AuthSessionScreen
import android.aizzak.com.feature.auth.AuthStage
import android.aizzak.com.feature.auth.AuthUiState
import android.aizzak.com.feature.home.HomeScreen
import android.aizzak.com.feature.home.foundationAssistants
import android.aizzak.com.feature.settings.SettingsScreen
import android.aizzak.com.feature.settings.SettingsUiState
import kotlinx.serialization.Serializable

@Serializable private data object SignIn : NavKey
@Serializable private data object SignUp : NavKey
@Serializable private data object Reset : NavKey
@Serializable private data object Preferences : NavKey

private enum class MainDestination(val label: Int, val tag: String, val icon: ImageVector) {
    HOME(R.string.home, "nav-home", navigationIcon("Home", "M10,20V14H14V20H19V11H22L12,2L2,11H5V20Z")),
    ACCOUNT(R.string.account, "nav-account", navigationIcon("Account", "M12,12A4,4 0 1,0 12,4A4,4 0 0,0 12,12M4,20V18C4,14 20,14 20,18V20Z")),
    SETTINGS(R.string.settings, "nav-settings", navigationIcon("Settings", "M3,5H7V3H9V5H21V7H9V9H7V7H3ZM3,11H15V9H17V11H21V13H17V15H15V13H3ZM3,17H7V15H9V17H21V19H9V21H7V19H3Z")),
}

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
    val ready = authState.stage == AuthStage.READY && authState.identity?.emailVerified == true && authState.context != null
    // Disposal prevents account changes or lost context from preserving protected destinations.
    key(authState.identity?.uid, ready) {
        if (ready) {
            AuthenticatedApp(state, language, version, onTheme, onLanguage, onRetry, authState, authActions)
        } else {
            AuthenticationApp(state, language, version, onTheme, onLanguage, onRetry, authState, authActions)
        }
    }
}

@Composable
private fun AuthenticationApp(
    state: SettingsUiState, language: String, version: String,
    onTheme: (ThemePreference) -> Unit, onLanguage: (String) -> Unit, onRetry: () -> Unit,
    authState: AuthUiState, authActions: AuthActions,
) {
    val backStack = rememberNavBackStack(SignIn)
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
        Surface(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets), color = MaterialTheme.colorScheme.background) {
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

@Composable
private fun AuthenticatedApp(
    state: SettingsUiState, language: String, version: String,
    onTheme: (ThemePreference) -> Unit, onLanguage: (String) -> Unit, onRetry: () -> Unit,
    authState: AuthUiState, actions: AuthActions,
) {
    var selected by rememberSaveable { mutableStateOf(MainDestination.HOME.name) }
    var password by remember { mutableStateOf(false) }
    val destination = MainDestination.valueOf(selected)
    fun cancelPassword() {
        if (!authState.busy) { password = false; actions.clearFeedback() }
    }
    LaunchedEffect(authState.message) {
        if (authState.message == AuthMessage.PASSWORD_CHANGED) password = false
    }
    BackHandler(enabled = password || destination != MainDestination.HOME) {
        if (password) cancelPassword() else selected = MainDestination.HOME.name
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 600.dp
        Scaffold(containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!wide && !password) {
                    NavigationBar {
                        MainDestination.entries.forEach { item ->
                            NavigationBarItem(selected = destination == item,
                                onClick = { selected = item.name; actions.clearFeedback() },
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(stringResource(item.label)) }, modifier = Modifier.testTag(item.tag))
                        }
                    }
                }
            }) { insets ->
            Row(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
                if (wide && !password) {
                    NavigationRail {
                        MainDestination.entries.forEach { item ->
                            NavigationRailItem(selected = destination == item,
                                onClick = { selected = item.name; actions.clearFeedback() },
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(stringResource(item.label)) }, modifier = Modifier.testTag(item.tag))
                        }
                    }
                }
                Surface(Modifier.weight(1f).fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (password) {
                        PasswordScreen(authState, actions.changePassword, actions.clearFeedback, ::cancelPassword)
                    } else when (destination) {
                        MainDestination.HOME -> HomeScreen(foundationAssistants)
                        MainDestination.ACCOUNT -> AccountScreen(authState.identity?.email, null,
                            onChangePassword = { actions.clearFeedback(); password = true }, onSignOut = actions.signOut, state = authState)
                        MainDestination.SETTINGS -> SettingsScreen(state, language, version, onTheme, onLanguage, onRetry,
                            onBack = { selected = MainDestination.HOME.name })
                    }
                }
            }
        }
    }
}

private fun navigationIcon(name: String, data: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString(data).toNodes(), fill = SolidColor(Color.Black))
        .build()
