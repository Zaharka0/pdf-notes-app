package com.pdfnotes.app.data.repository

import android.content.Context
import android.net.Uri
import com.pdfnotes.app.data.local.db.DocumentDao
import com.pdfnotes.app.data.local.db.DocumentEntity
import com.pdfnotes.app.data.local.pdf.PdfRendererWrapper
import com.pdfnotes.app.domain.model.Document
import com.pdfnotes.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DocumentRepositoryImpl(
    private val context: Context,
    private val dao: DocumentDao
) : DocumentRepository {
    override fun observeDocuments(): Flow<List<Document>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun importDocument(uri: String, displayName: String): Document {
        val parsed = Uri.parse(uri)
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                parsed,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val pageCount = PdfRendererWrapper(context).let { renderer ->
            try {
                renderer.open(parsed)
            } finally {
                renderer.close()
            }
        }
        val sizeBytes = context.contentResolver.openFileDescriptor(parsed, "r")
            ?.use { it.statSize } ?: 0L
        val entity = DocumentEntity(
            UUID.randomUUID().toString(),
            displayName,
            uri,
            sizeBytes,
            pageCount,
            System.currentTimeMillis()
        )
        dao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun getDocument(id: String): Document? =
        dao.getById(id)?.toDomain()

    override suspend fun deleteDocument(id: String) = dao.delete(id)

    override suspend fun touchLastOpened(id: String) =
        dao.updateLastOpened(id, System.currentTimeMillis())

    private fun DocumentEntity.toDomain() =
        Document(id, displayName, uri, sizeBytes, pageCount, lastOpenedAtEpochMs)
}
