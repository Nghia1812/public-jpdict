package com.prj.domain.usecase

import com.prj.domain.model.testscreen.JLPTTest
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.repository.IJlptTestRepository
import javax.inject.Inject

class GetTestQuestionsUseCase @Inject constructor(
    private val testRepository: IJlptTestRepository
) {
    suspend operator fun invoke(source: Source, level: Level, id: String, skill: String) : Result<JLPTTest> {
        val res = testRepository.getTestQuestions(source, level, id, skill)
        return res
    }
}