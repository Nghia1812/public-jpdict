package com.prj.domain.usecase

import com.prj.domain.model.ProgressStats
import com.prj.domain.repository.INotificationRepository
import java.time.LocalDateTime
import javax.inject.Inject

class CalculateProgressStatsUseCase @Inject constructor(
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(periodStart: LocalDateTime = LocalDateTime.now().minusDays(1)): ProgressStats {
        return notificationRepository.calculateProgressStats(periodStart)
    }
}
