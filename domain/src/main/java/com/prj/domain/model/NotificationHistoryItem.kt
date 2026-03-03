package com.prj.domain.model

data class NotificationHistoryItem(
    val id: Long = 0,
    val wordId: String,
    val notificationType: NotificationType,
    val sentDate: Long,
    val wasOpened: Boolean = false
)
