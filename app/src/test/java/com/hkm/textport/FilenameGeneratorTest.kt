package com.hkm.textport

import com.hkm.textport.core.detection.FilenameGenerator
import com.hkm.textport.core.detection.LanguageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilenameGeneratorTest {

    @Test
    fun sanitize_removesInvalidFileCharacters() {
        val input = "my:illegal/file\\name*is?here<and>there|done"
        val output = FilenameGenerator.sanitize(input)
        assertFalse(output.contains(":"))
        assertFalse(output.contains("/"))
        assertFalse(output.contains("\\"))
        assertFalse(output.contains("*"))
        assertFalse(output.contains("?"))
        assertFalse(output.contains("<"))
        assertFalse(output.contains(">"))
        assertFalse(output.contains("|"))
    }

    @Test
    fun generate_extractsMarkdownTitle() {
        val md = "# Super Awesome Project\n\nThis is some description."
        val filename = FilenameGenerator.generate(md, LanguageType.MARKDOWN)
        assertTrue(filename.startsWith("super-awesome-project"))
        assertTrue(filename.endsWith(".md"))
    }

    @Test
    fun generate_extractsHtmlTitle() {
        val html = "<html><head><title>Dashboard UI</title></head><body><h1>Hello</h1></body></html>"
        val filename = FilenameGenerator.generate(html, LanguageType.HTML)
        assertTrue(filename.startsWith("dashboard-ui"))
        assertTrue(filename.endsWith(".html"))
    }

    @Test
    fun generate_extractsKotlinClass() {
        val code = "package com.example\n\nclass UserProfileViewModel : ViewModel() {\n}"
        val filename = FilenameGenerator.generate(code, LanguageType.KOTLIN)
        assertTrue(filename.startsWith("userprofileviewmodel"))
        assertTrue(filename.endsWith(".kt"))
    }

    @Test
    fun generate_extractsPythonDef() {
        val code = "def calculate_statistics(data):\n    return sum(data)"
        val filename = FilenameGenerator.generate(code, LanguageType.PYTHON)
        assertTrue(filename.startsWith("calculate_statistics"))
        assertTrue(filename.endsWith(".py"))
    }
}
