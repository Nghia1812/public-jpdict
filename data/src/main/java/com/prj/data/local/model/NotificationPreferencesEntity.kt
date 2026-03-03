package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_preferences")
data class NotificationPreferencesEntity(
    @PrimaryKey
    val id: Int = 0, // Single row table
    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean = true,
    @ColumnInfo(name = "daily_reminder_time")
    val dailyReminderTime: String = "20:00", // Stored as HH:mm
    @ColumnInfo(name = "progress_notifications_enabled")
    val progressNotificationsEnabled: Boolean = true,
    @ColumnInfo(name = "review_reminders_enabled")
    val reviewRemindersEnabled: Boolean = true,
    @ColumnInfo(name = "random_word_enabled")
    val randomWordEnabled: Boolean = true,
    @ColumnInfo(name = "notification_frequency")
    val notificationFrequency: String = "DAILY",
    @ColumnInfo(name = "minimum_words_for_progress")
    val minimumWordsForProgress: Int = 5,
    @ColumnInfo(name = "silent_hours_start")
    val silentHoursStart: String? = "22:00",
    @ColumnInfo(name = "silent_hours_end")
    val silentHoursEnd: String? = "08:00"
)
