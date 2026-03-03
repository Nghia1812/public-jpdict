package com.prj.japanlib.uistate

sealed class DictionaryUiState<out T> {
    data object Loading : DictionaryUiState<Nothing>()
    data object Empty : DictionaryUiState<Nothing>()
    class Success<out T>(val data: T) : DictionaryUiState<T>()
    class Error(val message: String?) : DictionaryUiState<Nothing>()
}