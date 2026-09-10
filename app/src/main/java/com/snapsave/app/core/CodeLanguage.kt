package com.snapsave.app.core

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CodeLanguage(val extension: String, val label: String) {
    HTML("html", "HTML"),
    XML("xml", "XML"),
    JSON("json", "JSON"),
    KOTLIN("kt", "Kotlin"),
    JAVA("java", "Java"),
    PYTHON("py", "Python"),
    JAVASCRIPT("js", "JavaScript"),
    TYPESCRIPT("ts", "TypeScript"),
    CSS("css", "CSS"),
    CPP("cpp", "C++"),
    CSHARP("cs", "C#"),
    SQL("sql", "SQL"),
    SHELL("sh", "Shell"),
    MARKDOWN("md", "Markdown"),
    YAML("yml", "YAML"),
    PHP("php", "PHP"),
    PLAIN("txt", "Plain Text");

    companion object {
        /** Thứ tự ưu tiên hiển thị trên hàng chip chọn ngôn ngữ. */
        val common: List<CodeLanguage> = listOf(
            HTML, KOTLIN, JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, CSS, JSON, SQL, MARKDOWN, PLAIN
        )
    }
}

object LanguageDetector {

    private const val MIN_SCORE = 3
    private const val SCAN_LIMIT = 150_000

    fun detect(raw: String): CodeLanguage {
        val text = if (raw.length > SCAN_LIMIT) raw.substring(0, SCAN_LIMIT) else raw
        val trimmed = text.trimStart()
        if (trimmed.isEmpty()) return CodeLanguage.PLAIN

        // JSON: thử parse thật — chắc chắn nhất.
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            val parsed = runCatching {
                if (trimmed.startsWith("{")) JSONObject(trimmed) else JSONArray(trimmed)
            }.isSuccess
            if (parsed) return CodeLanguage.JSON
        }
        if (text.contains("<?php")) return CodeLanguage.PHP

        val scores = mutableMapOf<CodeLanguage, Int>()
        fun bump(lang: CodeLanguage, pts: Int) {
            scores[lang] = (scores[lang] ?: 0) + pts
        }
        fun has(s: String) = text.contains(s)
        fun rx(pattern: String) = Regex(pattern).containsMatchIn(text)

        // ---- HTML ----
        if (rx("(?i)<!doctype\\s+html")) bump(CodeLanguage.HTML, 8)
        if (rx("(?i)</?(html|head|body|header|footer|section|article|nav|main|aside)[^>a-z]*>")) bump(CodeLanguage.HTML, 5)
        if (rx("(?i)</?(div|span|img|input|button|textarea|select|form|table|ul|ol|li|script|style|title|meta|link|h[1-6])\\b")) bump(CodeLanguage.HTML, 4)
        if (rx("(?i)\\s(class|id|src|href|alt|charset|rel|content)=\"")) bump(CodeLanguage.HTML, 3)
        if (rx("(?i)</(div|span|p|a|script|style|body|html|section)>")) bump(CodeLanguage.HTML, 4)

        // ---- XML ----
        if (rx("(?i)<\\?xml\\s+version")) bump(CodeLanguage.XML, 8)
        if (rx("</[a-zA-Z]+:[a-zA-Z]+")) bump(CodeLanguage.XML, 3)
        if (rx("(?i)<(manifest|resources|vector|shape|selector|paths|adaptive-icon|LinearLayout|RelativeLayout|ConstraintLayout)\\b")) bump(CodeLanguage.XML, 4)

        // ---- Kotlin ----
        if (rx("\\bfun\\s+[a-zA-Z_]\\w*\\s*\\(")) bump(CodeLanguage.KOTLIN, 5)
        if (rx("\\b(val|var)\\s+[a-zA-Z_]\\w*\\s*[:=]")) bump(CodeLanguage.KOTLIN, 3)
        if (has("androidx.compose") || has("@Composable")) bump(CodeLanguage.KOTLIN, 5)
        if (rx("\\bdata\\s+class\\b") || rx("\\bobject\\s+[A-Z]\\w*")) bump(CodeLanguage.KOTLIN, 2)
        if (rx("\\bwhen\\s*\\(") || has("?.") || has("?:")) bump(CodeLanguage.KOTLIN, 1)
        if (rx("(?m)^import\\s+[a-z][\\w.]*[a-zA-Z]$")) bump(CodeLanguage.KOTLIN, 1)

        // ---- Java ----
        if (rx("(?m)^import\\s+[\\w.]+;")) bump(CodeLanguage.JAVA, 3)
        if (rx("\\bpublic\\s+(final\\s+)?(class|interface|enum|record)\\s+\\w+")) bump(CodeLanguage.JAVA, 5)
        if (rx("\\b(private|protected|public)\\s+(static\\s+)?(void|int|long|boolean|String|double|float)\\b")) bump(CodeLanguage.JAVA, 4)
        if (rx("\\bnew\\s+[A-Z]\\w*\\s*\\(")) bump(CodeLanguage.JAVA, 2)
        if (has("System.out.print")) bump(CodeLanguage.JAVA, 4)

        // ---- Python ----
        if (rx("(?m)^\\s*def\\s+\\w+\\s*\\([^)]*\\)\\s*:")) bump(CodeLanguage.PYTHON, 5)
        if (rx("(?m)^\\s*(from\\s+[\\w.]+\\s+import\\s+\\w+|import\\s+[\\w.]+\\s*(as\\s+\\w+)?)$")) bump(CodeLanguage.PYTHON, 3)
        if (rx("(?m)^\\s*class\\s+\\w+\\s*(\\([^)]*\\))?\\s*:")) bump(CodeLanguage.PYTHON, 3)
        if (rx("\\bprint\\s*\\(")) bump(CodeLanguage.PYTHON, 3)
        if (rx("\\b(self|__init__|__name__|elif)\\b")) bump(CodeLanguage.PYTHON, 2)
        if (rx("(?m)^\\s*(if|for|while|else|elif|return|try|except|with)\\b[^=]*:\\s*$")) bump(CodeLanguage.PYTHON, 2)

        // ---- JavaScript / TypeScript ----
        if (rx("\\bfunction\\s+\\w*\\s*\\(")) bump(CodeLanguage.JAVASCRIPT, 4)
        if (rx("\\b(const|let)\\s+[\\w{}\\[\\]]+\\s*=")) bump(CodeLanguage.JAVASCRIPT, 3)
        if (has("=>")) bump(CodeLanguage.JAVASCRIPT, 3)
        if (rx("\\bconsole\\.(log|error|warn|info)\\b")) bump(CodeLanguage.JAVASCRIPT, 4)
        if (rx("\\b(document|window|localStorage)\\.")) bump(CodeLanguage.JAVASCRIPT, 3)
        if (rx("\\brequire\\s*\\(|\\bmodule\\.exports")) bump(CodeLanguage.JAVASCRIPT, 3)
        if (rx("(?m)^\\s*export\\s+(default|const|function|class)")) bump(CodeLanguage.JAVASCRIPT, 2)
        if (rx(":\\s*(string|number|boolean|any|unknown|never|void)\\b")) bump(CodeLanguage.TYPESCRIPT, 5)
        if (rx("\\binterface\\s+\\w+\\s*\\{")) bump(CodeLanguage.TYPESCRIPT, 4)
        if (rx("\\btype\\s+\\w+\\s*=")) bump(CodeLanguage.TYPESCRIPT, 3)
        if (rx("\\b(public|private|readonly)\\s+[a-zA-Z_]\\w*\\s*:")) bump(CodeLanguage.TYPESCRIPT, 2)

        // ---- CSS ----
        if (rx("(?i)@(media|keyframes|import|font-face)\\b")) bump(CodeLanguage.CSS, 4)
        if (rx("[.#][a-zA-Z][\\w-]*\\s*[,{]")) bump(CodeLanguage.CSS, 2)
        if (rx("(?i)\\b(margin|padding|font-size|font-weight|line-height|background(-color)?|border-radius|display|position|z-index|grid-template)\\s*:")) bump(CodeLanguage.CSS, 5)
        if (rx("\\b\\d+(px|rem|em|vh|vw)%?\\b")) bump(CodeLanguage.CSS, 1)
        if (has("{") && has("}") && rx("[^{}]+\\{[^{}]*:[^{}]+\\}")) bump(CodeLanguage.CSS, 3)

        // ---- C / C++ ----
        if (rx("(?m)^\\s*#include\\s*[<\"]")) bump(CodeLanguage.CPP, 6)
        if (has("std::") || has("cout <<")) bump(CodeLanguage.CPP, 3)
        if (rx("\\bint\\s+main\\s*\\(")) bump(CodeLanguage.CPP, 2)

        // ---- C# ----
        if (rx("(?m)^\\s*using\\s+System[\\w.]*;")) bump(CodeLanguage.CSHARP, 6)
        if (has("Console.WriteLine")) bump(CodeLanguage.CSHARP, 4)
        if (rx("\\bnamespace\\s+[\\w.]+\\s*\\{")) bump(CodeLanguage.CSHARP, 2)

        // ---- SQL ----
        if (rx("(?is)\\bselect\\b.+?\\bfrom\\b")) bump(CodeLanguage.SQL, 5)
        if (rx("(?i)\\b(insert\\s+into|delete\\s+from|create\\s+table|alter\\s+table|group\\s+by|order\\s+by|inner\\s+join|left\\s+join)\\b")) bump(CodeLanguage.SQL, 2)

        // ---- Shell ----
        if (rx("(?m)^#!/(usr/)?bin/(ba|z|fi)?sh")) bump(CodeLanguage.SHELL, 6)
        if (rx("(?m)^\\s*(sudo|apt-get|apt|chmod|chown|mkdir|curl|wget|grep|echo|export|pip3?|npm|yarn)\\b")) bump(CodeLanguage.SHELL, 2)

        // ---- Markdown ----
        if (rx("(?m)^#{1,6}\\s+\\S")) bump(CodeLanguage.MARKDOWN, 3)
        if (rx("\\[[^\\]]+\\]\\([^)]+\\)")) bump(CodeLanguage.MARKDOWN, 3)
        if (rx("(?m)^\\s*```")) bump(CodeLanguage.MARKDOWN, 4)
        if (rx("(?m)^>\\s\\S")) bump(CodeLanguage.MARKDOWN, 1)
        if (rx("\\*\\*[^*]+\\*\\*")) bump(CodeLanguage.MARKDOWN, 2)

        // ---- YAML ----
        val yamlLines = Regex("(?m)^\\s*[\\w.\"'-]+:\\s*(\\S.*)?$").findAll(text).count()
        if (yamlLines >= 3 && !has(";") && !has("{")) bump(CodeLanguage.YAML, 4)
        if (rx("(?m)^\\s*-\\s+\\w+:") && yamlLines >= 2) bump(CodeLanguage.YAML, 2)

        val best = CodeLanguage.entries.maxByOrNull { scores[it] ?: 0 } ?: return CodeLanguage.PLAIN
        val bestScore = scores[best] ?: 0
        return if (bestScore >= MIN_SCORE) best else CodeLanguage.PLAIN
    }

    /** Gợi ý tên file thông minh theo nội dung (thẻ <title>, tên hàm/class, heading markdown…). */
    fun suggestTitle(raw: String, lang: CodeLanguage): String {
        val text = if (raw.length > 60_000) raw.substring(0, 60_000) else raw

        fun grab(pattern: String): String? =
            Regex(pattern).find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }

        val candidate: String? = when (lang) {
            CodeLanguage.HTML -> grab("(?is)<title>(.*?)</title>")
            CodeLanguage.KOTLIN -> grab("\\b(?:class|object|fun)\\s+([A-Za-z_]\\w*)")
            CodeLanguage.JAVA, CodeLanguage.CSHARP -> grab("\\b(?:class|interface|enum|record)\\s+([A-Za-z_]\\w*)")
            CodeLanguage.PYTHON ->
                grab("(?m)^\\s*def\\s+([A-Za-z_]\\w*)") ?: grab("(?m)^\\s*class\\s+([A-Za-z_]\\w*)")
            CodeLanguage.JAVASCRIPT, CodeLanguage.TYPESCRIPT ->
                grab("\\b(?:function|class)\\s+([A-Za-z_]\\w*)") ?: grab("\\bconst\\s+([A-Za-z_]\\w*)")
            CodeLanguage.XML -> grab("<([a-zA-Z][\\w-]*)[^>]*>")
            CodeLanguage.MARKDOWN -> grab("(?m)^#{1,3}\\s*(.+?)\\s*$")
            CodeLanguage.SQL -> grab("(?is)(?:from|into|table)\\s+([A-Za-z_]\\w*)")
            else -> null
        }

        val cleaned = candidate
            ?.replace(Regex("[_]+"), "_")
            ?.replace(Regex("\\s+"), "_")
            ?.trim('_')
            ?.take(40)

        val fallback = "snippet_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return if (cleaned.isNullOrBlank() || cleaned.length < 2) fallback else cleaned
    }
}
