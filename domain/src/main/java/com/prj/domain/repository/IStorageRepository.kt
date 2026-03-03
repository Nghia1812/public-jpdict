package com.prj.domain.repository

import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.domain.model.dictionaryscreen.CustomWordRef
import com.prj.domain.model.dictionaryscreen.JlptWordRef
import com.prj.domain.model.dictionaryscreen.ThemeWordRef
import com.prj.domain.model.testscreen.TestResult
import com.prj.domain.model.profilescreen.User
import com.prj.domain.model.testscreen.AllSkillTestResult
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.model.testscreen.WordListType

/**
 * Interface for storage repository
 */
interface IStorageRepository {

    /**
     * Get user info from storage
     *
     * @param userId: String
     * @return Result<User?>
     */
    suspend fun getUserFromStorage(userId: String): Result<User?>

    /**
     * Save test result to storage
     *
     * @param userId: String
     * @param testId: String
     * @param skill: String
     * @param testResult: TestResult
     *
     * @return Result<Unit>
     */
    suspend fun saveTestResult(userId: String, testId: String, skill: String, testResult: TestResult) : Result<Unit>

    /**
     * Get test result from storage
     */
    suspend fun getTestResultForSpecificSkill(userId: String, testId: String, skill: String) : Result<TestResult?>

    /**
     * Get custom lists from storage
     */
    suspend fun getCustomLists(userId: String): Result<List<WordList>>

    /**
     * Download words from all lists
     */
    suspend fun downloadWordsForAllCustomLists(userId: String, list: WordList): Result<List<CustomWordRef>>

    /**
     * Upload list to online storage
     */
    suspend fun uploadListToOnlineStorage(userId: String, listId: String, name: String) : Result<Unit>

    /**
     * Upload word to online storage
     */
    suspend fun uploadWordToOnlineStorage(userId: String, listId: String, entryId: Int) : Result<Unit>

    /**
     * Delete word from online storage
     */
    suspend fun deleteWordFromOnlineStorage(userId: String, listId: String, entryId: Int) : Result<Unit>

    /**
     * Update word's learning status in online storage
     */
    suspend fun updateWordInOnlineStorage(userId: String, listId: String, entryId: Int, newState: LearningState, listType: WordListType) : Result<Unit>

    suspend fun getAllTestResults(
        userId: String,
        testId: String
    ): Result<AllSkillTestResult?>

    /**
     * Get JLPT lists from storage
     */
    suspend fun getJlptLists(userId: String): Result<List<WordList>>

    /**
     * Download words for a specific JLPT list
     */
    suspend fun downloadWordsForJlptList(userId: String, list: WordList): Result<List<JlptWordRef>>

    /**
     * Get Theme lists from storage
     */
    suspend fun getThemeLists(userId: String): Result<List<WordList>>

    /**
     * Download words for a specific Theme list
     */
    suspend fun downloadWordsForThemeList(userId: String, list: WordList): Result<List<ThemeWordRef>>
}
