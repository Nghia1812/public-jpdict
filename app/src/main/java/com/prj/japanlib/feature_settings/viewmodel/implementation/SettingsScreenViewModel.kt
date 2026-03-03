package com.prj.japanlib.feature_settings.viewmodel.implementation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.data.repository.LoginStateRepository
import com.prj.domain.model.profilescreen.User
import com.prj.domain.model.settingsscreen.FontScale
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.usecase.GetCurrentFontScale
import com.prj.domain.usecase.GetCurrentNotiStatus
import com.prj.domain.usecase.GetCurrentUserUseCase
import com.prj.domain.usecase.ObserveFontScaleUseCase
import com.prj.domain.usecase.ObserveNotiStatus
import com.prj.domain.usecase.UpdateFontScaleUseCase
import com.prj.domain.usecase.UpdateNotiStatus
import com.prj.domain.usecase.GetNotificationPreferencesUseCase
import com.prj.domain.usecase.UpdateNotificationPreferencesUseCase
import com.prj.domain.usecase.GetRandomWordForNotificationUseCase
import com.prj.domain.model.NotificationPreferences
import com.prj.data.helper.NotificationScheduler
import com.prj.data.notification.NotificationSender
import com.prj.domain.model.ProgressStats
import com.prj.japanlib.common.utils.AppLocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    loginStateRepository: LoginStateRepository,
    private val settingsRepository: IAppSettingsRepository,
    private val observeFontScale: ObserveFontScaleUseCase,
    private val updateFontScaleUseCase: UpdateFontScaleUseCase,
    private val getCurrentFontScale: GetCurrentFontScale,
    private val observeNotiStatus: ObserveNotiStatus,
    private val updateNotiStatus: UpdateNotiStatus,
    private val getCurrentNotiStatus: GetCurrentNotiStatus,
    @param:ApplicationContext private val context: Context,
    private val getNotificationPreferencesUseCase: GetNotificationPreferencesUseCase,
    private val updateNotificationPreferencesUseCase: UpdateNotificationPreferencesUseCase,
    private val getRandomWordUseCase: GetRandomWordForNotificationUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val notificationSender: NotificationSender
) : ViewModel(){
    val isLoggedIn: StateFlow<Boolean> = loginStateRepository.isLoggedIn
    private val mUserInfoUiState: MutableStateFlow<SettingsUiState<User>> =
        MutableStateFlow(SettingsUiState.Loading)
    val userInfoUiState = mUserInfoUiState.asStateFlow()
    private var mFontScale = MutableStateFlow(FontScale.MEDIUM)
    val fontScale = mFontScale.asStateFlow()
    private var mNotificationEnabled = MutableStateFlow(false)
    val notificationEnabled = mNotificationEnabled.asStateFlow()
    private val mSelectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage = mSelectedLanguage.asStateFlow()
    private val mNotificationPreferences = MutableStateFlow(NotificationPreferences())
    val notificationPreferences = mNotificationPreferences.asStateFlow()

    init {
        // Observe Font Scale
        viewModelScope.launch {
            observeFontScale().collect {
                mFontScale.value = it
            }
        }

        // Observe Notification Status
        viewModelScope.launch {
            observeNotiStatus().collect {
                mNotificationEnabled.value = it
            }
        }

        // Observe Language
        viewModelScope.launch {
            settingsRepository.languageFlow.collect {
                mSelectedLanguage.value = it
            }
        }

        // Load and Observe Notification Preferences
        loadNotificationPreferences()

        getUserInfo()
    }

    private fun loadNotificationPreferences() {
        viewModelScope.launch {
            mNotificationPreferences.value = getNotificationPreferencesUseCase()
        }
    }


    fun changeLanguage(languageCode: String) {
        viewModelScope.launch{
            Timber.i("Changing language to $languageCode")
            settingsRepository.updateLanguage(languageCode)
            mSelectedLanguage.value = AppLanguage.fromString(languageCode)
        }
        AppLocaleManager.changeLanguage(context, languageCode)
    }

    fun getUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = getCurrentUserUseCase()
            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        mUserInfoUiState.value = SettingsUiState.Success(user)
                    } else {
                        mUserInfoUiState.value = SettingsUiState.Empty
                    }
                },
                onFailure = { e ->
                    mUserInfoUiState.value = SettingsUiState.Error(e.message ?: "Unknown error" )
                    Timber.e("Error getting user from Firestore: ${e.message}")
                }
            )
        }
    }

    fun updateFontScale(scale: Float) {
        viewModelScope.launch {
            updateFontScaleUseCase(scale)
        }
    }

    /**
     * User toggle notification in Settings UI
     */
    fun updateNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateNotiStatus(enabled)
            val updatedPrefs = mNotificationPreferences.value.copy(notificationEnabled = enabled)
            updateNotificationPreferences(updatedPrefs)
        }
    }

    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            updateNotificationPreferencesUseCase(preferences)
            mNotificationPreferences.value = preferences
            notificationScheduler.scheduleAllNotifications(preferences)
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            val wordData = getRandomWordUseCase()
            wordData?.let {
                notificationSender.sendWordNotification(it)
                notificationSender.sendProgressNotification(ProgressStats())
                notificationSender.sendReviewReminderNotification(10)
            }
        }
    }

    /**
     * Ask system permission (Android 13+)
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    sealed class SettingsUiState<out T> {
        object Loading : SettingsUiState<Nothing>()
        object Empty : SettingsUiState<Nothing>()
        data class Success<out T>(val data : T) : SettingsUiState<T>()
        data class Error(val message: String?) : SettingsUiState<Nothing>()
    }
}
