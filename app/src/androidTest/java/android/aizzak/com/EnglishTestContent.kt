package android.aizzak.com

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import java.util.Locale

/** These text assertions use English resources without changing the user's app locale. */
internal fun ComposeContentTestRule.setEnglishContent(content: @Composable () -> Unit) {
    setContent {
        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val englishConfiguration = remember(configuration) {
            Configuration(configuration).apply { setLocale(Locale.ENGLISH) }
        }
        val englishContext = remember(context, englishConfiguration) {
            context.createConfigurationContext(englishConfiguration)
        }
        CompositionLocalProvider(
            LocalContext provides englishContext,
            LocalConfiguration provides englishConfiguration,
            content = content,
        )
    }
}
