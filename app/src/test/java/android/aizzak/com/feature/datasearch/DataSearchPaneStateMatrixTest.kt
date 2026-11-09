package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

enum class PaneUnderTest { CONVERSATION, CONVERSATION_LIST, SOURCES }

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w840dp-h1200dp")
class DataSearchPaneStateMatrixTest(
    private val pane: PaneUnderTest,
    private val paneState: DataSearchViewState,
) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun paneStateRendersDataEmptyOrSafeSurfaceExclusively() {
        val base = dataSearchTestState()
        val state = when (pane) {
            PaneUnderTest.CONVERSATION -> base.copy(
                selectedSpace = null,
                conversationPaneState = paneState,
                conversationListPaneState = DataSearchViewState.EMPTY,
                sourcesPaneState = DataSearchViewState.EMPTY,
                conversations = emptyList(),
                sources = emptyList(),
            )
            PaneUnderTest.CONVERSATION_LIST -> base.copy(
                selectedSpace = null,
                selectedConversation = null,
                conversationPaneState = DataSearchViewState.EMPTY,
                conversationListPaneState = paneState,
                sourcesPaneState = DataSearchViewState.EMPTY,
                messages = emptyList(),
                sources = emptyList(),
            )
            PaneUnderTest.SOURCES -> base.copy(
                selectedSpace = null,
                selectedConversation = null,
                conversationPaneState = DataSearchViewState.EMPTY,
                conversationListPaneState = DataSearchViewState.EMPTY,
                sourcesPaneState = paneState,
                messages = emptyList(),
                conversations = emptyList(),
            )
        }
        compose.setContent {
            AizzakTheme { DataSearchContent(DataSearchDestination.CONVERSATION, state) }
        }

        when (paneState) {
            DataSearchViewState.CONTENT, DataSearchViewState.PARTIAL -> assertProtectedDataVisible()
            DataSearchViewState.EMPTY -> {
                assertTagExists(emptyTag())
                assertProtectedDataHidden()
            }
            else -> {
                assertTagExists("data-search-pane-state-${paneTag()}-${paneState.name.lowercase()}")
                assertProtectedDataHidden()
                blockedNames().forEach(::assertTextHidden)
            }
        }
    }

    private fun assertProtectedDataVisible() {
        assertTrue(
            "Expected ${protectedText()} for $pane/$paneState",
            compose.onAllNodesWithText(protectedText(), substring = true, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
    }

    private fun assertProtectedDataHidden() = assertTextHidden(protectedText())

    private fun assertTextHidden(text: String) {
        assertTrue(
            "Leaked $text for $pane/$paneState",
            compose.onAllNodesWithText(text, substring = true, useUnmergedTree = true).fetchSemanticsNodes().isEmpty(),
        )
    }

    private fun assertTagExists(tag: String) {
        assertTrue(
            "Missing $tag for $pane/$paneState",
            compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
    }

    private fun paneTag(): String = when (pane) {
        PaneUnderTest.CONVERSATION -> "conversation"
        PaneUnderTest.CONVERSATION_LIST -> "conversation-list"
        PaneUnderTest.SOURCES -> "sources"
    }

    private fun emptyTag(): String = when (pane) {
        PaneUnderTest.CONVERSATION -> "DS002-11"
        PaneUnderTest.CONVERSATION_LIST -> "DS003-07"
        PaneUnderTest.SOURCES -> "DS006-09"
    }

    private fun protectedText(): String = when (pane) {
        PaneUnderTest.CONVERSATION -> "What changed in the policy?"
        PaneUnderTest.CONVERSATION_LIST -> "Policy updates"
        PaneUnderTest.SOURCES -> "policy.pdf"
    }

    private fun blockedNames(): List<String> = when (pane) {
        PaneUnderTest.CONVERSATION -> listOf("Policy updates")
        PaneUnderTest.CONVERSATION_LIST -> listOf("Policy updates")
        PaneUnderTest.SOURCES -> listOf("policy.pdf", "guide.docx", "notes.txt")
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-{1}")
        fun matrix(): List<Array<Any>> = PaneUnderTest.entries.flatMap { pane ->
            DataSearchViewState.entries.map { state -> arrayOf(pane, state) }
        }
    }
}
