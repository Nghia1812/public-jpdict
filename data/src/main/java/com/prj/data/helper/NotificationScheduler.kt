package com.prj.data.helper

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prj.data.worker.DailyProgressWorker
import com.prj.data.worker.RandomWordNotificationWorker
import com.prj.data.worker.WordReviewReminderWorker
import com.prj.domain.model.NotificationPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    companion object {
        const val WORK_PROGRESS = "daily_progress_notification"
        const val WORK_DAILY_WORD = "daily_word_notification"
        const val WORK_REVIEW_REMINDER = "review_reminder_notification"
    }
    
    fun scheduleAllNotifications(preferences: NotificationPreferences) {
        Timber.d("Scheduling notifications with preferences: enabled=${preferences.notificationEnabled}")
        
        if (preferences.notificationEnabled) {
            scheduleProgressNotification(preferences)
            scheduleDailyWordNotification(preferences)
            scheduleReviewReminder(preferences)
        } else {
            cancelAllNotifications()
        }
    }
    
    private fun scheduleProgressNotification(preferences: NotificationPreferences) {
        if (!preferences.progressNotificationsEnabled) {
            Timber.d("Progress notifications disabled, cancelling work")
            workManager.cancelUniqueWork(WORK_PROGRESS)
            return
        }
        
        val delay = calculateDelayToTime(preferences.dailyReminderTime)
        val constraints = buildConstraints()
        
        Timber.d("Scheduling progress notification with delay: $delay ms")
        
        val request = PeriodicWorkRequestBuilder<DailyProgressWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_PROGRESS)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WORK_PROGRESS,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
    
    private fun scheduleDailyWordNotification(preferences: NotificationPreferences) {
        if (!preferences.randomWordEnabled) {
            Timber.d("Daily word notifications disabled, cancelling work")
            workManager.cancelUniqueWork(WORK_DAILY_WORD)
            return
        }
        
        // Schedule 30 minutes before reminder time
        val wordTime = preferences.dailyReminderTime.minusMinutes(30)
        val delay = calculateDelayToTime(wordTime)
        val constraints = buildConstraints()
        
        Timber.d("Scheduling daily word notification with delay: $delay ms")
        
        val request = PeriodicWorkRequestBuilder<RandomWordNotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_DAILY_WORD)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WORK_DAILY_WORD,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
    
    private fun scheduleReviewReminder(preferences: NotificationPreferences) {
        if (!preferences.reviewRemindersEnabled) {
            Timber.d("Review reminders disabled, cancelling work")
            workManager.cancelUniqueWork(WORK_REVIEW_REMINDER)
            return
        }
        
        val delay = calculateDelayToTime(preferences.dailyReminderTime)
        val constraints = buildConstraints()
        
        Timber.d("Scheduling review reminder with delay: $delay ms")
        
        // Run twice daily (every 12 hours)
        val request = PeriodicWorkRequestBuilder<WordReviewReminderWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_REVIEW_REMINDER)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WORK_REVIEW_REMINDER,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
    
    fun cancelAllNotifications() {
        Timber.d("Cancelling all notification work")
        workManager.cancelUniqueWork(WORK_PROGRESS)
        workManager.cancelUniqueWork(WORK_DAILY_WORD)
        workManager.cancelUniqueWork(WORK_REVIEW_REMINDER)
    }
    
    private fun buildConstraints() = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()
    
    private fun calculateDelayToTime(targetTime: LocalTime): Long {
        val now = LocalDateTime.now()
        var target = now.withHour(targetTime.hour).withMinute(targetTime.minute).withSecond(0)
        
        if (target.isBefore(now)) {
            target = target.plusDays(1)
        }
        
        val delayMillis = Duration.between(now, target).toMillis()
        Timber.d("Calculated delay to $targetTime: $delayMillis ms")
        return delayMillis
    }
}
