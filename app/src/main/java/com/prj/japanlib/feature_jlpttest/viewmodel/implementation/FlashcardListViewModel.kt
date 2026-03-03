package com.prj.japanlib.feature_jlpttest.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.testscreen.ListWordCountWithState
import com.prj.domain.usecase.GetCustomCountWithStateUseCase
import com.prj.domain.usecase.GetJlptCountWithStateUseCase
import com.prj.domain.usecase.GetThemeCountWithStateUseCase
import com.prj.japanlib.uistate.TestScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardListViewModel @Inject constructor(
    private val getAllJlptListsUseCase: GetJlptCountWithStateUseCase,
    private val getAllThemeListsUseCase: GetThemeCountWithStateUseCase,
    private val getAllCustomListsUseCase: GetCustomCountWithStateUseCase
) : ViewModel() {
    private val mJlptCountUiState =
        MutableStateFlow<TestScreenUiState<List<ListWordCountWithState>>>(TestScreenUiState.Empty)

    val jlptWordCountUiState = mJlptCountUiState.asStateFlow()

    /**
     * Internal state for the user's custom-created word lists and their word counts.
     */
    private val mCustomWordUiState =
        MutableStateFlow<TestScreenUiState<List<ListWordCountWithState>>>(TestScreenUiState.Empty)
    /**
     * Public, read-only state flow exposing the custom word lists to the UI.
     */
    val customWordListUiState = mCustomWordUiState.asStateFlow()

    /**
     * Internal state for the theme-based word lists and their word counts.
     */
    private val mThemeListUiState =
        MutableStateFlow<TestScreenUiState<List<ListWordCountWithState>>>(TestScreenUiState.Empty)
    /**
     * Public, read-only state flow exposing the theme-based word lists to the UI.
     */
    val themeListUiState = mThemeListUiState.asStateFlow()

    init {
        // Fetch all data when the ViewModel is created
        getWordCount()
        getAllCustomWordLists()
        getAllThemeWordLists()
    }

    /**
     * Fetches all JLPT word lists along with their respective word counts.
     */
    fun getWordCount() {
        viewModelScope.launch {
            mJlptCountUiState.value = TestScreenUiState.Loading
            getAllJlptListsUseCase()
                .catch { e ->
                    mJlptCountUiState.value =
                        TestScreenUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { lists ->
                    if (lists.isEmpty()) {
                        mJlptCountUiState.value = TestScreenUiState.Empty
                    } else {
                        mJlptCountUiState.value = TestScreenUiState.Success(lists)
                    }
                }
        }
    }


    /**
     * Fetches all user-created custom word lists along with their respective word counts.
     * It listens to a Flow from the repository and updates [mCustomWordUiState] on each emission.
     */
    fun getAllCustomWordLists() {
        viewModelScope.launch {
            mCustomWordUiState.value = TestScreenUiState.Loading
            // Collect from the flow to receive updates automatically when data changes.
            getAllCustomListsUseCase()
                .catch { e ->
                    mCustomWordUiState.value =
                        TestScreenUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { lists ->
                    if (lists.isEmpty()) {
                        mCustomWordUiState.value = TestScreenUiState.Empty
                    } else {
                        mCustomWordUiState.value = TestScreenUiState.Success(lists)
                    }
                }

        }
    }

    /**
     * Fetches all theme-based word lists along with their respective word counts.
     */
    fun getAllThemeWordLists() {
        viewModelScope.launch {
            mThemeListUiState.value = TestScreenUiState.Loading
            // Collect from the flow to receive updates automatically when data changes.
            getAllThemeListsUseCase().catch { e ->
                mThemeListUiState.value =
                    TestScreenUiState.Error(e.message ?: "Unknown error occurred")
            }
                .collect { lists ->
                    if (lists.isEmpty()) {
                        mThemeListUiState.value = TestScreenUiState.Empty
                    } else {
                        mThemeListUiState.value = TestScreenUiState.Success(lists)
                    }
                }
        }
    }
}