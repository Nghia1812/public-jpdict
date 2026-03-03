package com.prj.domain.usecase

import com.prj.domain.model.NotificationType
import com.prj.domain.model.WordNotificationData
import com.prj.domain.repository.INotificationRepository
import javax.inject.Inject

class GetRandomWordForNotificationUseCase @Inject constructor(
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(): WordNotificationData? {
        val word = notificationRepository.getRandomWordForNotification()
        return word?.let { 
            WordNotificationData(
                word = it,
                notificationType = NotificationType.DAILY_WORD
            )
        }
    }
}
