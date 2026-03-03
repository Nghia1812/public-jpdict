package com.prj.data.repository

import com.prj.data.local.dao.CustomWordListDao
import com.prj.data.local.dao.JlptWordDao
import com.prj.data.local.dao.ThemeVocabularyDao
import com.prj.data.mapper.toDomain
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.model.testscreen.ListWordCountWithState
import com.prj.domain.model.testscreen.WordListType
import com.prj.domain.repository.ITopicVocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.map

/**
 * Repository implementation for fetching topic-based vocabulary from the local database.
 *
 * @param mJlptWordDao Data Access Object for JLPT-related word operations.
 * @param mCustomWordListDao Data Access Object for custom user-created word lists.
 * @param mThemeVocabularyDao Data Access Object for theme-based vocabulary.
 */
class TopicVocabularyRepository @Inject constructor(
    private val mJlptWordDao: JlptWordDao,
    private val mCustomWordListDao: CustomWordListDao,
    private val mThemeVocabularyDao: ThemeVocabularyDao
) :
    ITopicVocabularyRepository {
    override suspend fun getWordsByLevel(level: String, language: String): List<JapaneseWord> {
        val words = mJlptWordDao.getWordsByLevel(level, language)
        return words.map { it.toDomain() }
    }

    override suspend fun getWordsbyCustomList(listId: String, language: String): List<JapaneseWord> {
        val words = mCustomWordListDao.getListWithEntriesById(listId, language)
        return words.entries.map { it.toDomain() }
    }

    override suspend fun getWordsByTheme(themeId: Int, language: String): List<JapaneseWord> {
        val words = mThemeVocabularyDao.getThemeWithWords(themeId, language)
        return words.entries.map { it.toDomain() }
    }


    override fun getAllCustomListCountWithState(): Flow<List<ListWordCountWithState>> {
        return mCustomWordListDao.getAllListsWithLearningStates().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllThemeListCountWithState(): Flow<List<ListWordCountWithState>> {
        return mThemeVocabularyDao.getAllListsWithLearningStates().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllJLPTListCountWithState(): Flow<List<ListWordCountWithState>> {
        return mJlptWordDao.getAllListsWithLearningStates().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getWordsByLearningState(
        listId: String,
        newState: LearningState,
        listType: WordListType,
        language: String
    ): List<JapaneseWord> {
        when (listType) {
            WordListType.THEME -> {
                return mThemeVocabularyDao.getWordsByLearningState(
                    listId.toIntOrNull() ?: 0,
                    newState
                )
                    .map { list ->
                        list.toDomain()
                    }
            }

            WordListType.CUSTOM -> {
                return mCustomWordListDao.getWordsByLearningState(listId, newState)
                    .map { list ->
                        list.toDomain()
                    }
            }

            WordListType.JLPT -> {
                Timber.i("getWordsByLearningState: $listId, $newState, $listType")
                return mJlptWordDao.getWordsByLearningState(listId, newState, language).map { list ->
                    Timber.i("getWordsByLearningState: $list")
                    list.toDomain()
                }
            }
        }
    }


    override suspend fun updateWordByLearningState(
        listId: String,
        entryId: Int,
        newState: LearningState,
        listType: WordListType
    ) {
        Timber.i("updateWordByLearningState: $listId, $entryId, $newState, $listType")
        when (listType) {
            WordListType.THEME -> {
                mThemeVocabularyDao.updateWordLearningState(listId, entryId, newState)
            }

            WordListType.JLPT -> {
                val res = mJlptWordDao.updateWordLearningState(listId, entryId, newState)
                Timber.i("updateWordByLearningState: rows $res")
            }

            WordListType.CUSTOM -> {
                mCustomWordListDao.updateWordLearningState(listId, entryId, newState)
            }
        }
    }

    override suspend fun getAllWordsByListType(listType: WordListType, listId: String, language: String): List<JapaneseWord> {
        Timber.i("updateWordByLearningState: $listType")
        when (listType) {
            WordListType.THEME -> {
                val words = mThemeVocabularyDao.getThemeWithWords(listId.toIntOrNull() ?: 0, language)
                return words.entries.map { it.toDomain() }
            }

            WordListType.JLPT -> {
                return mJlptWordDao.getWordsByLevel(listId, language)
                    .map { it.toDomain() }
            }

            WordListType.CUSTOM -> {
                val words = mCustomWordListDao.getListWithEntriesById(listId, language)
                return words.entries.map { it.toDomain() }
            }
        }
    }

    override suspend fun getListLearningStates(listId: String, listType: WordListType): ListWordCountWithState {
        var result = ListWordCountWithState("", "", 0, 0, 0, 0)
        result = when (listType) {
            WordListType.THEME -> {
                mThemeVocabularyDao.getListLearningStates(listId)?.toDomain() ?: result
            }

            WordListType.JLPT -> {
                mJlptWordDao.getLevelLearningStates(listId)?.toDomain() ?: result
            }

            WordListType.CUSTOM -> {
                mCustomWordListDao.getListLearningStates(listId)?.toDomain() ?: result
            }
        }
        return result
    }

    override suspend fun addWordToTheme(themeId: Int, entryId: Int) {
        val crossRef = com.prj.data.local.model.ThemeEntryCrossRef(
            themeId = themeId,
            entryId = entryId,
            learningState = com.prj.domain.model.testscreen.LearningState.NOT_LEARNT_YET
        )
        mThemeVocabularyDao.addWordToTheme(crossRef)
    }

    override suspend fun removeWordFromTheme(themeId: Int, entryId: Int) {
        mThemeVocabularyDao.removeWord(themeId, entryId)
    }
}
