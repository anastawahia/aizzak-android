package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchReview2Test {
    @get:Rule val compose = createComposeRule()

    @Test
    fun sourceConfirmationUsesExplicitPendingId() {
        var confirmed = ""
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.DELETE_SOURCE,
                    dataSearchTestState().copy(
                        selectedSourceId = "stale-selection",
                        pendingSourceDelete = PendingDeleteTarget("guide", "guide.docx"),
                    ),
                    actions = DataSearchActions(onConfirmDeleteSource = { confirmed = it }),
                )
            }
        }
        compose.onNodeWithTag("DS010-08").assertIsEnabled().performClick()
        assertEquals("guide", confirmed)
    }

    @Test
    fun conversationConfirmationUsesExplicitPendingId() {
        var confirmed = ""
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.DELETE_CONVERSATION,
                    dataSearchTestState().copy(
                        selectedConversation = ConversationItem("wrong", "Wrong conversation"),
                        pendingConversationDelete = PendingDeleteTarget("policy", "Policy updates"),
                    ),
                    actions = DataSearchActions(onConfirmDeleteConversation = { confirmed = it }),
                )
            }
        }
        compose.onNodeWithTag("DS005-07").assertIsEnabled().performClick()
        assertEquals("policy", confirmed)
    }

    @Test
    fun missingPendingDeleteTargetDisablesConfirmation() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.DELETE_SOURCE,
                    dataSearchTestState().copy(pendingSourceDelete = null),
                )
            }
        }
        compose.onNodeWithTag("DS010-08").assertIsNotEnabled()
    }

    @Test
    fun missingPendingConversationTargetDisablesConfirmation() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.DELETE_CONVERSATION,
                    dataSearchTestState().copy(pendingConversationDelete = null),
                )
            }
        }
        compose.onNodeWithTag("DS005-07").assertIsNotEnabled()
    }

    @Test
    fun staleSelectedSourceDoesNotFallBackToFirstSource() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCE_DETAILS,
                    dataSearchTestState().copy(selectedSourceId = "missing-source"),
                )
            }
        }
        assertTagExists("data-search-state-empty")
        assertTrue(compose.onAllNodesWithText("policy.pdf", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun conversationEmptyKeepsScopeComposerAndStartPrompt() {
        var draft = ""
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    dataSearchTestState(DataSearchViewState.EMPTY),
                    actions = DataSearchActions(onDraftChange = { draft = it }),
                )
            }
        }
        assertTagExists("DS002-01")
        assertTagExists("DS002-02")
        assertTagExists("DS002-09")
        assertTagExists("DS002-11")
    }

    @Test
    fun conversationListEmptyKeepsStartAction() {
        var created = 0
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATIONS,
                    dataSearchTestState(DataSearchViewState.EMPTY),
                    actions = DataSearchActions(onCreateConversation = { created++ }),
                )
            }
        }
        compose.onNodeWithTag("DS003-02").assertIsEnabled().performClick()
        assertTagExists("DS003-07")
        assertEquals(1, created)
    }

    @Test
    fun sourcesEmptyKeepsAddAction() {
        var added = 0
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCES,
                    dataSearchTestState(DataSearchViewState.EMPTY),
                    actions = DataSearchActions(onAddSource = { added++ }),
                )
            }
        }
        compose.onNodeWithTag("DS006-02").assertIsEnabled().performClick()
        assertTagExists("DS006-09")
        assertEquals(1, added)
    }

    @Test
    fun unconfirmedCapabilitiesHideAddTextAndMessageDelete() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.ADD_SOURCE,
                    dataSearchTestState().copy(capabilities = DataSearchCapabilities(canAddSource = true)),
                )
            }
        }
        assertTagAbsent("add-text-source-action")
        assertTagAbsent("DS007-03")
    }

    @Test
    fun unconfirmedMessageDeleteCapabilityHidesDeleteAction() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    dataSearchTestState().copy(
                        capabilities = dataSearchTestState().capabilities.copy(canDeleteMessages = false),
                    ),
                )
            }
        }
        assertTagAbsent("delete-message-assistant-1")
        assertTagExists("copy-message-assistant-1")
    }

    @Test
    fun scopeRadioEmitsTypedSelection() {
        var selected: AnswerScopeMode? = null
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.ANSWER_SCOPE,
                    dataSearchTestState().copy(answerScope = AnswerScopeMode.ALL_INDEXED),
                    actions = DataSearchActions(onAnswerScopeChange = { selected = it }),
                )
            }
        }
        compose.onNodeWithTag("DS009-03").performClick()
        assertEquals(AnswerScopeMode.SELECTED_SOURCES, selected)
    }

    private fun assertTagExists(tag: String) {
        assertTrue("Missing tag $tag", compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }

    private fun assertTagAbsent(tag: String) {
        assertTrue("Unexpected tag $tag", compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
    }
}
