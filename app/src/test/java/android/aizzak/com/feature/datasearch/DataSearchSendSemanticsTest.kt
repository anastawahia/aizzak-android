package android.aizzak.com.feature.datasearch

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTheme
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchSendSemanticsTest(private val language: String) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun sendButtonHasLocalizedTalkBackLabel() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val locale = Locale.forLanguageTag(language)
        val configuration = Configuration(context.resources.configuration).apply { setLocales(LocaleList(locale)) }
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        compose.setContent {
            AizzakTheme { DataSearchContent(DataSearchDestination.CONVERSATION, dataSearchTestState()) }
        }
        val descriptions = compose.onNodeWithTag("DS002-10", useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        assertEquals(listOf(context.getString(R.string.ds_send_message)), descriptions)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun locales(): List<Array<Any>> = listOf(arrayOf("en"), arrayOf("ar"))
    }
}
