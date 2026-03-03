package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class for kanji_detail table
 */
@Entity(tableName = "kanji_detail")
data class KanjiDetailEntity(
    @PrimaryKey val kanji: String,
    val onyomi: String,     // JSON
    val kunyomi: String,    // JSON
    val meanings: String,   // JSON
    @ColumnInfo(name = "stroke_count") val strokeCount: Int?,
    val grade: Int?,
    val jlpt: Int?,
    val strokes: String    // JSON
)
