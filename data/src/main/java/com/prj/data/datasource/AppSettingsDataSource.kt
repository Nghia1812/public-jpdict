package com.prj.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.model.settingsscreen.AppLanguage.Companion.fromString
import com.prj.domain.model.settingsscreen.FontScale
import com.prj.domain.model.settingsscreen.FontScale.Companion.fromScaleFactor
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppSettingsDataSource
@Inject
constructor(
        private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val FONT_SCALE = floatPreferencesKey("font_scale")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")

        private val LANGUAGE = stringPreferencesKey("language")
    }

    val fontScaleFlow: Flow<FontScale> =
            dataStore.data.map { preferences -> fromScaleFactor(preferences[FONT_SCALE]) }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { preferences -> preferences[FONT_SCALE] = scale }
    }

    // -------- Notification --------
    val notificationEnabledFlow: Flow<Boolean> =
            dataStore.data.map { preferences -> preferences[NOTIFICATION_ENABLED] ?: true }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[NOTIFICATION_ENABLED] = enabled }
    }

    val languageFlow: Flow<AppLanguage> =
            dataStore.data.map { preferences -> fromString(preferences[LANGUAGE]) }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences -> preferences[LANGUAGE] = language }
    }

    suspend fun getLanguage(): String? {
        val preferences = dataStore.data.first()
        return preferences[LANGUAGE]
    }
}
