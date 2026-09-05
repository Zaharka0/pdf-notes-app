package com.pdfnotes.app.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfnotes.app.domain.model.Document
import com.pdfnotes.app.domain.usecase.ImportDocumentUseCase
import com.pdfnotes.app.domain.usecase.ObserveDocumentsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentListViewModel(
    observeDocuments: ObserveDocumentsUseCase,
    private val importDocument: ImportDocumentUseCase
) : ViewModel() {

    val documents: StateFlow<List<Document>> = observeDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDocumentPicked(uri: String, displayName: String) {
        viewModelScope.launch {
            importDocument(uri, displayName)
        }
    }
}
