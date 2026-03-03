package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.CustomWordRef
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IStorageRepository
import com.prj.domain.repository.IWordRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Add word to custom list use case
 * Handle both offline and online storage
 */
class AddWordToCustomListUseCase @Inject constructor(
    private val wordRepository: IWordRepository,
    private val authRepo: IAuthRepository,
    private val storageRepository: IStorageRepository,
){
    suspend operator fun invoke(listId: String, entryId: Int) : Result<Unit>{
        // Offline first
        wordRepository.addWordToCustomList(CustomWordRef(listId, entryId, LearningState.NOT_LEARNT_YET))

        val user = authRepo.getCurrentAuthUser()
            ?: return Result.failure(Exception("No user logged in"))

        // Attempt to upload it to the online storage
        storageRepository.uploadWordToOnlineStorage(user.id, listId, entryId)
        return Result.success(Unit)
    }
}