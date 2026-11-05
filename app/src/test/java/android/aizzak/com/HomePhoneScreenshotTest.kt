package android.aizzak.com

import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.feature.auth.*
import android.aizzak.com.feature.settings.SettingsUiState
import android.aizzak.com.navigation.LocalFoundationApp
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1000dp-h1400dp-xxhdpi")
class HomePhoneScreenshotTest(private val localeTag: String, private val dark: Boolean, private val width: Int, private val scale: Float) {
    @get:Rule val compose = createComposeRule()

    @Test fun compactLayoutAndNavigationRemainUsable() {
        val locale = Locale.forLanguageTag(localeTag)
        Locale.setDefault(locale)
        val resources = ApplicationProvider.getApplicationContext<android.content.Context>().resources
        val config = Configuration(resources.configuration).apply {
            setLocales(LocaleList(locale))
            fontScale = scale
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(resources.displayMetrics.density, scale),
                LocalLayoutDirection provides if (localeTag == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr) {
                AizzakTheme(if (dark) ThemePreference.DARK else ThemePreference.LIGHT) {
                    Box(Modifier.width(width.dp).height(812.dp).testTag("phone-capture")) {
                        LocalFoundationApp(
                            state = SettingsUiState(loading = false, preferences = AppPreferences(ThemePreference.SYSTEM, true)),
                            language = localeTag, version = "1.0.0", onTheme = {}, onLanguage = {}, onRetry = {},
                            authState = AuthUiState(stage = AuthStage.READY, identity = AuthIdentity("fixture", "user@example.com", true),
                                context = AccountContext("fixture", "workspace", setOf("member"), emptySet())),
                            authActions = AuthActions(signIn = { _, _ -> }, signUp = { _, _, _ -> }, resetPassword = {},
                                resendVerification = {}, refreshIdentity = {}, signOut = {}, clearFeedback = {}),
                        )
                    }
                }
            }
        }
        compose.waitForIdle()
        val icon = compose.onNodeWithTag("home-icon-smart", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val name = compose.onNodeWithTag("home-name-smart", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue("Compact card icon must sit beside its title", if (localeTag == "ar") icon.left > name.right else icon.right < name.left)
        assertTrue("Icon and title must share a top row", kotlin.math.abs(icon.top - name.top) < 3f)
        if (scale <= 1.2f) {
            val title = compose.onNodeWithTag("home-phone-title", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertTrue(
                "Phone title must remain on one line at normal font scale; height=${title.height}",
                title.height <= 50f * resources.displayMetrics.density,
            )
        }
        compose.onNodeWithTag("nav-home").assertIsSelected()
        capture("initial")
        compose.onNodeWithTag("home-phone-notice").performScrollTo().assertIsDisplayed()
        capture("scrolled")
        compose.onNodeWithTag("nav-account").performClick().assertIsSelected()
        compose.onNodeWithTag("nav-home").performClick().assertIsSelected()
        compose.onNodeWithTag("nav-settings").performClick().assertIsSelected()
    }

    private fun capture(position: String) {
        val output = File(System.getProperty("user.dir"), "build/outputs/roborazzi")
        check(output.exists() || output.mkdirs())
        val screen = if (position == "initial") "SCR-HOME-01" else "SCR-HOME-01-SCROLLED"
        compose.onNodeWithTag("phone-capture").captureRoboImage(File(output,
            "${screen}__${localeTag}_${if (dark) "dark" else "light"}_w${width}_fs${scale}.png").absolutePath)
    }

    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}_{1}_w{2}_fs{3}")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("en", false, 390, 1f), arrayOf("ar", true, 390, 1f),
            arrayOf("en", true, 390, 1f), arrayOf("ar", false, 390, 1f),
            arrayOf("en", false, 360, 1.5f), arrayOf("ar", true, 360, 1.5f),
            arrayOf("en", true, 360, 2f), arrayOf("ar", false, 360, 2f),
        )
    }
}
