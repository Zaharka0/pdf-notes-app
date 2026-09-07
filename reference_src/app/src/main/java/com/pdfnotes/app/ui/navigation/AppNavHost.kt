package com.pdfnotes.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.pdfnotes.app.di.ViewModelFactory
import com.pdfnotes.app.ui.documents.DocumentListScreen
import com.pdfnotes.app.ui.documents.DocumentListViewModel
import com.pdfnotes.app.ui.notes.NotesScreen
import com.pdfnotes.app.ui.tools.ConverterScreen
import com.pdfnotes.app.ui.tools.ToolsScreen
import com.pdfnotes.app.ui.viewer.PdfViewerScreen
import com.pdfnotes.app.ui.viewer.PdfViewerViewModel

private object Routes { const val DOCUMENTS="documents"; const val NOTES="notes"; const val CONVERTER="converter"; const val TOOLS="tools"; const val SETTINGS="settings"; const val VIEWER="viewer/{documentId}"; fun viewer(id:String)="viewer/$id" }

@Composable fun AppNavHost(factory: ViewModelFactory, navController: NavHostController = rememberNavController()) {
    val route=navController.currentBackStackEntryAsState().value?.destination?.route ?: Routes.DOCUMENTS
    if(route.startsWith("viewer")){ NavHost(navController,Routes.VIEWER,Modifier.fillMaxSize()){ composable(Routes.VIEWER){ e -> val id=e.arguments?.getString("documentId") ?: return@composable; val vm:PdfViewerViewModel=viewModel(factory=factory); PdfViewerScreen(id,vm){navController.popBackStack()} } }; return }
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavigationRail(modifier=Modifier.fillMaxHeight().width(204.dp),containerColor=MaterialTheme.colorScheme.surface,header={ Column(Modifier.fillMaxWidth().padding(18.dp),horizontalAlignment=Alignment.Start){ Row(verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),Alignment.Center){Text("P",fontWeight=FontWeight.Bold)};Spacer(Modifier.width(10.dp));Text("PDF Notes",style=MaterialTheme.typography.titleLarge)};Spacer(Modifier.height(26.dp)) } }) {
            RailItem(route==Routes.DOCUMENTS,Icons.Default.Description,"Документы"){navController.navigate(Routes.DOCUMENTS){launchSingleTop=true}}
            RailItem(false,Icons.Default.History,"Недавние"){}; RailItem(false,Icons.Default.StarBorder,"Избранное"){}
            RailItem(route==Routes.NOTES,Icons.Default.EditNote,"Заметки"){navController.navigate(Routes.NOTES){launchSingleTop=true}}
            RailItem(route==Routes.CONVERTER,Icons.Default.SwapHoriz,"Конвертер"){navController.navigate(Routes.CONVERTER){launchSingleTop=true}}
            RailItem(route==Routes.TOOLS,Icons.Default.Build,"Инструменты"){navController.navigate(Routes.TOOLS){launchSingleTop=true}}
            Spacer(Modifier.weight(1f)); RailItem(false,Icons.Default.DeleteOutline,"Корзина"){}; RailItem(route==Routes.SETTINGS,Icons.Default.Settings,"Настройки"){navController.navigate(Routes.SETTINGS){launchSingleTop=true}}
            Spacer(Modifier.height(12.dp))
        }
        NavHost(navController,Routes.DOCUMENTS,Modifier.weight(1f).fillMaxHeight()) {
            composable(Routes.DOCUMENTS){val vm:DocumentListViewModel=viewModel(factory=factory);DocumentListScreen(vm){navController.navigate(Routes.viewer(it.id))}}
            composable(Routes.NOTES){val vm:DocumentListViewModel=viewModel(factory=factory);NotesScreen(vm.documents.collectAsState().value)}
            composable(Routes.CONVERTER){val vm:DocumentListViewModel=viewModel(factory=factory);ConverterScreen(vm.documents.collectAsState().value,factory)}
            composable(Routes.TOOLS){ToolsScreen()}; composable(Routes.SETTINGS){SettingsScreen()}
        }
    }
}

@Composable private fun RailItem(selected:Boolean,icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,onClick:()->Unit){NavigationRailItem(selected=selected,onClick=onClick,icon={Icon(icon,null)},label={Text(label)},alwaysShowLabel=true,modifier=Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=2.dp))}
@Composable private fun SettingsScreen(){Column(Modifier.fillMaxSize().padding(34.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){Text("Настройки",style=MaterialTheme.typography.headlineLarge);Text("Параметры PDF Notes",color=MaterialTheme.colorScheme.onSurfaceVariant);Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)){Column(Modifier.padding(20.dp)){Text("Внешний вид",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(8.dp));Text("Тёмная тема • интерфейс оптимизирован для планшета",color=MaterialTheme.colorScheme.onSurfaceVariant)}};ListItem(headlineContent={Text("Хранение")},supportingContent={Text("Файлы и заметки остаются на устройстве")},leadingContent={Icon(Icons.Default.Storage,null)});ListItem(headlineContent={Text("Версия")},supportingContent={Text("PDF Notes 1.0")},leadingContent={Icon(Icons.Default.Info,null)})}}
