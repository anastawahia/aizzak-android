package android.aizzak.com.feature.datasearch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataSearchModelsTest {
    @Test
    fun productionDefaultsAreAuthorizationSafeAndContainNoFixtures() {
        val state = DataSearchUiState()
        assertEquals(DataSearchViewState.LOADING, state.viewState)
        assertNull(state.selectedSpace)
        assertNull(state.selectedConversation)
        assertNull(state.selectedSourceId)
        assertNull(state.conversationPaneState)
        assertNull(state.conversationListPaneState)
        assertNull(state.sourcesPaneState)
        assertEquals(ConversationEditMode.CREATE, state.conversationEditMode)
        assertNull(state.conversationEditTarget)
        assertEquals(AddSourceMode.CHOICE, state.addSourceMode)
        assertEquals(UploadPhase.IDLE, state.uploadPhase)
        assertTrue(state.spaces.isEmpty())
        assertTrue(state.conversations.isEmpty())
        assertTrue(state.sources.isEmpty())
        assertTrue(state.messages.isEmpty())
        assertEquals(DataSearchCapabilities(), state.capabilities)
    }
}
