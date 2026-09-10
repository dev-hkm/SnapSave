package com.snapsave.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,          // Tên hiển thị (có thể sửa)
    val fileName: String,       // Tên file thật trong filesDir/snippets
    val extension: String,      // html, kt, py…
    val language: String,       // Nhãn ngôn ngữ hiển thị
    val sizeBytes: Long,
    val lineCount: Int,
    val preview: String,        // 280 ký tự đầu cho list (khỏi đọc file)
    val createdAt: Long,
    val updatedAt: Long
)
