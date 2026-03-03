package com.prj.domain.usecase

import com.prj.domain.repository.IAppSettingsRepository
import javax.inject.Inject


class UpdateFontScaleUseCase @Inject constructor(
    private val appSettingsRepository: IAppSettingsRepository
) {
    suspend operator fun invoke(scale: Float) =
        appSettingsRepository.updateFontScale(scale)
}
