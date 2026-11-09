package android.aizzak.com.feature.datasearch

import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.model.ThemePreference
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1000dp-h1400dp-xxhdpi")
class DataSearchScreenshotTest(private val fileName: String) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun recordInventoryConfiguration() {
        val case = screenshotCases().single { it.fileName == fileName }
        configureResources(case)
        compose.setContent {
            val density = ApplicationProvider.getApplicationContext<android.content.Context>().resources.displayMetrics.density
            CompositionLocalProvider(
                LocalLayoutDirection provides if (case.locale == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalDensity provides Density(density, case.fontScale),
            ) {
                AizzakTheme(if (case.dark) ThemePreference.DARK else ThemePreference.LIGHT) {
                    Box(Modifier.width(case.width.dp).height(1000.dp).testTag(CAPTURE_ROOT)) {
                        DataSearchContent(case.destination, case.state)
                    }
                }
            }
        }
        compose.waitForIdle()
        case.scrollToTag?.let { compose.onNodeWithTag(it).performScrollTo() }
        compose.waitForIdle()
        val output = File(OUTPUT, case.fileName)
        val outputDirectory = requireNotNull(output.parentFile)
        check(outputDirectory.exists() || outputDirectory.mkdirs()) { "Could not create $outputDirectory" }
        compose.onNodeWithTag(CAPTURE_ROOT).captureRoboImage(output.absolutePath)
    }

    private fun configureResources(case: ScreenshotCase) {
        val locale = Locale.forLanguageTag(case.locale)
        Locale.setDefault(locale)
        val resources = ApplicationProvider.getApplicationContext<android.content.Context>().resources
        val configuration = Configuration(resources.configuration).apply {
            setLocales(LocaleList(locale))
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (case.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            fontScale = case.fontScale
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    companion object {
        private const val CAPTURE_ROOT = "data-search-capture-root"
        private val OUTPUT = File(System.getProperty("user.dir"), "build/outputs/roborazzi/data-search-agent-design-repair")

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun configurations(): List<Array<Any>> = screenshotCases().map { arrayOf(it.fileName) }
    }
}

private data class ScreenshotConfig(val locale: String, val dark: Boolean, val width: Int, val fontScale: Float)

private data class ScreenshotCase(
    val destination: DataSearchDestination,
    val config: ScreenshotConfig,
    val scenario: String = "",
    val state: DataSearchUiState = dataSearchTestState(),
    val scrollToTag: String? = null,
) {
    val locale get() = config.locale
    val dark get() = config.dark
    val width get() = config.width
    val fontScale get() = config.fontScale
    // Keep the inventory filename schema, placing state variants in named subdirectories.
    val fileName get() = (if (scenario.isEmpty()) "" else "$scenario/") +
        "${destination.screenId}__${locale}_${if (dark) "dark" else "light"}_w${width}_fs${fontScale}.png"
}

private val DataSearchDestination.screenId: String get() = when (this) {
    DataSearchDestination.SPACES -> "SCR-DS-001"
    DataSearchDestination.CONVERSATION -> "SCR-DS-002"
    DataSearchDestination.CONVERSATIONS -> "SCR-DS-003"
    DataSearchDestination.EDIT_CONVERSATION -> "SCR-DS-004"
    DataSearchDestination.DELETE_CONVERSATION -> "SCR-DS-005"
    DataSearchDestination.SOURCES -> "SCR-DS-006"
    DataSearchDestination.ADD_SOURCE -> "SCR-DS-007"
    DataSearchDestination.SOURCE_DETAILS -> "SCR-DS-008"
    DataSearchDestination.ANSWER_SCOPE -> "SCR-DS-009"
    DataSearchDestination.DELETE_SOURCE -> "SCR-DS-010"
}

private fun screenshotCases(): List<ScreenshotCase> {
    val configurations = listOf(
        ScreenshotConfig("en", false, 390, 1.0f),
        ScreenshotConfig("ar", true, 390, 1.0f),
        ScreenshotConfig("en", true, 840, 1.0f),
        ScreenshotConfig("ar", false, 840, 1.3f),
    )
    val baseline = dataSearchTestState()
    val cases = DataSearchDestination.entries.flatMap { destination ->
        configurations.map { ScreenshotCase(destination, it) }
    }.toMutableList()
    fun add(
        destination: DataSearchDestination,
        scenario: String,
        state: DataSearchUiState,
        scrollToTag: String? = null,
    ) {
        configurations.forEach { cases += ScreenshotCase(destination, it, scenario, state, scrollToTag) }
    }

    val screensWithStateRequirements = listOf(
        DataSearchDestination.SPACES,
        DataSearchDestination.CONVERSATION,
        DataSearchDestination.CONVERSATIONS,
        DataSearchDestination.EDIT_CONVERSATION,
        DataSearchDestination.SOURCES,
        DataSearchDestination.ADD_SOURCE,
        DataSearchDestination.SOURCE_DETAILS,
        DataSearchDestination.ANSWER_SCOPE,
    )
    screensWithStateRequirements.forEach { destination ->
        listOf(DataSearchViewState.OFFLINE, DataSearchViewState.PERMISSION_DENIED, DataSearchViewState.SESSION_EXPIRED)
            .forEach { state -> add(destination, state.name.lowercase(), baseline.copy(viewState = state)) }
    }
    add(DataSearchDestination.SPACES, "empty", baseline.copy(
        viewState = DataSearchViewState.EMPTY, spaces = emptyList(), selectedSpace = null, spacesPage = PageState(),
    ))
    add(DataSearchDestination.CONVERSATION, "empty", baseline.copy(
        viewState = DataSearchViewState.EMPTY, messages = emptyList(), messagesPage = PageState(), draft = "",
    ))
    add(DataSearchDestination.CONVERSATION, "sending", baseline.copy(
        messages = listOf(baseline.messages.first().copy(delivery = MessageDelivery.SENDING)),
        messagesPage = PageState(), working = true,
    ))
    add(DataSearchDestination.CONVERSATION, "interrupted", baseline.copy(
        messages = listOf(baseline.messages.last()), messagesPage = PageState(),
    ))
    add(DataSearchDestination.CONVERSATIONS, "empty", baseline.copy(
        viewState = DataSearchViewState.EMPTY, conversations = emptyList(), selectedConversation = null,
        conversationsPage = PageState(),
    ))
    add(DataSearchDestination.SOURCES, "empty", baseline.copy(
        viewState = DataSearchViewState.EMPTY, sources = emptyList(), selectedSourceId = null, sourcesPage = PageState(),
    ))
    add(DataSearchDestination.SOURCES, "filtered_empty", baseline.copy(
        viewState = DataSearchViewState.EMPTY, sources = emptyList(), sourceFilter = SourceFilter.FAILED,
        selectedSourceId = null, sourcesPage = PageState(),
    ))
    add(DataSearchDestination.ANSWER_SCOPE, "empty", baseline.copy(
        viewState = DataSearchViewState.EMPTY, sources = emptyList(), pinnedCount = 0,
        answerScope = AnswerScopeMode.ALL_INDEXED, sourcesPage = PageState(),
    ))
    listOf(DataSearchDestination.CONVERSATIONS, DataSearchDestination.ANSWER_SCOPE).forEach { destination ->
        add(destination, "loading", baseline.copy(viewState = DataSearchViewState.LOADING))
    }
    listOf(
        DataSearchDestination.CONVERSATIONS,
        DataSearchDestination.EDIT_CONVERSATION,
        DataSearchDestination.ANSWER_SCOPE,
    ).forEach { destination -> add(destination, "error", baseline.copy(viewState = DataSearchViewState.ERROR)) }
    listOf(
        DataSearchDestination.EDIT_CONVERSATION,
        DataSearchDestination.SOURCES,
        DataSearchDestination.SOURCE_DETAILS,
        DataSearchDestination.ANSWER_SCOPE,
    ).forEach { destination -> add(destination, "partial", baseline.copy(viewState = DataSearchViewState.PARTIAL)) }
    add(DataSearchDestination.EDIT_CONVERSATION, "rename", baseline.copy(
        conversationEditMode = ConversationEditMode.RENAME,
        conversationEditTarget = baseline.selectedConversation,
        conversationNameInput = "Policy updates revised",
    ))
    add(DataSearchDestination.EDIT_CONVERSATION, "validation", baseline.copy(conversationNameInput = ""))
    add(DataSearchDestination.ADD_SOURCE, "choice", baseline.copy(
        addSourceMode = AddSourceMode.CHOICE, uploadPhase = UploadPhase.IDLE,
    ))
    listOf(
        UploadPhase.BYTE_UPLOAD,
        UploadPhase.UPLOAD_COMPLETE,
        UploadPhase.READY_NOT_INDEXED,
        UploadPhase.QUARANTINED,
        UploadPhase.FAILED,
        UploadPhase.EXPIRED,
        UploadPhase.RECOVERY_REQUIRED,
    ).forEach { phase ->
        add(DataSearchDestination.ADD_SOURCE, "upload_${phase.name.lowercase()}", baseline.copy(
            addSourceMode = AddSourceMode.FILE, uploadPhase = phase,
        ), scrollToTag = "DS007-08")
    }
    val selectedSource = baseline.sources.first { it.id == baseline.selectedSourceId }
    listOf(FileSafety.UPLOADED, FileSafety.SCANNING, FileSafety.QUARANTINED).forEach { safety ->
        add(DataSearchDestination.SOURCE_DETAILS, "safety_${safety.name.lowercase()}", baseline.copy(
            sources = listOf(selectedSource.copy(safety = safety, index = AnswerIndex.NOT_STARTED, pinned = false)),
        ))
    }
    listOf(AnswerIndex.NOT_STARTED, AnswerIndex.PENDING, AnswerIndex.INDEXING, AnswerIndex.FAILED).forEach { index ->
        add(DataSearchDestination.SOURCE_DETAILS, "index_${index.name.lowercase()}", baseline.copy(
            sources = listOf(selectedSource.copy(index = index, pinned = false)),
        ))
    }
    return cases
}
