package com.prj.japanlib.feature_translator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.translatescreen.TranslationResult
import com.prj.domain.usecase.TranslateTextUseCase
import com.prj.domain.usecase.CheckNetworkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val mTranslateUseCase: TranslateTextUseCase,
    private val checkNetworkUseCase: CheckNetworkUseCase
) : ViewModel() {
    private val _mTranslations = MutableStateFlow<TranslationUiState<TranslationResult>>(
        value = TranslationUiState.Empty
    )
    val translations = _mTranslations.asStateFlow()

    fun getTranslatedText(text: String, source: String, target: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isConnected = checkNetworkUseCase().stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = true
            )
            if (!isConnected.value) {
                _mTranslations.value = TranslationUiState.Error("No internet connection")
                return@launch
            }
            _mTranslations.value = TranslationUiState.Loading
            val result = mTranslateUseCase(text, source, target, onRetry = { attempt ->
                _mTranslations.value =
                    TranslationUiState.Retrying(attempt, 3)
            })
            result.fold(
                onSuccess = {
                    _mTranslations.value = TranslationUiState.Success(it)
                },
                onFailure = {
                    if(it is IllegalStateException){
                        Timber.e("Error mapping json data: ${it.message}")
                        _mTranslations.value = TranslationUiState.Error("Server error")
                    } else {
                        Timber.e("Network error: ${it.message}")
                        _mTranslations.value = TranslationUiState.Error(it.message ?: "")
                    }
                }
            )

        }
    }
    sealed class TranslationUiState<out T> {
        class Success<T>(val data: T) : TranslationUiState<T>()
        class Error<T>(val message: String) : TranslationUiState<T>()
        class Retrying(val attempt: Int, val max: Int) : TranslationUiState<Nothing>()
        object Loading : TranslationUiState<Nothing>()
        object Empty : TranslationUiState<Nothing>()
    }
}