package com.prj.domain.usecase

import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

class GetJlptCountWithStateUseCase @Inject constructor(
    private val wordRepository: ITopicVocabularyRepository
) {
    operator fun invoke() = wordRepository.getAllJLPTListCountWithState()
}