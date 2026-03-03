package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.IWordRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * A use case responsible for searching for words in the repository.
 *
 * @param mWordRepository The repository implementation for accessing word data.
 * @param mSettingsRepository The repository implementation for getting language settings.
 */
class FindWordListUseCase @Inject constructor(
    private val mWordRepository: IWordRepository,
    private val mSettingsRepository: IAppSettingsRepository
) {
    suspend operator fun invoke(search: String): List<JapaneseWord> {
        val language = mSettingsRepository.getCurrentLanguage()
        Timber.d("Language code: ${language.code}")
        if(language == AppLanguage.VIETNAMESE) {
            return mWordRepository.findWordList(removeVietnameseTones(search), language.code)
        }
        return mWordRepository.findWordList(search, language.code)
    }
    private fun removeVietnameseTones(str: String): String {
        var result = str.lowercase()
        result = result.replace("[àáạảãâầấậẩẫăằắặẳẵ]".toRegex(), "a")
        result = result.replace("[èéẹẻẽêềếệểễ]".toRegex(), "e")
        result = result.replace("[ìíịỉĩ]".toRegex(), "i")
        result = result.replace("[òóọỏõôồốộổỗơờớợởỡ]".toRegex(), "o")
        result = result.replace("[ùúụủũưừứựửữ]".toRegex(), "u")
        result = result.replace("[ỳýỵỷỹ]".toRegex(), "y")
        result = result.replace("[đ]".toRegex(), "d")
        return result
    }
}
