package com.pdfnotes.app.ui.documents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pdfnotes.app.domain.model.Document

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    viewModel: DocumentListViewModel,
    onOpenDocument: (Document) -> Unit
) {
    val documents by viewModel.documents.collectAsState()
    val context = LocalContext.current

    val pickPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = queryDisplayName(context, uri) ?: "Untitled.pdf"
            viewModel.onDocumentPicked(uri.toString(), name)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Документы") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { pickPdfLauncher.launch(arrayOf("application/pdf")) }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить PDF")
            }
        }
    ) { padding ->
        if (documents.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Нет файлов. Нажмите + чтобы добавить PDF.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(documents, key = { it.id }) { doc ->
                    ListItem(
                        headlineContent = { Text(doc.displayName) },
                        supportingContent = { Text("${doc.pageCount} стр. • ${doc.sizeBytes / 1024} КБ") },
                        modifier = Modifier.clickable { onOpenDocument(doc) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    cursor.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) return it.getString(nameIndex)
    }
    return null
}
