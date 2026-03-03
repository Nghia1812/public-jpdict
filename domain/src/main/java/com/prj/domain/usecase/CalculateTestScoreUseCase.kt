package com.prj.domain.usecase

import com.prj.domain.model.testscreen.BaseTestSection
import com.prj.domain.model.testscreen.TestSectionType
import javax.inject.Inject

class CalculateTestScoreUseCase @Inject constructor(){
    data class ScoreResult(
        val correctAnswers: Int,
        val totalQuestions: Int,
        val score: Int
    )

    operator fun invoke(
        testSections: List<BaseTestSection>,
        selectedAnswers: Map<Int, Int>,
        targetSkillType: TestSectionType
    ): ScoreResult {
        var correct = 0
        var totalQuestions = 0

        for (section in testSections) {
            // Count total questions only for the target skill type
            if (targetSkillType == section.type && section.questions.isNotEmpty()) {
                totalQuestions += section.questions.size
                // Count correct answers across all sections
                for (question in section.questions) {
                    if (question.answer == selectedAnswers[question.number]) {
                        correct++
                    }
                }
            }
        }

        return ScoreResult(
            correctAnswers = correct,
            totalQuestions = totalQuestions,
            score = correct
        )
    }
}