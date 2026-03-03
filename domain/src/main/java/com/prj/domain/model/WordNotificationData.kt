package com.prj.domain.model

import com.prj.domain.model.dictionaryscreen.JapaneseWord

data class WordNotificationData(
    val word: JapaneseWord,
    val notificationType: NotificationType,
    val additionalInfo: String? = null // For example sentences, tips, etc.
)

enum class NotificationType {
    DAILY_WORD,
    REVIEW_REMINDER,
    ACHIEVEMENT
}
