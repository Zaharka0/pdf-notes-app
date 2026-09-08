package com.pdfnotes.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.pdfnotes.app.di.ViewModelFactory
import com.pdfnotes.app.ui.documents.*
import com.pdfnotes.app.ui.notes.NotesScreen
import com.pdfnotes.app.ui.tools.*
import com.pdfnotes.app.ui.viewer.*

private object Routes {
    const val DOCUMENTS = "documents"
    const val NOTES = "notes"
    const val CONVERTER = "converter"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"
    const val VIEWER = "viewer/{documentId}"
    fun viewer(id: String) = "viewer/$id"
}

data class NavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

@Composable
fun AppNavHost(factory: ViewModelFactory, navController: NavHostController = rememberNavController()) {
    val route = navController.currentBackStackEntryAsState().value?.destination?.route ?: Routes.DOCUMENTS
    if (route.startsWith("viewer")) {
        NavHost(navController, Routes.VIEWER, Modifier.fillMaxSize()) {
            composable(Routes.VIEWER) { entry ->
                val id = entry.arguments?.getString("documentId") ?: return@composable
                PdfViewerScreen(id, viewModel(factory = factory)) { navController.popBackStack() }
            }
        }
        return
    }

    val items = listOf(
        NavItem(Routes.DOCUMENTS, Icons.Default.Description, "Документы"),
        NavItem("recent", Icons.Default.History, "Недавние"),
        NavItem("favorites", Icons.Default.StarBorder, "Избранное"),
        NavItem(Routes.NOTES, Icons.Default.EditNote, "Заметки"),
        NavItem(Routes.CONVERTER, Icons.Default.SwapHoriz, "Конвертер"),
        NavItem(Routes.TOOLS, Icons.Default.Build, "Инструменты")
    )

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val width = maxWidth
        if (width < 600.dp) {
            Column(Modifier.fillMaxSize()) {
                MainContent(factory, navController, Modifier.weight(1f).fillMaxWidth())
                PhoneNavigation(items, route, navController)
            }
        } else {
            val expanded = width >= 900.dp
            val railWidth = if (expanded) 224.dp else 88.dp
            Row(Modifier.fillMaxSize()) {
                Surface(Modifier.width(railWidth).fillMaxHeight(), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.fillMaxSize()) {
                        if (expanded) {
                            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                BrandMark()
                                Spacer(Modifier.width(10.dp))
                                Text("PDF Notes", style = MaterialTheme.typography.titleLarge)
                            }
                        } else {
                            Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), Alignment.Center) { BrandMark() }
                        }
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            items.forEach { RailItem(it, route, navController, expanded) }
                            Spacer(Modifier.height(8.dp))
                            RailItem(NavItem("trash", Icons.Default.DeleteOutline, "Корзина"), route, navController, expanded)
                            RailItem(NavItem(Routes.SETTINGS, Icons.Default.Settings, "Настройки"), route, navController, expanded)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                MainContent(factory, navController, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable private fun BrandMark() {
    Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), Alignment.Center) {
        Text("P", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PhoneNavigation(items: List<NavItem>, route: String, navController: NavHostController) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, tonalElevation = 5.dp) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            (items + NavItem(Routes.SETTINGS, Icons.Default.Settings, "Настройки")).forEach { item ->
                Box(Modifier.width(78.dp).height(62.dp), Alignment.Center) {
                    TextButton(
                        onClick = {
                            if (item.route != "recent" && item.route != "favorites" && item.route != "trash")
                                navController.navigate(item.route) { launchSingleTop = true }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(item.icon, item.label, tint = if (route == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(item.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainContent(factory: ViewModelFactory, navController: NavHostController, modifier: Modifier) {
    NavHost(navController, Routes.DOCUMENTS, modifier) {
        composable(Routes.DOCUMENTS) {
            val vm: DocumentListViewModel = viewModel(factory = factory)
            DocumentListScreen(vm) { navController.navigate(Routes.viewer(it.id)) }
        }
        composable(Routes.NOTES) {
            val vm: DocumentListViewModel = viewModel(factory = factory)
            NotesScreen(vm.documents.collectAsState().value)
        }
        composable(Routes.CONVERTER) {
            val vm: DocumentListViewModel = viewModel(factory = factory)
            ConverterScreen(vm.documents.collectAsState().value, factory)
        }
        composable(Routes.TOOLS) { ToolsScreen() }
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}

@Composable
private fun RailItem(item: NavItem, route: String, navController: NavHostController, expanded: Boolean) {
    NavigationRailItem(
        selected = route == item.route,
        onClick = { if (item.route != "trash" && item.route != "recent" && item.route != "favorites") navController.navigate(item.route) { launchSingleTop = true } },
        icon = { Icon(item.icon, item.label) },
        label = { if (expanded) Text(item.label, maxLines = 1) },
        alwaysShowLabel = expanded,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 1.dp)
    )
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Настройки", style = MaterialTheme.typography.headlineLarge)
        Text("Параметры PDF Notes", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(20.dp)) {
                Text("Внешний вид", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Интерфейс автоматически адаптируется под телефон, планшет и большой экран.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ListItem(headlineContent = { Text("Хранение") }, supportingContent = { Text("Файлы и заметки остаются на устройстве") }, leadingContent = { Icon(Icons.Default.Storage, null) })
        ListItem(headlineContent = { Text("Версия") }, supportingContent = { Text("PDF Notes 1.3") }, leadingContent = { Icon(Icons.Default.Info, null) })
    }
}
