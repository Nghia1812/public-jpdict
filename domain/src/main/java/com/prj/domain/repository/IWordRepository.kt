package com.prj.domain.repository

import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.domain.model.dictionaryscreen.CustomWordRef
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.dictionaryscreen.JlptWordRef
import com.prj.domain.model.dictionaryscreen.KanjiDetail
import com.prj.domain.model.dictionaryscreen.ThemeCount
import com.prj.domain.model.dictionaryscreen.ThemeWordRef
import com.prj.domain.model.dictionaryscreen.WordWithFavorite
import com.prj.domain.model.testscreen.ListWordCountWithState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for repository handling operations related to Japanese words.
 */
interface IWordRepository {

    /**
     * Finds a list of Japanese words that match a given search query.
     *
     * @param search The search string to filter words.
     * @return A list of [JapaneseWord] objects that match the search criteria.
     */
    suspend fun findWordList(search: String): List<JapaneseWord>

    suspend fun findWordList(search: String, language: String): List<JapaneseWord>

    /**
     * Retrieves a specific word by its ID, including its favorite status.
     *
     * @param wordId The unique identifier of the word to retrieve.
     * @return A [WordWithFavorite] object containing the word details and its saved status.
     */
    suspend fun getWordById(wordId: Int, language: String): JapaneseWord

    /**
     * Gets the count of words for each specified JLPT level.
     *
     * @param levels A list of level strings (e.g., "N5", "N4") to get counts for.
     * @return A list of [WordList] objects, each representing a level and the number of words in it.
     */
    suspend fun getJlptWordCount(levels: List<String>): List<WordList>

    /**
     * Retrieves all custom word lists created by the user, along with their respective word counts.
     *
     * @return A flow list of [CustomWordListWithEntries] objects, each representing a custom list and its word count.
     */
    fun getAllCustomWordLists(language: String): Flow<List<CustomWordListWithEntries>>

    /**
     * Retrieves all custom word lists created by the user, along with their respective word counts in oneshot operation
     *
     * @return A flow list of [CustomWordListWithEntries] objects, each representing a custom list and its word count.
     */
    suspend fun getAllCustomWordListsSnapshot(language: String): Result<List<CustomWordListWithEntries>>

    fun getAllCustomWordListsWithCount(): Flow<List<WordList>>

    /**
     * Retrieves all available themes, along with their respective word counts.
     *
     * @return A list of [ThemeCount] objects, each representing a theme and its word count.
     */
    fun getAllThemeLists(): Flow<List<ThemeCount>>

    /**
     * Add a word to a custom word list.
     *
     * @param customWordRef The reference to the custom word list and the word to be added.
     */
    suspend fun addWordToCustomList(customWordRef: CustomWordRef)

    /**
     * Create a new custom word list.
     *
     * @param name The name of the new custom word list.
     */
    suspend fun createList(name: String) : String

    /**
     * Remove a word from a custom word list.
     *
     * @param listId The ID of the custom word list.
     * @param entryId The ID of the word to be removed from the list.
     */
    fun removeWordFromCustomList(listId: String, entryId: Int): Result<Unit>

    /**
     * Clear user-specific local data, such as custom word lists.
     */
    suspend fun clearUserLocalData()

    /**
     * Get information about a specific kanji.
     *
     * @param kanji The kanji to retrieve information for.
     * @return The [KanjiDetail] object containing information about the kanji.
     */
    suspend fun getKanjiInfo(kanji: String) : KanjiDetail

    /**
     * Get related words for a given word ID and a list of kanji.
     *
     * @param wordId The ID of the word to retrieve related words for.
     * @param kanjis A list of kanji to search for related words.
     *
     * @return A list of [JapaneseWord] objects that are related to the given word and kanji.
     */
    suspend fun getRelatedWords(wordId: Int, kanjis: List<String>, language: String): List<JapaneseWord>

    /**
     * Add words to a custom list in a batch operation.
     */
    suspend fun addWordsToCustomListBatch(wordRefs: List<CustomWordRef>) : Result<Unit>

    suspend fun createListFromOnline(wordList: WordList) : Result<Unit>

    /**
     * Update learning states for JLPT words in a batch operation.
     * Only updates existing words, does not insert new ones.
     */
    suspend fun updateJlptWordsLearningStateBatch(wordRefs: List<JlptWordRef>): Result<Unit>

    /**
     * Update learning states for Theme words in a batch operation.
     * Only updates existing words, does not insert new ones.
     */
    suspend fun updateThemeWordsLearningStateBatch(wordRefs: List<ThemeWordRef>): Result<Unit>
}
