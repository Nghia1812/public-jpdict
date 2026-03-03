package com.prj.data.worker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prj.data.notification.NotificationSender
import com.prj.domain.model.NotificationType
import com.prj.domain.repository.INotificationRepository
import com.prj.domain.usecase.GetRandomWordForNotificationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.LocalTime

@HiltWorker
class RandomWordNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getRandomWordUseCase: GetRandomWordForNotificationUseCase,
    private val notificationSender: NotificationSender,
    private val notificationRepository: INotificationRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("RandomWordNotificationWorker: Starting work")
            
            val prefs = notificationRepository.getNotificationPreferences()
            
            if (!prefs.randomWordEnabled) {
                Timber.d("Random word notifications disabled, skipping")
                return Result.success()
            }
            
            if (!notificationRepository.shouldSendNotification(LocalTime.now())) {
                Timber.d("In silent hours or notifications disabled, skipping")
                return Result.success()
            }
            
            val wordData = getRandomWordUseCase()
            
            if (wordData != null) {
                Timber.d("Sending word notification: ${wordData.word.kanji ?: wordData.word.reading}")
                notificationSender.sendWordNotification(wordData)
                notificationRepository.markWordAsNotified(
                    wordData.word.id.toString(),
                    NotificationType.DAILY_WORD
                )
            } else {
                Timber.w("No word available for notification")
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to send word notification")
            Result.retry()
        }
    }
}
