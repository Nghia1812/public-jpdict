package com.prj.domain.usecase

import com.prj.domain.model.testscreen.WordListType
import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

/**
 * Use case for getting list learning state
 */
class GetListLearningStateUseCase @Inject constructor(
    private val mTopicVocabularyRepository: ITopicVocabularyRepository
) {
    suspend operator fun invoke(listId: String, listType: WordListType) = mTopicVocabularyRepository.getListLearningStates(listId, listType)
}