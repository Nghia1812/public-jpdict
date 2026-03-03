package com.prj.domain.usecase

import com.prj.domain.repository.IWordRepository
import javax.inject.Inject

/**
 * A use case for retrieving all available theme lists from the repository.
 *
 * @property mWordRepository The repository responsible for providing word and theme data.
 */
class GetAllThemeListsUseCase @Inject constructor(
    private val mWordRepository: IWordRepository
) {
    /**
     * Get all theme lists.
     *
     * @return Flow of ThemeCount objects representing all available theme lists.
     */
    operator fun invoke() = mWordRepository.getAllThemeLists()
}