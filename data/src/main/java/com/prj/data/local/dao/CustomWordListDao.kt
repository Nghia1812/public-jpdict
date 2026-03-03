package com.prj.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.prj.domain.model.testscreen.LearningState
import com.prj.data.local.model.CustomWordEntryCrossRef
import com.prj.data.local.model.CustomWordListEntity
import com.prj.data.local.model.CustomWordListWithEntriesEntity
import com.prj.data.local.model.JapaneseWordEntity
import com.prj.data.local.model.JapaneseWordWithTranslation
import com.prj.data.local.model.ListWordCount
import com.prj.data.local.model.ListWordCountWithStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

/**
 * Data Access Object (DAO) for managing user-created custom word lists.
 * This interface defines the database interactions for creating, reading,
 * and modifying custom lists and their associated words.
 */
@Dao
interface CustomWordListDao {

    /**
     * Create new list with sync fields
     * Note: Now returns the UUID string instead of Long
     */
    @Insert
    suspend fun insertList(list: CustomWordListEntity)

    // Add a word to list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWordToList(crossRef: CustomWordEntryCrossRef)

    /**
     * Get all lists
     */
    @Query("SELECT * FROM custom_word_list")
    fun getAllListsEntity(): Flow<List<CustomWordListEntity>>

    /**
     * Manual observation for list entries changes
     */
    @Query("""
    SELECT DISTINCT list_id
    FROM custom_word_entry
""")
    fun observeListChanges(): Flow<List<String>>

    /**
     * Get all lists (without translation) at once
     */
    @Query("SELECT * FROM custom_word_list")
    suspend fun getAllListsEntityOneshot(): List<CustomWordListEntity>

    /**
     * Get all lists at once
     */
    @Transaction
    suspend fun getAllListsOneshot(
        language: String
    ): List<CustomWordListWithEntriesEntity> {
        return getAllListsEntityOneshot().map { list ->
            CustomWordListWithEntriesEntity(
                list = list,
                entries = getEntriesWithTranslation(list.id, language)
            )
        }
    }
    @Query("""
    SELECT *
    FROM custom_word_list
    WHERE id = :listId
    LIMIT 1
""")
    suspend fun getListById(listId: String): CustomWordListEntity

    /**
     * Get translation for líst by id
     */
    @Transaction
    @Query("""
    SELECT
        e.*,
        t.*
    FROM custom_word_entry cr
    JOIN entry_new e
        ON e.id = cr.entry_id
    JOIN entry_translation t
        ON t.entry_id = e.id
       AND t.language_code = :language
    WHERE cr.list_id = :listId
""")
    fun getEntriesWithTranslation(
        listId: String,
        language: String
    ): List<JapaneseWordWithTranslation>

    /**
     * Get list by id
     */
    @Transaction
    suspend fun getListWithEntriesById(
        listId: String,
        language: String
    ): CustomWordListWithEntriesEntity {

        val list = getListById(listId)
        val entries = getEntriesWithTranslation(listId, language)

        return CustomWordListWithEntriesEntity(
            list = list,
            entries = entries
        )
    }

    /**
     * Get all custom lists with word count
     */
    @Query(
        """
    SELECT l.id, l.name, COUNT(e.entry_id) AS count
    FROM custom_word_list l
    LEFT JOIN custom_word_entry e ON l.id = e.list_id
    GROUP BY l.id
"""
    )
    fun getAllListsWithCount(): Flow<List<ListWordCount>>

    /**
     * Remove word from list
     */
    @Query("DELETE FROM custom_word_entry WHERE list_id = :listId AND entry_id = :entryId")
    fun removeWordFromList(listId: String, entryId: Int)

    /**
     * Clear user local data
     */
    @Query("DELETE FROM custom_word_list")
    fun clearUserLocalData()

    /**
     * Batch insert words to lists
     * More efficient than inserting one by one
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWordsToListBatch(crossRefs: List<CustomWordEntryCrossRef>)

    /**
     * Get all custom lists with detailed learning state counts
     */
    @Query(
        """
        SELECT 
            l.id, 
            l.name,
            COUNT(e.entry_id) AS total_count,
            SUM(CASE WHEN e.learning_state = 'REMEMBERED' THEN 1 ELSE 0 END) AS remembered_count,
            SUM(CASE WHEN e.learning_state = 'FORGOT' THEN 1 ELSE 0 END) AS forgot_count,
            SUM(CASE WHEN e.learning_state = 'NOT_LEARNT_YET' THEN 1 ELSE 0 END) AS not_learnt_count
        FROM custom_word_list l
        LEFT JOIN custom_word_entry e ON l.id = e.list_id
        GROUP BY l.id
        """
    )
    fun getAllListsWithLearningStates(): Flow<List<ListWordCountWithStateEntity>>

    /**
     * Get learning state counts for a specific list
     */
    @Query(
        """
        SELECT 
            l.id, 
            l.name,
            COUNT(e.entry_id) AS total_count,
            SUM(CASE WHEN e.learning_state = 'REMEMBERED' THEN 1 ELSE 0 END) AS remembered_count,
            SUM(CASE WHEN e.learning_state = 'FORGOT' THEN 1 ELSE 0 END) AS forgot_count,
            SUM(CASE WHEN e.learning_state = 'NOT_LEARNT_YET' THEN 1 ELSE 0 END) AS not_learnt_count
        FROM custom_word_list l
        LEFT JOIN custom_word_entry e ON l.id = e.list_id
        WHERE l.id = :listId
        GROUP BY l.id
        """
    )
    suspend fun getListLearningStates(listId: String): ListWordCountWithStateEntity?

    /**
     * Update learning state for a word in a list
     */
    @Query(
        """
        UPDATE custom_word_entry 
        SET learning_state = :newState 
        WHERE list_id = :listId AND entry_id = :entryId
        """
    )
    suspend fun updateWordLearningState(listId: String, entryId: Int, newState: LearningState)

    /**
     * Get words by learning state for a specific list
     */
    @Transaction
    @Query(
        """
        SELECT e.* FROM entry_new e
        INNER JOIN custom_word_entry cwe ON e.id = cwe.entry_id
        WHERE cwe.list_id = :listId AND cwe.learning_state = :learningState
        """
    )
    suspend fun getWordsByLearningState(listId: String, learningState: LearningState): List<JapaneseWordEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createListFromOnline(list: CustomWordListEntity)
}
