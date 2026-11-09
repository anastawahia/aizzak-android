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

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1000dp")
class DataSearchStateMatrixTest(
    private val destination: DataSearchDestination,
    private val viewState: DataSearchViewState,
) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun stateIsExclusiveAndAuthorizationSafe() {
        compose.setContent { AizzakTheme { DataSearchContent(destination, dataSearchTestState(viewState)) } }
        val destinationSpecificEmpty = viewState == DataSearchViewState.EMPTY && destination in setOf(
            DataSearchDestination.CONVERSATION,
            DataSearchDestination.CONVERSATIONS,
            DataSearchDestination.SOURCES,
        )
        val recoverableFormError = viewState == DataSearchViewState.ERROR && destination in setOf(
            DataSearchDestination.EDIT_CONVERSATION,
            DataSearchDestination.ANSWER_SCOPE,
        )
        val readableOffline = viewState == DataSearchViewState.OFFLINE && destination in setOf(
            DataSearchDestination.CONVERSATION, DataSearchDestination.SOURCES,
            DataSearchDestination.SOURCE_DETAILS, DataSearchDestination.EDIT_CONVERSATION,
        )
        if (viewState == DataSearchViewState.PARTIAL) {
            assertTrue(compose.onAllNodesWithTag("data-search-state-partial", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            assertTrue(compose.onAllNodesWithTag(contentTag(destination), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        } else if (readableOffline) {
            assertTrue(compose.onAllNodesWithTag("data-search-state-offline", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            assertTrue(compose.onAllNodesWithTag(contentTag(destination), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        } else if (recoverableFormError) {
            assertTrue(compose.onAllNodesWithTag(contentTag(destination), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            if (destination == DataSearchDestination.EDIT_CONVERSATION) {
                assertTrue(compose.onAllNodesWithText("Release review", substring = false, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            } else {
                assertTrue(compose.onAllNodesWithText("policy.pdf", substring = false, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            }
        } else if (destinationSpecificEmpty) {
            assertTrue(compose.onAllNodesWithTag(contentTag(destination), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            when (destination) {
                DataSearchDestination.CONVERSATION -> {
                    assertTrue(compose.onAllNodesWithTag("DS002-09", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
                    assertTrue(compose.onAllNodesWithTag("DS002-11", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
                }
                DataSearchDestination.CONVERSATIONS -> {
                    assertTrue(compose.onAllNodesWithTag("DS003-02", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
                    assertTrue(compose.onAllNodesWithTag("DS003-07", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
                }
                DataSearchDestination.SOURCES -> {
                    assertTrue(compose.onAllNodesWithTag("DS006-02", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
                    assertTrue(compose.onAllNodesWithTag("DS006-09", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
                }
                else -> Unit
            }
        } else {
            assertTrue(compose.onAllNodesWithTag("data-search-state-${viewState.name.lowercase()}", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
            assertTrue(compose.onAllNodesWithTag(contentTag(destination), useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
            assertTrue(compose.onAllNodesWithText("Product knowledge", substring = false, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
            assertTrue(compose.onAllNodesWithText("Policy updates", substring = false, useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        }
        assertTrue(
            "Missing inventory state element ${inventoryStateTag(destination, viewState)}",
            compose.onAllNodesWithTag(inventoryStateTag(destination, viewState), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
        if (destination == DataSearchDestination.ADD_SOURCE && viewState == DataSearchViewState.ERROR) {
            assertTrue(compose.onAllNodesWithTag("DS007-06", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        }
        if (destination == DataSearchDestination.ANSWER_SCOPE && viewState == DataSearchViewState.ERROR) {
            assertTrue(compose.onAllNodesWithTag("DS009-06", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-{1}")
        fun matrix(): List<Array<Any>> = DataSearchDestination.entries.flatMap { destination ->
            listOf(
                DataSearchViewState.LOADING,
                DataSearchViewState.EMPTY,
                DataSearchViewState.ERROR,
                DataSearchViewState.PERMISSION_DENIED,
                DataSearchViewState.OFFLINE,
                DataSearchViewState.SESSION_EXPIRED,
                DataSearchViewState.PARTIAL,
            ).map { state -> arrayOf(destination, state) }
        }

        private fun contentTag(destination: DataSearchDestination): String = when (destination) {
            DataSearchDestination.SPACES -> "DS001-03"
            DataSearchDestination.CONVERSATION -> "DS002-03"
            DataSearchDestination.CONVERSATIONS -> "DS003-03"
            DataSearchDestination.EDIT_CONVERSATION -> "DS004-03"
            DataSearchDestination.DELETE_CONVERSATION -> "DS005-01"
            DataSearchDestination.SOURCES -> "DS006-05"
            DataSearchDestination.ADD_SOURCE -> "DS007-04"
            DataSearchDestination.SOURCE_DETAILS -> "DS008-03"
            DataSearchDestination.ANSWER_SCOPE -> "DS009-04"
            DataSearchDestination.DELETE_SOURCE -> "DS010-01"
        }

        private fun inventoryStateTag(destination: DataSearchDestination, viewState: DataSearchViewState): String {
            if (viewState == DataSearchViewState.EMPTY) return when (destination) {
                DataSearchDestination.SPACES -> "DS001-07"
                DataSearchDestination.CONVERSATION -> "DS002-11"
                DataSearchDestination.CONVERSATIONS -> "DS003-07"
                DataSearchDestination.SOURCES -> "DS006-09"
                else -> inventoryNoticeTag(destination)
            }
            return inventoryNoticeTag(destination)
        }

        private fun inventoryNoticeTag(destination: DataSearchDestination): String = when (destination) {
            DataSearchDestination.SPACES -> "DS001-08"
            DataSearchDestination.CONVERSATION -> "DS002-12"
            DataSearchDestination.CONVERSATIONS -> "DS003-08"
            DataSearchDestination.EDIT_CONVERSATION -> "DS004-08"
            DataSearchDestination.DELETE_CONVERSATION -> "DS005-05"
            DataSearchDestination.SOURCES -> "DS006-10"
            DataSearchDestination.ADD_SOURCE -> "DS007-09"
            DataSearchDestination.SOURCE_DETAILS -> "DS008-09"
            DataSearchDestination.ANSWER_SCOPE -> "DS009-09"
            DataSearchDestination.DELETE_SOURCE -> "DS010-06"
        }
    }
}
