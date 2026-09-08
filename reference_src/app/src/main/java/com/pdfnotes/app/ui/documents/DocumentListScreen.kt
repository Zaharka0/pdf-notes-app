package com.pdfnotes.app.ui.documents

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pdfnotes.app.domain.model.Document

@Composable
fun DocumentListScreen(viewModel: DocumentListViewModel, onOpenDocument: (Document) -> Unit) {
    val documents by viewModel.documents.collectAsState()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var grid by remember { mutableStateOf(true) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) viewModel.onDocumentPicked(uri.toString(), queryDisplayName(context, uri) ?: "Документ.pdf")
    }
    val filtered = documents.filter { it.displayName.contains(query, true) }
    val addPdf = { picker.launch(arrayOf("application/pdf")) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 26.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Добрый день 👋", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text("Мои документы", style = MaterialTheme.typography.headlineLarge)
            }
            FilledTonalIconButton(onClick = {}, modifier = Modifier.size(46.dp)) { Icon(Icons.Default.NotificationsNone, "Уведомления") }
            Spacer(Modifier.width(10.dp))
            Button(onClick = addPdf, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(7.dp)); Text("Импорт PDF")
            }
        }
        Spacer(Modifier.height(22.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Работайте с PDF быстрее", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text("Открывайте, отмечайте страницы и создавайте конспекты в одном месте.", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
                TextButton(onClick = addPdf) { Text("Начать") }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Недавние документы", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { grid = true }) { Icon(Icons.Default.GridView, "Сетка", tint = if (grid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = { grid = false }) { Icon(Icons.Default.ViewList, "Список", tint = if (!grid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().height(54.dp), singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Поиск по документам") },
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(18.dp))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(360.dp), Alignment.Center) { EmptyState(addPdf) }
        } else if (grid) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(190.dp), modifier = Modifier.heightIn(min = 300.dp, max = 700.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) { items(filtered, key = { it.id }) { DocumentCard(it, onOpenDocument) } }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { filtered.forEach { DocumentRow(it, onOpenDocument) } }
        }
    }
}

@Composable
private fun DocumentCard(doc: Document, onOpen: (Document) -> Unit) {
    Card(
        onClick = { onOpen(doc) }, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(10.dp)) {
            Box(Modifier.fillMaxWidth().height(205.dp).clip(RoundedCornerShape(14.dp)).background(Brush.verticalGradient(listOf(Color(0xFF272A3A), Color(0xFF141620)))), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PictureAsPdf, null, Modifier.size(58.dp), tint = Color(0xFFFF6D79))
                    Spacer(Modifier.height(5.dp))
                    Text("PDF", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Surface(Modifier.align(Alignment.TopStart).padding(10.dp), shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = .10f)) {
                    Text("PDF", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
                Surface(Modifier.align(Alignment.BottomEnd).padding(10.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .88f)) {
                    Icon(Icons.Default.OpenInNew, null, Modifier.padding(7.dp).size(16.dp), tint = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(doc.displayName, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(5.dp))
            Text("${doc.pageCount} стр.  •  ${formatBytes(doc.sizeBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DocumentRow(doc: Document, onOpen: (Document) -> Unit) {
    Card(onClick = { onOpen(doc) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Icon(Icons.Default.PictureAsPdf, null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Text(doc.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium); Text("${doc.pageCount} страниц • ${formatBytes(doc.sizeBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(76.dp).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Icon(Icons.Default.Description, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(14.dp)); Text("Здесь пока пусто", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp)); Text("Импортируйте первый PDF-файл", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp)); Button(onClick = onAdd) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Импортировать PDF") }
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? { val p = context.contentResolver.query(uri, arrayOf("_display_name"), null, null, null) ?: return null; return p.use { if (it.moveToFirst()) it.getString(0) else null } }
private fun formatBytes(bytes: Long): String = when { bytes >= 1024 * 1024 -> "%.1f МБ".format(bytes / 1024f / 1024f); bytes >= 1024 -> "%.0f КБ".format(bytes / 1024f); else -> "$bytes Б" }
