package com.prj.domain.usecase

import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

/**
 * Use case for adding a word to a theme vocabulary list.
 *
 * This allows testers to manually add words to theme-based vocabulary collections.
 *
 * @param repository The repository handling topic vocabulary operations.
 */
class AddWordToThemeUseCase @Inject constructor(
    private val repository: ITopicVocabularyRepository
) {
    /**
     * Adds a word (entry) to a specific theme list.
     *
     * @param themeId The ID of the theme list.
     * @param entryId The ID of the word/entry to add.
     */
    suspend operator fun invoke(themeId: Int, entryId: Int) {
        repository.addWordToTheme(themeId, entryId)
    }
}
