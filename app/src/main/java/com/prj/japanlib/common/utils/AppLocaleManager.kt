package com.prj.japanlib.common.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.prj.domain.model.settingsscreen.AppLanguage
import timber.log.Timber

/**
 * List of supported languages in the application.
 */
val appLanguages = listOf(AppLanguage.ENGLISH, AppLanguage.VIETNAMESE)

/**
 * Utility object for managing the application's locale and language settings.
 * Handles language switching for both Android 13 (Tiramisu) and older versions.
 */
object AppLocaleManager {
    /**
     * Changes the application's language.
     *
     * @param context The application context.
     * @param languageCode The ISO 639-1 language code (e.g., "en", "vi").
     */
    fun changeLanguage(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Timber.i("TIRAMISU Changing language to $languageCode")
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        } else {
            Timber.i("Pre-TIRAMISU Changing language to $languageCode")
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        }
    }

    /**
     * Retrieves the current application language code.
     *
     * @param context The application context.
     * @return The current language code or the default if none is found.
     */
    fun getLanguageCode(context: Context): String {
        val locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)?.applicationLocales?.get(0)
            } else {
                val locales = AppCompatDelegate.getApplicationLocales()
                if (locales.isEmpty) null else locales[0]
            }

        Timber.i("Current locale: $locale")
        return locale?.language ?: getDefaultLanguageCode()
    }

    /**
     * Returns the default language code (first item in [appLanguages]).
     */
    private fun getDefaultLanguageCode(): String {
        return appLanguages.first().code
    }

    /**
     * Checks if a specific language code is supported by the application.
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        return appLanguages.any { it.code == languageCode }
    }
}
