package com.prj.domain.usecase

import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IStorageRepository
import com.prj.domain.repository.IWordRepository
import javax.inject.Inject

/**
 * Remove word from list use case
 * Handle both offline and online storage
 */
class RemoveWordFromListUseCase @Inject constructor(
    private val wordRepository: IWordRepository,
    private val authRepo: IAuthRepository,
    private val storageRepository: IStorageRepository

    ) {
    suspend operator fun invoke(listId: String, entryId: Int): Result<Unit> {
        // Offline first
        val localResult = wordRepository.removeWordFromCustomList(listId, entryId)
        if (localResult.isFailure) {
            return localResult // Return failure if local fail (e.g., DB error)
        }

        val user = authRepo.getCurrentAuthUser()
            ?: return Result.failure(Exception("No user logged in"))

        return storageRepository.deleteWordFromOnlineStorage(user.id, listId, entryId)
    }
}