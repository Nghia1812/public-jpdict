package com.prj.domain.usecase

import com.prj.domain.model.testscreen.TestResult
import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IStorageRepository
import javax.inject.Inject

class GetTestResultUseCase @Inject constructor(
    private val authRepo: IAuthRepository,
    private val storageRepo: IStorageRepository
){
    suspend operator fun invoke(testId: String, skill: String): Result<TestResult?> {
        val user = authRepo.getCurrentAuthUser() ?: return Result.failure(Exception("User not logged in"))
        return storageRepo.getTestResultForSpecificSkill(user.id, testId, skill)
    }
}