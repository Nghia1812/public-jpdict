package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.ITopicVocabularyRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * A use case for retrieving a list of words from a custom vocabulary list.
 *
 * @param mTopicRepository The repository responsible for accessing topic-based
 *                          and custom list vocabulary data.
 * @param mSettingsRepository The repository implementation for getting language settings.
 */
class GetCustomWordListUseCase @Inject constructor(
    private val mTopicRepository: ITopicVocabularyRepository,
    private val mSettingsRepository: IAppSettingsRepository
) {
    /**
     * Delegates the call to the `getWordsbyCustomList` method of the
     * injected [ITopicVocabularyRepository].
     *
     * @param listId A string representing the name of the custom vocabulary list
     *                 for which to retrieve the words.
     * @return The result from the repository, a list of words
     *         corresponding to the given custom list name.
     */
    suspend operator fun invoke(listId: String): List<JapaneseWord> {
        val language = mSettingsRepository.getCurrentLanguage()
        Timber.i("Language code: ${language.code}")
        return mTopicRepository.getWordsbyCustomList(listId, language.code)
    }

}


