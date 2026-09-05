package com.pdfnotes.app.di

import android.content.Context
import com.pdfnotes.app.data.local.db.AppDatabase
import com.pdfnotes.app.data.repository.AnnotationRepositoryImpl
import com.pdfnotes.app.data.repository.DocumentRepositoryImpl
import com.pdfnotes.app.domain.repository.AnnotationRepository
import com.pdfnotes.app.domain.repository.DocumentRepository
import com.pdfnotes.app.domain.usecase.*

/**
 * Minimal hand-rolled DI graph. It's intentionally simple for the MVP skeleton
 * so the whole wiring is readable in one file; swap for Hilt/Koin once the
 * dependency graph grows (e.g. when cloud sync or the AI repo are added).
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    private val documentRepository: DocumentRepository =
        DocumentRepositoryImpl(context, database.documentDao())

    private val annotationRepository: AnnotationRepository =
        AnnotationRepositoryImpl(database.annotationDao())

    val observeDocuments = ObserveDocumentsUseCase(documentRepository)
    val importDocument = ImportDocumentUseCase(documentRepository)
    val openDocument = OpenDocumentUseCase(documentRepository)
    val observeAnnotations = ObserveAnnotationsUseCase(annotationRepository)
    val addAnnotation = AddAnnotationUseCase(annotationRepository)
    val deleteAnnotation = DeleteAnnotationUseCase(annotationRepository)

    val appContext: Context = context.applicationContext
}
