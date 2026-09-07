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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pdfnotes.app.di.ViewModelFactory
import com.pdfnotes.app.ui.documents.DocumentListScreen
import com.pdfnotes.app.ui.documents.DocumentListViewModel
import com.pdfnotes.app.ui.notes.NotesScreen
import com.pdfnotes.app.ui.tools.ConverterScreen
import com.pdfnotes.app.ui.tools.ToolsScreen
import com.pdfnotes.app.ui.viewer.PdfViewerScreen
import com.pdfnotes.app.ui.viewer.PdfViewerViewModel

private object Routes {
    const val DOCUMENTS = "documents"
    const val NOTES = "notes"
    const val CONVERTER = "converter"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"
    const val VIEWER = "viewer/{documentId}"
    fun viewer(id: String) = "viewer/$id"
}

data class NavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

@Composable
fun AppNavHost(
    factory: ViewModelFactory,
    navController: NavHostController = rememberNavController()
) {
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: Routes.DOCUMENTS

    if (route.startsWith("viewer")) {
        NavHost(
            navController = navController,
            startDestination = Routes.VIEWER,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.VIEWER) { entry ->
                val id = entry.arguments?.getString("documentId") ?: return@composable
                val vm: PdfViewerViewModel = viewModel(factory = factory)
                PdfViewerScreen(id, vm) { navController.popBackStack() }
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        val screenWidth = maxWidth
        val compact = screenWidth < 600.dp

        if (compact) {
            Column(Modifier.fillMaxSize()) {
                MainContent(factory, navController, Modifier.weight(1f).fillMaxWidth())
                CompactBottomBar(items, route, navController)
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                val railWidth = if (screenWidth < 840.dp) 88.dp else 220.dp
                val showLabels = railWidth > 100.dp
                NavigationRail(
                    modifier = Modifier.fillMaxHeight().width(railWidth),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        if (showLabels) {
                            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        Alignment.Center
                                    ) { Text("P", fontWeight = FontWeight.Bold) }
                                    Spacer(Modifier.width(10.dp))
                                    Text("PDF Notes", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), Alignment.Center) {
                                Box(
                                    Modifier.size(38.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    Alignment.Center
                                ) { Text("P", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                ) {
                    items.forEach { item -> RailItem(item, route, navController, showLabels) }
                    Spacer(Modifier.weight(1f))
                    RailItem(NavItem("trash", Icons.Default.DeleteOutline, "Корзина"), route, navController, showLabels)
                    RailItem(NavItem(Routes.SETTINGS, Icons.Default.Settings, "Настройки"), route, navController, showLabels)
                }
                MainContent(factory, navController, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun CompactBottomBar(
    items: List<NavItem>,
    route: String,
    navController: NavHostController
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.take(4).forEach { item ->
            BottomItem(item, route, navController, Modifier.weight(1f))
        }
        BottomItem(
            NavItem(Routes.SETTINGS, Icons.Default.Settings, "Настройки"),
            route,
            navController,
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun MainContent(
    factory: ViewModelFactory,
    navController: NavHostController,
    modifier: Modifier
) {
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
private fun RailItem(
    item: NavItem,
    route: String,
    navController: NavHostController,
    showLabel: Boolean
) {
    NavigationRailItem(
        selected = route == item.route,
        onClick = {
            if (item.route != "trash" && item.route != "recent" && item.route != "favorites") {
                navController.navigate(item.route) { launchSingleTop = true }
            }
        },
        icon = { Icon(item.icon, null) },
        label = { if (showLabel) Text(item.label) },
        alwaysShowLabel = showLabel,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

@Composable
private fun BottomItem(
    item: NavItem,
    route: String,
    navController: NavHostController,
    modifier: Modifier
) {
    Box(modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        TextButton(onClick = { navController.navigate(item.route) { launchSingleTop = true } }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    item.icon,
                    contentDescription = item.label,
                    tint = if (route == item.route) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (route == item.route) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineLarge)
        Text("Параметры PDF Notes", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Внешний вид", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Интерфейс автоматически адаптируется под телефон, планшет и большой экран.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ListItem(
            headlineContent = { Text("Хранение") },
            supportingContent = { Text("Файлы и заметки остаются на устройстве") },
            leadingContent = { Icon(Icons.Default.Storage, null) }
        )
        ListItem(
            headlineContent = { Text("Версия") },
            supportingContent = { Text("PDF Notes 1.1") },
            leadingContent = { Icon(Icons.Default.Info, null) }
        )
    }
}
