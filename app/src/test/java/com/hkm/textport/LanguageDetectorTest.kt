package com.hkm.textport

import com.hkm.textport.core.detection.LanguageDetector
import com.hkm.textport.core.detection.LanguageType
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDetectorTest {

    @Test
    fun detect_htmlDocument() {
        val html = "<!DOCTYPE html>\n<html>\n<head><title>Test</title></head>\n<body><h1>Hello</h1></body>\n</html>"
        val lang = LanguageDetector.detect(html)
        assertEquals(LanguageType.HTML, lang)
    }

    @Test
    fun detect_pythonShebang() {
        val py = "#!/usr/bin/env python3\n\nimport os\n\ndef main():\n    print('Hello World')\n\nif __name__ == '__main__':\n    main()"
        val lang = LanguageDetector.detect(py)
        assertEquals(LanguageType.PYTHON, lang)
    }

    @Test
    fun detect_bashShebang() {
        val sh = "#!/bin/bash\necho 'Deploying app...'\nexit 0"
        val lang = LanguageDetector.detect(sh)
        assertEquals(LanguageType.SHELL, lang)
    }

    @Test
    fun detect_kotlinCode() {
        val kt = "package com.example\n\ndata class User(val id: Long, val name: String)\n\nfun greetUser(user: User) {\n    println(\"Hello \${user.name}\")\n}"
        val lang = LanguageDetector.detect(kt)
        assertEquals(LanguageType.KOTLIN, lang)
    }

    @Test
    fun detect_sqlQuery() {
        val sql = "SELECT u.id, u.username, o.total FROM users u JOIN orders o ON u.id = o.user_id WHERE o.total > 100 GROUP BY u.id ORDER BY o.total DESC;"
        val lang = LanguageDetector.detect(sql)
        assertEquals(LanguageType.SQL, lang)
    }

    @Test
    fun detect_markdownContent() {
        val md = "# Project Readme\n\nHere is a list of features:\n- Offline first\n- Fast SAF saving\n- Lucide icons\n\n```kotlin\nfun test() {}\n```"
        val lang = LanguageDetector.detect(md)
        assertEquals(LanguageType.MARKDOWN, lang)
    }
}
