package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a JLPT word list (N1-N5)
 *
 * This table stores metadata for fixed JLPT levels.
 * Lists are pre-defined and not user-modifiable.
 *
 * @property id The JLPT level identifier (N1, N2, N3, N4, N5)
 * @property name Display name of the JLPT level
 */
@Entity(tableName = "jlpt_word_list")
data class JlptWordListEntity(
    @PrimaryKey 
    @ColumnInfo(name = "id") 
    val id: String,
    
    @ColumnInfo(name = "name") 
    val name: String
)
