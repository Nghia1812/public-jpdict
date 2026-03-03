package com.prj.domain.repository

import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.model.settingsscreen.FontScale
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the repository for managing application settings.
 * This includes font scaling, notification preferences, and localization settings.
 */
interface IAppSettingsRepository {
    /** A flow that emits the current [FontScale] whenever it changes. */
    val fontScaleFlow: Flow<FontScale>

    /** A flow that emits the current notification status (enabled/disabled). */
    val notificationsEnabledFlow: Flow<Boolean>

    /** A flow that emits the current [AppLanguage] whenever it changes. */
    val languageFlow: Flow<AppLanguage>

    /** Updates the system-wide font scale. */
    suspend fun updateFontScale(scale: Float)

    /** Retrieves the currently applied [FontScale]. */
    suspend fun getCurrentFontScale(): FontScale

    /** Updates whether notifications are enabled for the application. */
    suspend fun updateNotificationsEnabled(enabled: Boolean)

    /** Retrieves the current notification permission/status. */
    suspend fun getCurrentNotificationStatus(): Boolean

    /** Retrieves the current [AppLanguage] object. */
    suspend fun getCurrentLanguage(): AppLanguage

    /** Retrieves the raw language code string (e.g., "en", "ja") saved in storage. */
    suspend fun getSavedLanguageCode(): String?

    /** Updates the application language using the provided language code. */
    suspend fun updateLanguage(language: String)
}
