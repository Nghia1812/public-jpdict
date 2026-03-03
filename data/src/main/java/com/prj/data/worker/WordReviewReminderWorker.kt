package com.prj.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prj.data.notification.NotificationSender
import com.prj.domain.repository.INotificationRepository
import com.prj.domain.usecase.GetDueWordsForReviewUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalTime

@HiltWorker
class WordReviewReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getDueWordsUseCase: GetDueWordsForReviewUseCase,
    private val notificationSender: NotificationSender,
    private val notificationRepository: INotificationRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("WordReviewReminderWorker: Starting work")
            
            val prefs = notificationRepository.getNotificationPreferences()
            
            if (!prefs.reviewRemindersEnabled) {
                Timber.d("Review reminders disabled, skipping")
                return Result.success()
            }
            
            if (!notificationRepository.shouldSendNotification(LocalTime.now())) {
                Timber.d("In silent hours or notifications disabled, skipping")
                return Result.success()
            }
            
            val dueWords = getDueWordsUseCase()
            
            if (dueWords.isNotEmpty()) {
                Timber.d("Sending review reminder: ${dueWords.size} words due")
                notificationSender.sendReviewReminderNotification(dueWords.size)
            } else {
                Timber.d("No words due for review")
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to send review reminder")
            Result.retry()
        }
    }
}
