package com.prj.domain.model.testscreen

/**
 * Represents a selectable test item in a list.
 *
 * This data class provides a lightweight summary of a test
 *
 * @property id The unique identifier for the test.
 * @property title The display title of the test.
 */
data class TestItem(
    val id: String,
    val title: String,
    val status: TestStatus,
)

/**
 * Represents a collection of available JLPT tests.
 *
 * This class acts as a container for a list of [TestItem] objects,
 *
 * @property items A list of [TestItem] objects, each representing an available test.
 */
data class JlptTestInfo(
    val items: List<TestItem>
)
