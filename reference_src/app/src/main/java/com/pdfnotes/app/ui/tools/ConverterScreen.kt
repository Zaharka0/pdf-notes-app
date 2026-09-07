package com.pdfnotes.app.ui.tools

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pdfnotes.app.data.local.pdf.PdfRendererWrapper
import com.pdfnotes.app.di.ViewModelFactory
import com.pdfnotes.app.domain.model.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable fun ConverterScreen(documents:List<Document>,factory:ViewModelFactory){val context=androidx.compose.ui.platform.LocalContext.current;val scope=rememberCoroutineScope();var selected by remember{mutableStateOf(documents.firstOrNull())};var status by remember{mutableStateOf<String?>(null)};Column(Modifier.fillMaxSize().padding(28.dp)){Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Конвертер",style=MaterialTheme.typography.headlineLarge);Text("Преобразуйте PDF в нужный формат",color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick={}){Icon(Icons.Default.Close,null)}};Spacer(Modifier.height(20.dp));Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(24.dp)){Card(Modifier.width(330.dp),colors=CardDefaults.cardColors(MaterialTheme.colorScheme.surface)){Column(Modifier.padding(18.dp)){Text("Документ.pdf",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(12.dp));documents.forEach{d->ListItem(headlineContent={Text(d.displayName,maxLines=1)},supportingContent={Text("${d.sizeBytes/1024/1024} МБ • ${d.pageCount} стр.")},leadingContent={Icon(Icons.Default.PictureAsPdf,null,tint=Color(0xFFFF5C65))},trailingContent={RadioButton(selected=selected?.id==d.id,onClick={selected=d})},colors=ListItemDefaults.colors(containerColor=if(selected?.id==d.id)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface))}}};Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Выберите формат",style=MaterialTheme.typography.headlineMedium);FormatCard("Word (.docx)",Icons.Default.Description){status="Word экспорт будет доступен в следующей версии."};FormatCard("Excel (.xlsx)",Icons.Default.TableChart){status="Excel экспорт будет доступен в следующей версии."};FormatCard("Изображения (.jpg)",Icons.Default.Image){selected?.let{d->scope.launch{status=convertPdfToJpg(context,d)}}};FormatCard("Текст (.txt)",Icons.Default.TextSnippet){status="Текстовый экспорт будет доступен в следующей версии."};status?.let{Text(it,color=MaterialTheme.colorScheme.primary)};Button(onClick={},Modifier.fillMaxWidth().height(48.dp),enabled=selected!=null){Text("Конвертировать")}}}}}
@Composable private fun FormatCard(title:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){Card(onClick=onClick,Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)){ListItem(headlineContent={Text(title)},leadingContent={Icon(icon,null,tint=MaterialTheme.colorScheme.primary)},trailingContent={Icon(Icons.Default.ChevronRight,null)},colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceVariant))}}
private suspend fun convertPdfToJpg(context:android.content.Context,doc:Document):String=withContext(Dispatchers.IO){val r=PdfRendererWrapper(context);try{val count=r.open(android.net.Uri.parse(doc.uri));var saved=0;for(page in 0 until count){val b=r.renderPage(page,1600);val v=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"${doc.displayName.substringBeforeLast('.')}_${page+1}.jpg");put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/PDF Notes")};val u=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(u!=null)context.contentResolver.openOutputStream(u)?.use{b.compress(Bitmap.CompressFormat.JPEG,92,it);saved++}};"Готово: $saved изображений"}catch(e:Exception){"Ошибка: ${e.message}"}finally{r.close()}}
