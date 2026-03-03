package com.prj.domain.repository

import com.prj.domain.model.testscreen.JLPTTest
import com.prj.domain.model.testscreen.JlptTestInfo
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source

interface IJlptTestRepository {
    suspend fun getTestsForLevel(source: Source, level: Level) :  Result<JlptTestInfo>

    suspend fun getTestQuestions(source: Source, level: Level, id: String, skills: String) : Result<JLPTTest>

}