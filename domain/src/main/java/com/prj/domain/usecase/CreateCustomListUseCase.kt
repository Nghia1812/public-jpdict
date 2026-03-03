package com.prj.domain.usecase

import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IStorageRepository
import com.prj.domain.repository.IWordRepository
import javax.inject.Inject

/**
 * Create custom list use case
 * Handle both offline and online storage
 */
class CreateCustomListUseCase @Inject constructor(
    private val wordRepository: IWordRepository,
    private val authRepo: IAuthRepository,
    private val storageRepository: IStorageRepository,
) {
    suspend operator fun invoke(name: String) : Result<String> {
        val listId = wordRepository.createList(name)

        val user = authRepo.getCurrentAuthUser()?:
        return Result.failure(Exception("No user logged in"))
        storageRepository.uploadListToOnlineStorage(user.id, listId, name)
        return Result.success(listId)
    }
}