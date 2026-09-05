package com.pdfnotes.app.domain.usecase

import com.pdfnotes.app.domain.model.Annotation
import com.pdfnotes.app.domain.model.AnnotationType
import com.pdfnotes.app.domain.model.Document
import com.pdfnotes.app.domain.repository.AnnotationRepository
import com.pdfnotes.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ObserveDocumentsUseCase(private val repo: DocumentRepository) {
    operator fun invoke(): Flow<List<Document>> = repo.observeDocuments()
}

class ImportDocumentUseCase(private val repo: DocumentRepository) {
    suspend operator fun invoke(uri: String, displayName: String): Document =
        repo.importDocument(uri, displayName)
}

class OpenDocumentUseCase(
    private val repo: DocumentRepository
) {
    suspend operator fun invoke(id: String): Document? {
        repo.touchLastOpened(id)
        return repo.getDocument(id)
    }
}

class ObserveAnnotationsUseCase(private val repo: AnnotationRepository) {
    operator fun invoke(documentId: String, pageIndex: Int): Flow<List<Annotation>> =
        repo.observeAnnotations(documentId, pageIndex)
}

/** Covers both highlight and free-text note creation - same shape, different type. */
class AddAnnotationUseCase(private val repo: AnnotationRepository) {
    suspend operator fun invoke(
        documentId: String,
        pageIndex: Int,
        type: AnnotationType,
        text: String?,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        colorHex: String
    ) {
        require(right > left && bottom > top) { "Annotation bounds must be non-empty" }
        repo.addAnnotation(
            Annotation(
                id = UUID.randomUUID().toString(),
                documentId = documentId,
                pageIndex = pageIndex,
                type = type,
                text = text,
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                colorHex = colorHex,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }
}

class DeleteAnnotationUseCase(private val repo: AnnotationRepository) {
    suspend operator fun invoke(id: String) = repo.deleteAnnotation(id)
}
