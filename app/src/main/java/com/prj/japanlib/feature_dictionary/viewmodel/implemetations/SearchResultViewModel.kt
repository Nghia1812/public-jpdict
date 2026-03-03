package com.prj.japanlib.feature_dictionary.viewmodel.implemetations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.repository.IWordRepository
import com.prj.domain.usecase.AddWordToCustomListUseCase
import com.prj.domain.usecase.CreateCustomListUseCase
import com.prj.domain.usecase.FindWordListUseCase
import com.prj.domain.usecase.GetAllCustomListsUseCase
import com.prj.domain.usecase.RemoveWordFromListUseCase
import com.prj.japanlib.uistate.DictionaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the search result screen.
 *
 * This ViewModel is responsible for fetching and managing the list of Japanese words
 * based on a search query provided by the user. It interacts with the domain layer
 * through the [IWordRepository] to retrieve search results.
 *
 * @property mFindWordListUseCase The usecase for accessing word data.
 */
@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val mFindWordListUseCase: FindWordListUseCase,
    private val mCreateCustomListUseCase: CreateCustomListUseCase,
    private val mGetAllCustomListsUseCase: GetAllCustomListsUseCase,
    private val mAddWordToCustomListUseCase: AddWordToCustomListUseCase,
    private val mRemoveWordFromListUseCase: RemoveWordFromListUseCase
) : ViewModel() {
    private val mWordListUiStateFlow = MutableStateFlow<DictionaryUiState<List<JapaneseWord>>>(
        value = DictionaryUiState.Empty
    )
    val wordListUiStateFlow = mWordListUiStateFlow.asStateFlow()
    private val mCustomLists = MutableStateFlow<DictionaryUiState<List<CustomWordListWithEntries>>>(
        DictionaryUiState.Empty
    )
    val customWordList = mCustomLists.asStateFlow()


    /**
     * Fetches a list of words that match the given search query.
     *
     * This function launches a coroutine in the [viewModelScope] to perform the search
     * asynchronously.
     *
     * @param search The search query string used to find words.
     */
    fun getSearchWordList(search: String) {
        viewModelScope.launch {
            mWordListUiStateFlow.value = DictionaryUiState.Loading
            val list = mFindWordListUseCase(search)
            Timber.d("getSearchWordList: $list")
            if (list.isEmpty()){
                mWordListUiStateFlow.value = DictionaryUiState.Empty
            } else {
                mWordListUiStateFlow.value = DictionaryUiState.Success(list)
            }
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