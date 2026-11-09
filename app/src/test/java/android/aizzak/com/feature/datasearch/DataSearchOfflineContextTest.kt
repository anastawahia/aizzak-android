package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchOfflineContextTest(private val destination: DataSearchDestination) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun loadedOfflineContextIsReadableFrozenAndNeverAutomaticallyResubmitted() {
        var mutations = 0
        val state = mutableStateOf(dataSearchTestState(DataSearchViewState.OFFLINE))
        compose.setContent { AizzakTheme { DataSearchContent(destination, state.value, actions = DataSearchActions(
            onSend = { mutations++ }, onAddSource = { mutations++ }, onIndexSource = { mutations++ },
            onRenameSource = { mutations++ }, onRequestDeleteSource = { mutations++ },
            onToggleSourcePin = { _, _ -> mutations++ }, onCreateConversationSubmit = { mutations++ },
            onLoadOlderMessages = { mutations++ }, onLoadMoreSources = { mutations++ },
        )) } }
        assertTag("data-search-state-offline")
        when (destination) {
            DataSearchDestination.CONVERSATION -> {
                assertText("What changed in the policy?")
                assertText("Summarize the approval step")
                compose.onNodeWithTag("DS002-09").assertIsNotEnabled()
            }
            DataSearchDestination.SOURCES, DataSearchDestination.SOURCE_DETAILS -> assertText("policy.pdf")
            DataSearchDestination.EDIT_CONVERSATION -> {
                assertText("Release review")
                compose.onNodeWithTag("DS004-03").assertIsNotEnabled()
            }
            else -> error("Unsupported test destination")
        }
        compose.onNodeWithTag(actionTag()).assertIsNotEnabled().performTouchInput { click() }
        assertEquals(0, mutations)
        compose.runOnIdle { state.value = state.value.copy(viewState = DataSearchViewState.CONTENT) }
        assertEquals(0, mutations)
        compose.onNodeWithTag(actionTag()).assertIsEnabled().performClick()
        assertEquals(1, mutations)
    }

    @Test
    fun offlineWithoutSelectedSpaceNeverDisplaysCachedNamesOrTranscript() {
        compose.setContent { AizzakTheme {
            DataSearchContent(destination, dataSearchTestState(DataSearchViewState.OFFLINE).copy(selectedSpace = null))
        } }
        listOf("policy.pdf", "What changed in the policy?", "Release review", "Policy updates").forEach {
            assertTrue("Cached content appeared without a selected space", compose.onAllNodesWithText(it, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        }
        assertTag("data-search-state-offline")
    }

    @Test
    fun offlineRenameNeverRevealsNamesFromDeniedOrExpiredConversationPane() {
        if (destination != DataSearchDestination.EDIT_CONVERSATION) return
        val state = mutableStateOf(dataSearchTestState(DataSearchViewState.OFFLINE).copy(
            conversationEditMode = ConversationEditMode.RENAME,
            conversationEditTarget = ConversationItem("restricted", "Restricted previous title"),
            conversationNameInput = "Restricted previous input",
            conversationPaneState = DataSearchViewState.PERMISSION_DENIED,
        ))
        compose.setContent { AizzakTheme { DataSearchContent(destination, state.value) } }
        listOf(DataSearchViewState.PERMISSION_DENIED, DataSearchViewState.SESSION_EXPIRED).forEach { blockedPane ->
            compose.runOnIdle { state.value = state.value.copy(conversationPaneState = blockedPane) }
            listOf("Restricted previous title", "Restricted previous input").forEach { text ->
                assertTrue("$blockedPane leaked $text", compose.onAllNodesWithText(text, substring = true, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
            }
            assertTrue(compose.onAllNodesWithTag("DS004-03", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        }
        compose.runOnIdle { state.value = state.value.copy(conversationPaneState = DataSearchViewState.CONTENT) }
        assertText("Restricted previous input")
        compose.onNodeWithTag("DS004-03").assertIsNotEnabled()
    }

    private fun actionTag(): String = when (destination) {
        DataSearchDestination.CONVERSATION -> "DS002-10"
        DataSearchDestination.SOURCES -> "DS006-02"
        DataSearchDestination.SOURCE_DETAILS -> "DS008-08"
        DataSearchDestination.EDIT_CONVERSATION -> "DS004-07"
        else -> error("Unsupported test destination")
    }

    private fun assertText(text: String) = assertTrue(compose.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    private fun assertTag(tag: String) = assertTrue(compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())

    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun destinations(): List<Array<Any>> = listOf(
            arrayOf(DataSearchDestination.CONVERSATION), arrayOf(DataSearchDestination.SOURCES),
            arrayOf(DataSearchDestination.SOURCE_DETAILS), arrayOf(DataSearchDestination.EDIT_CONVERSATION),
        )
    }
}
