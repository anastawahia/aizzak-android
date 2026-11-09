package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w1000dp-h3000dp")
class DataSearchSemanticsTest(
    private val destination: DataSearchDestination,
    private val inventoryIds: Set<String>,
) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun everyApprovedInventoryElementHasSemanticsNode() {
        compose.setContent { AizzakTheme { DataSearchContent(destination, dataSearchTestState()) } }
        inventoryIds.forEach { id ->
            assertTrue("Missing inventory element $id", compose.onAllNodesWithTag(id, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        }
    }

    @Test
    fun emptyConversationElementIsRenderedOnlyByEmptyState() {
        if (destination != DataSearchDestination.CONVERSATION) return
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    dataSearchTestState(DataSearchViewState.EMPTY),
                )
            }
        }
        assertTrue("Missing conditional element DS002-11", compose.onAllNodesWithTag("DS002-11", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        assertTrue("Conversation shell missing from empty state", compose.onAllNodesWithTag("DS002-03", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        assertTrue("Message content leaked into empty state", compose.onAllNodesWithTag("DS002-04", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        assertTrue("Composer missing from empty state", compose.onAllNodesWithTag("DS002-09", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun offlineConversationKeepsAuthorizedLoadedTranscriptReadOnly() {
        if (destination != DataSearchDestination.CONVERSATION) return
        compose.setContent {
            AizzakTheme { DataSearchContent(DataSearchDestination.CONVERSATION, dataSearchTestState(DataSearchViewState.OFFLINE)) }
        }
        assertTrue("Missing conditional element DS002-12", compose.onAllNodesWithTag("DS002-12", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        assertTrue("Authorized loaded transcript was lost offline", compose.onAllNodesWithTag("DS002-03", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun unconfirmedPurgeWordingIsNotExposedToUsers() {
        if (destination != DataSearchDestination.DELETE_SOURCE) return
        compose.setContent { AizzakTheme { DataSearchContent(destination, dataSearchTestState()) } }
        assertTrue(compose.onAllNodesWithTag("DS010-05", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        assertTrue(compose.onAllNodesWithTag("DS010-04", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = listOf(
            arrayOf(DataSearchDestination.SPACES, ids("DS001", 8) - setOf("DS001-07", "DS001-08")),
            arrayOf(DataSearchDestination.CONVERSATION, ids("DS002", 13) - setOf("DS002-11", "DS002-12")),
            arrayOf(DataSearchDestination.CONVERSATIONS, ids("DS003", 8) - setOf("DS003-07", "DS003-08")),
            arrayOf(DataSearchDestination.EDIT_CONVERSATION, ids("DS004", 8) - "DS004-08"),
            arrayOf(DataSearchDestination.DELETE_CONVERSATION, ids("DS005", 7)),
            arrayOf(DataSearchDestination.SOURCES, ids("DS006", 11) - setOf("DS006-09", "DS006-10")),
            arrayOf(DataSearchDestination.ADD_SOURCE, ids("DS007", 9) - "DS007-09"),
            arrayOf(DataSearchDestination.SOURCE_DETAILS, ids("DS008", 9) - "DS008-09"),
            arrayOf(DataSearchDestination.ANSWER_SCOPE, ids("DS009", 9) - setOf("DS009-06", "DS009-09")),
            arrayOf(DataSearchDestination.DELETE_SOURCE, ids("DS010", 8) - "DS010-05"),
        )

        private fun ids(prefix: String, count: Int): Set<String> =
            (1..count).mapTo(linkedSetOf()) { "$prefix-${it.toString().padStart(2, '0')}" }
    }
}
