package com.prj.domain.usecase

import com.prj.domain.model.testscreen.TestResult
import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IStorageRepository
import javax.inject.Inject

class SaveTestResultUseCase @Inject constructor(
    private val authRepo: IAuthRepository,
    private val storageRepo: IStorageRepository
){
    suspend operator fun invoke(testId: String, skill: String, testResult: TestResult): Result<Unit> {
        val user = authRepo.getCurrentAuthUser() ?: return Result.failure(Exception("User not logged in"))
        return storageRepo.saveTestResult(user.id, testId, skill, testResult)
    }
}