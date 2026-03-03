package com.prj.data.local.model

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Full Text Search entity for EntryTranslationEntity class
 * Allow finding word by gloss
 */
@Fts4(contentEntity = EntryTranslationEntity::class)
@Entity(tableName = "entry_translation_fts")
data class WordTranslationFtsEntity(
    val gloss: String?,
    val gloss_norm: String?
)
