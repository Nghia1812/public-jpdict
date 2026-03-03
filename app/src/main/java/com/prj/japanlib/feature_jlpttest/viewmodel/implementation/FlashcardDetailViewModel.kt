package com.prj.japanlib.feature_jlpttest.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.repository.ITopicVocabularyRepository
import com.prj.domain.repository.IWordRepository
import com.prj.domain.model.testscreen.WordListType
import com.prj.domain.usecase.GetLearningPreferencesUseCase
import com.prj.domain.usecase.GetWordsByLearningStateUseCase
import com.prj.domain.usecase.UpdateLearningStateUseCase
import com.prj.japanlib.uistate.TestScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the detail view of flashcards.
 *
 * This ViewModel handles fetching words based on their learning state and updating
 * the learning progress of individual words. It also manages user preferences
 * such as whether to show the meaning first and if the word list should be shuffled.
 *
 * @property mGetWordsByLearningStateUseCase Use case to fetch words filtered by state and list type.
 * @property mUpdateLearningStateUseCase Use case to update the learning state of a word.
 * @property mGetLearningPreferencesUseCase Use case to retrieve user preferences for learning.
 */
@HiltViewModel
class FlashcardDetailViewModel @Inject constructor(
    private val mGetWordsByLearningStateUseCase: GetWordsByLearningStateUseCase,
    private val mUpdateLearningStateUseCase: UpdateLearningStateUseCase,
    private val mGetLearningPreferencesUseCase: GetLearningPreferencesUseCase
): ViewModel(){
    /**
     * Internal state flow for the list of Japanese words to be displayed.
     */
    private val mWordsUiState = MutableStateFlow<TestScreenUiState<List<JapaneseWord>>>(TestScreenUiState.Empty)
    
    /**
     * Exposed read-only state flow for the list of Japanese words.
     */
    val wordsUiState = mWordsUiState.asStateFlow()

    /**
     * Internal state flow for the preference of showing meaning first.
     */
    val mShowMeaning = MutableStateFlow(false)
    
    /**
     * Exposed read-only state flow for the show meaning preference.
     */
    val showMeaning = mShowMeaning.asStateFlow()

    init {
        // Initialize show meaning preference from repository
        viewModelScope.launch(Dispatchers.IO) {
            mShowMeaning.value = mGetLearningPreferencesUseCase.getShowMeaningFirstPreference()
        }
    }

    /**
     * Fetches words associated with a specific list and learning state.
     *
     * The results are optionally shuffled based on user preferences before being posted to [wordsUiState].
     *
     * @param listId The ID of the list (topic or favorite list) to fetch words from.
     * @param newState The learning state to filter by.
     * @param listType The type of list being accessed (e.g., Topic or Favorites).
     */
    fun getWordsByLearningState(listId: String, newState: LearningState, listType: WordListType){
        viewModelScope.launch(Dispatchers.IO) {
            mGetWordsByLearningStateUseCase(listId, newState, listType).let {
                mWordsUiState.value =
                    if (!mGetLearningPreferencesUseCase.getShufflePreference())
                        TestScreenUiState.Success(it)
                    else
                        TestScreenUiState.Success(it.shuffled())
            }
        }
    }

    /**
     * Updates the learning state for a specific word entry.
     *
     * @param listId The ID of the list the word belongs to.
     * @param entryId The unique identifier of the word entry.
     * @param newState The new learning state to be assigned.
     * @param listType The type of list being accessed.
     */
    fun updateLearningState(listId: String, entryId: Int, newState: LearningState, listType: WordListType) {
        viewModelScope.launch(Dispatchers.IO) {
            mUpdateLearningStateUseCase(listId, entryId, newState, listType)
        }
    }
}
