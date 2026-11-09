package android.aizzak.com.feature.datasearch

import androidx.compose.runtime.Immutable

enum class DataSearchDestination { SPACES, CONVERSATION, CONVERSATIONS, EDIT_CONVERSATION, DELETE_CONVERSATION, SOURCES, ADD_SOURCE, SOURCE_DETAILS, ANSWER_SCOPE, DELETE_SOURCE }

enum class DataSearchViewState { LOADING, CONTENT, EMPTY, ERROR, PERMISSION_DENIED, OFFLINE, SESSION_EXPIRED, PARTIAL }
enum class MessageAuthor { USER, ASSISTANT }
enum class MessageDelivery { SENDING, COMPLETE, STREAMING, INTERRUPTED, PARTIAL, ERROR }
enum class FileSafety { UPLOADED, READY, SCANNING, QUARANTINED }
enum class AnswerIndex { NOT_STARTED, PENDING, INDEXING, INDEXED, FAILED }
enum class SourceFilter { ALL, INDEXED, NOT_INDEXED, FAILED }
enum class AnswerScopeMode { ALL_INDEXED, SELECTED_SOURCES }
enum class ConversationEditMode { CREATE, RENAME }
enum class AddSourceMode { CHOICE, FILE, TEXT }

enum class UploadPhase(
    val cancellable: Boolean,
    val terminal: Boolean,
    val successful: Boolean,
    val progressRank: Int,
) {
    IDLE(cancellable = false, terminal = false, successful = false, progressRank = -1),
    LOCAL_PREPARATION(cancellable = true, terminal = false, successful = false, progressRank = 0),
    REGISTRATION(cancellable = false, terminal = false, successful = false, progressRank = 1),
    BYTE_UPLOAD(cancellable = true, terminal = false, successful = false, progressRank = 2),
    UPLOAD_COMPLETE(cancellable = false, terminal = false, successful = false, progressRank = 3),
    SAFETY_SCAN(cancellable = false, terminal = false, successful = false, progressRank = 4),
    READY_NOT_INDEXED(cancellable = false, terminal = true, successful = true, progressRank = 4),
    INDEXING(cancellable = false, terminal = false, successful = false, progressRank = 5),
    SEARCHABLE(cancellable = false, terminal = true, successful = true, progressRank = 6),
    QUARANTINED(cancellable = false, terminal = true, successful = false, progressRank = 4),
    FAILED(cancellable = false, terminal = true, successful = false, progressRank = -1),
    EXPIRED(cancellable = false, terminal = true, successful = false, progressRank = -1),
    RECOVERY_REQUIRED(cancellable = false, terminal = true, successful = false, progressRank = -1),
}

@Immutable data class SpaceItem(val id: String, val name: String, val sourceCount: Int? = null, val conversationCount: Int? = null)
@Immutable data class ConversationItem(val id: String, val title: String)
@Immutable data class SourceItem(
    val id: String,
    val name: String,
    val type: String? = null,
    val safety: FileSafety,
    val index: AnswerIndex,
    val pinned: Boolean = false,
)
@Immutable data class ChatMessage(
    val id: String,
    val author: MessageAuthor,
    val body: String,
    val time: String? = null,
    val delivery: MessageDelivery,
    val references: List<String> = emptyList(),
)
@Immutable data class PageState(val hasMore: Boolean = false, val loadingMore: Boolean = false, val complete: Boolean = true)
@Immutable data class PendingDeleteTarget(val id: String, val displayName: String? = null)
@Immutable data class TextSourceSubmission(val fileName: String, val content: String)

internal fun SourceItem.canRequestIndex(capabilities: DataSearchCapabilities): Boolean =
    safety == FileSafety.READY &&
        index in setOf(AnswerIndex.NOT_STARTED, AnswerIndex.FAILED) &&
        capabilities.canIndexSource

@Immutable
data class DataSearchCapabilities(
    val canSendMessages: Boolean = false,
    val canCreateConversation: Boolean = false,
    val canRenameConversation: Boolean = false,
    val canDeleteConversation: Boolean = false,
    val canAddSource: Boolean = false,
    val canAddTextSource: Boolean = false,
    val canIndexSource: Boolean = false,
    val canRenameSource: Boolean = false,
    val canDeleteSource: Boolean = false,
    val canDeleteMessages: Boolean = false,
    val canPinSources: Boolean = false,
    val modelCatalogueAvailable: Boolean = false,
    val structuredReferencesAvailable: Boolean = false,
)

@Immutable
data class DataSearchUiState(
    val viewState: DataSearchViewState = DataSearchViewState.LOADING,
    val conversationPaneState: DataSearchViewState? = null,
    val conversationListPaneState: DataSearchViewState? = null,
    val sourcesPaneState: DataSearchViewState? = null,
    val selectedSpace: SpaceItem? = null,
    val selectedConversation: ConversationItem? = null,
    val conversationEditMode: ConversationEditMode = ConversationEditMode.CREATE,
    val conversationEditTarget: ConversationItem? = null,
    val selectedSourceId: String? = null,
    val pendingConversationDelete: PendingDeleteTarget? = null,
    val pendingSourceDelete: PendingDeleteTarget? = null,
    val spaces: List<SpaceItem> = emptyList(),
    val conversations: List<ConversationItem> = emptyList(),
    val sources: List<SourceItem> = emptyList(),
    val sourceFilter: SourceFilter = SourceFilter.ALL,
    val messages: List<ChatMessage> = emptyList(),
    val pinnedCount: Int = 0,
    val answerScope: AnswerScopeMode = AnswerScopeMode.ALL_INDEXED,
    val addSourceMode: AddSourceMode = AddSourceMode.CHOICE,
    val uploadPhase: UploadPhase = UploadPhase.IDLE,
    val draft: String = "",
    val conversationNameInput: String = "",
    val sourceFileNameInput: String = "",
    val sourceTextInput: String = "",
    val working: Boolean = false,
    val spacesPage: PageState = PageState(),
    val conversationsPage: PageState = PageState(),
    val messagesPage: PageState = PageState(),
    val sourcesPage: PageState = PageState(),
    val capabilities: DataSearchCapabilities = DataSearchCapabilities(),
)

/** Hoisted UI events keep this package usable without navigation or backend wiring. */
@Immutable
data class DataSearchActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onReturnHome: () -> Unit = {},
    val onSelectSpace: (String) -> Unit = {},
    val onLoadMoreSpaces: () -> Unit = {},
    val onOpenConversations: () -> Unit = {},
    val onOpenSources: () -> Unit = {},
    val onOpenAnswerScope: () -> Unit = {},
    val onOpenModelSelector: () -> Unit = {},
    val onLoadOlderMessages: () -> Unit = {},
    val onDraftChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onCopyMessage: (String) -> Unit = {},
    val onRequestDeleteMessage: (String) -> Unit = {},
    val onRetryMessage: (String) -> Unit = {},
    val onCreateConversation: () -> Unit = {},
    val onSelectConversation: (String) -> Unit = {},
    val onRenameConversation: (String) -> Unit = {},
    val onRequestDeleteConversation: (String) -> Unit = {},
    val onLoadMoreConversations: () -> Unit = {},
    val onConversationNameChange: (String) -> Unit = {},
    val onCreateConversationSubmit: (String) -> Unit = {},
    val onRenameConversationSubmit: (String, String) -> Unit = { _, _ -> },
    val onConfirmDeleteConversation: (String) -> Unit = {},
    val onCancel: () -> Unit = {},
    val onAddSource: () -> Unit = {},
    val onAddSourceModeChange: (AddSourceMode) -> Unit = {},
    val onSourceFilterChange: (SourceFilter) -> Unit = {},
    val onSourceFileNameChange: (String) -> Unit = {},
    val onSourceTextChange: (String) -> Unit = {},
    val onSubmitTextSource: (TextSourceSubmission) -> Unit = {},
    val onLoadMoreSources: () -> Unit = {},
    val onSelectSource: (String) -> Unit = {},
    val onIndexSource: (String) -> Unit = {},
    val onRenameSource: (String) -> Unit = {},
    val onRequestDeleteSource: (String) -> Unit = {},
    val onConfirmDeleteSource: (String) -> Unit = {},
    val onToggleSourcePin: (String, Boolean) -> Unit = { _, _ -> },
    val onUseAllSources: () -> Unit = {},
    val onAnswerScopeChange: (AnswerScopeMode) -> Unit = {},
    val onDone: () -> Unit = {},
)
