package android.aizzak.com

import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.feature.account.PasswordScreen
import android.aizzak.com.feature.auth.AccountContext
import android.aizzak.com.feature.auth.AccountContextError
import android.aizzak.com.feature.auth.AuthActions
import android.aizzak.com.feature.auth.AuthIdentity
import android.aizzak.com.feature.auth.AuthPage
import android.aizzak.com.feature.auth.AuthScreen
import android.aizzak.com.feature.auth.AuthSessionScreen
import android.aizzak.com.feature.auth.AuthStage
import android.aizzak.com.feature.auth.AuthUiState
import android.aizzak.com.feature.settings.SettingsUiState
import android.aizzak.com.navigation.LocalFoundationApp
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1000dp-h1400dp-xxhdpi")
class DesignScreenshotTest(private val approvedFileName: String) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun recordApprovedConfiguration() {
        val case = approvedCases().single { it.fileName == approvedFileName }
        configureResources(case)
        compose.setContent {
            val direction = if (case.locale == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            val density = ApplicationProvider.getApplicationContext<android.content.Context>()
                .resources.displayMetrics.density
            CompositionLocalProvider(
                LocalLayoutDirection provides direction,
                LocalDensity provides Density(density, case.fontScale),
            ) {
                AizzakTheme(if (case.dark) ThemePreference.DARK else ThemePreference.LIGHT) {
                    Box(Modifier.width(case.width.dp).height(1000.dp).testTag(CAPTURE_ROOT)) {
                        ScreenContent(case)
                    }
                }
            }
        }
        when (case.screen) {
            Screen.ACCOUNT -> compose.onNodeWithTag("nav-account").performClick()
            Screen.SETTINGS -> compose.onNodeWithTag("nav-settings").performClick()
            else -> Unit
        }
        compose.waitForIdle()
        compose.onNodeWithTag(CAPTURE_ROOT).captureRoboImage(File(outputDirectory, case.fileName).absolutePath)
        if (recording) assertExactApprovedOutputSetAfterFinalCase()
    }

    private fun configureResources(case: ShotCase) {
        val locale = Locale.forLanguageTag(case.locale)
        Locale.setDefault(locale)
        val resources = ApplicationProvider.getApplicationContext<android.content.Context>().resources
        val configuration = Configuration(resources.configuration).apply {
            setLocales(LocaleList(locale))
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (case.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            fontScale = case.fontScale
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    @Composable
    private fun ScreenContent(case: ShotCase) {
        val actions = noOpActions()
        val identity = AuthIdentity("fixture-user", "user@example.com", true)
        val ready = AuthUiState(
            stage = AuthStage.READY,
            identity = identity,
            context = AccountContext("fixture-user", "fixture-workspace", setOf("member"), emptySet()),
        )
        val settings = SettingsUiState(
            loading = false,
            preferences = AppPreferences(if (case.dark) ThemePreference.DARK else ThemePreference.LIGHT, true),
        )
        when (case.screen) {
            Screen.HOME, Screen.ACCOUNT, Screen.SETTINGS -> LocalFoundationApp(
                state = settings,
                language = case.locale,
                version = "1.0.0",
                onTheme = {}, onLanguage = {}, onRetry = {},
                authState = ready,
                authActions = actions,
            )
            Screen.SIGN_IN -> AuthScreen(AuthPage.SIGN_IN, AuthUiState(stage = AuthStage.SIGNED_OUT), actions, {}, {}, {}, {})
            Screen.SIGN_UP -> AuthScreen(AuthPage.SIGN_UP, AuthUiState(stage = AuthStage.SIGNED_OUT), actions, {}, {}, {}, {})
            Screen.RESET -> AuthScreen(AuthPage.RESET, AuthUiState(stage = AuthStage.SIGNED_OUT), actions, {}, {}, {}, {})
            Screen.VERIFY -> AuthSessionScreen(
                AuthUiState(stage = AuthStage.NEEDS_VERIFICATION, identity = identity.copy(emailVerified = false)), actions, {},
            )
            Screen.LOADING -> AuthSessionScreen(AuthUiState(stage = AuthStage.RESTORING), actions, {})
            Screen.CONTEXT -> AuthSessionScreen(
                AuthUiState(stage = AuthStage.CONTEXT_UNAVAILABLE, identity = identity, contextError = AccountContextError.NETWORK),
                actions,
                {},
            )
            Screen.PASSWORD -> PasswordScreen(ready, { _, _, _ -> }, {}, {})
        }
    }

    private fun noOpActions() = AuthActions(
        signIn = { _, _ -> }, signUp = { _, _, _ -> }, resetPassword = {},
        resendVerification = {}, refreshIdentity = {}, signOut = {}, clearFeedback = {},
    )

    companion object {
        private const val CAPTURE_ROOT = "design-capture-root"
        private val outputDirectory = File(System.getProperty("user.dir"), "build/outputs/roborazzi")
        private val recording = System.getProperty("roborazzi.test.record") == "true"
        private val outputsCleaned = AtomicBoolean(false)
        private val completedCases = AtomicInteger(0)

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun approvedConfigurations(): List<Array<Any>> = approvedCases().map { arrayOf(it.fileName) }

        @JvmStatic
        @BeforeClass
        fun cleanOnlyApprovedOutputs() {
            if (!recording) return
            if (!outputsCleaned.compareAndSet(false, true)) return
            val targets = approvedCases().mapTo(mutableSetOf()) { it.fileName }
            check(targets.size == 40) { "Expected 40 unique approved screenshot names, found ${targets.size}" }
            check(outputDirectory.exists() || outputDirectory.mkdirs()) { "Could not create $outputDirectory" }
            outputDirectory.listFiles().orEmpty()
                .filter { it.isFile && it.name in targets }
                .forEach { check(it.delete()) { "Could not remove targeted screenshot ${it.absolutePath}" } }
        }

        private fun assertExactApprovedOutputSetAfterFinalCase() {
            if (completedCases.incrementAndGet() != 40) return
            val expected = approvedCases().mapTo(mutableSetOf()) { it.fileName }
            val actual = outputDirectory.listFiles().orEmpty()
                .filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
                .mapTo(mutableSetOf()) { it.name }
            check(actual.size == 40 && actual == expected) {
                "Roborazzi output must be the exact approved 40-file set; missing=${expected - actual}, extra=${actual - expected}"
            }
        }
    }
}

private fun approvedCases(): List<ShotCase> {
    val configs = listOf(
        ConfigCase("en", false, 390, 1.0f),
        ConfigCase("ar", true, 390, 1.0f),
        ConfigCase("en", true, 840, 1.0f),
        ConfigCase("ar", false, 840, 1.5f),
    )
    return Screen.entries.flatMap { screen -> configs.map { ShotCase(screen, it) } }
}

private enum class Screen(val id: String) {
    HOME("SCR-HOME-01"), ACCOUNT("SCR-ACCT-01"), SIGN_IN("SCR-AUTH-01"), SIGN_UP("SCR-AUTH-02"),
    RESET("SCR-AUTH-03"), VERIFY("SCR-AUTH-04"), LOADING("SCR-LOAD-01"), CONTEXT("SCR-CTX-01"),
    SETTINGS("SCR-SET-01"), PASSWORD("SCR-ACCT-02"),
}

private data class ConfigCase(val locale: String, val dark: Boolean, val width: Int, val fontScale: Float)

private data class ShotCase(val screen: Screen, val config: ConfigCase) {
    val locale get() = config.locale
    val dark get() = config.dark
    val width get() = config.width
    val fontScale get() = config.fontScale
    val fileName get() = "${screen.id}__${locale}_${if (dark) "dark" else "light"}_w${width}_fs${fontScale}.png"
}
