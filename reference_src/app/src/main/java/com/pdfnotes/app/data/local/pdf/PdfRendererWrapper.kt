package com.pdfnotes.app.data.local.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

/**
 * PDF renderer that normalizes SAF/cloud-provider documents to a real local,
 * seekable file before handing them to Android's PdfRenderer.
 *
 * Some DocumentsProvider implementations return a pipe/non-seekable
 * ParcelFileDescriptor. PdfRenderer cannot reliably work with those FDs, even
 * though the PDF itself is perfectly valid. Copying the stream to app cache
 * makes opening PDFs consistent across local storage, Drive-like providers,
 * Downloads and other SAF sources.
 */
class PdfRendererWrapper(private val context: Context) {

    private var sourceFile: File? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    suspend fun open(uri: Uri): Int = withContext(Dispatchers.IO) {
        close()

        val cacheDir = File(context.cacheDir, "pdf-renderer")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val localFile = File.createTempFile("document-", ".pdf", cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                localFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("Could not read PDF at $uri")

            if (localFile.length() == 0L) {
                error("PDF file is empty")
            }

            val pfd = ParcelFileDescriptor.open(
                localFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            val pdfRenderer = PdfRenderer(pfd)

            sourceFile = localFile
            fileDescriptor = pfd
            renderer = pdfRenderer
            pdfRenderer.pageCount
        } catch (e: Exception) {
            localFile.delete()
            throw e
        }
    }

    suspend fun renderPage(pageIndex: Int, targetWidthPx: Int): Bitmap = withContext(Dispatchers.IO) {
        val pdfRenderer = renderer ?: error("Call open() before renderPage()")
        require(pageIndex in 0 until pdfRenderer.pageCount) { "Invalid PDF page index: $pageIndex" }

        pdfRenderer.openPage(pageIndex).use { page ->
            val width = targetWidthPx.coerceIn(360, 2400)
            val scale = width.toFloat() / page.width
            val height = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
        sourceFile?.delete()
        sourceFile = null
    }
}
