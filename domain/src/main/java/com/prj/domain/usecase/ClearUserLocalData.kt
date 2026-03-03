package com.prj.domain.usecase

import com.prj.domain.repository.IWordRepository
import javax.inject.Inject

/**
 * Clear user local data use case
 */
class ClearUserLocalData @Inject constructor(
    private val wordRepository: IWordRepository
) {
    suspend operator fun invoke() = wordRepository.clearUserLocalData()

}