package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Entity represents a Jp-En sentence
 *
 * entryId: id of word from entry table - JapaneseWordEntity
 * sentence_id: id of the sentence itself
 * sentence_ja: sentence in japanese
 * sentence_en: sentence in english
 */
@Entity(tableName = "examples", primaryKeys = ["entry_id", "sentence_id"], indices = [Index(value = ["sentence_id"])])
data class JpExampleEntity(
    @ColumnInfo(name = "entry_id") val entryId: Int,
    @ColumnInfo(name = "sentence_id") val exampleId: Int,
    @ColumnInfo(name = "sentence_ja") val japanese: String,
    @ColumnInfo(name = "sentence_en") val english: String,
)
