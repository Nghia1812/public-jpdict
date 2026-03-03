package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.domain.repository.IStorageRepository
import com.prj.domain.repository.IWordRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

/**
 * Load all lists (Custom, JLPT, Theme) from Firebase and download words for each list.
 */
class LoadWordsForListsUseCase @Inject constructor(
    private val wordRepository: IWordRepository,
    private val storageRepository: IStorageRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> = runCatching {
        coroutineScope {
            // Process all three types in parallel
            val customJob = async { processCustomLists(userId) }
            val jlptJob = async { processJlptLists(userId) }
            val themeJob = async { processThemeLists(userId) }

            // Wait for all to complete
            customJob.await()
            jlptJob.await()
            themeJob.await()
        }
    }

    private suspend fun processCustomLists(userId: String) {
        // Load custom word lists from online storage
        val listsOfCustomWord = storageRepository.getCustomLists(userId).getOrElse {
            Timber.e(it, "Failed to load custom lists")
            return
        }

        // Process all lists in parallel
        coroutineScope {
            listsOfCustomWord.map { wordList ->
                async {
                    processCustomWordList(userId, wordList)
                }
            }.awaitAll()
        }
    }

    private suspend fun processCustomWordList(
        userId: String,
        wordList: WordList
    ): Boolean {
        val listCreationResult = wordRepository.createListFromOnline(wordList)

        if (listCreationResult.isFailure) {
            return false // Stop here; cannot insert words for a non-existent list
        }

        return storageRepository.downloadWordsForAllCustomLists(userId, wordList).fold(
            onSuccess = { wordRefs ->
                wordRepository.addWordsToCustomListBatch(wordRefs)
                    .onSuccess {
                        return true
                    }
                    .onFailure { exception ->
                        Timber.e(exception, "Failed to save words for wordList: $wordList")
                        return false
                    }
                false // Fallback
            },
            onFailure = { exception ->
                Timber.e(exception, "Failed to download words for wordList: $wordList")
                false
            }
        )
    }

    private suspend fun processJlptLists(userId: String) {
        val jlptLists = storageRepository.getJlptLists(userId).getOrElse {
            Timber.e(it, "Failed to load JLPT lists")
            return
        }

        coroutineScope {
            jlptLists.map { list ->
                async { processJlptList(userId, list) }
            }.awaitAll()
        }
    }

    private suspend fun processJlptList(userId: String, list: WordList): Boolean {
        return storageRepository.downloadWordsForJlptList(userId, list).fold(
            onSuccess = { wordRefs ->
                wordRepository.updateJlptWordsLearningStateBatch(wordRefs)
                    .onSuccess { return true }
                    .onFailure { Timber.e(it, "Failed to update JLPT word states for list: $list") }
                false
            },
            onFailure = { exception ->
                Timber.e(exception, "Failed to download JLPT words for list: $list")
                false
            }
        )
    }

    private suspend fun processThemeLists(userId: String) {
        val themeLists = storageRepository.getThemeLists(userId).getOrElse {
            Timber.e(it, "Failed to load Theme lists")
            return
        }

        coroutineScope {
            themeLists.map { list ->
                async { processThemeList(userId, list) }
            }.awaitAll()
        }
    }

    private suspend fun processThemeList(userId: String, list: WordList): Boolean {
        return storageRepository.downloadWordsForThemeList(userId, list).fold(
            onSuccess = { wordRefs ->
                wordRepository.updateThemeWordsLearningStateBatch(wordRefs)
                    .onSuccess { return true }
                    .onFailure { Timber.e(it, "Failed to update Theme word states for list: $list") }
                false
            },
            onFailure = { exception ->
                Timber.e(exception, "Failed to download Theme words for list: $list")
                false
            }
        )
    }
}
