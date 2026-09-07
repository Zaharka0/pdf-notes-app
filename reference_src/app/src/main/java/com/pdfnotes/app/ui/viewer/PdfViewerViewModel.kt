package com.pdfnotes.app.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfnotes.app.data.local.pdf.PdfRendererWrapper
import com.pdfnotes.app.domain.model.Annotation
import com.pdfnotes.app.domain.model.AnnotationType
import com.pdfnotes.app.domain.usecase.*
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
            val document = runCatching { openDocument(documentId) }.getOrNull()
            if (document == null) {
                _state.update {
                    it.copy(isLoading = false, error = "Документ не найден или больше недоступен")
                }
                return@launch
            }
            try {
                val count = renderer.open(Uri.parse(document.uri))
                _state.update {
                    it.copy(documentId = documentId, pageCount = count, isLoading = false)
                }
                loadPage(0, targetWidthPx)
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Ошибка чтения PDF")
                }
            }
        }
    }

    fun loadPage(pageIndex: Int, targetWidthPx: Int) {
        viewModelScope.launch {
            try {
                val bitmap = renderer.renderPage(
                    pageIndex,
                    targetWidthPx.coerceIn(360, 1800)
                )
                _state.update { it.copy(pageIndex = pageIndex, pageBitmap = bitmap) }
                annotationsJob?.cancel()
                annotationsJob = viewModelScope.launch {
                    observeAnnotations(_state.value.documentId, pageIndex).collect { list ->
                        _state.update { it.copy(annotations = list) }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Не удалось отобразить страницу")
                }
            }
        }
    }

    fun addHighlight(l: Float, t: Float, r: Float, b: Float, c: String) =
        viewModelScope.launch {
            addAnnotation(
                _state.value.documentId,
                _state.value.pageIndex,
                AnnotationType.HIGHLIGHT,
                null,
                l, t, r, b, c
            )
        }

    fun addNote(l: Float, t: Float, r: Float, b: Float, text: String) =
        viewModelScope.launch {
            addAnnotation(
                _state.value.documentId,
                _state.value.pageIndex,
                AnnotationType.NOTE,
                text,
                l, t, r, b, "#FFF59D"
            )
        }

    fun removeAnnotation(id: String) = viewModelScope.launch { deleteAnnotation(id) }

    override fun onCleared() {
        annotationsJob?.cancel()
        renderer.close()
        super.onCleared()
    }
}
