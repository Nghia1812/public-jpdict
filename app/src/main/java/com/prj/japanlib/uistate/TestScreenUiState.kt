package com.prj.japanlib.uistate


sealed class TestScreenUiState<out T> {
    data object Loading : TestScreenUiState<Nothing>()
    data object Empty : TestScreenUiState<Nothing>()
    data object NoInternet : TestScreenUiState<Nothing>()
    class Success<out T>(val data: T) : TestScreenUiState<T>()
    class Error(val message: String?) : TestScreenUiState<Nothing>()
}