package com.prj.data.local.model

import androidx.room.Embedded

/**
 * Represents data for a custom word list with its associated words.
 */
data class CustomWordListWithEntriesEntity(
    @Embedded val list: CustomWordListEntity,
    @Embedded val entries: List<JapaneseWordWithTranslation>
)
