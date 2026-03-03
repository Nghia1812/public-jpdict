package com.prj.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.prj.data.local.model.JapaneseWordWithTranslation
import com.prj.data.local.model.JlptWordEntryEntity
import com.prj.data.local.model.JlptWordListEntity
import com.prj.data.local.model.JlptWordListWithEntriesEntity
import com.prj.data.local.model.ListWordCount
import com.prj.data.local.model.ListWordCountWithStateEntity
import com.prj.domain.model.testscreen.LearningState
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for handling JLPT related words.
 * This interface provides methods to query words based on their JLPT level.
 *
 * Updated to use normalized schema with:
 * - jlpt_word_list: JLPT level metadata
 * - jlpt_word_entry: Junction table linking lists to entries with learning states
 * - entry_new: Core word data
 */
@Dao
interface JlptWordDao {
    /**
     * Calculates the total number of words for each of the specified JLPT levels.
     *
     * @param levels A list of JLPT level strings (e.g., ["N5", "N4"]) to get counts for.
     * @return A list of [ListWordCount] objects, where each object contains a level name and the corresponding word count.
     */
    @Query("""
    SELECT 
        jwe.list_id AS id, 
        jwl.name AS name, 
        COUNT(jwe.entry_id) AS count
    FROM jlpt_word_entry jwe
    INNER JOIN jlpt_word_list jwl ON jwe.list_id = jwl.id
    WHERE jwe.list_id IN (:levels)
    GROUP BY jwe.list_id, jwl.name
    """)
    suspend fun getWordCount(levels: List<String>): List<ListWordCount>

    /**
     * Retrieves a list of words with translations for a specific JLPT level.
     *
     * @param level The JLPT level string (e.g., "N5", "N4") to filter words by.
     * @param language The language code for translations
     * @return A list of [JapaneseWordWithTranslation] objects for the specified level.
     */
    @Transaction
    @Query("""
        SELECT entry_new.*, entry_translation.*
        FROM jlpt_word_entry
        INNER JOIN entry_new ON jlpt_word_entry.entry_id = entry_new.id
        LEFT JOIN entry_translation ON entry_new.id = entry_translation.entry_id 
            AND entry_translation.language_code = :language
        WHERE jlpt_word_entry.list_id = :level
        ORDER BY entry_new.id
    """)
    suspend fun getWordsByLevel(level: String, language: String): List<JapaneseWordWithTranslation>

    /**
     * Retrieves words for a specific JLPT level filtered by learning state.
     *
     * @param level The JLPT level string
     * @param learningState The learning state to filter by
     * @param language The language code for translations
     * @return A list of [JapaneseWordWithTranslation] filtered by state.
     */
    @Transaction
    @Query("""
        SELECT entry_new.*, entry_translation.*
        FROM jlpt_word_entry
        INNER JOIN entry_new ON jlpt_word_entry.entry_id = entry_new.id
        LEFT JOIN entry_translation ON entry_new.id = entry_translation.entry_id 
            AND entry_translation.language_code = :language
        WHERE jlpt_word_entry.list_id = :level 
            AND jlpt_word_entry.learning_state = :learningState
        ORDER BY entry_new.id
    """)
    suspend fun getWordsByLearningState(
        level: String, 
        learningState: LearningState,
        language: String
    ): List<JapaneseWordWithTranslation>

    /**
     * Get all JLPT levels with learning state counts.
     *
     * @return A Flow emitting list of [ListWordCountWithStateEntity] for all JLPT levels.
     */
    @Query("""
        SELECT 
            jwe.list_id AS id,
            jwl.name AS name,
            COUNT(jwe.entry_id) AS total_count,
            SUM(CASE WHEN jwe.learning_state = 'REMEMBERED' THEN 1 ELSE 0 END) AS remembered_count,
            SUM(CASE WHEN jwe.learning_state = 'FORGOT' THEN 1 ELSE 0 END) AS forgot_count,
            SUM(CASE WHEN jwe.learning_state = 'NOT_LEARNT_YET' THEN 1 ELSE 0 END) AS not_learnt_count
        FROM jlpt_word_entry jwe
        INNER JOIN jlpt_word_list jwl ON jwe.list_id = jwl.id
        GROUP BY jwe.list_id, jwl.name
        ORDER BY jwe.list_id
    """)
    fun getAllListsWithLearningStates(): Flow<List<ListWordCountWithStateEntity>>

    /**
     * Get learning state counts for a specific JLPT level.
     *
     * @param level The JLPT level string
     * @return The [ListWordCountWithStateEntity] for the specified level, or null if not found.
     */
    @Query("""
        SELECT
            jwe.list_id AS id,
            jwl.name AS name,
            COUNT(jwe.entry_id) AS total_count,
            SUM(CASE WHEN jwe.learning_state = 'REMEMBERED' THEN 1 ELSE 0 END) AS remembered_count,
            SUM(CASE WHEN jwe.learning_state = 'FORGOT' THEN 1 ELSE 0 END) AS forgot_count,
            SUM(CASE WHEN jwe.learning_state = 'NOT_LEARNT_YET' THEN 1 ELSE 0 END) AS not_learnt_count
        FROM jlpt_word_entry jwe
        INNER JOIN jlpt_word_list jwl ON jwe.list_id = jwl.id
        WHERE jwe.list_id = :level
        GROUP BY jwe.list_id, jwl.name
    """)
    suspend fun getLevelLearningStates(level: String): ListWordCountWithStateEntity?

    /**
     * Update learning state for a word in a specific JLPT level.
     *
     * @param level The JLPT level string
     * @param wordId The entry ID of the word
     * @param newState The new learning state to set
     * @return Number of rows updated
     */
    @Query("""
        UPDATE jlpt_word_entry 
        SET learning_state = :newState, last_reviewed_date = :lastReviewedDate
        WHERE list_id = :level AND entry_id = :wordId
    """)
    suspend fun updateWordLearningState(
        level: String, 
        wordId: Int, 
        newState: LearningState, 
        lastReviewedDate: Long = System.currentTimeMillis()
    ): Int

    /**
     * Insert a new JLPT word list.
     *
     * @param list The JLPT list entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: JlptWordListEntity)

    /**
     * Insert a word entry into a JLPT list.
     *
     * @param entry The junction table entry to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordEntry(entry: JlptWordEntryEntity)

    /**
     * Insert multiple word entries into JLPT lists in a batch operation.
     *
     * @param entries The list of junction table entries to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordEntries(entries: List<JlptWordEntryEntity>)

    /**
     * Delete a word from a JLPT list.
     *
     * @param listId The JLPT level string
     * @param entryId The entry ID to remove
     */
    @Query("DELETE FROM jlpt_word_entry WHERE list_id = :listId AND entry_id = :entryId")
    suspend fun deleteWordEntry(listId: String, entryId: Int)

    /**
     * Get a random word by learning state for notifications.
     * Excludes words notified recently (within the notBefore timestamp).
     *
     * @param learningState The learning state to filter by
     * @param notBefore Timestamp to exclude recently notified words (default: 7 days ago)
     * @param language The language code for translations
     * @return A random word with the specified learning state, or null if none found
     */
    @Transaction
    @Query("""
        SELECT entry_new.*, entry_translation.*
        FROM jlpt_word_entry
        INNER JOIN entry_new ON jlpt_word_entry.entry_id = entry_new.id
        LEFT JOIN entry_translation ON entry_new.id = entry_translation.entry_id 
            AND entry_translation.language_code = :language
        WHERE jlpt_word_entry.learning_state = :learningState
        AND entry_new.id NOT IN (
            SELECT word_id FROM notification_history 
            WHERE sent_date > :notBefore
        )
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomWordByLearningState(
        learningState: LearningState,
        notBefore: Long = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, // 7 days
        language: String
    ): JapaneseWordWithTranslation?

    /**
     * Get words due for review based on last reviewed date.
     * Returns words in "REMEMBERED" or "FORGOT" state that haven't been reviewed recently.
     *
     * @param beforeDate Timestamp before which words are considered due
     * @param limit Maximum number of words to return
     * @param language The language code for translations
     * @return List of words due for review
     */
    @Transaction
    @Query("""
        SELECT entry_new.*, entry_translation.*
        FROM jlpt_word_entry
        INNER JOIN entry_new ON jlpt_word_entry.entry_id = entry_new.id
        LEFT JOIN entry_translation ON entry_new.id = entry_translation.entry_id 
            AND entry_translation.language_code = :language
        WHERE jlpt_word_entry.last_reviewed_date < :beforeDate
        AND jlpt_word_entry.learning_state IN ('REMEMBERED', 'FORGOT')
        ORDER BY jlpt_word_entry.last_reviewed_date ASC
        LIMIT :limit
    """)
    suspend fun getDueForReview(beforeDate: Long, limit: Int = 10, language: String): List<JapaneseWordWithTranslation>
}
