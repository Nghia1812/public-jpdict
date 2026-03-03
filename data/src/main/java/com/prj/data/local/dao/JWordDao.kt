package com.prj.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.prj.data.local.model.JapaneseWordEntity
import com.prj.data.local.model.JapaneseWordWithTranslation

@Dao
interface JWordDao {
    /**
     * Finds a list of Japanese words that match a given search query.
     *
     * @param query The search string to filter words.
     */
    @Query("SELECT * FROM entry_new WHERE rowid IN (SELECT rowid FROM entry_fts WHERE entry_fts MATCH :query)")
    suspend fun findWord(query: String): List<JapaneseWordEntity>

    /**
     * Finds a list of Japanese words that match a given search query.
     *
     * @param query The search string to filter words.
     * @param language The language to search in translation table.
     */
    @Query(
        """
        SELECT e.*, t.*
        FROM entry_new e
        JOIN entry_translation t
          ON t.entry_id = e.id
         AND t.language_code = :language
        WHERE e.id IN (
            -- kanji / reading
            SELECT rowid
            FROM entry_fts
            WHERE entry_fts MATCH :query

            UNION

            -- gloss
            SELECT t2.entry_id
            FROM entry_translation_fts fts
            JOIN entry_translation t2 ON t2.translationId = fts.rowid
            WHERE entry_translation_fts MATCH :query
              AND t2.language_code = :language
        )
    """
    )
    suspend fun findWord(
        query: String,
        language: String
    ): List<JapaneseWordWithTranslation>

    /**
     * Retrieves a specific word by its ID from entry_new table.
     *
     * @param id The unique identifier of the word to retrieve.
     * @param languageCode The language to search in translation table.
     * @return A [JapaneseWordWithTranslation] object.
     */
    @Query(
        """
    SELECT e.id, e.kanji, e.reading, 
           t.translationId, t.entry_id, t.language_code, t.gloss, t.position
    FROM entry_new e
    LEFT JOIN entry_translation t 
        ON e.id = t.entry_id 
        AND t.language_code = :languageCode
    WHERE e.id = :id
"""
    )
    suspend fun getWordById(id: Int, languageCode: String): JapaneseWordWithTranslation

    /**
     * Retrieves a list of related words for a given word ID and kanji.
     *
     * @param wordId The ID of the word to retrieve related words for.
     * @param kanji The kanji to search for related words.
     * @param language The language to search in translation table.
     * @return A list of [JapaneseWordWithTranslation] objects that are related to the given word and kanji.
     */
    @Query(
        """
    SELECT DISTINCT
        w.*,
        t.*
    FROM entry_new w
    JOIN entry_translation t
      ON t.entry_id = w.id
     AND t.language_code = :language
    WHERE w.id IN (
        SELECT rowid
        FROM entry_fts
        WHERE entry_fts MATCH :kanji
    )
      AND w.id != :wordId
    LIMIT 1
    """
    )
    suspend fun getRelatedWords(
        wordId: Int,
        kanji: String,
        language: String
    ): List<JapaneseWordWithTranslation>
}