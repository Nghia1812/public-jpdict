package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.prj.domain.model.testscreen.LearningState

/**
 * Junction table entity linking JLPT lists to word entries with learning states
 *
 * This enables:
 * - Same word to appear in multiple JLPT levels with independent learning states
 * - Cascade deletion when either list or entry is removed
 * - Tracking learning progress per (list, word) combination
 *
 * @property listId The JLPT level identifier (foreign key to jlpt_word_list)
 * @property entryId The word entry ID (foreign key to entry_new)
 * @property learningState Current learning state for this word in this list
 */
@Entity(
    tableName = "jlpt_word_entry",
    primaryKeys = ["list_id", "entry_id"],
    foreignKeys = [
        ForeignKey(
            entity = JapaneseWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JlptWordListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entry_id"]),
        Index(value = ["list_id"])
    ]
)
data class JlptWordEntryEntity(
    @ColumnInfo(name = "list_id") 
    val listId: String,
    
    @ColumnInfo(name = "entry_id") 
    val entryId: Int,
    
    @ColumnInfo(name = "learning_state") 
    val learningState: LearningState = LearningState.NOT_LEARNT_YET,
    
    @ColumnInfo(name = "last_reviewed_date")
    val lastReviewedDate: Long = 0L
)
