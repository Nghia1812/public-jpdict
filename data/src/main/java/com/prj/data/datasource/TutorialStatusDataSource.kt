package com.prj.data.datasource

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Manages the storage and retrieval of tutorial-related flags using SharedPreferences.
 * This class is responsible for persisting whether a user has seen specific tutorial hints,
 * such as the swipe gestures.
 *
 * @param mContext The application context, injected by Hilt, used to access SharedPreferences.
 */
class TutorialStatusDataSource @Inject constructor(@ApplicationContext mContext: Context) {
    companion object {
        private const val TUTORIAL_PREFS = "tutorial_prefs"
        private const val SEEN_LEFT_PAGE = "has_seen_left_swipe"
        private const val SEEN_RIGHT_PAGE = "has_seen_right_swipe"
    }

    private val mSharedPreference: SharedPreferences by lazy {
        mContext.getSharedPreferences(TUTORIAL_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * Checks if the user has seen the left swipe hint.
     * @return `true` if the hint has been seen, `false` otherwise.
     */
    fun hasSeenLeftPage(): Boolean {
        return mSharedPreference.getBoolean(SEEN_LEFT_PAGE, false)
    }

    /**
     * Checks if the user has seen the right swipe hint.
     * @return `true` if the hint has been seen, `false` otherwise.
     */
    fun hasSeenRightPage(): Boolean {
        return mSharedPreference.getBoolean(SEEN_RIGHT_PAGE, false)
    }

    /**
     * Sets the status for the left swipe hint.
     * @param hasSeen `true` to mark the hint as seen, `false` to clear it.
     */
    fun setSeenLeftPage(hasSeen: Boolean) {
        mSharedPreference.edit().putBoolean(SEEN_LEFT_PAGE, hasSeen).apply()
    }

    /**
     * Sets the status for the right swipe hint.
     * @param hasSeen `true` to mark the hint as seen, `false` to clear it.
     */
    fun setSeenRightPage(hasSeen: Boolean) {
        mSharedPreference.edit().putBoolean(SEEN_RIGHT_PAGE, hasSeen).apply()
    }


}