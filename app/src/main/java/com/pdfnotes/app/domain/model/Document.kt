package com.pdfnotes.app.domain.model

/**
 * A PDF document known to the app. [uri] is a content:// or file:// URI
 * obtained via the Storage Access Framework - the app never assumes a raw
 * file path because of scoped storage on Android 11+.
 */
data class Document(
    val id: String,
    val displayName: String,
    val uri: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val lastOpenedAtEpochMs: Long
)
