package com.prj.data.repository

import androidx.room.withTransaction
import com.prj.data.local.dao.CustomWordListDao
import com.prj.data.local.dao.JWordDao
import com.prj.data.local.dao.JlptWordDao
import com.prj.data.local.dao.KanjiDao
import com.prj.data.local.dao.ThemeVocabularyDao
import com.prj.data.local.database.DictionaryDatabase
import com.prj.data.local.model.CustomWordListEntity
import com.prj.data.local.model.CustomWordListWithEntriesEntity
import com.prj.data.mapper.toDomain
import com.prj.data.mapper.toEntity
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.CustomWordRef
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.dictionaryscreen.JlptWordRef
import com.prj.domain.model.dictionaryscreen.KanjiDetail
import com.prj.domain.model.dictionaryscreen.ThemeCount
import com.prj.domain.model.dictionaryscreen.ThemeWordRef
import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.domain.repository.IWordRepository
import javax.inject.Inject
import kotlin.collections.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository for handling core word-related data operations.
 *
 * @param mWordDao DAO for general word and favorite operations.
 * @param mJlptWordDao DAO for JLPT-specific data.
 * @param mCustomWordListDao DAO for user-created custom word lists.
 * @param mThemeVocabularyDao DAO for predefined theme lists.
 */
class WordRepository
@Inject
constructor(
    private val mWordDao: JWordDao,
    private val mJlptWordDao: JlptWordDao,
    private val mCustomWordListDao: CustomWordListDao,
    private val mThemeVocabularyDao: ThemeVocabularyDao,
    private val mKanjiDao: KanjiDao,
    private val database: DictionaryDatabase
) : IWordRepository {
    override suspend fun findWordList(search: String): List<JapaneseWord> {
        return mWordDao.findWord(search).map { entity -> entity.toDomain() }
    }

    override suspend fun findWordList(search: String, language: String): List<JapaneseWord> {
        Timber.i("search: $search, language: $language")
        return mWordDao.findWord(search, language).map { entity ->
            Timber.i("Result: $entity")
            entity.toDomain()
        }
    }

    override suspend fun getWordById(wordId: Int, language: String): JapaneseWord {
        return mWordDao.getWordById(wordId, language).toDomain()
    }

    override suspend fun getJlptWordCount(levels: List<String>): List<WordList> {
        return mJlptWordDao.getWordCount(levels.map { it.lowercase() }).map { it.toDomain() }
    }

    override fun getAllCustomWordListsWithCount(): Flow<List<WordList>> {
        return mCustomWordListDao.getAllListsWithCount().map {
            it.map { entity -> entity.toDomain() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllCustomWordLists(language: String): Flow<List<CustomWordListWithEntries>> {
//        return mCustomWordListDao.getAllLists(language)
//            .map { it.map { entity -> entity.toDomain() }}
        return combine(
            mCustomWordListDao.getAllListsEntity(),
            mCustomWordListDao.observeListChanges()
        ) { lists, _ -> lists }
            .flatMapLatest { lists ->
                flow {
                    val databaseResult = database.withTransaction {
                        lists.map { list ->
                            CustomWordListWithEntriesEntity(
                                list = list,
                                entries = mCustomWordListDao.getEntriesWithTranslation(
                                    list.id,
                                    language
                                )
                            )
                        }
                    }
                    val result = databaseResult.map { it.toDomain() }
                    emit(result)
                }
            }
    }

    override suspend fun getAllCustomWordListsSnapshot(language: String): Result<List<CustomWordListWithEntries>> {
        return runCatching {
            withContext(Dispatchers.IO) {
                mCustomWordListDao.getAllListsOneshot(language).map { it.toDomain() } // query Room
            }
        }
            .onFailure { Timber.e(it, "Failed to load custom word lists snapshot") }
    }

    override fun getAllThemeLists(): Flow<List<ThemeCount>> {
        return mThemeVocabularyDao.getAllThemesWithWordCount()
    }

    override suspend fun addWordToCustomList(customWordRef: CustomWordRef) {
        mCustomWordListDao.addWordToList(customWordRef.toEntity())
    }

    override suspend fun createList(name: String): String {
        val entity =
            CustomWordListEntity(
                name = name,
            )
        mCustomWordListDao.insertList(entity)
        return entity.id
    }

    override fun removeWordFromCustomList(listId: String, entryId: Int): Result<Unit> {
        return runCatching {
            mCustomWordListDao.removeWordFromList(listId, entryId)
        }.onFailure { exception ->
            Timber.e("Error removing word from custom list: ${exception.message}")
        }
    }

    override suspend fun clearUserLocalData() {
        mCustomWordListDao.clearUserLocalData()
    }

    override suspend fun getKanjiInfo(kanji: String): KanjiDetail {
        return mKanjiDao.getKanjiInfo(kanji).toDomain()
    }

    override suspend fun getRelatedWords(
        wordId: Int,
        kanjis: List<String>,
        language: String
    ): List<JapaneseWord> {
        return kanjis.flatMap { kanji ->
            mWordDao.getRelatedWords(wordId, kanji, language).map { it.toDomain() }
        }
    }

    override suspend fun addWordsToCustomListBatch(wordRefs: List<CustomWordRef>): Result<Unit> =
        runCatching {
            if (wordRefs.isEmpty()) return Result.success(Unit)
            val crossRefs = wordRefs.map { it.toEntity() }
            mCustomWordListDao.addWordsToListBatch(crossRefs)
        }


    override suspend fun createListFromOnline(wordList: WordList): Result<Unit> = runCatching {
        val entity = CustomWordListEntity(
            id = wordList.listId,
            name = wordList.name
        )
        mCustomWordListDao.createListFromOnline(entity)
    }.onFailure {
        Timber.e(it, "Failed to add list: ${wordList.name}")
    }

    override suspend fun updateJlptWordsLearningStateBatch(wordRefs: List<JlptWordRef>): Result<Unit> =
        runCatching {
            if (wordRefs.isEmpty()) return Result.success(Unit)

            // Update each word's learning state individually
            wordRefs.forEach { wordRef ->
                mJlptWordDao.updateWordLearningState(
                    level = wordRef.listId,
                    wordId = wordRef.entryId,
                    newState = wordRef.learningState
                )
            }
        }.onFailure {
            Timber.e(it, "Failed to batch update JLPT word learning states")
        }

    override suspend fun updateThemeWordsLearningStateBatch(wordRefs: List<ThemeWordRef>): Result<Unit> =
        runCatching {
            if (wordRefs.isEmpty()) return Result.success(Unit)

            // Update each word's learning state individually
            wordRefs.forEach { wordRef ->
                mThemeVocabularyDao.updateWordLearningState(
                    listId = wordRef.listId,
                    entryId = wordRef.entryId,
                    newState = wordRef.learningState
                )
            }
        }.onFailure {
            Timber.e(it, "Failed to batch update Theme word learning states")
        }
}