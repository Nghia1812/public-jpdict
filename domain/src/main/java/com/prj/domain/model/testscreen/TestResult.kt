package com.prj.domain.model.testscreen

/**
 * Represents the result of a specific skill's test.
 *
 * This data class stores the user's performance on a test, including a map of
 * chosen answers for each question and  score.
 *
 * @property chosenAnswers A map where the key is the question identifier (e.g., question number as a String)
 *                         and the value is the zero-based index of the answer selected by the user.
 * @property score The final score achieved by the user in the test, typically calculated based on the number of correct answers.
 * @property totalQuestions The total number of questions in the test.
 */
data class TestResult(
    val chosenAnswers: Map<String, Int> = emptyMap(),
    val score: Int = 0,
    val totalQuestions: Int = 0
)
