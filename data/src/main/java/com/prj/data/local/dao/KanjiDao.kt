package com.prj.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.prj.data.local.model.KanjiDetailEntity

/**
 * Data access object for kanji information, taken from kanji_detail table
 */
@Dao
interface KanjiDao {
    @Query("SELECT * FROM kanji_detail WHERE kanji = :kanji")
    suspend fun getKanjiInfo(kanji: String): KanjiDetailEntity
}