package com.prj.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.prj.data.local.model.CustomWordListEntity
import com.prj.domain.model.testscreen.LearningState
import com.prj.data.local.model.JapaneseWordEntity
import com.prj.data.local.model.JapaneseWordWithTranslation
import com.prj.data.local.model.ListWordCount
import com.prj.data.local.model.ListWordCountWithStateEntity
import com.prj.data.local.model.ThemeEntryCrossRef
import com.prj.data.local.model.ThemeListEntity
import com.prj.data.local.model.ThemeListWithEntriesEntity

import com.prj.domain.model.dictionaryscreen.ThemeCount
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing theme-based vocabulary lists.
 */
@Dao
interface ThemeVocabularyDao {

    /**
     * Inserts a new theme list into the database.
     *
     * @param theme The [ThemeListEntity] to be inserted.
     * @return The row ID of the newly inserted theme.
     */
    @Insert
    suspend fun insertThemeList(theme: ThemeListEntity): Long

    /**
     * Associates a word with a theme by inserting a cross-reference.
     * If the word is already in the theme, the operation is ignored.
     *
     * @param crossRef The [ThemeEntryCrossRef] linking a word to a theme.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addWordToTheme(crossRef: ThemeEntryCrossRef)

    /**
     * Associates multiple words with themes by inserting cross-references in a batch operation.
     * If a word is already in the theme, the operation is ignored.
     *
     * @param crossRefs The list of [ThemeEntryCrossRef] linking words to themes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWordsToThemeBatch(crossRefs: List<ThemeEntryCrossRef>)

    /**
     * Removes a specific word from a specific theme.
     *
     * @param themeId The ID of the theme from which to remove the word.
     * @param entryId The ID of the word to remove.
     */
    @Query("DELETE FROM theme_entry WHERE theme_id = :themeId AND entry_id = :entryId")
    suspend fun removeWord(themeId: Int, entryId: Int)

    /**
     * Retrieves all theme lists from the database.
     * The result is a [Flow] that automatically updates when the data changes.
     *
     * @return A Flow emitting a list of all [ThemeListEntity] objects.
     */
    @Query("SELECT * FROM theme_list")
    fun getAllThemes(): Flow<List<ThemeListEntity>>

    /**
     * Retrieves theme list from the database.
     *
     * @return [ThemeListEntity] object.
     */
    @Query("""
    SELECT *
    FROM theme_list
    WHERE id = :listId
    LIMIT 1
""")
    suspend fun getListById(listId: String): ThemeListEntity

    /**
     * Retrieves all words associated with a specific theme, including translation
     *
     * @param themeId The ID of the theme to retrieve words for.
     * @param language The language to search in translation table.
     */
    @Transaction
    @Query("""
    SELECT
        e.*,
        t.*
    FROM theme_entry cr
    JOIN entry_new e
        ON e.id = cr.entry_id
    JOIN entry_translation t
        ON t.entry_id = e.id
       AND t.language_code = :language
    WHERE cr.theme_id = :themeId
""")
    suspend fun getEntriesWithTranslation(
        themeId: String,
        language: String
    ): List<JapaneseWordWithTranslation>

    /**
     * Retrieves a specific theme along with all of its associated words.
     * This is a transactional operation that ensures data consistency.
     *
     * @param themeId The name of the theme to retrieve.
     * @param language The language to search in translation table.
     * @return A [ThemeListWithEntriesEntity] containing the theme and its related words.
     */
    @Transaction
    suspend fun getThemeWithWords(
        themeId: Int,
        language: String
    ): ThemeListWithEntriesEntity {
        val theme = getListById(themeId.toString())
        val entries = getEntriesWithTranslation(themeId.toString(), "en")
        return ThemeListWithEntriesEntity(
            theme = theme,
            entries = entries
        )
    }

    /**
     * Retrieves all themes along with their associated image and total word count.
     * The result is a [Flow] that emits new data whenever the underlying tables change.
     *
     * @return A Flow emitting a list of [ThemeCount] objects.
     */
    @Query(
        """
    SELECT  t.id AS id,
            t.name AS name,
            t.image AS image,
            COUNT(e.entry_id) AS count
    FROM theme_list t
    LEFT JOIN theme_entry e ON t.id = e.theme_id
    GROUP BY t.id
    """
    )
    fun getAllThemesWithWordCount(): Flow<List<ThemeCount>>

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
        FROM theme_list l
        LEFT JOIN theme_entry e ON l.id = e.theme_id
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
        FROM theme_list l
        LEFT JOIN theme_entry e ON l.id = e.theme_id
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
        UPDATE theme_entry 
        SET learning_state = :newState 
        WHERE theme_id = :listId AND entry_id = :entryId
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
        INNER JOIN theme_entry te ON e.id = te.entry_id
        WHERE te.theme_id = :listId AND te.learning_state = :learningState
        """
    )
    suspend fun getWordsByLearningState(
        listId: Int, learningState: LearningState
    ): List<JapaneseWordEntity>
}