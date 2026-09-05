package com.pdfnotes.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val pageIndex: Int,
    val type: String,       // "HIGHLIGHT" or "NOTE" - stored as string for readability/migrations
    val text: String?,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val colorHex: String,
    val createdAtEpochMs: Long
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val uri: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val lastOpenedAtEpochMs: Long
)
