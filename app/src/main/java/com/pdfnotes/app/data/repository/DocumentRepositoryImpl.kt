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
        // Persist read access across app/device restarts (required once the
        // picker's one-shot grant would otherwise expire).
        context.contentResolver.takePersistableUriPermission(
            Uri.parse(uri),
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val pageCount = PdfRendererWrapper(context).let { renderer ->
            val count = renderer.open(Uri.parse(uri))
            renderer.close()
            count
        }

        val sizeBytes = context.contentResolver.openFileDescriptor(Uri.parse(uri), "r")
            ?.use { it.statSize } ?: 0L

        val entity = DocumentEntity(
            id = UUID.randomUUID().toString(),
            displayName = displayName,
            uri = uri,
            sizeBytes = sizeBytes,
            pageCount = pageCount,
            lastOpenedAtEpochMs = System.currentTimeMillis()
        )
        dao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun getDocument(id: String): Document? = dao.getById(id)?.toDomain()

    override suspend fun deleteDocument(id: String) = dao.delete(id)

    override suspend fun touchLastOpened(id: String) =
        dao.updateLastOpened(id, System.currentTimeMillis())

    private fun DocumentEntity.toDomain() = Document(
        id = id,
        displayName = displayName,
        uri = uri,
        sizeBytes = sizeBytes,
        pageCount = pageCount,
        lastOpenedAtEpochMs = lastOpenedAtEpochMs
    )
}
