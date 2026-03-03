package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.IWordRepository
import javax.inject.Inject

/**
 * Use case to get related words for a given word.
 *
 * @param mWordRepository: IWordRepository
 * @param mSettingsRepository The repository implementation for getting language settings.
 *
 */
class GetRelatedWordListUseCase @Inject constructor(
    private val mWordRepository: IWordRepository,
    private val mSettingsRepository: IAppSettingsRepository
) {
    suspend operator fun invoke(wordId: Int, kanjis: List<String>): List<JapaneseWord>{
        val language = mSettingsRepository.getCurrentLanguage()
        return mWordRepository.getRelatedWords(wordId, kanjis, language.code)
    }
}