package com.prj.japanlib.feature_dictionary.viewmodel.implemetations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.data.local.dao.ExampleDao
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.ExampleWithFurigana
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.dictionaryscreen.KanjiDetail
import com.prj.domain.usecase.AddWordToCustomListUseCase
import com.prj.domain.usecase.CreateCustomListUseCase
import com.prj.domain.usecase.GetAllCustomListsUseCase
import com.prj.domain.usecase.GetKanjiDetailUseCase
import com.prj.domain.usecase.GetRelatedWordListUseCase
import com.prj.domain.usecase.GetWordByIdUseCase
import com.prj.domain.usecase.RemoveWordFromListUseCase
import com.prj.domain.usecase.TokenizeTextUseCase
import com.prj.japanlib.uistate.DictionaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the word detail screen.
 *
 *  @param mExampleDao The data access object for examples.
 *  @param mTokenizeTextUseCase The use case for tokenizing text.
 *  @param mGetWordByIdUseCase The use case for getting a word by its ID.
 *  @param mGetAllCustomListsUseCase The use case for getting all custom lists.
 *  @param mCreateCustomListUseCase The use case for creating a new custom list.
 *  @param mAddWordToCustomListUseCase The use case for adding a word to a custom list.
 *  @param mRemoveWordFromListUseCase The use case for removing a word from a custom list.
 *  @param mGetKanjiDetailUseCase The use case for getting kanji details.
 *  @param mGetRelatedWordsUseCase The use case for getting related words
 */
@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val mExampleDao: ExampleDao,
    private val mTokenizeTextUseCase: TokenizeTextUseCase,
    private val mGetWordByIdUseCase: GetWordByIdUseCase,
    private val mGetAllCustomListsUseCase: GetAllCustomListsUseCase,
    private val mCreateCustomListUseCase: CreateCustomListUseCase,
    private val mAddWordToCustomListUseCase: AddWordToCustomListUseCase,
    private val mRemoveWordFromListUseCase: RemoveWordFromListUseCase,
    private val mGetKanjiDetailUseCase: GetKanjiDetailUseCase,
    private val mGetRelatedWordsUseCase: GetRelatedWordListUseCase
    ) : ViewModel() {
    private val mExamples = MutableStateFlow<DictionaryUiState<List<ExampleWithFurigana>>>(
        DictionaryUiState.Empty
    )
    val examples = mExamples.asStateFlow()
    private val mCustomLists = MutableStateFlow<DictionaryUiState<List<CustomWordListWithEntries>>>(
        DictionaryUiState.Empty
    )
    val customWordList = mCustomLists.asStateFlow()
    private val mWord = MutableStateFlow(
        JapaneseWord(
            id = 0,
            kanji = "",
            reading = "",
            meaning = "",
            type = ""
        )
    )
    val word: StateFlow<JapaneseWord> = mWord.asStateFlow()
    private val mKanjiDetail = MutableStateFlow<DictionaryUiState<List<KanjiDetail>>>(
        DictionaryUiState.Empty
    )
    val kanjiDetail = mKanjiDetail.asStateFlow()
    private val mRelatedWords = MutableStateFlow<DictionaryUiState<List<JapaneseWord>>>(
        DictionaryUiState.Empty
    )
    val relatedWords = mRelatedWords.asStateFlow()

    fun getRelatedWords(wordId: Int, kanjis: List<String>) {
        mRelatedWords.value = DictionaryUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = mGetRelatedWordsUseCase(wordId, kanjis)
            mRelatedWords.value = DictionaryUiState.Success(result)
        }
    }

    fun getKanjiInfo(kanji: List<String>) {
        mKanjiDetail.value = DictionaryUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result: MutableList<KanjiDetail> = mutableListOf<KanjiDetail>()
            if (kanji.isEmpty()) {
                mKanjiDetail.value = DictionaryUiState.Empty
                return@launch
            }
            kanji.forEach {
                val detail = mGetKanjiDetailUseCase(it)
                Timber.d("Loading kanji details: $detail")
                result.add(detail)
            }
            mKanjiDetail.value = DictionaryUiState.Success(result)
        }
    }

    fun loadExamples(wordId: Int) {
        mExamples.value = DictionaryUiState.Loading
        viewModelScope.launch {
            try {
                val rawExamples = mExampleDao.getExamples(wordId)

                val examplesWithFurigana = rawExamples.map { example ->
                    async(Dispatchers.Default) {
                        val tokens = mTokenizeTextUseCase(example.japanese)
                        ExampleWithFurigana(
                            japanese = example.japanese,
                            english = example.english,
                            tokens = tokens
                        )
                    }
                }.awaitAll()

                mExamples.value = DictionaryUiState.Success(examplesWithFurigana)
            } finally {
            }
        }
    }

    fun getWord(wordId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = mGetWordByIdUseCase(wordId)
            mWord.value = result
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