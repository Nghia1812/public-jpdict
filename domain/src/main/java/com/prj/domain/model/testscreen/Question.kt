package com.prj.domain.model.testscreen


/**
 * Represents a question within a test in [BaseTestSection].
 *
 * This sealed class defines the common properties that all questions must have,
 * such as a question number, text, a list of possible options, and the correct answer.
 * It serves as a base for more specific question types.
 *
 * @property number The sequential number of the question within its section (e.g., 1, 2, 3).
 * @property text The main text or prompt of the question itself.
 * @property options A list of possible answers (choices) for the question.
 * @property answer The zero-based index of the correct option in the `options` list.
 */
sealed class Question {
    abstract val number: Int
    abstract val text: String
    abstract val options: List<String>
    abstract val answer: Int

    data class TextQuestion(
        override val number: Int,
        override val text: String,
        override val options: List<String>,
        override val answer: Int
    ) : Question()


    data class AudioQuestion(
        override val number: Int,
        override val text: String,
        override val options: List<String>,
        override val answer: Int,
        val audioURL: String
    ) : Question()

    data class PassageQuestion(
        override val number: Int,
        override val text: String,
        override val options: List<String>,
        override val answer: Int,
        val passage: String
    ) : Question()
}