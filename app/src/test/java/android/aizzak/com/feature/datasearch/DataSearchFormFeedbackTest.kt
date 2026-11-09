package android.aizzak.com.feature.datasearch

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTheme
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1200dp")
class DataSearchFormFeedbackTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun blankNameExplainsValidationAndValidNameCanBeSubmittedOnce() {
        var submitted = ""
        val state = mutableStateOf(dataSearchTestState().copy(conversationNameInput = ""))
        val required = ApplicationProvider.getApplicationContext<Context>().getString(R.string.ds_conversation_name_required)
        compose.setContent { AizzakTheme { DataSearchContent(DataSearchDestination.EDIT_CONVERSATION, state.value, actions = DataSearchActions(
            onConversationNameChange = { state.value = state.value.copy(conversationNameInput = it) },
            onCreateConversationSubmit = { submitted = it },
        )) } }
        compose.onNodeWithText(required).assertExists()
        compose.onNodeWithTag("DS004-07").assertIsNotEnabled()
        compose.onNodeWithTag("DS004-03").performTextInput("Travel policy")
        assertEquals(0, compose.onAllNodesWithText(required).fetchSemanticsNodes().size)
        compose.onNodeWithTag("DS004-07").assertIsEnabled().performClick()
        assertEquals("Travel policy", submitted)
    }
}
