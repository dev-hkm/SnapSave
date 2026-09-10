package com.snapsave.app.core

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Toàn bộ thao tác file: lưu nội bộ, đọc/xóa, share URI, xuất ra Download. */
class FileStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, "snippets").apply { if (!exists()) mkdirs() }

    private fun file(name: String) = File(dir, name)

    /** Ghi file với tên mong muốn, tự thêm hậu tố _1, _2… nếu bị trùng. Trả về File thật đã tạo. */
    suspend fun writeAutoName(base: String, extension: String, content: String): File =
        withContext(Dispatchers.IO) {
            val target = uniqueFile("$base.$extension")
            target.writeText(content)
            target
        }

    suspend fun writeExact(name: String, content: String): File =
        withContext(Dispatchers.IO) { file(name).apply { writeText(content) } }

    suspend fun read(name: String): String =
        withContext(Dispatchers.IO) { file(name).readText() }

    suspend fun delete(name: String): Boolean =
        withContext(Dispatchers.IO) { file(name).delete() }

    fun uriFor(name: String): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file(name))

    fun mimeFor(extension: String): String = when (extension.lowercase()) {
        "html", "htm" -> "text/html"
        "xml" -> "text/xml"
        "css" -> "text/css"
        "json" -> "application/json"
        "md" -> "text/markdown"
        else -> "text/plain"
    }

    /** Xuất ra Download/SnapSave. API 29+ dùng MediaStore; API 26–28 cần quyền WRITE_EXTERNAL_STORAGE. */
    suspend fun exportToDownloads(fileName: String, content: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeFor(fileName.substringAfterLast('.', "txt")))
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SnapSave")
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: error("Hệ thống từ chối tạo tệp trong Download")
                    resolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                        ?: error("Không mở được luồng ghi tệp")
                    "Download/SnapSave/$fileName"
                } else {
                    val granted = ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) error("Chưa được cấp quyền ghi bộ nhớ")
                    val outDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "SnapSave"
                    ).apply { mkdirs() }
                    File(outDir, fileName).writeText(content)
                    "Download/SnapSave/$fileName"
                }
            }
        }

    /** Lưu trực tiếp vào thư mục do người dùng chỉ định qua SAF (Tree URI). */
    suspend fun saveToCustomFolder(treeUri: Uri, fileName: String, content: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = DocumentFile.fromTreeUri(context, treeUri)
                    ?: error("Không thể truy cập thư mục đã chọn")
                if (!dir.canWrite()) error("Không có quyền ghi vào thư mục này")

                val ext = fileName.substringAfterLast('.', "txt")
                val mime = mimeFor(ext)

                // Nếu file đã tồn tại, xóa file cũ hoặc ghi đè
                val existing = dir.findFile(fileName)
                val target = existing ?: dir.createFile(mime, fileName)
                    ?: error("Không thể tạo tệp trong thư mục đã chọn")

                context.contentResolver.openOutputStream(target.uri, "wt")?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                } ?: error("Không thể mở luồng ghi vào tệp")

                "${dir.name ?: "Thư mục"}/$fileName"
            }
        }

    private fun uniqueFile(name: String): File {
        val clean = name.trim().ifBlank { "snippet.txt" }
        var candidate = file(clean)
        if (!candidate.exists()) return candidate
        val base = clean.substringBeforeLast('.', clean)
        val ext = clean.substringAfterLast('.', "")
        var i = 1
        while (candidate.exists()) {
            candidate = file(if (ext.isEmpty()) "${base}_$i" else "${base}_$i.$ext")
            i += 1
        }
        return candidate
    }
}
