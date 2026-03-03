package com.prj.domain.usecase

import com.prj.domain.model.NotificationPreferences
import com.prj.domain.repository.INotificationRepository
import javax.inject.Inject

class GetNotificationPreferencesUseCase @Inject constructor(
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(): NotificationPreferences {
        return notificationRepository.getNotificationPreferences()
    }
}
