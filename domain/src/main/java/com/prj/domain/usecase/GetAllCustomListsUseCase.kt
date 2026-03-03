package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Get all custom lists use case
 * 2 methods: one return all lists with no count, another return all lists with count
 *
 * @param mWordRepository: IWordRepository
 * @param mSettingsRepository The repository implementation for getting language settings.
 */
class GetAllCustomListsUseCase @Inject constructor(
    private val mWordRepository: IWordRepository,
    private val mSettingsRepository: IAppSettingsRepository
) {
    suspend fun getAllLists(): Flow<List<CustomWordListWithEntries>> {
        val language = mSettingsRepository.getCurrentLanguage()
        return mWordRepository.getAllCustomWordLists(language.code)
    }

    fun getAllListsWithCount() = mWordRepository.getAllCustomWordListsWithCount()

}