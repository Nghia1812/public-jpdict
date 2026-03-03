package com.prj.domain.model.dictionaryscreen

/**
 * A domain model representing a theme along with its associated image and the total count of words it contains.
 *
 *
 * @property name The name of the theme (e.g., "Food", "Travel").
 * @property image A byte array containing the image data for the theme.
 * @property count The total number of word entries associated with this theme.
 */
data class ThemeCount(
    val id: Int,
    val name: String,
    val image: ByteArray,
    val count: Int
)