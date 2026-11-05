package android.aizzak.com.feature.home

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.designsystem.BrandBar
import android.aizzak.com.core.designsystem.Eyebrow
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.core.designsystem.SupportingText
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection

data class AssistantCardModel(
    val id: String,
    @param:StringRes val name: Int,
    @param:StringRes val description: Int,
    val accent: AssistantAccent,
)

enum class AssistantAccent { VIOLET, CYAN, AMBER }

val foundationAssistants = listOf(
    AssistantCardModel("smart", R.string.smart_assistant, R.string.smart_description, AssistantAccent.VIOLET),
    AssistantCardModel("search", R.string.data_search, R.string.data_description, AssistantAccent.CYAN),
    AssistantCardModel("wire", R.string.wire_twin, R.string.wire_description, AssistantAccent.AMBER),
)

/** Stateless authorized-content view; cards are intentionally descriptive and non-clickable. */
@Composable
fun HomeScreen(assistants: List<AssistantCardModel>, phone: Boolean? = null) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (phone ?: (maxWidth < 600.dp)) {
            HomePhoneContent(assistants)
        } else {
            WideHomeScreen(assistants)
        }
    }
}

@Composable
private fun WideHomeScreen(assistants: List<AssistantCardModel>) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandBar()
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val fontScale = LocalDensity.current.fontScale
            val columns = when {
                maxWidth >= 840.dp && fontScale <= 1.2f -> 3
                maxWidth >= 600.dp -> 2
                else -> 1
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(Modifier.widthIn(max = 1080.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Eyebrow(stringResource(R.string.home_eyebrow))
                    PageTitle(stringResource(R.string.home_title))
                    SupportingText(stringResource(R.string.home_description))
                    if (assistants.isEmpty()) SupportingText(stringResource(R.string.no_assistants))
                    assistants.chunked(columns).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            row.forEach { assistant -> AssistantCard(assistant, Modifier.weight(1f)) }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    InformationNotice(stringResource(R.string.assistants_preparing))
                }
            }
        }
    }
}

@Composable
private fun AssistantCard(item: AssistantCardModel, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val layoutDirection = LocalLayoutDirection.current
    val (accent, container, symbol) = when (item.accent) {
        AssistantAccent.VIOLET -> Triple(colors.tertiary, colors.tertiaryContainer, "✦")
        AssistantAccent.CYAN -> Triple(colors.secondary, colors.secondaryContainer, "⌕")
        AssistantAccent.AMBER -> Triple(
            if (colors.background == Color(0xFF16233B)) Color(0xFFFFD37C) else Color(0xFF8A5100),
            if (colors.background == Color(0xFF16233B)) Color(0xFF5B4725) else Color(0xFFFFF0CE),
            "⌁",
        )
    }
    Box(
        modifier.testTag("assistant-${item.id}").semantics(mergeDescendants = true) {}
            .drawWithContent {
                drawContent()
                val stripeWidth = 5.dp.toPx()
                val stripeStart = if (layoutDirection == LayoutDirection.Ltr) 0f else size.width - stripeWidth
                drawRect(accent, topLeft = Offset(stripeStart, 0f), size = Size(stripeWidth, size.height))
            },
    ) {
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(Modifier.padding(start = 24.dp, end = 20.dp, top = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(Modifier.size(48.dp), color = container, contentColor = accent, shape = MaterialTheme.shapes.small) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(symbol, Modifier.clearAndSetSemantics {}, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Text(
                    stringResource(item.name),
                    style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.Ltr),
                )
                Text(stringResource(item.description), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Surface(color = colors.surfaceVariant, shape = CircleShape) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.size(7.dp).background(accent, CircleShape))
                        Text(stringResource(R.string.coming_later), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() { AizzakTheme { HomeScreen(foundationAssistants) } }

@Preview(showBackground = true, locale = "ar", widthDp = 840, fontScale = 1.5f)
@Composable
private fun ArabicHomePreview() { AizzakTheme { HomeScreen(foundationAssistants) } }
