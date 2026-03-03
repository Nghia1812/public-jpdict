package com.prj.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.prj.data.local.model.NotificationHistoryEntity
import com.prj.data.local.model.NotificationPreferencesEntity
import com.prj.domain.model.NotificationFrequency
import com.prj.domain.model.NotificationHistoryItem
import com.prj.domain.model.NotificationPreferences
import com.prj.domain.model.NotificationType
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class NotificationMapper @Inject constructor() {
    
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    fun toDomain(entity: NotificationPreferencesEntity): NotificationPreferences {
        return NotificationPreferences(
            notificationEnabled = entity.notificationEnabled,
            dailyReminderTime = LocalTime.parse(entity.dailyReminderTime, timeFormatter),
            progressNotificationsEnabled = entity.progressNotificationsEnabled,
            reviewRemindersEnabled = entity.reviewRemindersEnabled,
            randomWordEnabled = entity.randomWordEnabled,
            notificationFrequency = NotificationFrequency.valueOf(entity.notificationFrequency),
            minimumWordsForProgress = entity.minimumWordsForProgress,
            silentHoursStart = entity.silentHoursStart?.let { LocalTime.parse(it, timeFormatter) },
            silentHoursEnd = entity.silentHoursEnd?.let { LocalTime.parse(it, timeFormatter) }
        )
    }
    
    fun toEntity(domain: NotificationPreferences): NotificationPreferencesEntity {
        return NotificationPreferencesEntity(
            id = 0,
            notificationEnabled = domain.notificationEnabled,
            dailyReminderTime = domain.dailyReminderTime.format(timeFormatter),
            progressNotificationsEnabled = domain.progressNotificationsEnabled,
            reviewRemindersEnabled = domain.reviewRemindersEnabled,
            randomWordEnabled = domain.randomWordEnabled,
            notificationFrequency = domain.notificationFrequency.name,
            minimumWordsForProgress = domain.minimumWordsForProgress,
            silentHoursStart = domain.silentHoursStart?.format(timeFormatter),
            silentHoursEnd = domain.silentHoursEnd?.format(timeFormatter)
        )
    }
    
    fun toHistoryItem(entity: NotificationHistoryEntity): NotificationHistoryItem {
        return NotificationHistoryItem(
            id = entity.id,
            wordId = entity.wordId,
            notificationType = NotificationType.valueOf(entity.notificationType),
            sentDate = entity.sentDate,
            wasOpened = entity.wasOpened
        )
    }
}
