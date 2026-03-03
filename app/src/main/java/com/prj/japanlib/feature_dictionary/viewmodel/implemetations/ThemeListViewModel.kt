package com.prj.japanlib.feature_dictionary.viewmodel.implemetations

import androidx.lifecycle.viewModelScope
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.usecase.AddWordToCustomListUseCase
import com.prj.domain.usecase.CreateCustomListUseCase
import com.prj.domain.usecase.GetAllCustomListsUseCase
import com.prj.domain.usecase.GetThemeWordListUseCase
import com.prj.domain.usecase.GetTutorialStatusUseCase
import com.prj.domain.usecase.RemoveWordFromListUseCase
import com.prj.domain.usecase.SaveTutorialStatusUseCase
import com.prj.japanlib.uistate.DictionaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing and providing data for the theme-based vocabulary screen.
 *
 *
 * @param getThemeWordListUseCase The usecase responsible for fetching topic-based vocabulary,
 *                        injected by Hilt.
 */
@HiltViewModel
class ThemeListViewModel @Inject constructor(
    private val getThemeWordListUseCase: GetThemeWordListUseCase,
    mGetTutorialStatusUseCase: GetTutorialStatusUseCase,
    mSaveTutorialStatusUseCase: SaveTutorialStatusUseCase,
    mGetAllCustomListsUseCase: GetAllCustomListsUseCase,
    mCreateCustomListUseCase: CreateCustomListUseCase,
    mAddWordToCustomListUseCase: AddWordToCustomListUseCase,
    mRemoveWordFromListUseCase: RemoveWordFromListUseCase,
) : BaseVocabularyViewModel(
    mGetTutorialStatusUseCase, mSaveTutorialStatusUseCase, mCreateCustomListUseCase,
    mGetAllCustomListsUseCase, mAddWordToCustomListUseCase, mRemoveWordFromListUseCase
) {
    /**
     * Internal, mutable state flow to hold the current UI state for the word list.
     */
    private val mWordListMutableStateFlow = MutableStateFlow<DictionaryUiState<List<JapaneseWord>>>(
        value = DictionaryUiState.Empty
    )
    /**
     * Public, read-only state flow that exposes the word list UI state to the UI.
     */
    val wordListMutableStateFlow = mWordListMutableStateFlow.asStateFlow()

    /**
     * Fetches the words for a specific theme list by its name.
     * It updates the [mWordListMutableStateFlow] state flow to reflect the loading and success states.
     *
     * @param themeId The name of the theme to fetch.
     */
    fun getCustomWordList(themeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mWordListMutableStateFlow.value = DictionaryUiState.Loading
            try {
                // Fetch the list of words from the repository.
                val list = getThemeWordListUseCase(themeId)
                if (list.isEmpty()) {
                    mWordListMutableStateFlow.value =
                        DictionaryUiState.Error("No words found in this list")
                } else {
                    // On success, update the state with the retrieved list.
                    mWordListMutableStateFlow.value = DictionaryUiState.Success(list)
                }
            } catch (e: Exception){
                // Handle any exception that occurs during data fetching
                mWordListMutableStateFlow.value =
                    DictionaryUiState.Error(e.message ?: "Unknown error occurred")
            }

        }
    }
}