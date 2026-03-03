package com.prj.domain.model.dictionaryscreen

/**
 * Representing a named list and its corresponding item count.
 *
 * This is a domain model used to transfer aggregated data from the data layer
 * to the UI layer.
 *
 * @property name The name of the list (e.g., "N5", "My Favorite Words").
 * @property count The total number of items within that list.
 */
data class WordList(
    val listId: String = "",
    val name: String = "",
    val count: Int = 0
)