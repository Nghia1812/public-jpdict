package com.prj.domain.model

import java.time.LocalTime

data class NotificationPreferences(
    val notificationEnabled: Boolean = true,
    val dailyReminderTime: LocalTime = LocalTime.of(20, 0), // 8 PM default
    val progressNotificationsEnabled: Boolean = true,
    val reviewRemindersEnabled: Boolean = true,
    val randomWordEnabled: Boolean = true,
    val notificationFrequency: NotificationFrequency = NotificationFrequency.DAILY,
    val minimumWordsForProgress: Int = 5, // Only notify if learned at least 5 words
    val silentHoursStart: LocalTime? = LocalTime.of(22, 0), // 10 PM
    val silentHoursEnd: LocalTime? = LocalTime.of(8, 0) // 8 AM
)

enum class NotificationFrequency {
    DAILY, WEEKLY, CUSTOM
}
