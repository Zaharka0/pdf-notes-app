package com.pdfnotes.app.data.local.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thin wrapper over android.graphics.pdf.PdfRenderer, the framework API
 * available since API 21. It is view-only (no editing), which is why the
 * Data layer will later add a separate PdfEditEngine for the editing feature
 * (e.g. PdfBox-Android) without touching this class or the Domain layer.
 */
class PdfRendererWrapper(private val context: Context) {

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    suspend fun open(uri: Uri): Int = withContext(Dispatchers.IO) {
        close()
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: error("Could not open PDF at $uri")
        fileDescriptor = pfd
        renderer = PdfRenderer(pfd)
        renderer!!.pageCount
    }

    suspend fun renderPage(pageIndex: Int, targetWidthPx: Int): Bitmap = withContext(Dispatchers.IO) {
        val pdfRenderer = renderer ?: error("Call open() before renderPage()")
        pdfRenderer.openPage(pageIndex).use { page ->
            val scale = targetWidthPx.toFloat() / page.width
            val targetHeightPx = (page.height * scale).toInt()
            val bitmap = Bitmap.createBitmap(targetWidthPx, targetHeightPx, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        }
    }

    fun close() {
        renderer?.close()
        fileDescriptor?.close()
        renderer = null
        fileDescriptor = null
    }
}
