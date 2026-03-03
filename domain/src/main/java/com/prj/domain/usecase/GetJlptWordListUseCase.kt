package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

/**
 * A use case for retrieving a list of vocabulary words for a specific JLPT level.
 *
 * @property mTopicRepository The repository responsible for providing access to topic-based
 *                          and level-based vocabulary data.
 * @property mSettingsRepository The repository for retrieving app settings including language.
 */
class GetJlptWordListUseCase @Inject constructor(
    private val mTopicRepository: ITopicVocabularyRepository,
    private val mSettingsRepository: IAppSettingsRepository
) {
    /**
     * Retrieves the word list for a specific JLPT level.
     * Language is automatically retrieved from app settings.
     *
     * @param level A string representing the JLPT level (e.g., "N1", "N2") for which
     *              to retrieve the word list.
     * @return A list of words corresponding to the given level in the user's selected language.
     */
    suspend operator fun invoke(level: String): List<JapaneseWord> {
        val language = mSettingsRepository.getCurrentLanguage()
        return mTopicRepository.getWordsByLevel(level.lowercase(), language.code)
    }
}
