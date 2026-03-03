package com.prj.domain.usecase

import com.prj.domain.model.settingsscreen.FontScale
import com.prj.domain.repository.IAppSettingsRepository
import javax.inject.Inject

class GetCurrentNotiStatus @Inject constructor(
    private val appSettingsRepository: IAppSettingsRepository
) {
    suspend operator fun invoke(): Boolean = appSettingsRepository.getCurrentNotificationStatus()
}