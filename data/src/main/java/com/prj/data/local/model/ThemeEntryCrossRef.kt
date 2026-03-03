package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.prj.domain.model.testscreen.LearningState

/**
 * Represents the join table for a many-to-many relationship between a theme list
 * and a Japanese word entry.
 *
 * The table name in the database will be "theme_entry".
 */
@Entity(
    tableName = "theme_entry",
    primaryKeys = ["theme_id", "entry_id"],
    indices = [
        Index("entry_id"),
        Index("theme_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = ThemeListEntity::class,
            parentColumns = ["id"],
            childColumns = ["theme_id"],
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
data class ThemeEntryCrossRef(
    @ColumnInfo(name = "theme_id") val themeId: Int,
    @ColumnInfo(name = "entry_id") val entryId: Int,
    @ColumnInfo(name = "learning_state") val learningState: LearningState
)
