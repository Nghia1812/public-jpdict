package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.prj.domain.model.testscreen.LearningState

/**
 * Represents the join table for a many-to-many relationship between a custom word list
 * and a Japanese word entry.
 *
 * The table name in the database "custom_word_entry".
 */
@Entity(
    tableName = "custom_word_entry",
    primaryKeys = ["list_id", "entry_id"],
    indices = [
        Index("entry_id"),
        Index("list_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = CustomWordListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JapaneseWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomWordEntryCrossRef(
    @ColumnInfo(name = "list_id") val listId: String,
    @ColumnInfo(name = "entry_id") val entryId: Int,
    @ColumnInfo(name = "learning_state") val learningState: LearningState
)
