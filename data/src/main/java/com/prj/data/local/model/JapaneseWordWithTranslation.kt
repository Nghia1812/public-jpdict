package com.prj.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a Japanese word with its translation.
 * Acts as a join table between JapaneseWordEntity and EntryTranslationEntity.
 */
data class JapaneseWordWithTranslation(
    @Embedded val entry: JapaneseWordEntity,
    @Embedded val translation: EntryTranslationEntity
)
