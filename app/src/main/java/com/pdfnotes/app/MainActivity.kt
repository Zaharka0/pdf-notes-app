package com.pdfnotes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pdfnotes.app.di.ViewModelFactory
import com.pdfnotes.app.ui.navigation.AppNavHost
import com.pdfnotes.app.ui.theme.PdfNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as PdfNotesApplication).container
        val factory = ViewModelFactory(container)

        setContent {
            PdfNotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(factory = factory)
                }
            }
        }
    }
}
