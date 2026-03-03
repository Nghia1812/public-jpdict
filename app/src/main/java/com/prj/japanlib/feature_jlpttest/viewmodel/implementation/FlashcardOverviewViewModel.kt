package com.prj.japanlib.feature_jlpttest.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.testscreen.ListWordCountWithState
import com.prj.domain.model.testscreen.WordListType
import com.prj.domain.usecase.GetLearningPreferencesUseCase
import com.prj.domain.usecase.GetListLearningStateUseCase
import com.prj.domain.usecase.SetLearningPreferencesUseCase
import com.prj.japanlib.uistate.TestScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/*** ViewModel for the Flashcard Overview screen.
 *
 * This ViewModel handles fetching the learning status of word lists and managing
 * user preferences for flashcard sessions, such as shuffling words and showing
 * meanings first.
 *
 * @property mGetListLearningStateUseCase Use case for getting word counts per learning state.
 * @property mSetLearningPreferencesUseCase Use case for saving learning preferences.
 * @property mGetLearningPreferencesUseCase Use case for retrieving learning preferences.
 */
@HiltViewModel
class FlashcardOverviewViewModel @Inject constructor(
    private val mGetListLearningStateUseCase: GetListLearningStateUseCase,
    private val mSetLearningPreferencesUseCase: SetLearningPreferencesUseCase,
    private val mGetLearningPreferencesUseCase: GetLearningPreferencesUseCase
) : ViewModel() {
    /**
     * Internal state flow for the learning status of the selected list.
     */
    private val mLearningStateUiState = MutableStateFlow<TestScreenUiState<ListWordCountWithState>>(TestScreenUiState.Loading)

    /**
     * UI state flow for the learning status, exposed to the view.
     */
    val learningStateUiState = mLearningStateUiState.asStateFlow()

    /**
     * Internal state flow for the shuffle words preference.
     */
    private val mShuffleWords = MutableStateFlow<Boolean>(false)

    /**
     * State flow for the shuffle preference, exposed to the view.
     */
    val shuffleWords = mShuffleWords.asStateFlow()

    /**
     * Internal state flow for the "show meaning first" preference.
     */
    private val mShowMeaningFirst = MutableStateFlow<Boolean>(false)

    /**
     * State flow for the "show meaning first" preference, exposed to the view.
     */
    val showMeaningFirst = mShowMeaningFirst.asStateFlow()

    init {
        getPreferencesState()
    }

    /**
     * Loads the learning status for a specific word list.
     *
     * @param listId The ID of the word list (e.g., topic ID).
     * @param listType The type of the word list (e.g., Topic, Favorites).
     */
    fun loadLearningStatus(listId: String, listType: WordListType) {
        mLearningStateUiState.value = TestScreenUiState.Loading
        Timber.i("loadLearningStatus for list with: $listId, $listType")
        viewModelScope.launch(Dispatchers.IO) {
            val result = mGetListLearningStateUseCase(listId, listType)
            if(result.totalCount == 0){
                mLearningStateUiState.value = TestScreenUiState.Empty
                return@launch
            }
            mLearningStateUiState.value = TestScreenUiState.Success(result)
        }
    }

    /**
     * Updates the shuffle words preference.
     *
     * @param shuffle True if words should be shuffled, false otherwise.
     */
    fun setShuffleWordsPreference(shuffle: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            mSetLearningPreferencesUseCase.setShufflePreference(shuffle)
            mShuffleWords.value = mGetLearningPreferencesUseCase.getShufflePreference()
        }
    }

    /**
     * Updates the "show meaning first" preference.
     *
     * @param show True if meaning should be shown first, false otherwise.
     */
    fun setShowMeaningFirstPreference(show: Boolean) {
        mShowMeaningFirst.value = show
        viewModelScope.launch(Dispatchers.IO) {
            mSetLearningPreferencesUseCase.setShowMeaningFirstPreference(show)
            // Note: Updated to correctly reflect the preference being set
            mShowMeaningFirst.value = mGetLearningPreferencesUseCase.getShowMeaningFirstPreference()
        }
    }

    /**
     * Fetches the initial preference states from the repository.
     */
    private fun getPreferencesState(){
        viewModelScope.launch(Dispatchers.IO) {
            mShuffleWords.value = mGetLearningPreferencesUseCase.getShufflePreference()
            mShowMeaningFirst.value = mGetLearningPreferencesUseCase.getShowMeaningFirstPreference()
        }
    }
}
