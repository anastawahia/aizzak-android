package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1400dp")
class DataSearchIndexSafetyTest(
    private val safety: FileSafety,
    private val index: AnswerIndex,
    private val permitted: Boolean,
    private val destination: DataSearchDestination,
) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun onlySafeApplicableAndPermittedSourcesCanRequestIndexing() {
        var indexed = ""
        val source = SourceItem("target", "target.pdf", safety = safety, index = index)
        compose.setContent { AizzakTheme {
            val initial = dataSearchTestState()
            DataSearchContent(destination, initial.copy(
                sources = listOf(source), selectedSourceId = source.id,
                capabilities = initial.capabilities.copy(canIndexSource = permitted),
            ), actions = DataSearchActions(onIndexSource = { indexed = it }))
        } }
        val tag = if (destination == DataSearchDestination.SOURCES) "DS006-07" else "source-index-action"
        val action = compose.onNodeWithTag(tag)
        val applicable = index == AnswerIndex.NOT_STARTED || index == AnswerIndex.FAILED
        when {
            !applicable -> action.assertDoesNotExist()
            safety == FileSafety.READY && permitted -> action.assertIsEnabled().performClick()
            else -> action.assertIsNotEnabled().performTouchInput { click() }
        }
        assertEquals(if (applicable && safety == FileSafety.READY && permitted) source.id else "", indexed)
    }

    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-{1}-permitted={2}-{3}")
        fun matrix(): List<Array<Any>> = FileSafety.entries.flatMap { safety ->
            AnswerIndex.entries.flatMap { index -> listOf(false, true).flatMap { permitted ->
                listOf(DataSearchDestination.SOURCES, DataSearchDestination.SOURCE_DETAILS).map { destination ->
                    arrayOf(safety, index, permitted, destination)
                }
            } }
        }
    }
}
