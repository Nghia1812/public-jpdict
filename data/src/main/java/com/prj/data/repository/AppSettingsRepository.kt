package com.prj.data.repository

import com.prj.data.datasource.AppSettingsDataSource
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.model.settingsscreen.FontScale
import com.prj.domain.repository.IAppSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AppSettingsRepository @Inject constructor(private val local: AppSettingsDataSource) :
        IAppSettingsRepository {
    override val fontScaleFlow = local.fontScaleFlow
    override val notificationsEnabledFlow = local.notificationEnabledFlow
    override val languageFlow = local.languageFlow

    override suspend fun updateFontScale(scale: Float) = local.setFontScale(scale)

    override suspend fun getCurrentFontScale(): FontScale {
        val scaleValue = fontScaleFlow.first()
        return scaleValue
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        local.setNotificationEnabled(enabled)
    }

    override suspend fun getCurrentNotificationStatus(): Boolean {
        val status = notificationsEnabledFlow.first()
        return status
    }

    override suspend fun getCurrentLanguage(): AppLanguage {
        val language = languageFlow.first()
        return language
    }

    override suspend fun updateLanguage(language: String) {
        local.setLanguage(language)
    }

    override suspend fun getSavedLanguageCode(): String? {
        return local.getLanguage()
    }
}
