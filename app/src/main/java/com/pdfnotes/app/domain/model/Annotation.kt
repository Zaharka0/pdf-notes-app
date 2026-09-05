package com.pdfnotes.app.domain.model

/** Kind of manual annotation the user can create while reading a page. */
enum class AnnotationType { HIGHLIGHT, NOTE }

/**
 * A single manual annotation on one page of a document.
 * Coordinates are normalized (0f..1f) relative to page width/height so they
 * stay correct regardless of zoom level or screen size.
 */
data class Annotation(
    val id: String,
    val documentId: String,
    val pageIndex: Int,
    val type: AnnotationType,
    val text: String?,          // note body, or highlighted text snippet
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val colorHex: String,
    val createdAtEpochMs: Long
)
