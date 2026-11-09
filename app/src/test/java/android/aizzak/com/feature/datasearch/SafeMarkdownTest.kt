package android.aizzak.com.feature.datasearch

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeMarkdownTest {
    @Test
    fun parserSeparatesCodeBlocksAndPreservesPlainText() {
        val blocks = parseMarkdownBlocks("Intro\n```kotlin\nval safe = true\n```\nDone")
        assertEquals(3, blocks.size)
        assertTrue(blocks[1] is MarkdownBlock.CodeBlock)
        assertEquals("val safe = true", (blocks[1] as MarkdownBlock.CodeBlock).code)
    }

    @Test
    fun inlineParserSupportsBoldCodeAndSafeLinks() {
        val parsed = inlineMarkdown("**Bold** `code` [Guide](https://example.com)", Color.Blue)
        assertEquals("Bold code Guide", parsed.text)
        assertTrue(parsed.getLinkAnnotations(0, parsed.length).isNotEmpty())
    }

    @Test
    fun unsafeSchemesAreNotLinkAnnotations() {
        val parsed = inlineMarkdown("[Run](javascript:alert(1))", Color.Blue)
        assertFalse(parsed.getLinkAnnotations(0, parsed.length).isNotEmpty())
        assertTrue(parsed.text.contains("Run"))
    }

    @Test
    fun webLinkPolicyAllowsOnlyHttpAndHttps() {
        assertTrue(isSafeWebLink("https://example.com"))
        assertTrue(isSafeWebLink("http://example.com"))
        assertFalse(isSafeWebLink("file:///secret"))
        assertFalse(isSafeWebLink("javascript:alert(1)"))
    }
}
