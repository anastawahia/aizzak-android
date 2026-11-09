package android.aizzak.com.feature.datasearch

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

internal sealed interface MarkdownBlock {
    data class TextBlock(val source: String) : MarkdownBlock
    data class CodeBlock(val code: String) : MarkdownBlock
}

internal fun parseMarkdownBlocks(source: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val text = StringBuilder()
    val code = StringBuilder()
    var fenced = false
    fun flushText() {
        if (text.isNotEmpty()) {
            blocks += MarkdownBlock.TextBlock(text.toString().trimEnd())
            text.clear()
        }
    }
    source.lines().forEach { line ->
        if (line.trimStart().startsWith("```")) {
            if (fenced) {
                blocks += MarkdownBlock.CodeBlock(code.toString().trimEnd())
                code.clear()
            } else flushText()
            fenced = !fenced
        } else if (fenced) code.appendLine(line) else text.appendLine(line)
    }
    if (fenced) {
        text.append("```").appendLine().append(code)
    }
    flushText()
    return blocks.ifEmpty { listOf(MarkdownBlock.TextBlock("")) }
}

internal fun isSafeWebLink(url: String): Boolean = runCatching {
    val scheme = java.net.URI(url).scheme?.lowercase()
    scheme == "https" || scheme == "http"
}.getOrDefault(false)

internal fun inlineMarkdown(source: String, linkColor: Color): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    val token = Regex("(\\*\\*[^*]+\\*\\*|`[^`]+`|\\[[^]]+]\\([^)]+\\))")
    token.findAll(source).forEach { match ->
        append(source.substring(cursor, match.range.first))
        val value = match.value
        when {
            value.startsWith("**") -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value.removePrefix("**").removeSuffix("**")) }
            value.startsWith("`") -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = linkColor.copy(alpha = .12f))) {
                append(value.removePrefix("`").removeSuffix("`"))
            }
            else -> {
                val separator = value.lastIndexOf("](")
                val label = value.substring(1, separator)
                val url = value.substring(separator + 2, value.length - 1)
                if (isSafeWebLink(url)) {
                    withLink(LinkAnnotation.Url(url)) {
                        withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) { append(label) }
                    }
                } else append(label)
            }
        }
        cursor = match.range.last + 1
    }
    append(source.substring(cursor))
}

@Composable
internal fun SafeMarkdown(
    source: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    SelectionContainer {
        Column(modifier.semantics { contentDescription = source }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            parseMarkdownBlocks(source).forEach { block ->
                when (block) {
                    is MarkdownBlock.TextBlock -> Text(
                        inlineMarkdown(block.source, MaterialTheme.colorScheme.primary),
                        style = style,
                    )
                    is MarkdownBlock.CodeBlock -> Surface(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            block.code,
                            Modifier.padding(12.dp),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
