package com.prj.data.datasource

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Data source responsible for managing learning preferences using [SharedPreferences].
 *
 * This class provides methods to save and retrieve user settings related to flashcards
 * and other learning activities, such as shuffling and display preferences.
 *
 * @property mContext The application context used to access shared preferences.
 */
class LearningPreferenceDataSource @Inject constructor(@ApplicationContext private val mContext: Context) {
    companion object {
        private const val LEARNING_PREFS = "learning_prefs"
        private const val SHUFFLE_PREFS = "is_shuffle"
        private const val SHOW_MEANING_FIRST_PREFS = "show_meaning_first"
    }

    /**
     * Lazy-initialized [SharedPreferences] instance for learning-related settings.
     */
    private val mSharedPreference by lazy {
        mContext.getSharedPreferences(LEARNING_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves the user preference for shuffling words in a learning session.
     *
     * @return `true` if words should be shuffled, `false` otherwise. Defaults to `false`.
     */
    fun isShuffleWords(): Boolean {
        return mSharedPreference.getBoolean(SHUFFLE_PREFS, false)
    }

    /**
     * Updates the user preference for shuffling words.
     *
     * @param shuffle `true` to enable shuffling, `false` to disable it.
     */
    fun setShuffleWords(shuffle: Boolean) {
        mSharedPreference.edit { putBoolean(SHUFFLE_PREFS, shuffle) }
    }

    /**
     * Retrieves the user preference for showing the meaning of a word first.
     *
     * @return `true` if meanings should be shown first, `false` if words should be shown first. Defaults to `false`.
     */
    fun isShowMeaningFirst(): Boolean {
        return mSharedPreference.getBoolean(SHOW_MEANING_FIRST_PREFS, false)
    }

    /**
     * Updates the user preference for showing the meaning of a word first.
     *
     * @param show `true` to show meanings first, `false` to show words first.
     */
    fun setShowMeaningFirst(show: Boolean) {
        mSharedPreference.edit { putBoolean(SHOW_MEANING_FIRST_PREFS, show) }
    }
}
