package com.prj.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.prj.data.local.model.JpExampleEntity

@Dao
interface ExampleDao {
    @Query("""
        SELECT * FROM examples
        WHERE entry_id = :id 
    """)
    suspend fun getExamples(id: Int): List<JpExampleEntity>
}