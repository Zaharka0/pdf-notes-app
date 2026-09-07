package com.pdfnotes.app.ui.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pdfnotes.app.domain.model.AnnotationType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private enum class Tool { HIGHLIGHT, NOTE }

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
    var noteRect by remember { mutableStateOf<Rect?>(null) }
    var noteText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(false) }

    LaunchedEffect(documentId, canvasSize) {
        if (canvasSize.width > 0 && (state.documentId != documentId || state.pageBitmap == null)) {
            viewModel.open(documentId, canvasSize.width)
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        val screenWidth = maxWidth
        val compact = screenWidth < 600.dp
        val showComments = comments && screenWidth >= 720.dp
        val commentsWidth = (screenWidth * 0.32f).coerceAtMost(320.dp)

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("Назад") }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.pageCount > 0) "${state.pageIndex + 1} / ${state.pageCount}" else "PDF",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {}) { Icon(Icons.Default.Search, "Поиск") }
                IconButton(onClick = { selectedTool = Tool.HIGHLIGHT }) {
                    Icon(
                        Icons.Default.Brush,
                        "Выделение",
                        tint = if (selectedTool == Tool.HIGHLIGHT) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { selectedTool = Tool.NOTE }) {
                    Icon(
                        Icons.Default.EditNote,
                        "Заметка",
                        tint = if (selectedTool == Tool.NOTE) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { comments = !comments }) {
                    Icon(
                        Icons.Default.Comment,
                        "Комментарии",
                        tint = if (comments) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!compact) {
                    IconButton(onClick = {}) { Icon(Icons.Default.ZoomOut, "Уменьшить") }
                    IconButton(onClick = {}) { Icon(Icons.Default.ZoomIn, "Увеличить") }
                }
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Закрыть") }
            }
            HorizontalDivider()

            when {
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Card(Modifier.padding(24.dp)) {
                            Column(
                                Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("Не удалось открыть PDF", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    state.error.orEmpty(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = onBack) { Text("Вернуться к документам") }
                            }
                        }
                    }
                }
                state.isLoading || state.pageBitmap == null -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Row(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(if (compact) 6.dp else 14.dp),
                            Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .onSizeChanged { canvasSize = it },
                                Alignment.Center
                            ) {
                                state.pageBitmap?.let { bitmap ->
                                    Image(
                                        bitmap.asImageBitmap(),
                                        "Страница PDF",
                                        Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Canvas(Modifier.fillMaxSize()) {
                                    state.annotations.forEach { annotation ->
                                        val rect = Rect(
                                            annotation.left * size.width,
                                            annotation.top * size.height,
                                            annotation.right * size.width,
                                            annotation.bottom * size.height
                                        )
                                        val color = runCatching {
                                            Color(android.graphics.Color.parseColor(annotation.colorHex))
                                        }.getOrDefault(Color.Yellow)
                                        drawRect(
                                            color.copy(
                                                alpha = if (annotation.type == AnnotationType.HIGHLIGHT) 0.35f else 0.65f
                                            ),
                                            rect.topLeft,
                                            rect.size
                                        )
                                    }
                                    dragStart?.let { start ->
                                        dragCurrent?.let { end ->
                                            drawRect(
                                                Color(0x553B82F6),
                                                Offset(min(start.x, end.x), min(start.y, end.y)),
                                                androidx.compose.ui.geometry.Size(
                                                    abs(end.x - start.x),
                                                    abs(end.y - start.y)
                                                )
                                            )
                                        }
                                    }
                                }
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .pointerInput(selectedTool, canvasSize) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    dragStart = it
                                                    dragCurrent = it
                                                },
                                                onDrag = { change, _ -> dragCurrent = change.position },
                                                onDragEnd = {
                                                    val start = dragStart
                                                    val end = dragCurrent
                                                    if (start != null && end != null && canvasSize.width > 0 && canvasSize.height > 0) {
                                                        val rect = Rect(
                                                            min(start.x, end.x) / canvasSize.width,
                                                            min(start.y, end.y) / canvasSize.height,
                                                            max(start.x, end.x) / canvasSize.width,
                                                            max(start.y, end.y) / canvasSize.height
                                                        )
                                                        if (rect.width > 0.01f && rect.height > 0.01f) {
                                                            if (selectedTool == Tool.HIGHLIGHT) {
                                                                viewModel.addHighlight(
                                                                    rect.left,
                                                                    rect.top,
                                                                    rect.right,
                                                                    rect.bottom,
                                                                    "#FFEB3B"
                                                                )
                                                            } else {
                                                                noteRect = rect
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

                        if (showComments) {
                            Surface(
                                Modifier.width(commentsWidth).fillMaxHeight(),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(Modifier.fillMaxSize().padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Комментарии", style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.weight(1f))
                                        IconButton(onClick = { comments = false }) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    }
                                    Text(
                                        "Комментарии к странице",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Comment("Иван П.", "Отличное определение!", "10:30")
                                    Spacer(Modifier.height(12.dp))
                                    Comment("Мария С.", "Добавить больше примеров в этот раздел", "11:15")
                                    Spacer(Modifier.weight(1f))
                                    OutlinedTextField(
                                        value = "",
                                        onValueChange = {},
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Добавить комментарий...") },
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    noteRect?.let { rect ->
        AlertDialog(
            onDismissRequest = { noteRect = null; noteText = "" },
            title = { Text("Новая заметка") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Текст заметки...") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            viewModel.addNote(
                                rect.left,
                                rect.top,
                                rect.right,
                                rect.bottom,
                                noteText
                            )
                        }
                        noteRect = null
                        noteText = ""
                    }
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { noteRect = null; noteText = "" }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun Comment(author: String, text: String, time: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row {
            Text(author, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.weight(1f))
            Text(time, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Text(text)
    }
}
