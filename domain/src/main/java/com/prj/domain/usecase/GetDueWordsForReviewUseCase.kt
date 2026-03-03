package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.repository.INotificationRepository
import javax.inject.Inject

class GetDueWordsForReviewUseCase @Inject constructor(
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(): List<JapaneseWord> {
        return notificationRepository.getDueWordsForReview()
    }
}
