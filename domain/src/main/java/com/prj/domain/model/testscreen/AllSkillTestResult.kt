package com.prj.domain.model.testscreen

data class AllSkillTestResult(
    val listeningResult: TestResult? = null,
    val readingResult: TestResult? = null,
    val vocabularyResult: TestResult? = null,
    val grammarResult: TestResult? = null,
    val completedSkillsCount: Int = 0
)
