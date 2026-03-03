package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

/**
 * Use case for retrieving all words in a specific theme.
 *
 * @param repository The repository handling topic vocabulary operations.
 * @param settingsRepository The repository for getting language settings.
 */
class GetWordsByThemeUseCase @Inject constructor(
    private val repository: ITopicVocabularyRepository,
    private val settingsRepository: IAppSettingsRepository
) {
    /**
     * Retrieves all words in a theme.
     *
     * @param themeId The ID of the theme.
     * @return A list of Japanese words in the theme.
     */
    suspend operator fun invoke(themeId: Int): List<JapaneseWord> {
        val language = settingsRepository.getCurrentLanguage()
        return repository.getWordsByTheme(themeId, language.code)
    }
}
