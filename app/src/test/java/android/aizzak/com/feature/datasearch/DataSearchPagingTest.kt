package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w1000dp-h1400dp")
class DataSearchPagingTest(private val width: Int) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun olderConversationAndLoadMoreRemainReachableBeyondViewport() {
        var selected = ""
        var requested = 0
        compose.setContent { AizzakTheme { Box(Modifier.requiredWidth(width.dp).requiredHeight(900.dp)) {
            DataSearchContent(
                if (width >= 840) DataSearchDestination.CONVERSATION else DataSearchDestination.CONVERSATIONS,
                dataSearchTestState().copy(conversations = (0..24).map { ConversationItem("chat-$it", "Conversation $it") }),
                actions = DataSearchActions(onSelectConversation = { selected = it }, onLoadMoreConversations = { requested++ }),
            )
        } } }
        compose.onNodeWithTag("DS003-03").performScrollToNode(hasTestTag("conversation-chat-24"))
        compose.onNodeWithTag("conversation-chat-24").assertIsDisplayed()
        compose.onNodeWithText("Conversation 24", useUnmergedTree = true).performClick()
        assertEquals("chat-24", selected)
        compose.onNodeWithTag("DS003-03").performScrollToNode(hasTestTag("DS003-06"))
        compose.onNodeWithTag("DS003-06").assertIsDisplayed().performClick()
        assertEquals(1, requested)
    }

    @Test
    fun sourcesPagingShowsPartialLoadingThenCompletesWithoutAnotherAction() {
        val state = mutableStateOf(dataSearchTestState().copy(sources = (0..19).map {
            SourceItem("source-$it", "Source $it", safety = FileSafety.READY, index = AnswerIndex.INDEXED)
        }))
        var requested = 0
        compose.setContent { AizzakTheme { Box(Modifier.requiredWidth(width.dp).requiredHeight(900.dp)) {
            DataSearchContent(DataSearchDestination.SOURCES, state.value, actions = DataSearchActions(onLoadMoreSources = {
                requested++
                state.value = state.value.copy(sourcesPage = state.value.sourcesPage.copy(loadingMore = true))
            }))
        } } }
        compose.onNodeWithTag("DS006-05").performScrollToNode(hasTestTag("source-page-status"))
        compose.onNodeWithTag("source-page-status").assertIsDisplayed()
        compose.onNodeWithTag("DS006-05").performScrollToNode(hasTestTag("source-load-more"))
        compose.onNodeWithTag("source-load-more").assertIsDisplayed().performClick()
        assertEquals(1, requested)
        compose.onNodeWithTag("source-load-more").assertIsNotEnabled()
        compose.onNodeWithTag("DS006-05").performScrollToNode(hasTestTag("source-page-loading"))
        compose.onNodeWithTag("source-page-loading").assertIsDisplayed()
        compose.runOnIdle { state.value = state.value.copy(sourcesPage = PageState()) }
        compose.onNodeWithTag("source-load-more").assertDoesNotExist()
        compose.onNodeWithTag("source-page-loading").assertDoesNotExist()
        compose.onNodeWithTag("source-page-status").assertDoesNotExist()
        assertEquals(1, requested)
    }

    @Test
    fun incompleteSourcePageWithoutCursorStillDisclosesPartialList() {
        compose.setContent { AizzakTheme { Box(Modifier.requiredWidth(width.dp).requiredHeight(900.dp)) {
            DataSearchContent(DataSearchDestination.SOURCES, dataSearchTestState().copy(
                sources = emptyList(), sourcesPage = PageState(hasMore = false, complete = false),
            ))
        } } }
        compose.onNodeWithTag("DS006-05").performScrollToNode(hasTestTag("source-page-status"))
        compose.onNodeWithTag("source-page-status").assertIsDisplayed()
        compose.onNodeWithTag("source-load-more").assertDoesNotExist()
    }

    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "width={0}")
        fun widths(): List<Array<Any>> = listOf(arrayOf(390), arrayOf(840))
    }
}
