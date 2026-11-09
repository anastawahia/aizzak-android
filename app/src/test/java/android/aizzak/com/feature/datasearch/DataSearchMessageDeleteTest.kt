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
class DataSearchMessageDeleteTest(
    private val author: MessageAuthor,
    private val canDelete: Boolean,
) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun deleteCapabilityAppliesToBothAuthorsWhileCopyRemainsAssistantOnly() {
        val message = ChatMessage("target", author, "Message body", "10:30", MessageDelivery.COMPLETE)
        val base = dataSearchTestState()
        compose.setContent {
            AizzakTheme {
                DataSearchContent(
                    DataSearchDestination.CONVERSATION,
                    base.copy(
                        messages = listOf(message),
                        capabilities = base.capabilities.copy(canDeleteMessages = canDelete),
                    ),
                )
            }
        }
        assertEquals(canDelete, tagExists("delete-message-target"))
        assertEquals(author == MessageAuthor.ASSISTANT, tagExists("copy-message-target"))
    }

    private fun tagExists(tag: String): Boolean =
        compose.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}-delete={1}")
        fun matrix(): List<Array<Any>> = MessageAuthor.entries.flatMap { author ->
            listOf(false, true).map { canDelete -> arrayOf(author, canDelete) }
        }
    }
}
