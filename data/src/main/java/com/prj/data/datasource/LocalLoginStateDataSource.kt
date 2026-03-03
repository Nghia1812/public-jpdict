package com.prj.data.datasource

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

/**
 * Manages the persistence of the user's authentication state locally.
 *
 * This class uses [SharedPreferences] to store and retrieve the user's login status,
 * ensuring that the state is preserved across application restarts.
 *
 * @param mContext The application context.
 */
class LocalLoginStateDataSource @Inject constructor(@ApplicationContext mContext: Context)
{
    companion object {
        private const val PREF_NAME = "AppPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }
    private val mSharedPreference: SharedPreferences by lazy {
        mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    private val _isLoggedIn = MutableStateFlow(getPersistedLoginStatus())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private fun getPersistedLoginStatus(): Boolean {
        return mSharedPreference.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveLoginStatus(isLoggedin: Boolean) {
        Timber.i("Saving login status: $isLoggedin")
        _isLoggedIn.value = isLoggedin
        mSharedPreference.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedin).apply()
    }

}