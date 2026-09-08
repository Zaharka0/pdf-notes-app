package com.pdfnotes.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ToolsScreen() {
    var message by remember { mutableStateOf<String?>(null) }
    val tools = listOf(
        Triple("Объединить PDF", "Собрать документы в один файл", Icons.Default.MergeType),
        Triple("Разделить PDF", "Выбрать нужные страницы", Icons.Default.ContentCut),
        Triple("Сжать PDF", "Уменьшить размер файла", Icons.Default.Compress),
        Triple("Защитить PDF", "Пароль и ограничения", Icons.Default.Lock),
        Triple("Водяной знак", "Добавить отметку на страницы", Icons.Default.TextFields),
        Triple("Печать", "Отправить документ на принтер", Icons.Default.Print)
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 26.dp)) {
        Text("Инструменты", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(5.dp)); Text("Всё необходимое для работы с PDF", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        LazyVerticalGrid(columns = GridCells.Adaptive(250.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(tools) { tool -> ToolCard(tool.first, tool.second, tool.third) { message = "Выберите документ, чтобы открыть «${tool.first.lowercase()}»." } }
        }
        message?.let { msg -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Text(msg, Modifier.weight(1f)); IconButton(onClick = { message = null }) { Icon(Icons.Default.Close, null) } } } }
    }
}

@Composable
private fun ToolCard(title: String, desc: String, icon: ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().height(148.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).padding(0.dp), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp)) }; Spacer(Modifier.weight(1f)); Icon(Icons.Default.ArrowOutward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.height(13.dp)); Text(title, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(3.dp)); Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
