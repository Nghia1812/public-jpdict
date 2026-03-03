package com.prj.domain.model.profilescreen

/**
 * Represents a user's profile data within the application.
 *
 *
 * @property id The unique identifier for the user.
 * @property name The display name of the user.
 * @property email The user's email address.
 * @property kanjiWords The total count of Kanji words the user has learned or saved.
 * @property jlptWords The total count of JLPT vocabulary words the user has learned or saved.
 */
data class User(
    val id: String = "",
    val name: String? = "",
    val email: String? = "" ,
    val kanjiWords: Int = 0,
    val jlptWords: Int = 0
)
