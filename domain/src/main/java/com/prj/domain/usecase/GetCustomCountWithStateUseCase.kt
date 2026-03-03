package com.prj.domain.usecase

import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

class GetCustomCountWithStateUseCase @Inject constructor(
    private val wordRepository: ITopicVocabularyRepository
) {
    operator fun invoke() = wordRepository.getAllCustomListCountWithState()
}