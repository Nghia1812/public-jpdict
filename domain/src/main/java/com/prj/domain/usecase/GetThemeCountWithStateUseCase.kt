package com.prj.domain.usecase

import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

class GetThemeCountWithStateUseCase @Inject constructor(
    private val wordRepository: ITopicVocabularyRepository
) {
    operator fun invoke() = wordRepository.getAllThemeListCountWithState()
}