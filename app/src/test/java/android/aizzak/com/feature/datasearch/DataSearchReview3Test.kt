package android.aizzak.com.feature.datasearch

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTheme
import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w840dp-h1200dp")
class DataSearchReview3Test {
    @get:Rule val compose = createComposeRule()

    @Test
    fun emptyConversationAt840KeepsPopulatedSupportingPanes() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    dataSearchTestState(DataSearchViewState.EMPTY),
                )
            }
        }
        assertTagExists("DS002-11")
        assertTagExists("DS003-04")
        assertTagExists("DS006-06")
        assertTagAbsent("DS003-07")
        assertTagAbsent("DS006-09")
    }

    @Test
    fun emptySourcesAt840KeepsConversationPanesPopulated() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCES,
                    dataSearchTestState(DataSearchViewState.EMPTY),
                )
            }
        }
        assertTagExists("DS006-09")
        assertTagExists("DS003-04")
        assertTagExists("DS002-05")
    }

    @Test
    fun createModeUsesCreateCapabilityLabelAndNameCallback() {
        var submittedName = ""
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.EDIT_CONVERSATION,
                    dataSearchTestState().copy(
                        conversationEditMode = ConversationEditMode.CREATE,
                        conversationEditTarget = null,
                        capabilities = dataSearchTestState().capabilities.copy(
                            canCreateConversation = true,
                            canRenameConversation = false,
                        ),
                    ),
                    actions = DataSearchActions(onCreateConversationSubmit = { submittedName = it }),
                )
            }
        }
        assertTextExists(context.getString(R.string.ds_new_conversation))
        assertTextExists(context.getString(R.string.ds_create))
        compose.onNodeWithTag("DS004-07").assertIsEnabled().performClick()
        assertEquals("Release review", submittedName)
    }

    @Test
    fun renameModeUsesRenameCapabilityTargetAndNameCallback() {
        var submittedId = ""
        var submittedName = ""
        val target = ConversationItem("rename-target", "Original title")
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.EDIT_CONVERSATION,
                    dataSearchTestState().copy(
                        conversationEditMode = ConversationEditMode.RENAME,
                        conversationEditTarget = target,
                        capabilities = dataSearchTestState().capabilities.copy(
                            canCreateConversation = false,
                            canRenameConversation = true,
                        ),
                    ),
                    actions = DataSearchActions(
                        onRenameConversationSubmit = { id, name ->
                            submittedId = id
                            submittedName = name
                        },
                    ),
                )
            }
        }
        assertTextExists(context.getString(R.string.ds_rename_conversation_title, target.title))
        assertTextExists(context.getString(R.string.ds_save_changes))
        compose.onNodeWithTag("DS004-07").assertIsEnabled().performClick()
        assertEquals(target.id, submittedId)
        assertEquals("Release review", submittedName)
    }

    @Test
    fun pinnedSourceUsesRemoveMeaningAndTogglesOff() {
        var toggled: Pair<String, Boolean>? = null
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCE_DETAILS,
                    dataSearchTestState(),
                    actions = DataSearchActions(onToggleSourcePin = { id, pinned -> toggled = id to pinned }),
                )
            }
        }
        assertScopeDescription(context.getString(R.string.ds_remove_scope))
        compose.onNodeWithTag("DS008-06").performClick()
        assertEquals("policy" to false, toggled)
    }

    @Test
    fun unpinnedSourceUsesIncludeMeaningAndTogglesOn() {
        var toggled: Pair<String, Boolean>? = null
        val context = ApplicationProvider.getApplicationContext<Context>()
        val unpinned = dataSearchTestState().sources.first().copy(pinned = false)
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCE_DETAILS,
                    dataSearchTestState().copy(sources = listOf(unpinned)),
                    actions = DataSearchActions(onToggleSourcePin = { id, pinned -> toggled = id to pinned }),
                )
            }
        }
        assertScopeDescription(context.getString(R.string.ds_include_scope))
        compose.onNodeWithTag("DS008-06").performClick()
        assertEquals("policy" to true, toggled)
    }

    @Test
    fun partialAndErrorMessagesRemainVisibleWithAccessibleRetry() {
        val retried = mutableListOf<String>()
        val partial = ChatMessage("partial", MessageAuthor.ASSISTANT, "Preserved partial answer", "10:26", MessageDelivery.PARTIAL)
        val error = ChatMessage("error", MessageAuthor.ASSISTANT, "Preserved failed answer", "10:27", MessageDelivery.ERROR)
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    dataSearchTestState().copy(messages = listOf(partial, error)),
                    actions = DataSearchActions(onRetryMessage = { retried += it }),
                )
            }
        }
        assertTagExists("assistant-markdown-partial")
        assertTagExists("assistant-markdown-error")
        assertStatusIsAccessible("message-status-partial")
        assertStatusIsAccessible("message-status-error")
        compose.onNodeWithTag("retry-message-partial").performClick()
        compose.onNodeWithTag("retry-message-error").performClick()
        assertEquals(listOf("partial", "error"), retried)
    }

    private fun assertScopeDescription(expected: String) {
        val descriptions = compose.onNodeWithTag("DS008-06", useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        assertEquals(listOf(expected), descriptions)
    }

    private fun assertStatusIsAccessible(tag: String) {
        val descriptions = compose.onNodeWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        assertTrue(descriptions.single().isNotBlank())
    }

    private fun assertTagExists(tag: String) {
        assertTrue("Missing tag $tag", compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }

    private fun assertTagAbsent(tag: String) {
        assertTrue("Unexpected tag $tag", compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
    }

    private fun assertTextExists(text: String) {
        assertTrue("Missing text $text", compose.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }
}
