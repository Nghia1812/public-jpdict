package com.prj.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Represents a complete theme list with all its associated Japanese word entries.
 *
 * This is a relational data class and is not an entity itself. It is primarily used
 * as a return type in DAOs to fetch a theme and all its related words in a single query.
 */
data class ThemeListWithEntriesEntity(
    @Embedded val theme: ThemeListEntity,
    val entries: List<JapaneseWordWithTranslation>
)