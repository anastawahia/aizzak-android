package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchConditionalActionTest(private val answerIndex: AnswerIndex) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun sourceDetailIndexActionMatchesItemState() {
        val source = SourceItem("target", "target.pdf", "PDF", FileSafety.READY, answerIndex)
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCE_DETAILS,
                    dataSearchTestState().copy(selectedSourceId = source.id, sources = listOf(source)),
                )
            }
        }
        assertEquals(
            answerIndex == AnswerIndex.NOT_STARTED || answerIndex == AnswerIndex.FAILED,
            compose.onAllNodesWithTag("source-index-action", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun states(): List<Array<Any>> = AnswerIndex.entries.map { arrayOf(it) }
    }
}
