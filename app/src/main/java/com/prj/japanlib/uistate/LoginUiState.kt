package com.prj.japanlib.uistate

sealed class LoginUiState {
    object Loading : LoginUiState()
    object Empty : LoginUiState()
    object Success : LoginUiState()
    object Exists : LoginUiState()
    object NotExists : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}