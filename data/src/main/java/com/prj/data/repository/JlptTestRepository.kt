package com.prj.data.repository

import com.prj.data.mapper.toDomain
import com.prj.data.remote.api.ExamApiService
import com.prj.domain.model.testscreen.JLPTTest
import com.prj.domain.model.testscreen.JlptTestInfo
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.repository.IJlptTestRepository
import javax.inject.Inject

class JlptTestRepository @Inject constructor(
    private val mExamApiService: ExamApiService
): BaseApiRepository() , IJlptTestRepository {

    private val apiKey: String = "japanlib_key_123"

    override suspend fun getTestsForLevel(
        source: Source,
        level: Level,
    ): Result<JlptTestInfo> {
        return safeApiCall {
            mExamApiService.getTestsForLevel(apiKey, source.value, level.value)
        }.map { it.toDomain() }
    }

    override suspend fun getTestQuestions(
        source: Source,
        level: Level,
        id: String,
        skills: String
    ): Result<JLPTTest> {
        return safeApiCall {
            mExamApiService.getTestQuestions(
                apiKey, source.value, level.value,
                id = id,
                skills = skills
            )
        }.map { it.toDomain() }
    }
}

