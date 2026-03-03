package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a translation entry for a Japanese word from entry_translation table
 *
 */
@Entity(
    tableName = "entry_translation",
    foreignKeys = [
        ForeignKey(
            entity = JapaneseWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entry_id", "language_code"], unique = true)
    ]
)
data class EntryTranslationEntity(
    @PrimaryKey(autoGenerate = true) val translationId: Int = 0,
    @ColumnInfo(name = "entry_id") val entryId: Int,
    @ColumnInfo(name = "language_code") val languageCode: String, // "en", "vi"
    @ColumnInfo(name = "gloss") val gloss: String?,
    @ColumnInfo(name = "position") val position: String?,
    @ColumnInfo(name = "gloss_norm") val glossNorm: String?,
)