package com.prj.domain.usecase

import com.prj.domain.repository.IWordRepository
import javax.inject.Inject

/**
 * A use case for retrieving the word count for specified JLPT levels.
 *
 * @property mWordRepository The repository responsible for accessing word and JLPT data.
 */
class GetKanjiDetailUseCase @Inject constructor(
    private val mWordRepository: IWordRepository
) {
    suspend operator fun invoke(kanji: String) = mWordRepository.getKanjiInfo(kanji)
}