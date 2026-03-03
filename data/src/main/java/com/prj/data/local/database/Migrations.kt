package com.prj.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 1 to version 2.
 * Adds notification feature tables:
 * - notification_preferences: Stores user notification settings
 * - notification_history: Tracks sent notifications to avoid repetition
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create notification_preferences table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS notification_preferences (
                id INTEGER PRIMARY KEY NOT NULL DEFAULT 0,
                notification_enabled INTEGER NOT NULL DEFAULT 1,
                daily_reminder_time TEXT NOT NULL DEFAULT '20:00',
                progress_notifications_enabled INTEGER NOT NULL DEFAULT 1,
                review_reminders_enabled INTEGER NOT NULL DEFAULT 1,
                random_word_enabled INTEGER NOT NULL DEFAULT 1,
                notification_frequency TEXT NOT NULL DEFAULT 'DAILY',
                minimum_words_for_progress INTEGER NOT NULL DEFAULT 5,
                silent_hours_start TEXT DEFAULT '22:00',
                silent_hours_end TEXT DEFAULT '08:00'
            )
        """.trimIndent())
        
        // Create notification_history table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS notification_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                word_id TEXT NOT NULL,
                notification_type TEXT NOT NULL,
                sent_date INTEGER NOT NULL,
                was_opened INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        
        // Insert default preferences
        db.execSQL("""
            INSERT INTO notification_preferences (id) VALUES (0)
        """.trimIndent())

        // Add last_reviewed_date column to jlpt_word_entry if it doesn't exist
        db.execSQL("""
            ALTER TABLE jlpt_word_entry ADD COLUMN last_reviewed_date INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
    }
}
