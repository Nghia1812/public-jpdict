package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity represents a Japanese word
 *
 * id: id of word - PrimaryKey
 * kanji: kanji of Jp word
 * reading: hiragana of Jp word
 * gloss: meaning in Eng
 *
 */
@Entity(tableName = "entry_new")
data class JapaneseWordEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "kanji") val kanji: String?,
    @ColumnInfo(name = "reading") val reading: String?,
)
