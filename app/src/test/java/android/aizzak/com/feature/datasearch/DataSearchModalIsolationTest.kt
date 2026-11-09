package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h1000dp")
@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
class DataSearchModalIsolationTest(private val destination: DataSearchDestination) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun outsideTapsAndKeyboardTraversalCannotActivateContextualControls() {
        var backgroundActions = 0
        var confirmations = 0
        var cancellations = 0
        lateinit var inputModeManager: InputModeManager
        compose.setContent { AizzakTheme {
            inputModeManager = LocalInputModeManager.current
            DataSearchContent(destination, dataSearchTestState(), actions = DataSearchActions(
                onBack = { backgroundActions++ },
                onCreateConversation = { backgroundActions++ },
                onSelectConversation = { backgroundActions++ },
                onRenameConversation = { backgroundActions++ },
                onRequestDeleteConversation = { backgroundActions++ },
                onLoadMoreConversations = { backgroundActions++ },
                onToggleSourcePin = { _, _ -> backgroundActions++ },
                onIndexSource = { backgroundActions++ },
                onRenameSource = { backgroundActions++ },
                onRequestDeleteSource = { backgroundActions++ },
                onConfirmDeleteConversation = { confirmations++ },
                onConfirmDeleteSource = { confirmations++ },
                onCancel = { cancellations++ },
            ))
        } }
        val prefix = if (destination == DataSearchDestination.DELETE_CONVERSATION) "DS005" else "DS010"
        val cancel = if (prefix == "DS005") "DS005-06" else "DS010-07"
        val delete = if (prefix == "DS005") "DS005-07" else "DS010-08"
        compose.runOnIdle { assertTrue(inputModeManager.requestInputMode(InputMode.Keyboard)) }
        compose.onNodeWithTag(cancel).assertIsFocused()
        compose.onNodeWithTag("$prefix-01").performTouchInput {
            // Top app bar and New conversation remain visible behind the modal.
            click(Offset(width * .1f, height * .035f))
            click(Offset(width * .5f, height * .1f))
            click(Offset(width * .1f, height * .95f))
        }
        assertEquals(0, backgroundActions)
        assertEquals(0, confirmations)
        assertEquals(0, cancellations)
        // Pointer events return Material to touch mode. Re-enter actual keyboard
        // navigation and prove the safe focus resets before checking traversal.
        compose.runOnIdle { assertTrue(inputModeManager.requestInputMode(InputMode.Keyboard)) }
        compose.onNodeWithTag(cancel).assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithTag(delete).assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithTag(cancel).assertIsFocused().performKeyInput {
            keyDown(Key.ShiftLeft)
            pressKey(Key.Tab)
            keyUp(Key.ShiftLeft)
        }
        compose.onNodeWithTag(delete).assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, confirmations)
        assertEquals(0, backgroundActions)
        compose.onNodeWithTag(delete).performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithTag(cancel).assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, cancellations)
        assertEquals(0, backgroundActions)
    }

    companion object {
        @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun destinations(): List<Array<Any>> = listOf(
            arrayOf(DataSearchDestination.DELETE_CONVERSATION),
            arrayOf(DataSearchDestination.DELETE_SOURCE),
        )
    }
}
