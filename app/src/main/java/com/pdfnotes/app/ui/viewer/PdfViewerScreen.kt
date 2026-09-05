package com.pdfnotes.app.ui.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pdfnotes.app.domain.model.AnnotationType

private enum class Tool { HIGHLIGHT, NOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    documentId: String,
    viewModel: PdfViewerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedTool by remember { mutableStateOf(Tool.HIGHLIGHT) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }
    var pendingNoteRect by remember { mutableStateOf<Rect?>(null) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(documentId, canvasSize) {
        if (canvasSize.width > 0) {
            if (state.documentId != documentId) viewModel.open(documentId, canvasSize.width)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Стр. ${state.pageIndex + 1} / ${state.pageCount}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    FilterChip(
                        selected = selectedTool == Tool.HIGHLIGHT,
                        onClick = { selectedTool = Tool.HIGHLIGHT },
                        label = { Text("Хайлайт") }
                    )
                    Spacer(Modifier.width(4.dp))
                    FilterChip(
                        selected = selectedTool == Tool.NOTE,
                        onClick = { selectedTool = Tool.NOTE },
                        label = { Text("Заметка") }
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = {
                        if (state.pageIndex > 0) viewModel.loadPage(state.pageIndex - 1, canvasSize.width)
                    }
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущая") }
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        if (state.pageIndex < state.pageCount - 1) viewModel.loadPage(state.pageIndex + 1, canvasSize.width)
                    }
                ) { Icon(Icons.Default.ChevronRight, contentDescription = "Следующая") }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .onSizeChanged { canvasSize = it }
        ) {
            state.pageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Страница ${state.pageIndex + 1}",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Draw existing annotations, scaled from normalized coords to canvas pixels.
            Canvas(Modifier.fillMaxSize()) {
                state.annotations.forEach { ann ->
                    val rect = Rect(
                        left = ann.left * size.width,
                        top = ann.top * size.height,
                        right = ann.right * size.width,
                        bottom = ann.bottom * size.height
                    )
                    val color = runCatching { Color(android.graphics.Color.parseColor(ann.colorHex)) }
                        .getOrDefault(Color.Yellow)
                    drawRect(
                        color = color.copy(alpha = if (ann.type == AnnotationType.HIGHLIGHT) 0.4f else 0.7f),
                        topLeft = rect.topLeft,
                        size = rect.size
                    )
                }
                dragStart?.let { start ->
                    dragCurrent?.let { current ->
                        drawRect(
                            color = Color.Cyan.copy(alpha = 0.3f),
                            topLeft = Offset(minOf(start.x, current.x), minOf(start.y, current.y)),
                            size = androidx.compose.ui.geometry.Size(
                                kotlin.math.abs(current.x - start.x),
                                kotlin.math.abs(current.y - start.y)
                            )
                        )
                    }
                }
            }

            // Gesture layer: drag to select a region for either tool.
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTool, canvasSize) {
                        detectDragGestures(
                            onDragStart = { offset -> dragStart = offset; dragCurrent = offset },
                            onDrag = { change, _ -> dragCurrent = change.position },
                            onDragEnd = {
                                val start = dragStart
                                val end = dragCurrent
                                if (start != null && end != null && canvasSize.width > 0 && canvasSize.height > 0) {
                                    val rect = Rect(
                                        left = minOf(start.x, end.x) / canvasSize.width,
                                        top = minOf(start.y, end.y) / canvasSize.height,
                                        right = maxOf(start.x, end.x) / canvasSize.width,
                                        bottom = maxOf(start.y, end.y) / canvasSize.height
                                    )
                                    if (rect.width > 0.01f && rect.height > 0.01f) {
                                        when (selectedTool) {
                                            Tool.HIGHLIGHT -> viewModel.addHighlight(
                                                rect.left, rect.top, rect.right, rect.bottom, "#FFEB3B"
                                            )
                                            Tool.NOTE -> pendingNoteRect = rect
                                        }
                                    }
                                }
                                dragStart = null
                                dragCurrent = null
                            }
                        )
                    }
            )
        }
    }

    pendingNoteRect?.let { rect ->
        AlertDialog(
            onDismissRequest = { pendingNoteRect = null; noteText = "" },
            title = { Text("Новая заметка") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Текст заметки...") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (noteText.isNotBlank()) {
                        viewModel.addNote(rect.left, rect.top, rect.right, rect.bottom, noteText)
                    }
                    pendingNoteRect = null
                    noteText = ""
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { pendingNoteRect = null; noteText = "" }) { Text("Отмена") }
            }
        )
    }
}
