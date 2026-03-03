package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.IWordRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case to get a word by its ID.
 *
 * @param mWordRepository The repository implementation for getting words.
 * @param mSettingsRepository The repository implementation for getting language settings.
 *
 */
class GetWordByIdUseCase @Inject constructor(
    private val mWordRepository: IWordRepository,
    private val mSettingsRepository: IAppSettingsRepository
) {
    suspend operator fun invoke(wordId: Int): JapaneseWord {
        val language = mSettingsRepository.getCurrentLanguage()
        Timber.i("Language code: ${language.code}")
        return mWordRepository.getWordById(wordId, language.code)
    }
}