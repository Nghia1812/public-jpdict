package com.prj.japanlib.feature_dictionary.viewmodel.implemetations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.domain.model.dictionaryscreen.ThemeCount
import com.prj.domain.usecase.GetAllCustomListsUseCase
import com.prj.domain.usecase.GetAllThemeListsUseCase
import com.prj.domain.usecase.GetAllJlptListsUseCase
import com.prj.japanlib.common.JLPT_LEVELS
import com.prj.japanlib.uistate.DictionaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the main dictionary screen, responsible for fetching and managing
 * summary data like word counts for different categories (JLPT, Custom Lists, Themes).
 *
 *
 * @param getAllJlptListsUseCase The usecase for accessing word-related data, injected by Hilt.
 * @param getAllThemeListsUseCase The usecase for accessing word-related data, injected by Hilt.
 * @param getAllCustomListsUseCase The usecase for accessing word-related data, injected by Hilt.
 * @param findWordListUseCase The usecase for searching words.
 * @param addWordToThemeUseCase The usecase for adding words to theme vocabulary.
 * @param removeWordFromThemeUseCase The usecase for removing words from theme vocabulary.
 * @param getWordsByThemeUseCase The usecase for retrieving words in a theme.
 */
@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val getAllJlptListsUseCase: GetAllJlptListsUseCase,
    private val getAllThemeListsUseCase: GetAllThemeListsUseCase,
    private val getAllCustomListsUseCase: GetAllCustomListsUseCase,
    private val findWordListUseCase: com.prj.domain.usecase.FindWordListUseCase,
    private val addWordToThemeUseCase: com.prj.domain.usecase.AddWordToThemeUseCase,
    private val removeWordFromThemeUseCase: com.prj.domain.usecase.RemoveWordFromThemeUseCase,
    private val getWordsByThemeUseCase: com.prj.domain.usecase.GetWordsByThemeUseCase
) : ViewModel() {
    /**
     * Internal state for the word count per JLPT level.
     */
    private val mWordCountUiState =
        MutableStateFlow<DictionaryUiState<List<WordList>>>(DictionaryUiState.Empty)
    /**
     * Public, read-only state flow exposing the word count per JLPT level to the UI.
     */
    val wordCountUiState = mWordCountUiState.asStateFlow()

    /**
     * Internal state for the user's custom-created word lists and their word counts.
     */
    private val mWordListUiState =
        MutableStateFlow<DictionaryUiState<List<WordList>>>(DictionaryUiState.Empty)
    /**
     * Public, read-only state flow exposing the custom word lists to the UI.
     */
    val customWordListUiState = mWordListUiState.asStateFlow()

    /**
     * Internal state for the theme-based word lists and their word counts.
     */
    private val mThemeListUiState =
        MutableStateFlow<DictionaryUiState<List<ThemeCount>>>(DictionaryUiState.Empty)
    /**
     * Public, read-only state flow exposing the theme-based word lists to the UI.
     */
    val themeListUiState = mThemeListUiState.asStateFlow()

    init {
        getWordCount(JLPT_LEVELS)
        getAllCustomWordLists()
        getAllThemeWordLists()
    }

    /**
     * Fetches the word count for a given list of JLPT levels.
     * Updates [mWordCountUiState] with Loading, Success, or Error states.
     * @param topicList A list of strings representing the JLPT levels (e.g., "N5", "N4").
     */
    fun getWordCount(topicList: List<String>) {
        viewModelScope.launch {
            mWordCountUiState.value = DictionaryUiState.Loading
            try {
                val counts = getAllJlptListsUseCase(topicList)
                mWordCountUiState.value = DictionaryUiState.Success(counts)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching jlpt word")
                mWordCountUiState.value =
                    DictionaryUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }


    /**
     * Fetches all user-created custom word lists along with their respective word counts.
     * It listens to a Flow from the repository and updates [mWordListUiState] on each emission.
     */
    fun getAllCustomWordLists() {
        viewModelScope.launch {
            mWordListUiState.value = DictionaryUiState.Loading
            // Collect from the flow to receive updates automatically when data changes.
            getAllCustomListsUseCase.getAllListsWithCount()
                .catch { e ->
                    Timber.e(e, "Error fetching custom word")
                    mWordListUiState.value =
                        DictionaryUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { lists ->
                    mWordListUiState.value = DictionaryUiState.Success(lists)
                }

        }
    }

    /**
     * Fetches all theme-based word lists along with their respective word counts.
     */
    fun getAllThemeWordLists() {
        viewModelScope.launch {
            mThemeListUiState.value = DictionaryUiState.Loading
            // Collect from the flow to receive updates automatically when data changes.
            getAllThemeListsUseCase().catch { e ->
                Timber.e(e, "Error fetching theme words")
                mThemeListUiState.value =
                    DictionaryUiState.Error(e.message ?: "Unknown error occurred")
            }
                .collect { lists ->
                    mThemeListUiState.value = DictionaryUiState.Success(lists)
                }
        }
    }

    /**
     * Internal state for search results
     */
    private val mSearchResultsUiState =
        MutableStateFlow<DictionaryUiState<List<com.prj.domain.model.dictionaryscreen.JapaneseWord>>>(DictionaryUiState.Empty)
    
    /**
     * Public, read-only state flow exposing the search results to the UI.
     */
    val searchResultsUiState = mSearchResultsUiState.asStateFlow()

    /**
     * Internal state for add word operation result
     */
    private val mAddWordResultUiState = MutableStateFlow<DictionaryUiState<Unit>>(DictionaryUiState.Empty)
    
    /**
     * Public, read-only state flow exposing the add word result to the UI.
     */
    val addWordResultUiState = mAddWordResultUiState.asStateFlow()

    /**
     * Internal state for remove word operation result
     */
    private val mRemoveWordResultUiState = MutableStateFlow<DictionaryUiState<Unit>>(DictionaryUiState.Empty)
    
    /**
     * Public, read-only state flow exposing the remove word result to the UI.
     */
    val removeWordResultUiState = mRemoveWordResultUiState.asStateFlow()

    /**
     * Internal state for words in the currently selected theme
     */
    private val mThemeWordsUiState = MutableStateFlow<DictionaryUiState<List<com.prj.domain.model.dictionaryscreen.JapaneseWord>>>(DictionaryUiState.Empty)
    
    /**
     * Public, read-only state flow exposing the theme words to the UI.
     */
    val themeWordsUiState = mThemeWordsUiState.asStateFlow()

    /**
     * Searches for words based on a search query.
     * Updates [mSearchResultsUiState] with Loading, Success, or Error states.
     * 
     * @param query The search query string.
     */
    fun searchWords(query: String) {
        viewModelScope.launch {
            mSearchResultsUiState.value = DictionaryUiState.Loading
            try {
                val results = findWordListUseCase(query)
                mSearchResultsUiState.value = if (results.isEmpty()) {
                    DictionaryUiState.Empty
                } else {
                    DictionaryUiState.Success(results)
                }
            } catch (e: Exception) {
                mSearchResultsUiState.value =
                    DictionaryUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Adds a word to a theme vocabulary list.
     * Updates [mAddWordResultUiState] with Loading, Success, or Error states.
     * 
     * @param themeId The ID of the theme to add the word to.
     * @param entryId The ID of the word entry to add.
     */
    fun addWordToTheme(themeId: Int, entryId: Int) {
        viewModelScope.launch {
            mAddWordResultUiState.value = DictionaryUiState.Loading
            try {
                addWordToThemeUseCase(themeId, entryId)
                mAddWordResultUiState.value = DictionaryUiState.Success(Unit)
                // Refresh theme words after addition
                loadThemeWords(themeId)
            } catch (e: Exception) {
                mAddWordResultUiState.value =
                    DictionaryUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Removes a word from a theme vocabulary list.
     * Updates [mRemoveWordResultUiState] with Loading, Success, or Error states.
     * 
     * @param themeId The ID of the theme to remove the word from.
     * @param entryId The ID of the word entry to remove.
     */
    fun removeWordFromTheme(themeId: Int, entryId: Int) {
        viewModelScope.launch {
            mRemoveWordResultUiState.value = DictionaryUiState.Loading
            try {
                removeWordFromThemeUseCase(themeId, entryId)
                mRemoveWordResultUiState.value = DictionaryUiState.Success(Unit)
                // Refresh theme words after removal
                loadThemeWords(themeId)
            } catch (e: Exception) {
                mRemoveWordResultUiState.value =
                    DictionaryUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Loads all words in a specific theme.
     * Updates [mThemeWordsUiState] with Loading, Success, or Error states.
     * 
     * @param themeId The ID of the theme to load words for.
     */
    fun loadThemeWords(themeId: Int) {
        viewModelScope.launch {
            mThemeWordsUiState.value = DictionaryUiState.Loading
            try {
                val words = getWordsByThemeUseCase(themeId)
                mThemeWordsUiState.value = if (words.isEmpty()) {
                    DictionaryUiState.Empty
                } else {
                    DictionaryUiState.Success(words)
                }
            } catch (e: Exception) {
                mThemeWordsUiState.value =
                    DictionaryUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Resets the add word result state to Empty.
     * Call this after handling the add word result.
     */
    fun resetAddWordResult() {
        mAddWordResultUiState.value = DictionaryUiState.Empty
    }

    /**
     * Resets the remove word result state to Empty.
     */
    fun resetRemoveWordResult() {
        mRemoveWordResultUiState.value = DictionaryUiState.Empty
    }

    /**
     * Resets the search results state to Empty.
     */
    fun resetSearchResults() {
        mSearchResultsUiState.value = DictionaryUiState.Empty
    }

    /**
     * Resets the theme words state to Empty.
     */
    fun resetThemeWords() {
        mThemeWordsUiState.value = DictionaryUiState.Empty
    }
}
