package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun composerHoistsTypingSendCopyDeleteAndBack() {
        var changed = ""
        var sent = 0
        var copied = ""
        var deleted = ""
        var backed = 0
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    dataSearchTestState(),
                    actions = DataSearchActions(
                        onDraftChange = { changed = it },
                        onSend = { sent++ },
                        onCopyMessage = { copied = it },
                        onRequestDeleteMessage = { deleted = it },
                        onBack = { backed++ },
                    ),
                )
            }
        }
        compose.onNodeWithTag("DS002-09").performTextReplacement("New question")
        compose.onNodeWithTag("DS002-10").assertIsEnabled().performClick()
        compose.onNodeWithTag("copy-message-assistant-1").performClick()
        compose.onNodeWithTag("delete-message-assistant-1").performClick()
        compose.onNodeWithTag("data-search-back").performClick()
        assertEquals("New question", changed)
        assertEquals(1, sent)
        assertEquals("assistant-1", copied)
        assertEquals("assistant-1", deleted)
        assertEquals(1, backed)
    }

    @Test
    fun sendAndDestructiveActionsRespectCapabilities() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    dataSearchTestState().copy(capabilities = DataSearchCapabilities(), draft = "Question"),
                )
            }
        }
        compose.onNodeWithTag("DS002-10").assertIsNotEnabled()
    }

    @Test
    fun deleteConfirmationAndCancelAreHoisted() {
        var deleted = ""
        var cancelled = 0
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.DELETE_SOURCE,
                    dataSearchTestState(),
                    actions = DataSearchActions(onConfirmDeleteSource = { deleted = it }, onCancel = { cancelled++ }),
                )
            }
        }
        compose.onNodeWithTag("DS010-08").assertIsEnabled().performClick()
        compose.onNodeWithTag("DS010-07").performClick()
        assertEquals("policy", deleted)
        assertEquals(1, cancelled)
    }

    @Test
    fun paginationAndSelectionsUseCallbacks() {
        var loaded = 0
        var selected = ""
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SPACES,
                    dataSearchTestState(),
                    actions = DataSearchActions(onLoadMoreSpaces = { loaded++ }, onSelectSpace = { selected = it }),
                )
            }
        }
        compose.onNodeWithTag("DS001-05").performClick()
        compose.onAllNodesWithTag("DS001-04")[0].performClick()
        assertEquals(1, loaded)
        assertTrue(selected.isNotEmpty())
    }
}
