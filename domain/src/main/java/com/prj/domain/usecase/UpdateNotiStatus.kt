package com.prj.domain.usecase

import com.prj.domain.repository.IAppSettingsRepository
import javax.inject.Inject

class UpdateNotiStatus @Inject constructor(
    private val appSettingsRepository: IAppSettingsRepository
) {
    suspend operator fun invoke(status: Boolean) =
        appSettingsRepository.updateNotificationsEnabled(status)
}