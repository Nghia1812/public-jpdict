package com.prj.domain.usecase

import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.model.testscreen.BaseTestSection
import com.prj.domain.model.testscreen.TestSectionType
import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IJlptTestRepository
import com.prj.domain.repository.IStorageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetTestSectionsUseCase @Inject constructor(
    private val mTestRepository: IJlptTestRepository,
    private val mStorageRepository: IStorageRepository,
    private val mAuthRepository: IAuthRepository
) {
    suspend operator fun invoke(
        source: Source,
        level: Level,
        testId: String
    ): Result<List<BaseTestSection>> = coroutineScope {
        val user = mAuthRepository.getCurrentAuthUser()
            ?: return@coroutineScope Result.failure(Exception("No user logged in"))

        val sections = TestSectionType.entries

        try {
            // Fetch all sections in parallel
            val testSections = sections.map { section ->
                async {
                    // Get test questions
                    val testQuestions = mTestRepository.getTestQuestions(
                        source,
                        level,
                        testId,
                        section.value
                    ).getOrElse {
                        return@async Result.failure<BaseTestSection>(it)
                    }

                    // Get user's test result
                    val userResult = mStorageRepository.getTestResultForSpecificSkill(
                        user.id,
                        testId,
                        section.value
                    ).getOrElse {
                        return@async Result.failure<BaseTestSection>(it)
                    }
                    val questionForSkillType = if (testQuestions.sections.isNotEmpty()) {
                        testQuestions.sections
                            .filter { it.type == section }
                    } else {
                        emptyList()
                    }
                    Result.success(
                        BaseTestSection(
                            mondai = "",
                            type = section,
                            title = "",
                            description = "",
                            questions = if (questionForSkillType.isEmpty()) {
                                emptyList()
                            } else {
                                questionForSkillType.flatMap { section ->
                                    section.questions
                                }
                            },
                            correctAnswerCount = userResult?.score ?: 0
                        )
                    )
                }
            }.awaitAll()

            // Check if any section failed
            val failure = testSections.firstOrNull { it.isFailure }
            if (failure != null) {
                return@coroutineScope Result.failure(
                    failure.exceptionOrNull() ?: Exception("Unknown error")
                )
            }

            // Extract all successful sections
            val successfulSections = testSections.mapNotNull { it.getOrNull() }
            Result.success(successfulSections)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


