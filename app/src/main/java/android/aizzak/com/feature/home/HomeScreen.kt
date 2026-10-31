package android.aizzak.com.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.designsystem.PageTitle

/** Presentation IDs, not backend agent keys or permissions. All cards are unavailable. */
data class AssistantCardModel(val id: String, @param:StringRes val name: Int, @param:StringRes val description: Int, val accent: AssistantAccent)
enum class AssistantAccent { VIOLET, CYAN, AMBER }

val foundationAssistants = listOf(
    AssistantCardModel("smart", R.string.smart_assistant, R.string.smart_description, AssistantAccent.VIOLET),
    AssistantCardModel("search", R.string.data_search, R.string.data_description, AssistantAccent.CYAN),
    AssistantCardModel("wire", R.string.wire_twin, R.string.wire_description, AssistantAccent.AMBER),
)

/** Stateless authorized-content view. Not registered in the unconfigured application's navigation. */
@Composable
fun HomeScreen(assistants: List<AssistantCardModel>) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 840.dp -> 3
            maxWidth >= 600.dp -> 2
            else -> 1
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 1080.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                PageTitle(stringResource(R.string.home_title))
                Text(stringResource(R.string.home_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (assistants.isEmpty()) Text(stringResource(R.string.no_assistants))
                assistants.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { assistant ->
                            AssistantCard(assistant, Modifier.weight(1f))
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantCard(item: AssistantCardModel, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val accent = when (item.accent) {
        AssistantAccent.VIOLET -> colors.tertiary
        AssistantAccent.CYAN -> colors.secondary
        AssistantAccent.AMBER -> if (colors.background == Color(0xFF16233B)) Color(0xFFFFD37C) else Color(0xFF8A5100)
    }
    Card(modifier.testTag("assistant-${item.id}").semantics(mergeDescendants = true) {},
        colors = CardDefaults.cardColors(containerColor = colors.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = accent, modifier = Modifier.size(width = 32.dp, height = 4.dp), shape = MaterialTheme.shapes.small) {}
            Text(stringResource(item.name), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(item.description), color = colors.onSurfaceVariant)
            Surface(color = colors.surfaceVariant, shape = MaterialTheme.shapes.small) {
                Text(stringResource(R.string.coming_later), Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() { AizzakTheme { HomeScreen(foundationAssistants) } }

@Preview(showBackground = true, locale = "ar", widthDp = 840, fontScale = 2f)
@Composable
private fun ArabicHomePreview() { AizzakTheme { HomeScreen(foundationAssistants) } }
