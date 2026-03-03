package com.prj.domain.repository

import com.prj.domain.model.NotificationHistoryItem
import com.prj.domain.model.NotificationPreferences
import com.prj.domain.model.NotificationType
import com.prj.domain.model.ProgressStats
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import java.time.LocalDateTime
import java.time.LocalTime

interface INotificationRepository {
    suspend fun getNotificationPreferences(): NotificationPreferences
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences)
    suspend fun getRandomWordForNotification(): JapaneseWord?
    suspend fun getDueWordsForReview(): List<JapaneseWord>
    suspend fun calculateProgressStats(periodStart: LocalDateTime): ProgressStats
    suspend fun markWordAsNotified(wordId: String, notificationType: NotificationType)
    suspend fun getNotificationHistory(limit: Int): List<NotificationHistoryItem>
    suspend fun shouldSendNotification(currentTime: LocalTime): Boolean
}
