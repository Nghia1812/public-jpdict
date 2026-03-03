package com.prj.data.local.model


/**
 * Representing the result of a query that retrieves a custom list's name
 * and the total count of words it contains.
 *
 *
 * @property name The name of the custom word list, retrieved from [CustomWordListEntity].
 * @property count The total number of word entries associated with that list.
 */
data class ListWordCount(
    val id: String,
    val name: String,
    val count: Int
)
