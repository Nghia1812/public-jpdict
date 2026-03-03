package com.prj.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.prj.data.local.dao.CustomWordListDao
import com.prj.data.local.dao.ExampleDao
import com.prj.data.local.dao.JWordDao
import com.prj.data.local.dao.JlptWordDao
import com.prj.data.local.dao.KanjiDao
import com.prj.data.local.dao.NotificationDao
import com.prj.data.local.dao.ThemeVocabularyDao
import com.prj.data.local.model.CustomWordEntryCrossRef
import com.prj.data.local.model.CustomWordListEntity
import com.prj.data.local.model.EntryTranslationEntity
import com.prj.data.local.model.FtsWordEntity
import com.prj.data.local.model.JapaneseWordEntity
import com.prj.data.local.model.JlptWordListEntity
import com.prj.data.local.model.JlptWordEntryEntity
import com.prj.data.local.model.JpExampleEntity
import com.prj.data.local.model.KanjiDetailEntity
import com.prj.data.local.model.ThemeEntryCrossRef
import com.prj.data.local.model.ThemeListEntity
import com.prj.data.local.model.WordTranslationFtsEntity
import com.prj.data.local.model.NotificationPreferencesEntity
import com.prj.data.local.model.NotificationHistoryEntity


@Database(
    entities =
    [
        JapaneseWordEntity::class,
        JpExampleEntity::class,
        JlptWordListEntity::class,        // NEW: JLPT list metadata
        JlptWordEntryEntity::class,       // NEW: Junction table for JLPT words
        CustomWordListEntity::class,
        CustomWordEntryCrossRef::class,
        ThemeListEntity::class,
        ThemeEntryCrossRef::class,
        FtsWordEntity::class,
        EntryTranslationEntity::class,
        WordTranslationFtsEntity::class,
        KanjiDetailEntity::class,
        NotificationPreferencesEntity::class,  // Notification preferences
        NotificationHistoryEntity::class       // Notification history
    ],
    version = 2
)
@TypeConverters(Converters::class)
abstract class DictionaryDatabase : RoomDatabase() {
    /**
     * Provides access to the [JWordDao] for querying Japanese words and saved words.
     * This DAO handles core word operations, full-text search, and favoriting.
     *
     * @return An instance of [JWordDao].
     */
    abstract fun jWordDao(): JWordDao

    /**
     * Provides access to the [ExampleDao] for querying sentence examples.
     *
     * @return An instance of [ExampleDao].
     */
    abstract fun exampleDao(): ExampleDao

    /**
     * Provides access to the [JlptWordDao] for querying words based on JLPT levels.
     *
     * @return An instance of [JlptWordDao].
     */
    abstract fun jlptWordDao(): JlptWordDao

    /**
     * Provides access to the [CustomWordListDao] for managing user-created word lists.
     *
     * @return An instance of [CustomWordListDao].
     */
    abstract fun customWordListDao(): CustomWordListDao

    /**
     * Provides access to the [ThemeVocabularyDao] for managing theme-based word lists.
     *
     * @return An instance of [ThemeVocabularyDao].
     */
    abstract fun themeVocabularyDao(): ThemeVocabularyDao

    abstract fun kanjiDao(): KanjiDao

    /**
     * Provides access to the [NotificationDao] for managing notification preferences and history.
     *
     * @return An instance of [NotificationDao].
     */
    abstract fun notificationDao(): NotificationDao
}