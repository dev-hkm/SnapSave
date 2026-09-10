package com.snapsave.app.core

import java.text.Normalizer

object Names {

    /** "Bài viết HTML Đẹp!" -> "bai_viet_html_dep" (ASCII an toàn cho tên file). */
    fun fileBase(title: String): String {
        val normalized = Normalizer.normalize(title, Normalizer.Form.NFKD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
        val slug = normalized
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(48)
        return slug.ifBlank { "snippet" }
    }
}
