package com.pdfnotes.app.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfnotes.app.data.local.pdf.PdfRendererWrapper
import com.pdfnotes.app.domain.model.Annotation
import com.pdfnotes.app.domain.model.AnnotationType
import com.pdfnotes.app.domain.usecase.AddAnnotationUseCase
import com.pdfnotes.app.domain.usecase.DeleteAnnotationUseCase
import com.pdfnotes.app.domain.usecase.ObserveAnnotationsUseCase
import com.pdfnotes.app.domain.usecase.OpenDocumentUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PdfViewerUiState(
    val documentId: String = "",
    val pageIndex: Int = 0,
    val pageCount: Int = 0,
    val pageBitmap: Bitmap? = null,
    val annotations: List<Annotation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class PdfViewerViewModel(
    private val appContext: Context,
    private val openDocument: OpenDocumentUseCase,
    private val observeAnnotations: ObserveAnnotationsUseCase,
    private val addAnnotation: AddAnnotationUseCase,
    private val deleteAnnotation: DeleteAnnotationUseCase
) : ViewModel() {

    private val renderer = PdfRendererWrapper(appContext)

    private val _state = MutableStateFlow(PdfViewerUiState())
    val state: StateFlow<PdfViewerUiState> = _state.asStateFlow()

    private var annotationsJob: kotlinx.coroutines.Job? = null

    fun open(documentId: String, targetWidthPx: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val document = openDocument(documentId)
            if (document == null) {
                _state.update { it.copy(isLoading = false, error = "Документ не найден") }
                return@launch
            }
            try {
                val pageCount = renderer.open(Uri.parse(document.uri))
                _state.update {
                    it.copy(documentId = documentId, pageCount = pageCount, isLoading = false)
                }
                loadPage(0, targetWidthPx)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadPage(pageIndex: Int, targetWidthPx: Int) {
        viewModelScope.launch {
            val bitmap = renderer.renderPage(pageIndex, targetWidthPx)
            _state.update { it.copy(pageIndex = pageIndex, pageBitmap = bitmap) }

            annotationsJob?.cancel()
            annotationsJob = viewModelScope.launch {
                observeAnnotations(_state.value.documentId, pageIndex).collect { list ->
                    _state.update { it.copy(annotations = list) }
                }
            }
        }
    }

    /** left/top/right/bottom are normalized 0f..1f relative to the page bitmap. */
    fun addHighlight(left: Float, top: Float, right: Float, bottom: Float, colorHex: String) {
        viewModelScope.launch {
            addAnnotation(
                documentId = _state.value.documentId,
                pageIndex = _state.value.pageIndex,
                type = AnnotationType.HIGHLIGHT,
                text = null,
                left = left, top = top, right = right, bottom = bottom,
                colorHex = colorHex
            )
        }
    }

    fun addNote(left: Float, top: Float, right: Float, bottom: Float, text: String) {
        viewModelScope.launch {
            addAnnotation(
                documentId = _state.value.documentId,
                pageIndex = _state.value.pageIndex,
                type = AnnotationType.NOTE,
                text = text,
                left = left, top = top, right = right, bottom = bottom,
                colorHex = "#FFF59D"
            )
        }
    }

    fun removeAnnotation(id: String) {
        viewModelScope.launch { deleteAnnotation(id) }
    }

    override fun onCleared() {
        renderer.close()
        super.onCleared()
    }
}
