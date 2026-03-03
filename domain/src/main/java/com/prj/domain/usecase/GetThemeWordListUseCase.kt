package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.ITopicVocabularyRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * A use case responsible for retrieving a list of words associated with a specific theme.
 *
 * @param mTopicRepository The repository that provides access to topic-based vocabulary data.
 * @param mSettingsRepository The repository implementation for getting language settings.
 *
 */
class GetThemeWordListUseCase @Inject constructor(
    private val mTopicRepository: ITopicVocabularyRepository,
    private val mSettingsRepository: IAppSettingsRepository
){
    /**
     * Retrieves a list of words for the given theme.
     *
     * @param theme The name of the theme for which to fetch the word list.
     * @return The result of calling [ITopicVocabularyRepository.getWordsByTheme] for the specified theme.
     */
    suspend operator fun invoke(theme: Int): List<JapaneseWord> {
        val language = mSettingsRepository.getCurrentLanguage()
        Timber.i("Language code: ${language.code}")
        return mTopicRepository.getWordsByTheme(theme, language.code)
    }
}