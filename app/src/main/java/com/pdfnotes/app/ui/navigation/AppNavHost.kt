package com.pdfnotes.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pdfnotes.app.di.ViewModelFactory
import com.pdfnotes.app.ui.documents.DocumentListScreen
import com.pdfnotes.app.ui.documents.DocumentListViewModel
import com.pdfnotes.app.ui.viewer.PdfViewerScreen
import com.pdfnotes.app.ui.viewer.PdfViewerViewModel

private object Routes {
    const val DOCUMENT_LIST = "documents"
    const val VIEWER = "viewer/{documentId}"
    fun viewer(documentId: String) = "viewer/$documentId"
}

@Composable
fun AppNavHost(factory: ViewModelFactory, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.DOCUMENT_LIST) {
        composable(Routes.DOCUMENT_LIST) {
            val vm: DocumentListViewModel = viewModel(factory = factory)
            DocumentListScreen(
                viewModel = vm,
                onOpenDocument = { doc -> navController.navigate(Routes.viewer(doc.id)) }
            )
        }
        composable(Routes.VIEWER) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: return@composable
            val vm: PdfViewerViewModel = viewModel(factory = factory)
            PdfViewerScreen(
                documentId = documentId,
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
