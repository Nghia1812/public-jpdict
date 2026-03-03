package com.prj.data.notification

import com.prj.domain.model.ProgressStats
import com.prj.domain.model.WordNotificationData

/**
 * Interface for sending notifications.
 * This interface is defined in the data layer but implemented in the app layer
 * to maintain Clean Architecture principles (UI logic stays in app layer).
 */
interface NotificationSender {
    /**
     * Send a progress notification showing learning statistics.
     */
    fun sendProgressNotification(stats: ProgressStats)
    
    /**
     * Send a notification displaying a word of the day.
     */
    fun sendWordNotification(wordData: WordNotificationData)
    
    /**
     * Send a reminder notification for words due for review.
     */
    fun sendReviewReminderNotification(dueWordsCount: Int)
}
