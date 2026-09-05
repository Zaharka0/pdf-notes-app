package com.pdfnotes.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pdfnotes.app.ui.documents.DocumentListViewModel
import com.pdfnotes.app.ui.viewer.PdfViewerViewModel

/**
 * Small factory bridging the manual AppContainer to Jetpack ViewModels.
 * Add a branch here whenever a new ViewModel needs constructor dependencies.
 */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        DocumentListViewModel::class.java -> DocumentListViewModel(
            container.observeDocuments,
            container.importDocument
        ) as T

        PdfViewerViewModel::class.java -> PdfViewerViewModel(
            container.appContext,
            container.openDocument,
            container.observeAnnotations,
            container.addAnnotation,
            container.deleteAnnotation
        ) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
