package com.prj.japanlib.feature_settings.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.data.local.dao.JWordDao
import com.prj.domain.model.profilescreen.User
import com.prj.domain.usecase.GetCurrentUserUseCase
import com.prj.domain.usecase.LogoutUseCase
import com.prj.domain.usecase.UpdateUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val jWordDao: JWordDao,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val logoutUseCase: LogoutUseCase
    ) : ViewModel() {

    private val _userInfoUiState: MutableStateFlow<UserUiState<User>> =
        MutableStateFlow(UserUiState.Loading)
    val userInfoUiState: StateFlow<UserUiState<User>> = _userInfoUiState.asStateFlow()
    private val _updateInfoUiState: MutableStateFlow<UserUiState<Boolean>> = MutableStateFlow(
        UserUiState.Loading
    )
    val updateInfoUiState: StateFlow<UserUiState<Boolean>> = _updateInfoUiState.asStateFlow()

    init {
        getUserInfo()
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            _userInfoUiState.value = UserUiState.Loading
            val result = getCurrentUserUseCase()
            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        _userInfoUiState.value = UserUiState.Success(user)
                    }
                },
                onFailure = { e ->
                    _userInfoUiState.value = UserUiState.Error(message = e.message)
                    Timber.e("Error getting user from Firestore: ${e.message}")
                }
            )
        }
    }

    fun updateUserInfo(name: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _updateInfoUiState.value = UserUiState.Loading
            val result = updateUserInfoUseCase(name, oldPassword, newPassword)
            result.fold(
                onSuccess = {
                    _updateInfoUiState.value = UserUiState.Success(true)
                    getUserInfo()
                },
                onFailure = { e ->
                    _updateInfoUiState.value = UserUiState.Error(message = e.message)
                    Timber.e("Error getting user from Firestore: ${e.message}")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    sealed class UserUiState<out T> {
        data object Loading : UserUiState<Nothing>()
        class Success<T>(val data : T) : UserUiState<T>()
        class Error<T>(val message: String?) : UserUiState<T>()
    }

}