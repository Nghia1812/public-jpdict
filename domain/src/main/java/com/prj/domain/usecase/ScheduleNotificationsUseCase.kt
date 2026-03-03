package com.prj.domain.usecase

import com.prj.domain.model.NotificationPreferences
import com.prj.domain.repository.INotificationRepository
import javax.inject.Inject

class ScheduleNotificationsUseCase @Inject constructor(
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(preferences: NotificationPreferences) {
        // This will trigger WorkManager rescheduling via repository
        notificationRepository.updateNotificationPreferences(preferences)
    }
}
