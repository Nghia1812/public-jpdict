package com.prj.domain.usecase

import com.prj.domain.model.testscreen.JlptTestInfo
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.model.testscreen.TestStatus
import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IJlptTestRepository
import com.prj.domain.repository.IStorageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class GetTestsForLevelUseCase @Inject constructor(
    private val mTestRepository: IJlptTestRepository,
    private val mStorageRepository: IStorageRepository,
    private val mAuthRepository: IAuthRepository
) {
    companion object {
        const val NUMBER_OF_SKILLS = 4
    }

    suspend operator fun invoke(
        source: Source,
        level: Level
    ): Result<JlptTestInfo> = coroutineScope {
        val user = mAuthRepository.getCurrentAuthUser()
            ?: return@coroutineScope Result.failure(Exception("No user logged in"))
        mTestRepository.getTestsForLevel(source, level).fold(
            onSuccess = { testInfo ->
                val deferredResult = testInfo.items.map { test ->
                    async {
                        val result = mStorageRepository.getAllTestResults(user.id, test.id)
                        Timber.i("Result of ${test.id}: $result")
                        if (result.isFailure) {
                            Timber.e("Error getting test results: ${result.exceptionOrNull()?.message}")
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        // Count completed skills
                        val isCompleted = result.getOrNull()?.completedSkillsCount ?: -1

                        // Update test status
                        test.copy(
                            status = when {
                                isCompleted == NUMBER_OF_SKILLS -> TestStatus.COMPLETED
                                isCompleted >= 0 -> TestStatus.IN_PROGRESS
                                else -> TestStatus.NOT_STARTED
                            }
                        )
                    }
                }
                val updatedTests = try {
                    deferredResult.awaitAll()
                } catch (e: Exception) {
                    Timber.e(e, "Error getting test results")
                    return@coroutineScope Result.failure(e)
                }
                Result.success(testInfo.copy(items = updatedTests))
            },
            onFailure = { Result.failure(it) }
        )
    }
}