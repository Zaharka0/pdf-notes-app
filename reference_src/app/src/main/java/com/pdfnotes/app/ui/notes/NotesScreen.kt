package com.pdfnotes.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfnotes.app.domain.model.Document

@Composable
fun NotesScreen(documents: List<Document>) {
    var selected by remember { mutableStateOf<Document?>(documents.firstOrNull()) }
    LaunchedEffect(documents) { if (selected == null) selected = documents.firstOrNull() }
    Row(Modifier.fillMaxSize()) {
        Surface(Modifier.widthIn(min = 220.dp, max = 290.dp).fillMaxHeight(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.EditNote, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(9.dp)); Text("Конспекты", style = MaterialTheme.typography.titleLarge) }
                Spacer(Modifier.height(18.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(documents) { d ->
                        Card(onClick = { selected = d }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(if (selected?.id == d.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp)); Column { Text(d.displayName, maxLines = 1); Text("${d.pageCount} страниц", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                }
            }
        }
        Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Конспектирование", style = MaterialTheme.typography.headlineLarge); Text("Структурируйте важное из документа", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                FilledTonalIconButton(onClick = {}) { Icon(Icons.Default.Share, "Поделиться") }
                Spacer(Modifier.width(8.dp)); Button(onClick = {}) { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(7.dp)); Text("Сформировать") }
            }
            selected?.let { doc ->
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)), Alignment.Center) { Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary) }; Spacer(Modifier.width(12.dp)); Column { Text(doc.displayName, style = MaterialTheme.typography.titleLarge); Text("${doc.pageCount} страниц", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text("01  Введение", style = MaterialTheme.typography.headlineMedium)
                        Text("Основные идеи и определения из документа", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NoteLine("Искусственный интеллект — область компьютерных наук.")
                            NoteLine("Цель — создание систем, способных решать интеллектуальные задачи.")
                            NoteLine("Ключевые направления: обучение, рассуждение и анализ данных.")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AssistChip(onClick = {}, label = { Text("ИИ") }); AssistChip(onClick = {}, label = { Text("Машинное обучение") }); AssistChip(onClick = {}, label = { Text("Нейросети") }) }
                    }
                }
            } ?: EmptyNotes()
        }
    }
}

@Composable private fun NoteLine(text: String) { Row(verticalAlignment = Alignment.Top) { Box(Modifier.padding(top = 7.dp).size(6.dp).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)); Spacer(Modifier.width(10.dp)); Text(text, style = MaterialTheme.typography.bodyLarge) } }
@Composable private fun EmptyNotes() { Box(Modifier.fillMaxSize(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.EditNote, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(12.dp)); Text("Выберите PDF для конспекта", style = MaterialTheme.typography.titleLarge); Text("Добавьте документ на экране «Документы»", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
