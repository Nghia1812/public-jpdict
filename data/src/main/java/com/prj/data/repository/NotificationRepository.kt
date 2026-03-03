package com.prj.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.prj.data.local.dao.JlptWordDao
import com.prj.data.local.dao.NotificationDao
import com.prj.data.local.model.NotificationHistoryEntity
import com.prj.data.mapper.NotificationMapper
import com.prj.data.mapper.toDomain
import com.prj.domain.model.NotificationHistoryItem
import com.prj.domain.model.NotificationPreferences
import com.prj.domain.model.NotificationType
import com.prj.domain.model.ProgressStats
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.repository.INotificationRepository
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val jlptWordDao: JlptWordDao,
    private val notificationMapper: NotificationMapper
) : INotificationRepository {
    
    // Default language for notifications - could be made configurable
    private val defaultLanguage = "en"
    
    override suspend fun getNotificationPreferences(): NotificationPreferences {
        val entity = notificationDao.getPreferences()
        return entity?.let { notificationMapper.toDomain(it) }
            ?: NotificationPreferences() // Return default if not found
    }
    
    override suspend fun updateNotificationPreferences(preferences: NotificationPreferences) {
        val entity = notificationMapper.toEntity(preferences)
        notificationDao.updatePreferences(entity)
    }
    
    override suspend fun getRandomWordForNotification(): JapaneseWord? {
        // Smart selection algorithm:
        // 1. Try "NOT_LEARNT_YET" words first (priority for new learning)
        var wordWithTranslation = jlptWordDao.getRandomWordByLearningState(
            LearningState.NOT_LEARNT_YET,
            language = defaultLanguage
        )
        
        // 2. Try "FORGOT" words (need reinforcement)
        if (wordWithTranslation == null) {
            wordWithTranslation = jlptWordDao.getRandomWordByLearningState(
                LearningState.FORGOT,
                language = defaultLanguage
            )
        }
        
        // 3. Try "REMEMBERED" words (for review)
        if (wordWithTranslation == null) {
            wordWithTranslation = jlptWordDao.getRandomWordByLearningState(
                LearningState.REMEMBERED,
                language = defaultLanguage
            )
        }
        
        return wordWithTranslation?.let { it.toDomain() }
    }
    
    override suspend fun getDueWordsForReview(): List<JapaneseWord> {
        // Words not reviewed in 3 days are considered due
        val threeDaysAgo = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000
        val entities = jlptWordDao.getDueForReview(
            beforeDate = threeDaysAgo,
            limit = 10,
            language = defaultLanguage
        )
        return entities.map { it.toDomain() }
    }
    
    override suspend fun calculateProgressStats(periodStart: LocalDateTime): ProgressStats {
        // TODO: Implement actual progress tracking
        // This requires maintaining learning activity records in the database
        // For now, return basic stats with due review count
        val dueWords = getDueWordsForReview()
        
        return ProgressStats(
            wordsLearnedToday = 0, // Will be implemented when learning tracking is added
            wordsLearnedThisWeek = 0,
            testsCompletedToday = 0,
            currentStreak = 0,
            accuracyRate = 0f,
            dueReviewCount = dueWords.size
        )
    }
    
    override suspend fun markWordAsNotified(wordId: String, notificationType: NotificationType) {
        val history = NotificationHistoryEntity(
            wordId = wordId,
            notificationType = notificationType.name,
            sentDate = System.currentTimeMillis()
        )
        notificationDao.insertHistory(history)
    }
    
    override suspend fun getNotificationHistory(limit: Int): List<NotificationHistoryItem> {
        return notificationDao.getHistory(limit).map { notificationMapper.toHistoryItem(it) }
    }
    
    override suspend fun shouldSendNotification(currentTime: LocalTime): Boolean {
        val prefs = getNotificationPreferences()
        
        // Check if notifications are enabled globally
        if (!prefs.notificationEnabled) {
            return false
        }
        
        // Check if in silent hours
        val silentStart = prefs.silentHoursStart
        val silentEnd = prefs.silentHoursEnd
        
        if (silentStart != null && silentEnd != null) {
            val isInSilentHours = if (silentStart.isBefore(silentEnd)) {
                // Normal range (e.g., 08:00 to 22:00 - don't send outside this range)
                currentTime.isBefore(silentStart) || currentTime.isAfter(silentEnd)
            } else {
                // Overnight range (e.g., 22:00 to 08:00 - don't send during this range)
                currentTime.isAfter(silentStart) && currentTime.isBefore(silentEnd)
            }
            
            if (isInSilentHours) {
                return false
            }
        }
        
        return true
    }
}
