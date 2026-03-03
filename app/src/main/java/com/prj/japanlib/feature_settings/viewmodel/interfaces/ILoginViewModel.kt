package com.prj.japanlib.feature_settings.viewmodel.interfaces

import android.app.Activity
import com.prj.japanlib.uistate.LoginUiState
import kotlinx.coroutines.flow.StateFlow

interface ILoginViewModel {
    val loginUiState: StateFlow<LoginUiState>
    val resetPasswordUiState: StateFlow<LoginUiState>
    val isResetPasswordMode: StateFlow<Boolean>
    fun saveLoginStatus(isLoggedin: Boolean)
    fun loginUser(email: String, password: String)
    fun validateEmail(email: String)
    fun registerUser(email: String, password: String)
    fun loginUserWithGoogle(activity: Activity)
    fun enterResetPasswordMode()
    fun exitResetPasswordMode()
    fun sendPasswordResetEmail(email: String)
    fun resetResetPasswordState()
    fun loadWordsFromOnlineStorage()
}