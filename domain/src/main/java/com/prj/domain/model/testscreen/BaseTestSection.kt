package com.prj.domain.model.testscreen

/**
 * Represents a basic section or part of a test or exam.
 * This class holds the structural information for a test section, including its title,
 * description, and the list of questions it contains.
 *
 * Domain model for BaseSectionDto from ExamApiService.
 *
 * @property mondai The identifier or name for the test section (e.g., "mondai1").
 * @property type The specific part number or identifier within a larger test structure.
 * @property title The main title of the test section.
 * @property description A detailed description or instruction for this section.
 * @property questions A list of [Question] objects that belong to this section.
 */
data class BaseTestSection (
    val mondai: String,
    val type: TestSectionType,
    val title: String,
    val description: String,
    val questions: List<Question>,
    val correctAnswerCount: Int = 0,
)

enum class TestSectionType(val value: String) {
    LISTENING("listening"),
    READING("reading"),
    VOCABULARY("vocabulary"),
    GRAMMAR("grammar");

    companion object {
        fun fromString(value: String): TestSectionType {
            return entries.find { it.value == value } ?: VOCABULARY
        }
        private val durationMap = mapOf(
            Level.N1 to mapOf(LISTENING to 60, READING to 40, VOCABULARY to 35, GRAMMAR to 35),
            Level.N2 to mapOf(LISTENING to 50, READING to 35, VOCABULARY to 30, GRAMMAR to 30),
            Level.N3 to mapOf(LISTENING to 40, READING to 40, VOCABULARY to 30, GRAMMAR to 30),
            Level.N4 to mapOf(LISTENING to 35, READING to 30, VOCABULARY to 25, GRAMMAR to 25),
            Level.N5 to mapOf(LISTENING to 30, READING to 20, VOCABULARY to 20, GRAMMAR to 20)
        )
        fun getDurationSeconds(level: Level, type: TestSectionType): Long {
            return (durationMap[level]?.get(type) ?: 0) * 60L // Convert minutes to seconds
        }
    }
}


