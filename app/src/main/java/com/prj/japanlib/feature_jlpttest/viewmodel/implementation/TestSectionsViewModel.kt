package com.prj.japanlib.feature_jlpttest.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.model.testscreen.BaseTestSection
import com.prj.domain.usecase.CheckNetworkUseCase
import com.prj.domain.usecase.GetTestSectionsUseCase
import com.prj.japanlib.uistate.TestScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class TestSectionsViewModel @Inject constructor(
    private val mGetTestSectionsUseCase: GetTestSectionsUseCase,
    private val mCheckNetworkUseCase: CheckNetworkUseCase
) : ViewModel() {
    private val mTestSections = MutableStateFlow<TestScreenUiState<List<BaseTestSection>>>(TestScreenUiState.Empty)
    val testSections = mTestSections.asStateFlow()

    fun getTestSections(source: Source, level: Level, testId: String) {
        mTestSections.value = TestScreenUiState.Loading
        viewModelScope.launch {
            val isConnected = mCheckNetworkUseCase().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true
            )
            if (!isConnected.value) {
                mTestSections.value = TestScreenUiState.NoInternet
                return@launch
            }
            val res = mGetTestSectionsUseCase(source, level, testId)
            res.fold(
                onSuccess = { mTestSections.value = TestScreenUiState.Success(it) },
                onFailure = {
                    Timber.e("Error getting data: $it")
                    mTestSections.value = TestScreenUiState.Error(it.message)
                }
            )
        }
    }



}