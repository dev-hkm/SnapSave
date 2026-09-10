package com.snapsave.app.data

import com.snapsave.app.core.CodeLanguage
import com.snapsave.app.core.FileStore
import com.snapsave.app.core.Names
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SnippetRepository(
    private val dao: SnippetDao,
    private val files: FileStore
) {

    val snippets: Flow<List<SnippetEntity>> = dao.observeAll()
    val count: Flow<Int> = dao.observeCount()

    fun observe(id: Long): Flow<SnippetEntity?> = dao.observeById(id)

    suspend fun get(id: Long): SnippetEntity? = dao.byId(id)

    suspend fun content(entity: SnippetEntity): String = files.read(entity.fileName)

    /** Lưu nhanh: ghi file trước, insert DB sau. Trả về entity đã có id. */
    suspend fun create(
        title: String,
        content: String,
        languageLabel: String,
        extension: String
    ): SnippetEntity = withContext(Dispatchers.IO) {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cleanExt = extension.trim().removePrefix(".").lowercase().ifBlank { "txt" }
        val cleanLabel = languageLabel.trim().ifBlank { cleanExt.uppercase() }
        val cleanTitle = title.trim().ifBlank { "$cleanLabel · $stamp" }
        val file = files.writeAutoName(Names.fileBase(cleanTitle), cleanExt, content)
        val now = System.currentTimeMillis()
        val entity = SnippetEntity(
            title = cleanTitle,
            fileName = file.name,
            extension = cleanExt,
            language = cleanLabel,
            sizeBytes = content.toByteArray(Charsets.UTF_8).size.toLong(),
            lineCount = content.count { it == '\n' } + 1,
            preview = content.take(280),
            createdAt = now,
            updatedAt = now
        )
        val id = dao.upsert(entity)
        entity.copy(id = id)
    }

    suspend fun create(title: String, content: String, language: CodeLanguage): SnippetEntity =
        create(title, content, language.label, language.extension)

    suspend fun updateContent(entity: SnippetEntity, newContent: String) =
        withContext(Dispatchers.IO) {
            files.writeExact(entity.fileName, newContent)
            dao.upsert(
                entity.copy(
                    sizeBytes = newContent.toByteArray(Charsets.UTF_8).size.toLong(),
                    lineCount = newContent.count { it == '\n' } + 1,
                    preview = newContent.take(280),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun rename(entity: SnippetEntity, newTitle: String) =
        withContext(Dispatchers.IO) {
            dao.upsert(
                entity.copy(
                    title = newTitle.trim().ifBlank { entity.title },
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun delete(entity: SnippetEntity) = withContext(Dispatchers.IO) {
        dao.deleteById(entity.id)
        files.delete(entity.fileName)
    }

    /** Khôi phục sau Hoàn tác: viết lại đúng tên file cũ + id cũ. */
    suspend fun restore(entity: SnippetEntity, content: String) = withContext(Dispatchers.IO) {
        files.writeExact(entity.fileName, content)
        dao.upsert(entity)
    }

    suspend fun deleteAll(list: List<SnippetEntity>) = withContext(Dispatchers.IO) {
        dao.clearAll()
        list.forEach { files.delete(it.fileName) }
    }
}
