package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchTextSubmissionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun chooseTextEditAndSubmitPayloadOnceThenFreezeInFlight() {
        val state = mutableStateOf(dataSearchTestState().copy(
            addSourceMode = AddSourceMode.CHOICE,
            uploadPhase = UploadPhase.IDLE,
            sourceFileNameInput = "",
            sourceTextInput = "",
        ))
        val submitted = mutableListOf<TextSourceSubmission>()
        compose.setContent {
            AizzakTheme {
                DataSearchContent(DataSearchDestination.ADD_SOURCE, state.value, actions = DataSearchActions(
                    onAddSourceModeChange = { state.value = state.value.copy(addSourceMode = it) },
                    onSourceFileNameChange = { state.value = state.value.copy(sourceFileNameInput = it) },
                    onSourceTextChange = { state.value = state.value.copy(sourceTextInput = it) },
                    onSubmitTextSource = {
                        submitted += it
                        state.value = state.value.copy(working = true, uploadPhase = UploadPhase.REGISTRATION)
                    },
                ))
            }
        }
        compose.onNodeWithTag("add-text-source-action").performClick()
        compose.onNodeWithTag("text-source-submit").assertIsNotEnabled()
        compose.onNodeWithTag("text-source-file-name").performTextReplacement("   ")
        compose.onNodeWithTag("text-source-content").performTextReplacement("Body")
        compose.onNodeWithTag("text-source-submit").assertIsNotEnabled()
        compose.onNodeWithTag("text-source-file-name").performTextReplacement(" notes.txt ")
        compose.onNodeWithTag("text-source-content").performTextReplacement("\n  ")
        compose.onNodeWithTag("text-source-submit").assertIsNotEnabled()
        compose.onNodeWithTag("text-source-content").performTextReplacement("  Keep whitespace\nالعربية  ")
        compose.onNodeWithTag("text-source-submit").performScrollTo().assertIsEnabled().performClick()
        assertEquals(listOf(TextSourceSubmission("notes.txt", "  Keep whitespace\nالعربية  ")), submitted)
        compose.onNodeWithTag("text-source-submit").assertDoesNotExist()
        compose.onNodeWithTag("text-source-content").assertIsNotEnabled()
        compose.onNodeWithTag("add-text-source-action").assertIsNotEnabled()
        compose.onNodeWithTag("add-file-source-action").assertIsNotEnabled()
    }

    @Test
    fun workingDisablesSubmissionEvenBeforePhaseChanges() {
        compose.setContent { AizzakTheme {
            DataSearchContent(DataSearchDestination.ADD_SOURCE, dataSearchTestState().copy(
                uploadPhase = UploadPhase.IDLE, working = true,
            ))
        } }
        compose.onNodeWithTag("text-source-submit").assertIsNotEnabled()
    }

    @Test
    fun unconfirmedTextCapabilityHidesFormAndSubmit() {
        compose.setContent { AizzakTheme {
            val initial = dataSearchTestState()
            DataSearchContent(DataSearchDestination.ADD_SOURCE, initial.copy(
                uploadPhase = UploadPhase.IDLE,
                capabilities = initial.capabilities.copy(canAddTextSource = false),
            ))
        } }
        compose.onNodeWithTag("text-source-submit").assertDoesNotExist()
        compose.onNodeWithTag("DS007-03").assertDoesNotExist()
    }
}
