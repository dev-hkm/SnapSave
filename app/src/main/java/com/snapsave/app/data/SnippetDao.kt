package com.snapsave.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {

    @Query("SELECT * FROM snippets ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<SnippetEntity?>

    @Query("SELECT * FROM snippets WHERE id = :id LIMIT 1")
    suspend fun byId(id: Long): SnippetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SnippetEntity): Long

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM snippets")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM snippets")
    fun observeCount(): Flow<Int>
}
