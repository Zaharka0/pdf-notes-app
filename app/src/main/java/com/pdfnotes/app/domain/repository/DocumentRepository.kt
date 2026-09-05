package com.pdfnotes.app.domain.repository

import com.pdfnotes.app.domain.model.Document
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing contract for document storage. The implementation (Data layer)
 * decides whether that means Room + SAF, a future cloud sync, etc. Domain and
 * UI never depend on that decision.
 */
interface DocumentRepository {
    fun observeDocuments(): Flow<List<Document>>
    suspend fun importDocument(uri: String, displayName: String): Document
    suspend fun getDocument(id: String): Document?
    suspend fun deleteDocument(id: String)
    suspend fun touchLastOpened(id: String)
}
