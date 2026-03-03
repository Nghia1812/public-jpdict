package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "word_id")
    val wordId: String,
    @ColumnInfo(name = "notification_type")
    val notificationType: String,
    @ColumnInfo(name = "sent_date")
    val sentDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "was_opened")
    val wasOpened: Boolean = false
)
