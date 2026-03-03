package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * Full Text Search entity for JapaneseWordEntity class
 * Allow finding word by kanji, reading and gloss
 */
@Fts4(contentEntity = JapaneseWordEntity::class)
@Entity(tableName = "entry_fts")
data class FtsWordEntity(
    @ColumnInfo(name = "reading") val reading: String?,
    @ColumnInfo(name = "kanji") val kanji: String?,
)
