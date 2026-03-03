package com.prj.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Relationship entity representing a JLPT list with its associated word entries
 *
 * This is used for queries that need to fetch a complete JLPT list with all its words.
 * Follows the same pattern as CustomWordListWithEntriesEntity and ThemeListWithEntriesEntity.
 *
 * @property list The JLPT list metadata
 * @property entries List of word entries with translations associated with this JLPT level
 */
data class JlptWordListWithEntriesEntity(
    @Embedded 
    val list: JlptWordListEntity,
    
    @Relation(
        entity = JapaneseWordEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = JlptWordEntryEntity::class,
            parentColumn = "list_id",
            entityColumn = "entry_id"
        )
    )
    val entries: List<JapaneseWordWithTranslation>
)
