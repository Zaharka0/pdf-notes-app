package com.pdfnotes.app.data.repository

import com.pdfnotes.app.data.local.db.AnnotationDao
import com.pdfnotes.app.data.local.db.AnnotationEntity
import com.pdfnotes.app.domain.model.Annotation
import com.pdfnotes.app.domain.model.AnnotationType
import com.pdfnotes.app.domain.repository.AnnotationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnnotationRepositoryImpl(
    private val dao: AnnotationDao
) : AnnotationRepository {

    override fun observeAnnotations(documentId: String, pageIndex: Int): Flow<List<Annotation>> =
        dao.observeForPage(documentId, pageIndex).map { list -> list.map { it.toDomain() } }

    override suspend fun addAnnotation(annotation: Annotation) =
        dao.insert(annotation.toEntity())

    override suspend fun deleteAnnotation(id: String) = dao.delete(id)

    private fun AnnotationEntity.toDomain() = Annotation(
        id = id,
        documentId = documentId,
        pageIndex = pageIndex,
        type = AnnotationType.valueOf(type),
        text = text,
        left = left, top = top, right = right, bottom = bottom,
        colorHex = colorHex,
        createdAtEpochMs = createdAtEpochMs
    )

    private fun Annotation.toEntity() = AnnotationEntity(
        id = id,
        documentId = documentId,
        pageIndex = pageIndex,
        type = type.name,
        text = text,
        left = left, top = top, right = right, bottom = bottom,
        colorHex = colorHex,
        createdAtEpochMs = createdAtEpochMs
    )
}
