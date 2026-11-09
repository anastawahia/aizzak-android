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
class DataSearchAddSourceModeTest(
    private val mode: AddSourceMode,
    private val active: Boolean,
) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun modeAndPhaseControlFieldsAndProgress() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.ADD_SOURCE,
                    dataSearchTestState().copy(
                        addSourceMode = mode,
                        uploadPhase = if (active) UploadPhase.BYTE_UPLOAD else UploadPhase.IDLE,
                    ),
                )
            }
        }
        assertEquals(true, tagExists("DS007-02"))
        assertEquals(mode == AddSourceMode.TEXT, tagExists("DS007-03"))
        assertEquals(active && mode != AddSourceMode.CHOICE, tagExists("DS007-04"))
        assertEquals(active && mode != AddSourceMode.CHOICE, tagExists("DS007-05"))
    }

    private fun tagExists(tag: String): Boolean =
        compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-active={1}")
        fun matrix(): List<Array<Any>> = AddSourceMode.entries.flatMap { mode ->
            listOf(false, true).map { active -> arrayOf(mode, active) }
        }
    }
}
