package com.pdfnotes.app.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun ToolsScreen(){var message by remember{mutableStateOf<String?>(null)};Column(Modifier.fillMaxSize().padding(30.dp)){Text("Инструменты",style=MaterialTheme.typography.headlineLarge);Text("Работа с PDF без лишних шагов",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(22.dp));Tool("Объединить PDF","Собрать несколько документов в один файл",Icons.Default.MergeType){message="Выберите PDF для объединения."};Tool("Разделить PDF","Разделить документ по страницам",Icons.Default.ContentCut){message="Выберите диапазон страниц."};Tool("Сжать PDF","Уменьшить размер файла",Icons.Default.Compress){message="Оптимизация PDF будет запущена после выбора файла."};Tool("Защитить PDF","Добавить пароль и ограничения",Icons.Default.Lock){message="Выберите пароль для документа."};Tool("Добавить водяной знак","Текстовая отметка на страницах",Icons.Default.TextFields){message="Введите текст водяного знака."};Tool("Печать","Отправить документ на печать",Icons.Default.Print){message="Выберите документ для печати."};message?.let{Spacer(Modifier.height(14.dp));Card(colors=CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)){Text(it,Modifier.padding(16.dp))}}}}
@Composable private fun Tool(title:String,desc:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){Card(onClick=onClick,Modifier.fillMaxWidth().padding(bottom=10.dp),colors=CardDefaults.cardColors(MaterialTheme.colorScheme.surface)){ListItem(headlineContent={Text(title)},supportingContent={Text(desc)},leadingContent={Icon(icon,null,tint=MaterialTheme.colorScheme.primary)},trailingContent={Icon(Icons.Default.ChevronRight,null)})}}
