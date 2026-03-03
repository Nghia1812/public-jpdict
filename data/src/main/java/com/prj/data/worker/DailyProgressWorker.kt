package com.prj.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prj.data.notification.NotificationSender
import com.prj.domain.repository.INotificationRepository
import com.prj.domain.usecase.CalculateProgressStatsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalDateTime
import java.time.LocalTime

@HiltWorker
class DailyProgressWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val calculateProgressStatsUseCase: CalculateProgressStatsUseCase,
    private val notificationSender: NotificationSender,
    private val notificationRepository: INotificationRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("DailyProgressWorker: Starting work")
            
            val prefs = notificationRepository.getNotificationPreferences()
            
            if (!prefs.progressNotificationsEnabled) {
                Timber.d("Progress notifications disabled, skipping")
                return Result.success()
            }
            
            if (!notificationRepository.shouldSendNotification(LocalTime.now())) {
                Timber.d("In silent hours or notifications disabled, skipping")
                return Result.success()
            }
            
            val stats = calculateProgressStatsUseCase(LocalDateTime.now().minusDays(1))
            
            // Only send if meaningful progress
            if (stats.wordsLearnedToday >= prefs.minimumWordsForProgress) {
                Timber.d("Sending progress notification: ${stats.wordsLearnedToday} words learned")
                notificationSender.sendProgressNotification(stats)
            } else {
                Timber.d("No significant progress to report")
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to send progress notification")
            Result.retry()
        }
    }
}
