package com.prj.japanlib.feature_dictionary.viewmodel.implemetations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.usecase.AddWordToCustomListUseCase
import com.prj.domain.usecase.CreateCustomListUseCase
import com.prj.domain.usecase.GetAllCustomListsUseCase
import com.prj.domain.usecase.GetTutorialStatusUseCase
import com.prj.domain.usecase.RemoveWordFromListUseCase
import com.prj.domain.usecase.SaveTutorialStatusUseCase
import com.prj.japanlib.uistate.DictionaryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * An abstract base ViewModel that provides shared logic for vocabulary screens.
 *
 * This class encapsulates the business logic for managing and displaying swipe tutorial hints
 * (e.g., "swipe left for more words"). It interacts with use cases to check if the user
 * has already seen these hints and saves their status when they are dismissed.
 *
 *
 * @property mGetTutorialStatusUseCase Use case to retrieve the saved status of tutorial hints.
 * @property mSaveTutorialStatusUseCase Use case to save the status of tutorial hints.
 */
abstract class BaseVocabularyViewModel(
    private val mGetTutorialStatusUseCase: GetTutorialStatusUseCase,
    private val mSaveTutorialStatusUseCase: SaveTutorialStatusUseCase,
    private val mCreateCustomListUseCase: CreateCustomListUseCase,
    private val mGetAllCustomListsUseCase: GetAllCustomListsUseCase,
    private val mAddWordToCustomListUseCase: AddWordToCustomListUseCase,
    private val mRemoveWordFromListUseCase: RemoveWordFromListUseCase
) : ViewModel() {
    // A private mutable state flow to control the visibility of the "swipe left" hint.
    private val mShowLeftSwipeHint = MutableStateFlow(false)
    val showLeftSwipeHint: StateFlow<Boolean> = mShowLeftSwipeHint.asStateFlow()

    // A private mutable state flow to control the visibility of the "swipe right" hint.
    private val _showRightSwipeHint = MutableStateFlow(false)
    val showRightSwipeHint: StateFlow<Boolean> = _showRightSwipeHint.asStateFlow()

    private val mCustomLists = MutableStateFlow<DictionaryUiState<List<CustomWordListWithEntries>>>(
        DictionaryUiState.Empty
    )
    val customWordList = mCustomLists.asStateFlow()

    init {
        checkTutorialStatus()
    }

    /**
     * Checks if the user has seen the initial "swipe left" tutorial hint.
     * If not, it updates the state to show the hint.
     */
    private fun checkTutorialStatus() {
        viewModelScope.launch {
            val hasSeenLeft = mGetTutorialStatusUseCase.hasSeenLeftSwipe()
            mShowLeftSwipeHint.value = !hasSeenLeft
        }
    }

    /**
     * Called when the page in a pager changes. It's used to trigger the "swipe right" hint
     * when the user navigates to the second page (index 1) for the first time.
     *
     * @param page The index of the new page.
     */
    fun onPageChanged(page: Int) {
        viewModelScope.launch {
            if (page == 1) {
                val hasSeenRight = mGetTutorialStatusUseCase.hasSeenRightSwipe()
                if (!hasSeenRight) {
                    mShowLeftSwipeHint.value = false
                    _showRightSwipeHint.value = true
                }
            }
        }
    }

    /**
     * Called when the user dismisses the "swipe left" hint.
     * It marks the hint as seen in the repository and updates the UI state to hide it.
     */
    fun onLeftSwipeHintDismissed() {
        viewModelScope.launch {
            mSaveTutorialStatusUseCase.markLeftSwipeSeen()
            mShowLeftSwipeHint.value = false
        }
    }

    /**
     * Called when the user dismisses the "swipe right" hint.
     * It marks the hint as seen in the repository and updates the UI state to hide it.
     */
    fun onRightSwipeHintDismissed() {
        viewModelScope.launch {
            mSaveTutorialStatusUseCase.markRightSwipeSeen()
            _showRightSwipeHint.value = false
        }
    }

    fun createNewCustomList(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mCreateCustomListUseCase(name)
                .onSuccess {
                    getAllCustomLists()
                }.onFailure {
                    mCustomLists.value = DictionaryUiState.Error(
                        it.message ?: "An unexpected error occurred"
                    )
                }
        }
    }

    fun getAllCustomLists() {
        mCustomLists.value = DictionaryUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            mGetAllCustomListsUseCase.getAllLists()
                .catch { e ->
                    mCustomLists.value = DictionaryUiState.Error(
                        e.message ?: "An unexpected error occurred"
                    )
                }
                .collect { lists ->
                    mCustomLists.value = DictionaryUiState.Success(lists)
                }
        }
    }

    fun addWordToCustomList(listId: String, entryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mAddWordToCustomListUseCase(listId, entryId)
                .onSuccess {
                    Timber.d("Word added to custom list")
                }.onFailure {
                    Timber.e("Failed to add word to custom list: ${it.message}")
                }
        }
    }

    fun removeWordFromCustomList(listId: String, entryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mRemoveWordFromListUseCase(listId, entryId)
                .onSuccess {
                    Timber.d("Word removed")
                }.onFailure {
                    Timber.e("Failed to remove word from custom list: ${it.message}")
                }
        }
    }
}