package com.prj.japanlib.feature_jlpttest.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.model.testscreen.TestItem
import com.prj.domain.usecase.CheckNetworkUseCase
import com.prj.domain.usecase.GetTestsForLevelUseCase
import com.prj.japanlib.uistate.TestScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestListViewModel @Inject constructor(
    private val getTestsForLevelUseCase: GetTestsForLevelUseCase,
    private val mCheckNetworkUseCase: CheckNetworkUseCase
) : ViewModel() {
    private var _testList = MutableStateFlow<TestScreenUiState<List<TestItem>>>(TestScreenUiState.Empty)
    val testList = _testList.asStateFlow()

    fun getTestsForLevel(source: Source, level: Level) {
        _testList.value = TestScreenUiState.Loading
        viewModelScope.launch {
            val isConnected = mCheckNetworkUseCase().first()
            if (!isConnected) {
                _testList.value = TestScreenUiState.NoInternet
                return@launch
            }
            val res = getTestsForLevelUseCase(source, level)
            res.fold(
                onSuccess = { _testList.value = TestScreenUiState.Success(it.items) },
                onFailure = { _testList.value = TestScreenUiState.Error(it.message) }
            )
        }
    }
}