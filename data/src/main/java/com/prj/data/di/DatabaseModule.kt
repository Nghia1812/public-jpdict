package com.prj.data.di

import android.content.Context
import androidx.room.Room
import com.prj.data.local.database.DictionaryDatabase
import com.prj.data.local.database.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideLibDatabase(@ApplicationContext context: Context) : DictionaryDatabase {
        return Room.databaseBuilder(
                context.applicationContext,
                DictionaryDatabase::class.java,
                "jpDictionary.db"
            )
            .createFromAsset("jpDictionary.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideJWordDao(database: DictionaryDatabase) = database.jWordDao()

    @Provides
    @Singleton
    fun provideJpExample(database: DictionaryDatabase) = database.exampleDao()

    @Provides
    @Singleton
    fun provideJlptWordDao(database: DictionaryDatabase) = database.jlptWordDao()

    @Provides
    @Singleton
    fun provideCustomWordListDao(database: DictionaryDatabase) = database.customWordListDao()

    @Provides
    @Singleton
    fun provideThemeVocabularyDao(database: DictionaryDatabase) = database.themeVocabularyDao()

    @Provides
    @Singleton
    fun provideKanjiDao(database: DictionaryDatabase) = database.kanjiDao()

    @Provides
    @Singleton
    fun provideNotificationDao(database: DictionaryDatabase) = database.notificationDao()

}