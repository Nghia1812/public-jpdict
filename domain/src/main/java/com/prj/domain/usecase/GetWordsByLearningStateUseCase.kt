package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.model.testscreen.WordListType
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

/**
 * Use case to get a word by its learning state.
 *
 * @param mTopicVocabularyRepository The repository implementation for getting topic related words.
 * @param mSettingsRepository The repository implementation for getting language settings.
 *
 */
class GetWordsByLearningStateUseCase @Inject constructor(
    private val mTopicVocabularyRepository: ITopicVocabularyRepository,
    private val mSettingsRepository: IAppSettingsRepository
) {
    suspend operator fun invoke(
        listId: String,
        newState: LearningState,
        listType: WordListType
    ): List<JapaneseWord> {
        val language = mSettingsRepository.getCurrentLanguage()
        return if (newState == LearningState.NONE) {
            mTopicVocabularyRepository.getAllWordsByListType(listType, listId, language.code)
        } else {
            mTopicVocabularyRepository.getWordsByLearningState(listId, newState, listType, language.code)
        }
    }
}