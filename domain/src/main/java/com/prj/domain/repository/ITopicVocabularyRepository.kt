package com.prj.domain.repository

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.model.testscreen.ListWordCountWithState
import com.prj.domain.model.testscreen.WordListType
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the repository for managing vocabulary across different categories.
 *
 * This repository handles operations related to fetching words by level, theme, or custom lists,
 * tracking learning progress, and retrieving statistics for various word groups.
 */
interface ITopicVocabularyRepository {
    /**
     * Retrieves a list of Japanese words based on a specific level (e.g., N1-N5).
     *
     * @param level The level to filter the words by.
     * @param language The language code for translations (e.g., "en", "vi")
     * @return A list of [JapaneseWord] objects belonging to the specified level.
     */
    suspend fun getWordsByLevel(level: String, language: String): List<JapaneseWord>

    /**
     * Retrieves a list of Japanese words from a user-created custom list.
     *
     * @param listId The unique identifier of the custom list.
     * @return A list of [JapaneseWord] objects in the custom list.
     */
    suspend fun getWordsbyCustomList(listId: String, language: String): List<JapaneseWord>

    /**
     * Retrieves a list of Japanese words based on a specific theme.
     *
     * @param themeId The identifier of the theme (e.g., Family, Food).
     * @return A list of [JapaneseWord] objects associated with the theme.
     */
    suspend fun getWordsByTheme(themeId: Int, language: String): List<JapaneseWord>

    /**
     * Retrieves all user-created custom word lists along with their learning state counts.
     *
     * @return A [Flow] emitting a list of [ListWordCountWithState] for all custom lists.
     */
    fun getAllCustomListCountWithState(): Flow<List<ListWordCountWithState>>

    /**
     * Retrieves all theme-based word lists along with their learning state counts.
     *
     * @return A [Flow] emitting a list of [ListWordCountWithState] for all theme lists.
     */
    fun getAllThemeListCountWithState(): Flow<List<ListWordCountWithState>>

    /**
     * Retrieves all JLPT level word lists along with their learning state counts.
     *
     * @return A [Flow] emitting a list of [ListWordCountWithState] for all JLPT levels.
     */
    fun getAllJLPTListCountWithState(): Flow<List<ListWordCountWithState>>

    /**
     * Retrieves words from a specific list that match a particular learning state.
     *
     * @param listId The identifier of the list (JLPT level, Theme ID, or Custom List ID).
     * @param newState The learning state to filter by (e.g., NOT_LEARNT_YET, LEARNING, LEARNT).
     * @param listType The category of the list ([WordListType]).
     * @param language The language code for translations (e.g., "en", "vi")
     * @return A list of [JapaneseWord] filtered by list and state.
     */
    suspend fun getWordsByLearningState(
        listId: String, 
        newState: LearningState = LearningState.NOT_LEARNT_YET, 
        listType: WordListType,
        language: String
    ): List<JapaneseWord>

    /**
     * Updates the learning state of a specific word entry within a list.
     *
     * @param listId The identifier of the list containing the word.
     * @param entryId The unique identifier of the word entry.
     * @param newState The new [LearningState] to be applied.
     * @param listType The category of the list ([WordListType]).
     */
    suspend fun updateWordByLearningState(listId: String, entryId: Int, newState: LearningState, listType: WordListType)

    /**
     * Retrieves the current learning statistics for a specific list.
     *
     * @param listId The identifier of the list.
     * @param listType The category of the list ([WordListType]).
     * @return A [ListWordCountWithState] containing counts for each learning state.
     */
    suspend fun getListLearningStates(listId: String, listType: WordListType): ListWordCountWithState

    /**
     * Retrieves all words belonging to a specified list, regardless of their learning state.
     *
     * @param listType The category of the list ([WordListType]).
     * @param listId The identifier of the list.
     * @return A list of all [JapaneseWord] objects in the specified list.
     */
    suspend fun getAllWordsByListType(listType: WordListType, listId: String, language: String): List<JapaneseWord>

    /**
     * Adds a word to a theme vocabulary list.
     *
     * @param themeId The ID of the theme to add the word to.
     * @param entryId The ID of the word entry to add.
     */
    suspend fun addWordToTheme(themeId: Int, entryId: Int)

    /**
     * Removes a word from a theme vocabulary list.
     *
     * @param themeId The ID of the theme to remove the word from.
     * @param entryId The ID of the word entry to remove.
     */
    suspend fun removeWordFromTheme(themeId: Int, entryId: Int)
}
