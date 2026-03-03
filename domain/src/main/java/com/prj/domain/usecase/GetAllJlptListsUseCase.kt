package com.prj.domain.usecase

import com.prj.domain.repository.IWordRepository
import javax.inject.Inject


/**
 * A use case for retrieving the word count for specified JLPT levels.
 *
 * @property mWordRepository The repository responsible for accessing word and JLPT data.
 */
class GetAllJlptListsUseCase @Inject constructor(
    private val mWordRepository: IWordRepository
) {
    /**
     * Delegates the call to the `getWordCount` method of the
     * injected [IWordRepository].
     *
     * @param levels A list of strings representing the JLPT levels (e.g., "N1", "N2")
     *               for which to retrieve the word count.
     * @return The result from the repository as list of ListCount objects
     */
    suspend operator fun invoke(levels: List<String>) = mWordRepository.getJlptWordCount(levels)
}