package com.prj.domain.usecase

import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.model.testscreen.WordListType
import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IStorageRepository
import com.prj.domain.repository.ITopicVocabularyRepository
import javax.inject.Inject

class UpdateLearningStateUseCase @Inject constructor(
    private val mWordRepository: ITopicVocabularyRepository,
    private val mAuthRepo: IAuthRepository,
    private val mStorageRepository: IStorageRepository,
){
    suspend operator fun invoke(
        listId: String,
        entryId: Int,
        newState: LearningState,
        listType: WordListType
    ): Result<Unit>{
        // Offline first
        mWordRepository.updateWordByLearningState(listId, entryId, newState, listType)
        val user = mAuthRepo.getCurrentAuthUser()
            ?: return Result.failure(Exception("No user logged in"))
        mStorageRepository.updateWordInOnlineStorage(user.id, listId, entryId, newState, listType)
        return Result.success(Unit)
    }
}