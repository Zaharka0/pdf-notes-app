package com.pdfnotes.app.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY lastOpenedAtEpochMs DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE documents SET lastOpenedAtEpochMs = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: String, timestamp: Long)
}

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotations WHERE documentId = :documentId AND pageIndex = :pageIndex ORDER BY createdAtEpochMs ASC")
    fun observeForPage(documentId: String, pageIndex: Int): Flow<List<AnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AnnotationEntity)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun delete(id: String)
}
