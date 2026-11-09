package android.aizzak.com.feature.datasearch

import android.aizzak.com.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextAlign

private val paneShape = RoundedCornerShape(16.dp)
private val actionShape = RoundedCornerShape(12.dp)
private val LocalReadOnlyContent = staticCompositionLocalOf { false }

// Amber is a named AIZZAK design role, independent of Material's tertiary role.
@Composable private fun attentionColor() = if (MaterialTheme.colorScheme.surface.luminance() < .5f) Color(0xFFFFD37C) else Color(0xFF8A5100)
@Composable private fun attentionContainer() = if (MaterialTheme.colorScheme.surface.luminance() < .5f) Color(0xFF5B4725) else Color(0xFFFFF0CE)

private fun inferredPaneState(hasContent: Boolean): DataSearchViewState =
    if (hasContent) DataSearchViewState.CONTENT else DataSearchViewState.EMPTY

@Composable
fun DataSearchContent(
    destination: DataSearchDestination,
    state: DataSearchUiState,
    modifier: Modifier = Modifier,
    actions: DataSearchActions = DataSearchActions(),
) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        val hasReadableOfflineContext = state.viewState == DataSearchViewState.OFFLINE && state.selectedSpace != null && when (destination) {
            DataSearchDestination.CONVERSATION -> state.selectedConversation != null && state.messages.isNotEmpty() && state.conversationPaneState !in protectedPaneStates
            DataSearchDestination.SOURCES -> state.sources.isNotEmpty() && state.sourcesPaneState !in protectedPaneStates
            DataSearchDestination.SOURCE_DETAILS -> state.sources.any { it.id == state.selectedSourceId } && state.sourcesPaneState !in protectedPaneStates
            DataSearchDestination.EDIT_CONVERSATION -> state.conversationPaneState !in protectedPaneStates
            else -> false
        }
        if (hasReadableOfflineContext) {
            // The state owner supplied this selected-space context as readable.
            // Offline never broadens access or dispatches a network/mutation event.
            val readOnlyState = state.copy(
                viewState = DataSearchViewState.CONTENT,
                conversationPaneState = offlinePaneState(state.conversationPaneState, state.messages.isNotEmpty()),
                conversationListPaneState = offlinePaneState(state.conversationListPaneState, state.conversations.isNotEmpty()),
                sourcesPaneState = offlinePaneState(state.sourcesPaneState, state.sources.isNotEmpty()),
                working = true,
                capabilities = DataSearchCapabilities(
                    structuredReferencesAvailable = state.capabilities.structuredReferencesAvailable,
                    modelCatalogueAvailable = state.capabilities.modelCatalogueAvailable,
                ),
            )
            Column(Modifier.fillMaxSize().testTag("data-search-state-offline")) {
                InfoNotice(stringResource(R.string.ds_offline), inventoryNoticeTag(destination), Modifier.padding(12.dp), attention = true)
                Box(Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalReadOnlyContent provides true) {
                    DataSearchContent(destination, readOnlyState, actions = DataSearchActions(
                        onBack = actions.onBack,
                        onCancel = actions.onCancel,
                        onCopyMessage = actions.onCopyMessage,
                    ))
                    }
                }
            }
            return@Surface
        }
        val destinationEmptyShell = state.viewState == DataSearchViewState.EMPTY && destination in setOf(
            DataSearchDestination.CONVERSATION,
            DataSearchDestination.CONVERSATIONS,
            DataSearchDestination.SOURCES,
        )
        val recoverableFormError = state.viewState == DataSearchViewState.ERROR && destination in setOf(
            DataSearchDestination.EDIT_CONVERSATION,
            DataSearchDestination.ANSWER_SCOPE,
        )
        if (state.viewState !in setOf(DataSearchViewState.CONTENT, DataSearchViewState.PARTIAL) && !destinationEmptyShell && !recoverableFormError) {
            DataSearchStatePanel(destination, state.viewState, actions)
        } else {
            Box(Modifier.fillMaxSize()) {
                when (destination) {
                    DataSearchDestination.SPACES -> SpacesScreen(state, actions)
                    DataSearchDestination.CONVERSATION -> ConversationScreen(state, actions)
                    DataSearchDestination.CONVERSATIONS -> ConversationListPane(
                        state,
                        paneState = state.conversationListPaneState ?: state.viewState,
                        compact = true,
                        actions,
                    )
                    DataSearchDestination.EDIT_CONVERSATION -> EditConversationScreen(state, actions)
                    DataSearchDestination.DELETE_CONVERSATION -> {
                        Box(Modifier.clearAndSetSemantics { }.focusProperties { canFocus = false }) {
                            ConversationListPane(state, inferredPaneState(state.conversations.isNotEmpty()), compact = true, DataSearchActions())
                        }
                        DeleteConversationDialog(state, actions)
                    }
                    DataSearchDestination.SOURCES -> SourcesScreen(state, actions)
                    DataSearchDestination.ADD_SOURCE -> AddSourceScreen(state, actions)
                    DataSearchDestination.SOURCE_DETAILS -> SourceDetailsScreen(state, actions)
                    DataSearchDestination.ANSWER_SCOPE -> AnswerScopeScreen(state, actions)
                    DataSearchDestination.DELETE_SOURCE -> {
                        Box(Modifier.clearAndSetSemantics { }.focusProperties { canFocus = false }) {
                            SourceDetailsScreen(state.copy(selectedSourceId = state.pendingSourceDelete?.id ?: state.selectedSourceId), DataSearchActions())
                        }
                        DeleteSourceDialog(state, actions)
                    }
                }
                if (state.viewState == DataSearchViewState.PARTIAL && destination !in setOf(DataSearchDestination.EDIT_CONVERSATION, DataSearchDestination.ANSWER_SCOPE)) {
                    Box(Modifier.align(Alignment.BottomCenter).padding(12.dp).testTag("data-search-state-partial")) {
                        InfoNotice(stringResource(R.string.ds_partial_data), inventoryNoticeTag(destination), error = true)
                    }
                }
            }
        }
    }
}

private val protectedPaneStates = setOf(DataSearchViewState.PERMISSION_DENIED, DataSearchViewState.SESSION_EXPIRED)

private fun offlinePaneState(paneState: DataSearchViewState?, hasContent: Boolean): DataSearchViewState? =
    if (paneState == null || paneState == DataSearchViewState.OFFLINE) inferredPaneState(hasContent) else paneState

@Composable
private fun DsHeader(
    title: String,
    subtitle: String? = null,
    tag: String,
    onBack: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    Surface(Modifier.fillMaxWidth().testTag(tag), color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onBack, shape = actionShape, modifier = Modifier.heightIn(min = 48.dp).testTag("data-search-back")) { Text(stringResource(R.string.ds_back)) }
            Column(Modifier.weight(1f)) {
                if (subtitle != null) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun SpacesScreen(state: DataSearchUiState, actions: DataSearchActions) {
    Column(Modifier.fillMaxSize()) {
        DsHeader(stringResource(R.string.ds_choose_space), stringResource(R.string.ds_eyebrow), "DS001-01", actions.onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.ds_space_explanation), Modifier.testTag("DS001-02"), style = MaterialTheme.typography.bodyLarge)
            Column(Modifier.testTag("DS001-03"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.spaces.forEach { space -> SpaceRow(space, actions.onSelectSpace) }
            }
            if (state.spacesPage.hasMore) TextButton(
                shape = actionShape,
                onClick = actions.onLoadMoreSpaces,
                enabled = !state.spacesPage.loadingMore,
                modifier = Modifier.testTag("DS001-05").heightIn(min = 48.dp),
            ) { Text(if (state.spacesPage.loadingMore) stringResource(R.string.loading) else stringResource(R.string.ds_load_more)) }
            InfoNotice(stringResource(R.string.ds_spaces_read_only), "DS001-06", attention = true)
        }
    }
}

@Composable
private fun SpaceRow(space: SpaceItem, onSelect: (String) -> Unit) {
    Card(
        Modifier.fillMaxWidth().testTag("DS001-04").clickable { onSelect(space.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IdentityAvatar(DsSymbol.SPACE)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(space.name, style = MaterialTheme.typography.titleMedium)
                if (space.sourceCount != null && space.conversationCount != null) {
                    Text(
                        stringResource(R.string.ds_space_counts, space.sourceCount, space.conversationCount),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text("›", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ConversationScreen(state: DataSearchUiState, actions: DataSearchActions) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp && LocalDensity.current.fontScale < 1.3f
        val conversationState = state.conversationPaneState ?: state.viewState
        if (expanded) {
            val listState = state.conversationListPaneState ?: inferredPaneState(state.conversations.isNotEmpty())
            val sourcesState = state.sourcesPaneState ?: inferredPaneState(state.sources.isNotEmpty())
            Row(Modifier.fillMaxSize().testTag("DS002-13")) {
                Box(Modifier.weight(.25f).widthIn(min = 200.dp).fillMaxHeight().testTag("data-search-start-pane")) {
                    ConversationListPane(state, paneState = listState, compact = false, actions)
                }
                VerticalDivider(Modifier.fillMaxHeight().width(1.dp))
                Box(Modifier.weight(.5f).widthIn(min = 360.dp).fillMaxHeight().testTag("data-search-center-pane")) { ConversationCenter(state, conversationState, actions) }
                VerticalDivider(Modifier.fillMaxHeight().width(1.dp))
                Box(Modifier.weight(.25f).widthIn(min = 200.dp).fillMaxHeight().testTag("data-search-end-pane")) { SourcesPane(state, paneState = sourcesState, supporting = true, actions) }
            }
        } else ConversationCenter(state, conversationState, actions)
    }
}

@Composable
private fun ConversationCenter(state: DataSearchUiState, paneState: DataSearchViewState, actions: DataSearchActions) {
    if (paneState !in setOf(DataSearchViewState.CONTENT, DataSearchViewState.PARTIAL, DataSearchViewState.EMPTY)) {
        SupportingPaneStatePanel("conversation", paneState)
        return
    }
    val spaceName = state.selectedSpace?.name ?: stringResource(R.string.ds_no_space_selected)
    val conversationTitle = state.selectedConversation?.title ?: stringResource(R.string.ds_no_conversation_selected)
    val sourcesLabel = stringResource(R.string.ds_sources)
    Column(Modifier.fillMaxSize()) {
        DsHeader(conversationTitle, spaceName, "DS002-01", actions.onBack) {
            Row {
                HeaderIconAction("☰", stringResource(R.string.ds_conversations), actions.onOpenConversations, enabled = !state.working)
                TextButton(onClick = actions.onOpenSources, enabled = !state.working, shape = actionShape, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = sourcesLabel }) {
                    DsIcon(DsSymbol.SOURCE, MaterialTheme.colorScheme.primary)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                enabled = !state.working,
                selected = true,
                onClick = actions.onOpenAnswerScope,
                label = { Text(if (state.pinnedCount == 0) stringResource(R.string.ds_all_indexed) else stringResource(R.string.ds_selected_count, state.pinnedCount)) },
                modifier = Modifier.testTag("DS002-02"),
            )
            if (state.capabilities.modelCatalogueAvailable) FilterChip(selected = false, enabled = !state.working, onClick = actions.onOpenModelSelector, label = { Text(stringResource(R.string.ds_agent_default)) })
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().testTag("DS002-03").padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (paneState != DataSearchViewState.EMPTY && state.messagesPage.hasMore) item {
                TextButton(onClick = actions.onLoadOlderMessages, shape = actionShape, enabled = !state.messagesPage.loadingMore && !state.working) { Text(stringResource(R.string.ds_load_older)) }
            }
            if (paneState == DataSearchViewState.EMPTY) item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp).testTag("DS002-11"), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    IdentityAvatar(DsSymbol.RAG)
                    Text(stringResource(R.string.ds_empty_chat), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.ds_empty_chat_body, spaceName), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
            if (paneState != DataSearchViewState.EMPTY) items(state.messages, key = { it.id }) {
                MessageCard(
                    message = it,
                    showReferences = state.capabilities.structuredReferencesAvailable,
                    canDeleteMessage = state.capabilities.canDeleteMessages,
                    referencesEnabled = !state.working,
                    actions = actions,
                )
            }
        }
        Composer(state, actions)
    }
}

@Composable
private fun MessageCard(
    message: ChatMessage,
    showReferences: Boolean,
    canDeleteMessage: Boolean,
    referencesEnabled: Boolean,
    actions: DataSearchActions,
) {
    val user = message.author == MessageAuthor.USER
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
        Card(
            Modifier.widthIn(max = 520.dp).fillMaxWidth(if (user) .84f else .94f).testTag(if (user) "DS002-04" else "DS002-05"),
            colors = CardDefaults.cardColors(containerColor = when {
                message.delivery in setOf(MessageDelivery.INTERRUPTED, MessageDelivery.PARTIAL, MessageDelivery.ERROR) -> MaterialTheme.colorScheme.errorContainer
                user -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }),
            border = if (user) null else CardDefaults.outlinedCardBorder(),
            shape = paneShape,
        ) {
            val assistantEdge = MaterialTheme.colorScheme.secondary
            Column(
                Modifier.drawBehind {
                    if (!user) {
                        val thickness = 4.dp.toPx()
                        drawRect(assistantEdge, Offset(if (layoutDirection == LayoutDirection.Rtl) size.width - thickness else 0f, 0f), Size(thickness, size.height))
                    }
                }.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(if (user) stringResource(R.string.ds_you) else stringResource(R.string.data_search), color = if (user) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
                if (user) Text(message.body, style = MaterialTheme.typography.bodyLarge) else SafeMarkdown(message.body, Modifier.testTag("assistant-markdown-${message.id}"))
                Text(listOfNotNull(message.time, deliveryText(message.delivery)).joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                if (message.delivery in setOf(MessageDelivery.SENDING, MessageDelivery.STREAMING) && !LocalReadOnlyContent.current) {
                    Row(
                        Modifier.testTag("DS002-06").semantics { liveRegion = LiveRegionMode.Polite },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = attentionColor(), strokeWidth = 2.dp)
                        Text(stringResource(if (message.delivery == MessageDelivery.SENDING) R.string.ds_sending else R.string.ds_generating), color = attentionColor())
                    }
                }
                if (!user && showReferences && message.references.isNotEmpty()) {
                    Row(Modifier.testTag("DS002-08"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        message.references.forEach { OutlinedButton(onClick = actions.onOpenSources, enabled = referencesEnabled, shape = CircleShape) { Text(it) } }
                    }
                }
                if (!user || canDeleteMessage) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!user) {
                        TextButton(onClick = { actions.onCopyMessage(message.id) }, shape = actionShape, modifier = Modifier.testTag("copy-message-${message.id}")) { Text(stringResource(R.string.ds_copy)) }
                    }
                    if (canDeleteMessage) {
                        TextButton(onClick = { actions.onRequestDeleteMessage(message.id) }, shape = actionShape, modifier = Modifier.testTag("delete-message-${message.id}")) { Text(stringResource(R.string.ds_delete)) }
                    }
                }
                if (message.delivery == MessageDelivery.INTERRUPTED) {
                    InfoNotice(stringResource(R.string.ds_incomplete_reply), "DS002-07", error = true)
                }
                val recoverableStatus = when (message.delivery) {
                    MessageDelivery.PARTIAL -> stringResource(R.string.ds_partial_message)
                    MessageDelivery.ERROR -> stringResource(R.string.ds_error_message)
                    else -> null
                }
                if (recoverableStatus != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("message-status-${message.id}")
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = recoverableStatus
                            },
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(recoverableStatus, color = MaterialTheme.colorScheme.onErrorContainer)
                            TextButton(
                                onClick = { actions.onRetryMessage(message.id) },
                                enabled = !LocalReadOnlyContent.current,
                                shape = actionShape,
                                modifier = Modifier.testTag("retry-message-${message.id}"),
                            ) { Text(stringResource(R.string.retry)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Composer(state: DataSearchUiState, actions: DataSearchActions) {
    val sendEnabled = state.capabilities.canSendMessages && state.draft.isNotBlank() && !state.working
    val sendLabel = stringResource(R.string.ds_send_message)
    Surface(Modifier.fillMaxWidth(), tonalElevation = 3.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    shape = actionShape,
                    enabled = !state.working,
                    value = state.draft,
                    onValueChange = actions.onDraftChange,
                    label = { Text(stringResource(R.string.ds_message)) },
                    placeholder = { Text(stringResource(R.string.ds_message_hint)) },
                    minLines = 1,
                    maxLines = 6,
                    modifier = Modifier.weight(1f).testTag("DS002-09"),
                )
                Button(
                    shape = actionShape,
                    onClick = actions.onSend,
                    enabled = sendEnabled,
                    modifier = Modifier.size(56.dp).testTag("DS002-10").semantics { contentDescription = sendLabel },
                    contentPadding = ButtonDefaults.ContentPadding,
                ) {
                    Text("↑")
                }
            }
            Text(stringResource(R.string.ds_draft_saved), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ConversationListPane(
    state: DataSearchUiState,
    paneState: DataSearchViewState,
    compact: Boolean,
    actions: DataSearchActions,
) {
    if (paneState !in setOf(DataSearchViewState.CONTENT, DataSearchViewState.PARTIAL, DataSearchViewState.EMPTY)) {
        SupportingPaneStatePanel("conversation-list", paneState)
        return
    }
    val spaceName = state.selectedSpace?.name ?: stringResource(R.string.ds_no_space_selected)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().testTag("DS003-01"), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.ds_conversations), style = MaterialTheme.typography.titleLarge)
                Text(spaceName, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (compact) TextButton(onClick = actions.onBack, shape = actionShape) { Text(stringResource(R.string.ds_close)) }
        }
        Button(
            shape = actionShape,
            onClick = actions.onCreateConversation,
            enabled = state.capabilities.canCreateConversation && !state.working,
            modifier = Modifier.fillMaxWidth().testTag("DS003-02"),
        ) { Text(stringResource(R.string.ds_new_conversation)) }
        LazyColumn(Modifier.weight(1f).testTag("DS003-03"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (paneState != DataSearchViewState.EMPTY) items(state.conversations, key = { "conversation:${it.id}" }) { conversation ->
                ConversationRow(conversation, state.selectedConversation?.id == conversation.id, state, actions)
            }
            if (paneState != DataSearchViewState.EMPTY && state.conversationsPage.hasMore) item(key = "conversation-page-action") {
                TextButton(
                    onClick = actions.onLoadMoreConversations,
                    enabled = !state.conversationsPage.loadingMore && !state.working,
                    shape = actionShape,
                    modifier = Modifier.testTag("DS003-06"),
                ) { Text(stringResource(R.string.ds_load_more)) }
            }
            if (paneState == DataSearchViewState.EMPTY) item(key = "conversation-empty") {
                Card(Modifier.fillMaxWidth().testTag("DS003-07"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.ds_no_conversations), style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(
                            shape = actionShape,
                            onClick = actions.onCreateConversation,
                            enabled = state.capabilities.canCreateConversation && !state.working,
                        ) { Text(stringResource(R.string.ds_start_conversation)) }
                    }
                }
            }
        }
        InfoNotice(stringResource(R.string.ds_scoped_content), "conversation-scope-notice")
    }
}

@Composable
private fun ConversationRow(item: ConversationItem, selected: Boolean, state: DataSearchUiState, actions: DataSearchActions) {
    Surface(
        Modifier.fillMaxWidth().testTag(if (selected) "DS003-04" else "conversation-${item.id}").semantics { this.selected = selected }.clickable(enabled = !state.working) { actions.onSelectConversation(item.id) },
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        shape = paneShape,
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                IdentityAvatar(DsSymbol.CONVERSATION)
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    if (selected) Text(
                        if (state.pinnedCount > 0) stringResource(R.string.ds_selected_count, state.pinnedCount) else stringResource(R.string.ds_all_indexed),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Row(Modifier.testTag("DS003-05")) {
                TextButton(
                    onClick = { actions.onRenameConversation(item.id) },
                    shape = actionShape,
                    enabled = state.capabilities.canRenameConversation,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.ds_rename)) }
                TextButton(
                    onClick = { actions.onRequestDeleteConversation(item.id) },
                    shape = actionShape,
                    enabled = state.capabilities.canDeleteConversation,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.ds_delete)) }
            }
        }
    }
}

@Composable
private fun EditConversationScreen(state: DataSearchUiState, actions: DataSearchActions) {
    val spaceName = state.selectedSpace?.name ?: stringResource(R.string.ds_no_space_selected)
    val renameTarget = state.conversationEditTarget
    val isRename = state.conversationEditMode == ConversationEditMode.RENAME
    val screenTitle = if (isRename) {
        stringResource(
            R.string.ds_rename_conversation_title,
            renameTarget?.title ?: stringResource(R.string.ds_unnamed_conversation),
        )
    } else {
        stringResource(R.string.ds_new_conversation)
    }
    val submitEnabled = state.conversationNameInput.isNotBlank() && !state.working && if (isRename) {
        renameTarget != null && state.capabilities.canRenameConversation
    } else {
        state.capabilities.canCreateConversation
    }
    Column(Modifier.fillMaxSize()) {
        DsHeader(screenTitle, spaceName, "DS004-01", actions.onBack) {
            TextButton(onClick = actions.onBack, shape = actionShape) { Text(stringResource(R.string.ds_close)) }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.ds_conversation_name), Modifier.testTag("DS004-02"), style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(state.conversationNameInput, actions.onConversationNameChange, enabled = !state.working, isError = state.conversationNameInput.isBlank(), shape = actionShape, label = { Text(stringResource(R.string.ds_conversation_name)) }, modifier = Modifier.fillMaxWidth().testTag("DS004-03"))
        if (state.conversationNameInput.isBlank()) {
            Text(stringResource(R.string.ds_conversation_name_required), Modifier.testTag("DS004-08"), color = MaterialTheme.colorScheme.error)
        }
        Text(stringResource(R.string.ds_conversation_space_copy, spaceName), Modifier.testTag("DS004-04"))
        if (state.capabilities.modelCatalogueAvailable) {
            OutlinedButton(onClick = actions.onOpenModelSelector, enabled = !state.working, shape = actionShape, modifier = Modifier.fillMaxWidth().testTag("DS004-05")) { Text(stringResource(R.string.ds_agent_default)) }
        }
        if (state.viewState in setOf(DataSearchViewState.ERROR, DataSearchViewState.PARTIAL)) {
            Box(Modifier.testTag("data-search-state-${state.viewState.name.lowercase()}")) { InfoNotice(stringResource(R.string.ds_title_preserved), "DS004-08", error = true) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            OutlinedButton(onClick = actions.onCancel, shape = actionShape, modifier = Modifier.testTag("DS004-06")) { Text(stringResource(R.string.ds_cancel)) }
            Button(
                shape = actionShape,
                onClick = {
                    if (isRename) {
                        renameTarget?.id?.let { actions.onRenameConversationSubmit(it, state.conversationNameInput) }
                    } else {
                        actions.onCreateConversationSubmit(state.conversationNameInput)
                    }
                },
                enabled = submitEnabled,
                modifier = Modifier.testTag("DS004-07"),
            ) { Text(if (isRename) stringResource(R.string.ds_save_changes) else stringResource(R.string.ds_create)) }
        }
        }
    }
}

@Composable
private fun DeleteConversationDialog(state: DataSearchUiState, actions: DataSearchActions) {
    val target = state.pendingConversationDelete
    val title = target?.displayName ?: stringResource(R.string.ds_unnamed_conversation)
    DestructiveDialog(
        title = stringResource(R.string.ds_delete_conversation_title, title),
        body = stringResource(R.string.ds_delete_conversation_body),
        pending = stringResource(R.string.ds_delete_pending),
        panelTag = "DS005-01", iconTag = "DS005-02", titleTag = "DS005-03", bodyTag = "DS005-04",
        pendingTag = "DS005-05", cancelTag = "DS005-06", deleteTag = "DS005-07",
        deleteLabel = stringResource(R.string.ds_delete_conversation),
        onCancel = actions.onCancel,
        onDelete = { target?.id?.let(actions.onConfirmDeleteConversation) },
        deleteEnabled = target != null && state.capabilities.canDeleteConversation && !state.working,
    )
}

@Composable
private fun SourcesScreen(state: DataSearchUiState, actions: DataSearchActions) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp && LocalDensity.current.fontScale < 1.3f
        val sourcesState = state.sourcesPaneState ?: state.viewState
        if (expanded) Row(Modifier.fillMaxSize().testTag("DS006-11")) {
            Box(Modifier.weight(.25f).widthIn(min = 200.dp).fillMaxHeight().testTag("data-search-start-pane")) {
                ConversationListPane(
                    state,
                    paneState = state.conversationListPaneState ?: inferredPaneState(state.conversations.isNotEmpty()),
                    compact = false,
                    actions,
                )
            }
            VerticalDivider(Modifier.fillMaxHeight().width(1.dp))
            Box(Modifier.weight(.5f).widthIn(min = 360.dp).testTag("data-search-center-pane")) {
                ConversationCenter(
                    state,
                    paneState = state.conversationPaneState ?: inferredPaneState(state.messages.isNotEmpty()),
                    actions,
                )
            }
            VerticalDivider(Modifier.fillMaxHeight().width(1.dp))
            Box(Modifier.weight(.25f).widthIn(min = 200.dp).testTag("data-search-end-pane")) { SourcesPane(state, paneState = sourcesState, supporting = true, actions) }
        } else SourcesPane(state, paneState = sourcesState, supporting = false, actions)
    }
}

@Composable
private fun SourcesPane(
    state: DataSearchUiState,
    paneState: DataSearchViewState,
    supporting: Boolean,
    actions: DataSearchActions,
) {
    if (paneState !in setOf(DataSearchViewState.CONTENT, DataSearchViewState.PARTIAL, DataSearchViewState.EMPTY)) {
        SupportingPaneStatePanel("sources", paneState)
        return
    }
    val spaceName = state.selectedSpace?.name ?: stringResource(R.string.ds_no_space_selected)
    Column(Modifier.fillMaxSize().padding(if (supporting) 12.dp else 0.dp)) {
        if (supporting) {
            Surface(Modifier.fillMaxWidth().testTag("DS006-01"), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(spaceName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.ds_sources), Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
                    Button(
                        onClick = actions.onAddSource,
                        shape = actionShape,
                        enabled = state.capabilities.canAddSource,
                        modifier = Modifier.fillMaxWidth().testTag("DS006-02"),
                    ) { Text(stringResource(R.string.ds_add)) }
                }
            }
        } else DsHeader(stringResource(R.string.ds_sources), spaceName, "DS006-01", actions.onBack) {
            Button(onClick = actions.onAddSource, enabled = state.capabilities.canAddSource, shape = actionShape, modifier = Modifier.testTag("DS006-02")) { Text(stringResource(R.string.ds_add)) }
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.ds_source_explanation), Modifier.testTag("DS006-03"))
            Column(Modifier.testTag("DS006-04"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val filters = listOf(
                    listOf(SourceFilter.ALL to R.string.ds_all, SourceFilter.INDEXED to R.string.ds_indexed),
                    listOf(SourceFilter.NOT_INDEXED to R.string.ds_not_indexed, SourceFilter.FAILED to R.string.ds_failed),
                )
                if (supporting) {
                    filters.flatten().forEach { (filter, label) ->
                        FilterChip(
                            enabled = !state.working,
                            selected = state.sourceFilter == filter,
                            onClick = { actions.onSourceFilterChange(filter) },
                            label = { Text(stringResource(label)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else filters.forEach { labels ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        labels.forEach { (filter, label) ->
                            FilterChip(selected = state.sourceFilter == filter, enabled = !state.working, onClick = { actions.onSourceFilterChange(filter) }, label = { Text(stringResource(label)) })
                        }
                    }
                }
            }
        }
        LazyColumn(Modifier.weight(1f).testTag("DS006-05").padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (paneState != DataSearchViewState.EMPTY) items(state.sources, key = { "source:${it.id}" }) { SourceRow(it, state, actions, narrow = supporting) }
            if (paneState == DataSearchViewState.EMPTY) item(key = "source-empty") {
                Card(Modifier.fillMaxWidth().testTag("DS006-09"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.ds_no_sources), style = MaterialTheme.typography.titleMedium)
                        if (state.sourceFilter != SourceFilter.ALL) {
                            TextButton(onClick = { actions.onSourceFilterChange(SourceFilter.ALL) }, shape = actionShape, modifier = Modifier.testTag("source-restore-all")) { Text(stringResource(R.string.ds_show_all_sources)) }
                        }
                        Button(onClick = actions.onAddSource, enabled = state.capabilities.canAddSource, shape = actionShape) { Text(stringResource(R.string.ds_add)) }
                    }
                }
            }
            if (!state.sourcesPage.complete || state.sourcesPage.hasMore) item(key = "source-page-status") {
                InfoNotice(stringResource(R.string.ds_sources_page_incomplete), "source-page-status")
            }
            if (state.sourcesPage.loadingMore) item(key = "source-page-loading") {
                Row(
                    Modifier.fillMaxWidth().testTag("source-page-loading").semantics { liveRegion = LiveRegionMode.Polite },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(stringResource(R.string.ds_sources_page_loading))
                }
            }
            if (state.sourcesPage.hasMore) item(key = "source-page-action") {
                TextButton(
                    onClick = actions.onLoadMoreSources,
                    shape = actionShape,
                    enabled = !state.sourcesPage.loadingMore && !state.working,
                    modifier = Modifier.fillMaxWidth().testTag("source-load-more"),
                ) { Text(stringResource(R.string.ds_load_more)) }
            }
        }
    }
}

@Composable
private fun SourceRow(source: SourceItem, state: DataSearchUiState, actions: DataSearchActions, narrow: Boolean) {
    Card(Modifier.fillMaxWidth().testTag("DS006-06"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                IdentityAvatar(DsSymbol.SOURCE)
                Text(source.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            }
            source.type?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            SourceStatus(stringResource(R.string.ds_file_safety_value, safetyText(source.safety)), safetyColor(source.safety))
            SourceStatus(stringResource(R.string.ds_answer_index_value, indexText(source.index)), indexColor(source.index))
            if (source.index == AnswerIndex.NOT_STARTED || source.index == AnswerIndex.FAILED) {
                OutlinedButton(
                    onClick = { actions.onIndexSource(source.id) },
                    shape = actionShape,
                    enabled = source.canRequestIndex(state.capabilities),
                    modifier = Modifier.testTag("DS006-07"),
                ) { Text(if (source.index == AnswerIndex.FAILED) stringResource(R.string.retry) else stringResource(R.string.ds_index)) }
            }
            if (narrow) Column(Modifier.fillMaxWidth().testTag("DS006-08")) {
                TextButton(onClick = { actions.onSelectSource(source.id) }, enabled = !state.working, shape = actionShape, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.ds_details)) }
                TextButton(onClick = { actions.onRenameSource(source.id) }, enabled = state.capabilities.canRenameSource, shape = actionShape, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.ds_rename)) }
                TextButton(onClick = { actions.onRequestDeleteSource(source.id) }, enabled = state.capabilities.canDeleteSource, shape = actionShape, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.ds_delete)) }
            } else Row(Modifier.testTag("DS006-08")) {
                TextButton(onClick = { actions.onSelectSource(source.id) }, enabled = !state.working, shape = actionShape) { Text(stringResource(R.string.ds_details)) }
                TextButton(onClick = { actions.onRenameSource(source.id) }, enabled = state.capabilities.canRenameSource, shape = actionShape) { Text(stringResource(R.string.ds_rename)) }
                TextButton(onClick = { actions.onRequestDeleteSource(source.id) }, enabled = state.capabilities.canDeleteSource, shape = actionShape) { Text(stringResource(R.string.ds_delete)) }
            }
        }
    }
}

@Composable
private fun AddSourceScreen(state: DataSearchUiState, actions: DataSearchActions) {
    val spaceName = state.selectedSpace?.name ?: stringResource(R.string.ds_no_space_selected)
    val canPrepareSource = !state.working && state.uploadPhase == UploadPhase.IDLE
    val validTextSource = state.sourceFileNameInput.isNotBlank() && state.sourceTextInput.isNotBlank()
    Column(Modifier.fillMaxSize()) {
        DsHeader(stringResource(R.string.ds_add_source), spaceName, "DS007-01", actions.onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlowRow(Modifier.fillMaxWidth().testTag("DS007-02"), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { actions.onAddSourceModeChange(AddSourceMode.FILE) },
                        enabled = state.capabilities.canAddSource && canPrepareSource,
                        modifier = Modifier.testTag("add-file-source-action"),
                        shape = actionShape,
                    ) {
                        DsIcon(DsSymbol.SOURCE, MaterialTheme.colorScheme.onPrimary)
                        Text(stringResource(R.string.ds_choose_file), Modifier.padding(start = 8.dp))
                    }
                    if (state.capabilities.canAddTextSource) {
                        OutlinedButton(
                            onClick = { actions.onAddSourceModeChange(AddSourceMode.TEXT) },
                            enabled = canPrepareSource,
                            modifier = Modifier.testTag("add-text-source-action"),
                            shape = actionShape,
                        ) { Text(stringResource(R.string.ds_add_text)) }
                    }
            }
            if (state.addSourceMode == AddSourceMode.TEXT && state.capabilities.canAddTextSource) Column(Modifier.testTag("DS007-03"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(state.sourceFileNameInput, actions.onSourceFileNameChange, enabled = canPrepareSource, shape = actionShape, label = { Text(stringResource(R.string.ds_file_name)) }, modifier = Modifier.fillMaxWidth().testTag("text-source-file-name"))
                OutlinedTextField(state.sourceTextInput, actions.onSourceTextChange, enabled = canPrepareSource, shape = actionShape, label = { Text(stringResource(R.string.ds_content)) }, minLines = 4, modifier = Modifier.fillMaxWidth().testTag("text-source-content"))
                if (state.uploadPhase == UploadPhase.IDLE) {
                    Text(stringResource(R.string.ds_text_source_required), Modifier.testTag("text-source-validation"), style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            actions.onSubmitTextSource(TextSourceSubmission(state.sourceFileNameInput.trim(), state.sourceTextInput))
                        },
                        enabled = canPrepareSource && validTextSource,
                        shape = actionShape,
                        modifier = Modifier.fillMaxWidth().testTag("text-source-submit"),
                    ) { Text(stringResource(R.string.ds_submit_text_source)) }
                }
            }
            val phase = state.uploadPhase
            val phaseText = uploadPhaseText(phase)
            val hasActiveOperation = state.addSourceMode != AddSourceMode.CHOICE && phase != UploadPhase.IDLE
            if (hasActiveOperation) {
                val stages = listOf(R.string.ds_stage_prepare, R.string.ds_stage_register, R.string.ds_stage_upload, R.string.ds_stage_complete, R.string.ds_stage_scan, R.string.ds_stage_index)
                Column(Modifier.testTag("DS007-04"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stages.forEachIndexed { index, label ->
                        val completed = phase.progressRank > index || phase.successful && phase.progressRank >= index
                        val active = !phase.terminal && phase.progressRank == index
                        val foreground = when {
                            completed -> MaterialTheme.colorScheme.secondary
                            active -> attentionColor()
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            Modifier.fillMaxWidth(),
                            color = when {
                                completed -> MaterialTheme.colorScheme.secondaryContainer
                                active -> attentionContainer()
                                else -> MaterialTheme.colorScheme.surface
                            },
                            contentColor = foreground,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = actionShape,
                        ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (active) {
                                CircularProgressIndicator(
                                    Modifier.size(20.dp).testTag("DS007-05").semantics {
                                        contentDescription = phaseText
                                    },
                                    strokeWidth = 2.dp,
                                    color = foreground,
                                )
                            } else {
                                Text(if (completed) "✓" else "${index + 1}")
                            }
                            Text(stringResource(label))
                        }
                        }
                    }
                }
                InfoNotice(phaseText, "DS007-06", error = phase.terminal && !phase.successful)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    if (phase.cancellable) {
                        OutlinedButton(onClick = actions.onCancel, shape = actionShape, modifier = Modifier.testTag("DS007-07")) { Text(stringResource(R.string.ds_cancel)) }
                    }
                    Button(
                        onClick = actions.onDone,
                        enabled = phase.terminal && phase.successful && !state.working,
                        shape = actionShape,
                        modifier = Modifier.testTag("DS007-08"),
                    ) { Text(stringResource(R.string.ds_done)) }
                }
            }
            InfoNotice(stringResource(R.string.ds_upload_answer_readiness), "upload-answer-readiness", attention = true)
        }
    }
}

@Composable
private fun SourceDetailsScreen(state: DataSearchUiState, actions: DataSearchActions) {
    val source = state.sources.firstOrNull { it.id == state.selectedSourceId }
    if (source == null) {
        DataSearchStatePanel(DataSearchDestination.SOURCE_DETAILS, DataSearchViewState.EMPTY, actions)
        return
    }
    val spaceName = state.selectedSpace?.name ?: stringResource(R.string.ds_no_space_selected)
    val scopeActionLabel = if (source.pinned) stringResource(R.string.ds_remove_scope) else stringResource(R.string.ds_include_scope)
    val scopeActionEnabled = state.capabilities.canPinSources && (source.pinned || source.safety == FileSafety.READY && source.index == AnswerIndex.INDEXED)
    Column(Modifier.fillMaxSize()) {
        DsHeader(stringResource(R.string.ds_source_details), spaceName, "DS008-01", actions.onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.testTag("DS008-02")) {
                Text(source.name, style = MaterialTheme.typography.headlineMedium)
                source.type?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            StatusPanel(stringResource(R.string.ds_file_safety), safetyText(source.safety), stringResource(if (source.safety == FileSafety.READY) R.string.ds_safety_ready_body else R.string.ds_safety_unavailable_body), "DS008-03", safetyColor(source.safety))
            StatusPanel(stringResource(R.string.ds_answer_index), indexText(source.index), stringResource(if (source.index == AnswerIndex.INDEXED) R.string.ds_index_ready_body else R.string.ds_index_unavailable_body), "DS008-04", indexColor(source.index))
            Text(stringResource(if (source.safety == FileSafety.READY && source.index == AnswerIndex.INDEXED) R.string.ds_rag_impact else R.string.ds_rag_unavailable), Modifier.testTag("DS008-05"), style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(
                onClick = { actions.onToggleSourcePin(source.id, !source.pinned) },
                shape = actionShape,
                enabled = scopeActionEnabled,
                modifier = Modifier.fillMaxWidth().testTag("DS008-06").semantics { contentDescription = scopeActionLabel },
            ) { Text(scopeActionLabel) }
            Row(Modifier.fillMaxWidth().testTag("DS008-07"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (source.index == AnswerIndex.NOT_STARTED || source.index == AnswerIndex.FAILED) {
                    OutlinedButton(
                        onClick = { actions.onIndexSource(source.id) },
                        shape = actionShape,
                        enabled = source.canRequestIndex(state.capabilities),
                        modifier = Modifier.testTag("source-index-action"),
                    ) {
                        Text(if (source.index == AnswerIndex.FAILED) stringResource(R.string.retry) else stringResource(R.string.ds_index))
                    }
                }
                OutlinedButton(onClick = { actions.onRenameSource(source.id) }, enabled = state.capabilities.canRenameSource, shape = actionShape) { Text(stringResource(R.string.ds_rename)) }
            }
            Button(
                onClick = { actions.onRequestDeleteSource(source.id) },
                enabled = state.capabilities.canDeleteSource,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                shape = actionShape,
                modifier = Modifier.fillMaxWidth().testTag("DS008-08"),
            ) { Text(stringResource(R.string.ds_delete_source)) }
        }
    }
}

@Composable
private fun AnswerScopeScreen(state: DataSearchUiState, actions: DataSearchActions) {
    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().testTag("DS009-01"), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.ds_answer_scope), Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = actions.onBack, shape = actionShape) { Text(stringResource(R.string.ds_close)) }
        }
        ScopeOption(
            selected = state.answerScope == AnswerScopeMode.ALL_INDEXED,
            title = stringResource(R.string.ds_all_indexed),
            body = stringResource(R.string.ds_all_indexed_body),
            tag = "DS009-02",
            onClick = { actions.onAnswerScopeChange(AnswerScopeMode.ALL_INDEXED) },
        )
        ScopeOption(
            selected = state.answerScope == AnswerScopeMode.SELECTED_SOURCES,
            title = stringResource(R.string.ds_selected_only),
            body = stringResource(R.string.ds_selected_only_body),
            tag = "DS009-03",
            onClick = { actions.onAnswerScopeChange(AnswerScopeMode.SELECTED_SOURCES) },
        )
        Column(Modifier.testTag("DS009-04"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.sources.forEach { source ->
                Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = source.pinned,
                        onCheckedChange = { actions.onToggleSourcePin(source.id, it) },
                        enabled = state.capabilities.canPinSources && (source.pinned || source.safety == FileSafety.READY && source.index == AnswerIndex.INDEXED),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(source.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(if (source.index == AnswerIndex.INDEXED) R.string.ds_index_ready_body else R.string.ds_index_unavailable_body),
                            if (source.index != AnswerIndex.INDEXED) Modifier.testTag("DS009-05") else Modifier,
                            color = indexColor(source.index), style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                }
            }
        }
        if (state.viewState in setOf(DataSearchViewState.ERROR, DataSearchViewState.PARTIAL)) {
            Box(Modifier.testTag("data-search-state-${state.viewState.name.lowercase()}")) {
                Box(Modifier.testTag("DS009-09")) { InfoNotice(stringResource(R.string.ds_pin_failure), "DS009-06", error = true) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            TextButton(onClick = actions.onUseAllSources, enabled = state.capabilities.canPinSources, shape = actionShape, modifier = Modifier.testTag("DS009-07")) { Text(stringResource(R.string.ds_use_all)) }
            Button(onClick = actions.onDone, shape = actionShape, modifier = Modifier.testTag("DS009-08")) { Text(stringResource(R.string.ds_done)) }
        }
    }
}

@Composable
private fun DeleteSourceDialog(state: DataSearchUiState, actions: DataSearchActions) {
    val target = state.pendingSourceDelete
    val name = target?.displayName ?: stringResource(R.string.ds_unknown_source)
    DestructiveDialog(
        title = stringResource(R.string.ds_delete_source_title, name),
        body = stringResource(R.string.ds_delete_source_body),
        pending = stringResource(R.string.ds_source_delete_pending),
        panelTag = "DS010-01", iconTag = "DS010-02", titleTag = "DS010-03", bodyTag = "DS010-04",
        pendingTag = "DS010-06", cancelTag = "DS010-07", deleteTag = "DS010-08",
        deleteLabel = stringResource(R.string.ds_delete_source),
        onCancel = actions.onCancel,
        onDelete = { target?.id?.let(actions.onConfirmDeleteSource) },
        deleteEnabled = target != null && state.capabilities.canDeleteSource && !state.working,
    )
}

@Composable
private fun DestructiveDialog(
    title: String,
    body: String,
    pending: String,
    panelTag: String,
    iconTag: String,
    titleTag: String,
    bodyTag: String,
    pendingTag: String,
    cancelTag: String,
    deleteTag: String,
    deleteLabel: String,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    deleteEnabled: Boolean,
) {
    val cancelFocus = remember { FocusRequester() }
    val deleteFocus = remember { FocusRequester() }
    val inputMode = LocalInputModeManager.current.inputMode
    // Material buttons do not accept keyboard focus in touch mode. Re-entering
    // keyboard navigation must always start at the safe modal action.
    LaunchedEffect(inputMode) {
        if (inputMode == InputMode.Keyboard) cancelFocus.requestFocus()
    }
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .42f)).testTag(panelTag)
            .pointerInput(Unit) { detectTapGestures(onTap = { }) },
        contentAlignment = Alignment.Center,
    ) {
        Card(Modifier.padding(24.dp).widthIn(max = 520.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.errorContainer, actionShape).testTag(iconTag), contentAlignment = Alignment.Center) { DsIcon(DsSymbol.DELETE, MaterialTheme.colorScheme.error) }
                Text(title, Modifier.testTag(titleTag), style = MaterialTheme.typography.headlineSmall)
                Text(body, Modifier.testTag(bodyTag), style = MaterialTheme.typography.bodyLarge)
                InfoNotice(pending, pendingTag, error = true)
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag(cancelTag).focusRequester(cancelFocus).focusProperties {
                            next = if (deleteEnabled) deleteFocus else cancelFocus
                            previous = if (deleteEnabled) deleteFocus else cancelFocus
                        },
                        shape = actionShape,
                    ) { Text(stringResource(R.string.ds_cancel)) }
                    Button(
                        onClick = onDelete,
                        enabled = deleteEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                        shape = actionShape,
                        modifier = Modifier.testTag(deleteTag).focusRequester(deleteFocus).focusProperties { next = cancelFocus; previous = cancelFocus },
                    ) { Text(deleteLabel) }
                }
            }
        }
    }
}

@Composable
private fun ScopeOption(selected: Boolean, title: String, body: String, tag: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().testTag(tag).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected, onClick = null)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusPanel(title: String, value: String, explanation: String, tag: String, statusColor: Color) {
    Card(Modifier.fillMaxWidth().testTag(tag), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("$title: $value", style = MaterialTheme.typography.titleMedium, color = statusColor)
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoNotice(text: String, tag: String, modifier: Modifier = Modifier, error: Boolean = false, attention: Boolean = false) {
    Surface(
        modifier.fillMaxWidth().testTag(tag),
        color = when { error -> MaterialTheme.colorScheme.errorContainer; attention -> attentionContainer(); else -> MaterialTheme.colorScheme.surfaceVariant },
        shape = RoundedCornerShape(12.dp),
    ) { Text(text, Modifier.padding(12.dp), color = when { error -> MaterialTheme.colorScheme.onErrorContainer; attention -> attentionColor(); else -> MaterialTheme.colorScheme.onSurfaceVariant }) }
}

@Composable
private fun SourceStatus(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Text(text, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

private enum class DsSymbol { SPACE, SOURCE, CONVERSATION, DELETE, RAG }

@Composable
private fun IdentityAvatar(symbol: DsSymbol) {
    Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, actionShape), contentAlignment = Alignment.Center) {
        DsIcon(symbol, MaterialTheme.colorScheme.secondary)
    }
}

/** Small deterministic vector marks; no font-dependent search or warning glyph substitution. */
@Composable
private fun DsIcon(symbol: DsSymbol, tint: Color) {
    Canvas(Modifier.size(22.dp)) {
        val unit = size.width / 24f
        val line = 1.8f * unit
        fun segment(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(tint, Offset(x1 * unit, y1 * unit), Offset(x2 * unit, y2 * unit), line)
        when (symbol) {
            DsSymbol.RAG -> {
                drawCircle(tint, 8 * unit, style = Stroke(line))
                drawCircle(tint, 3 * unit)
            }
            DsSymbol.DELETE -> {
                drawRect(tint, Offset(6 * unit, 7 * unit), Size(12 * unit, 14 * unit), style = Stroke(line))
                segment(3f, 5f, 21f, 5f); segment(9f, 2f, 15f, 2f)
                segment(10f, 10f, 10f, 18f); segment(14f, 10f, 14f, 18f)
            }
            DsSymbol.SOURCE -> {
                drawRect(tint, Offset(5 * unit, 3 * unit), Size(14 * unit, 18 * unit), style = Stroke(line))
                segment(8f, 8f, 16f, 8f); segment(8f, 12f, 16f, 12f); segment(8f, 16f, 14f, 16f)
            }
            DsSymbol.SPACE -> {
                listOf(Offset(3f, 3f), Offset(14f, 3f), Offset(3f, 14f), Offset(14f, 14f)).forEach {
                    drawRect(tint, Offset(it.x * unit, it.y * unit), Size(7 * unit, 7 * unit), style = Stroke(line))
                }
            }
            DsSymbol.CONVERSATION -> {
                drawCircle(tint, 8 * unit, Offset(12 * unit, 11 * unit), style = Stroke(line))
                segment(5f, 16f, 3f, 21f); segment(3f, 21f, 9f, 19f)
                segment(8f, 10f, 16f, 10f); segment(8f, 13f, 14f, 13f)
            }
        }
    }
}

@Composable private fun safetyColor(safety: FileSafety): Color = when (safety) {
    FileSafety.READY -> MaterialTheme.colorScheme.secondary
    FileSafety.SCANNING, FileSafety.UPLOADED -> attentionColor()
    FileSafety.QUARANTINED -> MaterialTheme.colorScheme.error
}

@Composable private fun indexColor(index: AnswerIndex): Color = when (index) {
    AnswerIndex.INDEXED -> MaterialTheme.colorScheme.secondary
    AnswerIndex.NOT_STARTED, AnswerIndex.PENDING, AnswerIndex.INDEXING -> attentionColor()
    AnswerIndex.FAILED -> MaterialTheme.colorScheme.error
}

@Composable
private fun HeaderIconAction(glyph: String, label: String, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(
        enabled = enabled,
        shape = actionShape,
        onClick = onClick,
        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = label },
    ) { Text(glyph, style = MaterialTheme.typography.titleLarge) }
}

@Composable private fun deliveryText(delivery: MessageDelivery): String = when (delivery) {
    MessageDelivery.SENDING -> stringResource(R.string.ds_sending)
    MessageDelivery.COMPLETE -> stringResource(R.string.ds_sent)
    MessageDelivery.STREAMING -> stringResource(R.string.ds_streaming)
    MessageDelivery.INTERRUPTED -> stringResource(R.string.ds_interrupted)
    MessageDelivery.PARTIAL -> stringResource(R.string.ds_partial_delivery)
    MessageDelivery.ERROR -> stringResource(R.string.ds_error_delivery)
}

@Composable private fun safetyText(safety: FileSafety): String = when (safety) {
    FileSafety.UPLOADED -> stringResource(R.string.ds_uploaded)
    FileSafety.READY -> stringResource(R.string.ds_ready)
    FileSafety.SCANNING -> stringResource(R.string.ds_scanning)
    FileSafety.QUARANTINED -> stringResource(R.string.ds_quarantined)
}

@Composable private fun indexText(index: AnswerIndex): String = when (index) {
    AnswerIndex.NOT_STARTED -> stringResource(R.string.ds_not_started)
    AnswerIndex.PENDING -> stringResource(R.string.ds_index_pending)
    AnswerIndex.INDEXING -> stringResource(R.string.ds_indexing)
    AnswerIndex.INDEXED -> stringResource(R.string.ds_indexed)
    AnswerIndex.FAILED -> stringResource(R.string.ds_failed)
}

@Composable
private fun uploadPhaseText(phase: UploadPhase): String = when (phase) {
    UploadPhase.IDLE -> stringResource(R.string.ds_upload_phase_idle)
    UploadPhase.LOCAL_PREPARATION -> stringResource(R.string.ds_upload_phase_preparing)
    UploadPhase.REGISTRATION -> stringResource(R.string.ds_upload_phase_registering)
    UploadPhase.BYTE_UPLOAD -> stringResource(R.string.ds_upload_phase_uploading)
    UploadPhase.UPLOAD_COMPLETE -> stringResource(R.string.ds_upload_phase_uploaded)
    UploadPhase.SAFETY_SCAN -> stringResource(R.string.ds_upload_phase_scanning)
    UploadPhase.READY_NOT_INDEXED -> stringResource(R.string.ds_upload_phase_ready_not_indexed)
    UploadPhase.INDEXING -> stringResource(R.string.ds_upload_phase_indexing)
    UploadPhase.SEARCHABLE -> stringResource(R.string.ds_upload_phase_searchable)
    UploadPhase.QUARANTINED -> stringResource(R.string.ds_upload_phase_quarantined)
    UploadPhase.FAILED -> stringResource(R.string.ds_upload_phase_failed)
    UploadPhase.EXPIRED -> stringResource(R.string.ds_upload_phase_expired)
    UploadPhase.RECOVERY_REQUIRED -> stringResource(R.string.ds_upload_phase_recovery)
}

@Composable
private fun SupportingPaneStatePanel(pane: String, paneState: DataSearchViewState) {
    val title = when (paneState) {
        DataSearchViewState.LOADING -> stringResource(R.string.ds_state_loading)
        DataSearchViewState.ERROR -> stringResource(R.string.ds_state_error)
        DataSearchViewState.PERMISSION_DENIED -> stringResource(R.string.ds_access_denied_title)
        DataSearchViewState.OFFLINE -> stringResource(R.string.ds_offline_title)
        DataSearchViewState.SESSION_EXPIRED -> stringResource(R.string.ds_session_expired_title)
        DataSearchViewState.CONTENT, DataSearchViewState.PARTIAL, DataSearchViewState.EMPTY -> ""
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("data-search-pane-state-$pane-${paneState.name.lowercase()}"),
        contentAlignment = Alignment.Center,
    ) {
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = stateContainer(paneState)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (paneState == DataSearchViewState.LOADING) CircularProgressIndicator(Modifier.size(28.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = stateContentColor(paneState))
                Text(stateDescription(paneState), color = stateContentColor(paneState))
            }
        }
    }
}

@Composable
private fun DataSearchStatePanel(destination: DataSearchDestination, viewState: DataSearchViewState, actions: DataSearchActions) {
    val noticeTag = inventoryStateTag(destination, viewState)
    val title = when (viewState) {
        DataSearchViewState.LOADING -> stringResource(R.string.ds_state_loading)
        DataSearchViewState.EMPTY -> emptyTitle(destination)
        DataSearchViewState.ERROR -> stringResource(R.string.ds_state_error)
        DataSearchViewState.PERMISSION_DENIED -> stringResource(R.string.ds_access_denied_title)
        DataSearchViewState.OFFLINE -> stringResource(R.string.ds_offline_title)
        DataSearchViewState.SESSION_EXPIRED -> stringResource(R.string.ds_session_expired_title)
        DataSearchViewState.CONTENT, DataSearchViewState.PARTIAL -> stringResource(R.string.ds_partial_data)
    }
    Column(
        Modifier.fillMaxSize().padding(24.dp).testTag("data-search-state-${viewState.name.lowercase()}"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(Modifier.fillMaxWidth().widthIn(max = 520.dp).testTag(noticeTag), colors = CardDefaults.cardColors(containerColor = stateContainer(viewState)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = paneShape) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (viewState == DataSearchViewState.LOADING) CircularProgressIndicator()
                Text(title, style = MaterialTheme.typography.headlineSmall, color = stateContentColor(viewState))
                Text(stateDescription(viewState), Modifier.testTag(secondaryInventoryStateTag(destination, viewState)), color = stateContentColor(viewState))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = actions.onBack, shape = actionShape, modifier = Modifier.testTag("data-search-state-back")) { Text(stringResource(R.string.ds_back)) }
                    if (viewState in setOf(DataSearchViewState.ERROR, DataSearchViewState.OFFLINE, DataSearchViewState.EMPTY)) {
                        Button(onClick = actions.onRetry, shape = actionShape, modifier = Modifier.testTag("data-search-state-retry")) { Text(stringResource(R.string.ds_refresh)) }
                    }
                    if (destination == DataSearchDestination.SPACES && viewState == DataSearchViewState.EMPTY) {
                        TextButton(onClick = actions.onReturnHome, shape = actionShape) { Text(stringResource(R.string.ds_return_home)) }
                    }
                }
            }
        }
    }
}

@Composable private fun stateContainer(state: DataSearchViewState): Color = when (state) {
    DataSearchViewState.OFFLINE -> attentionContainer()
    DataSearchViewState.ERROR, DataSearchViewState.PERMISSION_DENIED, DataSearchViewState.SESSION_EXPIRED -> MaterialTheme.colorScheme.errorContainer
    else -> MaterialTheme.colorScheme.surface
}

@Composable private fun stateContentColor(state: DataSearchViewState): Color = when (state) {
    DataSearchViewState.OFFLINE -> attentionColor()
    DataSearchViewState.ERROR, DataSearchViewState.PERMISSION_DENIED, DataSearchViewState.SESSION_EXPIRED -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

private fun inventoryNoticeTag(destination: DataSearchDestination): String = when (destination) {
    DataSearchDestination.SPACES -> "DS001-08"
    DataSearchDestination.CONVERSATION -> "DS002-12"
    DataSearchDestination.CONVERSATIONS -> "DS003-08"
    DataSearchDestination.EDIT_CONVERSATION -> "DS004-08"
    DataSearchDestination.DELETE_CONVERSATION -> "DS005-05"
    DataSearchDestination.SOURCES -> "DS006-10"
    DataSearchDestination.ADD_SOURCE -> "DS007-09"
    DataSearchDestination.SOURCE_DETAILS -> "DS008-09"
    DataSearchDestination.ANSWER_SCOPE -> "DS009-09"
    DataSearchDestination.DELETE_SOURCE -> "DS010-06"
}

private fun inventoryStateTag(destination: DataSearchDestination, viewState: DataSearchViewState): String =
    if (viewState == DataSearchViewState.EMPTY) when (destination) {
        DataSearchDestination.SPACES -> "DS001-07"
        DataSearchDestination.CONVERSATION -> "DS002-11"
        DataSearchDestination.CONVERSATIONS -> "DS003-07"
        DataSearchDestination.SOURCES -> "DS006-09"
        else -> inventoryNoticeTag(destination)
    } else inventoryNoticeTag(destination)

private fun secondaryInventoryStateTag(destination: DataSearchDestination, viewState: DataSearchViewState): String = when {
    destination == DataSearchDestination.ADD_SOURCE && viewState in setOf(DataSearchViewState.PARTIAL, DataSearchViewState.ERROR) -> "DS007-06"
    destination == DataSearchDestination.ANSWER_SCOPE && viewState == DataSearchViewState.ERROR -> "DS009-06"
    else -> "data-search-state-description"
}

@Composable
private fun emptyTitle(destination: DataSearchDestination): String = when (destination) {
    DataSearchDestination.SPACES -> stringResource(R.string.ds_no_spaces)
    DataSearchDestination.CONVERSATION -> stringResource(R.string.ds_empty_chat)
    DataSearchDestination.CONVERSATIONS -> stringResource(R.string.ds_no_conversations)
    DataSearchDestination.SOURCES, DataSearchDestination.SOURCE_DETAILS, DataSearchDestination.DELETE_SOURCE -> stringResource(R.string.ds_no_sources)
    else -> stringResource(R.string.ds_state_empty)
}

@Composable
private fun stateDescription(viewState: DataSearchViewState): String = when (viewState) {
    DataSearchViewState.LOADING -> stringResource(R.string.ds_state_loading_body)
    DataSearchViewState.EMPTY -> stringResource(R.string.ds_state_empty_body)
    DataSearchViewState.ERROR -> stringResource(R.string.ds_state_error_body)
    DataSearchViewState.PERMISSION_DENIED -> stringResource(R.string.ds_permission_denied)
    DataSearchViewState.OFFLINE -> stringResource(R.string.ds_offline)
    DataSearchViewState.SESSION_EXPIRED -> stringResource(R.string.ds_session_expired)
    DataSearchViewState.CONTENT -> ""
    DataSearchViewState.PARTIAL -> stringResource(R.string.ds_partial_data)
}
