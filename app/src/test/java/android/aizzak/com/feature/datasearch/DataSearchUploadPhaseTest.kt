package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchUploadPhaseTest(private val phase: UploadPhase) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun phaseDerivesIndicatorCancellationAndTerminalAction() {
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.ADD_SOURCE,
                    dataSearchTestState().copy(uploadPhase = phase),
                )
            }
        }
        assertEquals(
            phase.cancellable,
            compose.onAllNodesWithTag("DS007-07", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
        assertEquals(
            phase != UploadPhase.IDLE && !phase.terminal && phase.progressRank >= 0,
            compose.onAllNodesWithTag("DS007-05", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
        assertEquals(
            phase != UploadPhase.IDLE,
            compose.onAllNodesWithTag("DS007-06", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
        if (phase == UploadPhase.IDLE) {
            assertTrue(compose.onAllNodesWithTag("DS007-08", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        } else if (phase.terminal && phase.successful) {
            compose.onNodeWithTag("DS007-08").assertIsEnabled()
        } else {
            compose.onNodeWithTag("DS007-08").assertIsNotEnabled()
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun phases(): List<Array<Any>> = UploadPhase.entries.map { arrayOf(it) }
    }
}
