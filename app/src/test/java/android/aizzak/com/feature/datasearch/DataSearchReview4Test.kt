package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchReview4Test {
    @get:Rule val compose = createComposeRule()

    @Test
    fun unpinnedNonIndexedSourceCannotBeIncluded() {
        val source = SourceItem("target", "target.pdf", "PDF", FileSafety.READY, AnswerIndex.NOT_STARTED, pinned = false)
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCE_DETAILS,
                    dataSearchTestState().copy(selectedSourceId = source.id, sources = listOf(source)),
                )
            }
        }
        compose.onNodeWithTag("DS008-06").assertIsNotEnabled()
    }

    @Test
    fun pinnedNonIndexedSourceCanStillBeRemoved() {
        var toggled: Pair<String, Boolean>? = null
        val source = SourceItem("target", "target.pdf", "PDF", FileSafety.READY, AnswerIndex.FAILED, pinned = true)
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.SOURCE_DETAILS,
                    dataSearchTestState().copy(selectedSourceId = source.id, sources = listOf(source)),
                    actions = DataSearchActions(onToggleSourcePin = { id, pinned -> toggled = id to pinned }),
                )
            }
        }
        compose.onNodeWithTag("DS008-06").assertIsEnabled().performClick()
        assertEquals(source.id to false, toggled)
    }

    @Test
    fun acquisitionChoiceCallbacksEmitTypedModes() {
        val modes = mutableListOf<AddSourceMode>()
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.ADD_SOURCE,
                    dataSearchTestState().copy(addSourceMode = AddSourceMode.CHOICE, uploadPhase = UploadPhase.IDLE),
                    actions = DataSearchActions(onAddSourceModeChange = { modes += it }),
                )
            }
        }
        compose.onNodeWithTag("add-file-source-action").performClick()
        compose.onNodeWithTag("add-text-source-action").performClick()
        assertEquals(listOf(AddSourceMode.FILE, AddSourceMode.TEXT), modes)
    }
}
