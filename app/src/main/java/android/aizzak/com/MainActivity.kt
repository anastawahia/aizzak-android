package android.aizzak.com

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.data.preferences.DataStorePreferencesRepository
import android.aizzak.com.data.preferences.aizzakPreferencesDataStore
import android.aizzak.com.feature.auth.AuthActions
import android.aizzak.com.feature.auth.AuthViewModel
import android.aizzak.com.feature.settings.SettingsError
import android.aizzak.com.feature.settings.SettingsViewModel
import android.aizzak.com.navigation.LocalFoundationApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                        SettingsViewModel(DataStorePreferencesRepository(applicationContext.aizzakPreferencesDataStore))
                    modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                        AuthViewModel(AppContainer.authRepository, AppContainer.accountContextRepository)
                    else -> error("Unsupported ViewModel")
                } as T
            }
        }
        @Suppress("DEPRECATION")
        val version = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
        setContent {
            val settings: SettingsViewModel = viewModel(factory = factory)
            val auth: AuthViewModel = viewModel(factory = factory)
            val state by settings.uiState.collectAsStateWithLifecycle()
            val authState by auth.uiState.collectAsStateWithLifecycle()
            val preferences = state.preferences
            val systemDark = isSystemInDarkTheme()
            val dark = when (preferences?.theme ?: ThemePreference.SYSTEM) {
                ThemePreference.SYSTEM -> systemDark
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
            SideEffect {
                // App appearance, including a forced choice, controls system-bar icon contrast.
                val bars = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            LaunchedEffect(preferences?.localeInitialized, state.loading) {
                if (!state.loading && preferences != null && !preferences.localeInitialized) {
                    if (AppCompatDelegate.getApplicationLocales().isEmpty) {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                    }
                    settings.markLocaleInitialized()
                }
            }
            AizzakTheme(preferences?.theme ?: ThemePreference.SYSTEM) {
                if (preferences == null || !preferences.localeInitialized) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Column(Modifier.safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (state.error == null) {
                                CircularProgressIndicator()
                                Text(stringResource(R.string.loading))
                            } else {
                                InformationNotice(stringResource(if (state.error == SettingsError.READ) R.string.preferences_read_error else R.string.preferences_write_error), isError = true)
                                TextButton(onClick = { if (state.error == SettingsError.READ) settings.retryRead() else settings.retrySave() }) { Text(stringResource(R.string.retry)) }
                            }
                        }
                    }
                } else {
                    val language = LocalConfiguration.current.locales[0].language
                    LocalFoundationApp(state, language, version, settings::setTheme,
                        onLanguage = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(it)) },
                        onRetry = { if (state.error == SettingsError.READ) settings.retryRead() else settings.retrySave() },
                        authState = authState,
                        authActions = AuthActions(auth::signIn, auth::signUp, auth::resetPassword,
                            auth::resendVerification, auth::refreshIdentity, auth::signOut, auth::clearFeedback,
                            changePassword = auth::changePassword))
                }
            }
        }
    }
}
