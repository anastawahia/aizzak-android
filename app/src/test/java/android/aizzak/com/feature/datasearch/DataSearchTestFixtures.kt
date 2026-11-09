package android.aizzak.com.feature.datasearch

internal fun dataSearchTestState(viewState: DataSearchViewState = DataSearchViewState.CONTENT): DataSearchUiState {
    val selectedSpace = SpaceItem("product", "Product knowledge", 8, 3)
    val selectedConversation = ConversationItem("policy", "Policy updates")
    return DataSearchUiState(
        viewState = viewState,
        selectedSpace = selectedSpace,
        selectedConversation = selectedConversation,
        selectedSourceId = "policy",
        pendingConversationDelete = PendingDeleteTarget("policy", "Policy updates"),
        pendingSourceDelete = PendingDeleteTarget("policy", "policy.pdf"),
        spaces = listOf(selectedSpace, SpaceItem("operations", "Operations", 2, 1)),
        conversations = listOf(selectedConversation, ConversationItem("onboarding", "Onboarding questions")),
        sources = listOf(
            SourceItem("policy", "policy.pdf", "PDF", FileSafety.READY, AnswerIndex.INDEXED, pinned = true),
            SourceItem("guide", "guide.docx", "DOCX", FileSafety.READY, AnswerIndex.INDEXED, pinned = true),
            SourceItem("notes", "notes.txt", "TXT", FileSafety.READY, AnswerIndex.NOT_STARTED),
        ),
        messages = listOf(
            ChatMessage("user-1", MessageAuthor.USER, "What changed in the policy?", "10:24", MessageDelivery.COMPLETE),
            ChatMessage(
                "assistant-1",
                MessageAuthor.ASSISTANT,
                "The **updated policy** adds `review()` before publication. Read [the guide](https://example.com/guide).\n```kotlin\napprove(release)\n```",
                "10:24",
                MessageDelivery.STREAMING,
                references = listOf("policy.pdf"),
            ),
            ChatMessage("assistant-2", MessageAuthor.ASSISTANT, "The connection ended before reconciliation.", "10:25", MessageDelivery.INTERRUPTED),
        ),
        pinnedCount = 2,
        answerScope = AnswerScopeMode.SELECTED_SOURCES,
        addSourceMode = AddSourceMode.TEXT,
        uploadPhase = UploadPhase.BYTE_UPLOAD,
        draft = "Summarize the approval step",
        conversationNameInput = "Release review",
        sourceFileNameInput = "notes.txt",
        sourceTextInput = "Approved notes",
        spacesPage = PageState(hasMore = true, complete = false),
        conversationsPage = PageState(hasMore = true, complete = false),
        messagesPage = PageState(hasMore = true, complete = false),
        sourcesPage = PageState(hasMore = true, complete = false),
        capabilities = DataSearchCapabilities(
            canSendMessages = true,
            canCreateConversation = true,
            canRenameConversation = true,
            canDeleteConversation = true,
            canAddSource = true,
            canAddTextSource = true,
            canIndexSource = true,
            canRenameSource = true,
            canDeleteSource = true,
            canDeleteMessages = true,
            canPinSources = true,
            modelCatalogueAvailable = true,
            structuredReferencesAvailable = true,
        ),
    )
}
