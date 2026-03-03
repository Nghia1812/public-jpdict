package com.prj.domain.usecase

import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

/**
 * Use case for removing a word from a theme vocabulary list.
 *
 * This allows testers to manually remove words from theme-based vocabulary collections.
 *
 * @param repository The repository handling topic vocabulary operations.
 */
class RemoveWordFromThemeUseCase @Inject constructor(
    private val repository: ITopicVocabularyRepository
) {
    /**
     * Removes a word (entry) from a specific theme list.
     *
     * @param themeId The ID of the theme list.
     * @param entryId The ID of the word/entry to remove.
     */
    suspend operator fun invoke(themeId: Int, entryId: Int) {
        repository.removeWordFromTheme(themeId, entryId)
    }
}
