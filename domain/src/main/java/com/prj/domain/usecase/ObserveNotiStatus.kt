package com.prj.domain.usecase

import com.prj.domain.model.settingsscreen.FontScale
import com.prj.domain.repository.IAppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotiStatus @Inject constructor(
    private val appSettingsRepository: IAppSettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = appSettingsRepository.notificationsEnabledFlow
}