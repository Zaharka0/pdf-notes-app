package com.pdfnotes.app.domain.repository

import com.pdfnotes.app.domain.model.Annotation
import kotlinx.coroutines.flow.Flow

interface AnnotationRepository {
    fun observeAnnotations(documentId: String, pageIndex: Int): Flow<List<Annotation>>
    suspend fun addAnnotation(annotation: Annotation)
    suspend fun deleteAnnotation(id: String)
}
