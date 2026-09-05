package com.pdfnotes.app

import android.app.Application
import com.pdfnotes.app.di.AppContainer

class PdfNotesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
