package com.prj.domain.model.testscreen

/**
 * Represents a complete test structure.
 *
 *
 * @property id The unique identifier for this specific JLPT test.
 * @property title The official title or name of the test (e.g., "JLPT N3 Practice Test 1").
 * @property level The JLPT level this test corresponds to (e.g., "N1", "N2", "N3").
 * @property skills A list of language skills being evaluated in this test (e.g., "Reading", "Listening", "Grammar").
 * @property sections A list of [BaseTestSection] objects, where each object represents a major section of the test.
 */
data class JLPTTest (
    val id: String,
    val title: String,
    val level: String,
    val duration: Int,
    val sections: List<BaseTestSection>
)
