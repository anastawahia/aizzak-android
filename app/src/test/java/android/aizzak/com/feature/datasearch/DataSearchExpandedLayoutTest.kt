package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w840dp-h1000dp")
class DataSearchExpandedLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun centerConversationIsWiderThanEachSidePaneAt840dp() {
        compose.setContent { AizzakTheme { DataSearchContent(DataSearchDestination.CONVERSATION, dataSearchTestState()) } }
        val center = compose.onNodeWithTag("data-search-center-pane", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.width
        val start = compose.onNodeWithTag("data-search-start-pane", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.width
        val end = compose.onNodeWithTag("data-search-end-pane", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.width
        assertTrue("center=$center start=$start", center > start)
        assertTrue("center=$center end=$end", center > end)
    }
}
