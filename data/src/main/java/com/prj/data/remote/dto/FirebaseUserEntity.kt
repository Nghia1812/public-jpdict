package com.prj.data.remote.dto

/**
 * Represents the data structure of a user document in Firestore.
 */
data class FirebaseUserEntity(
    val id: String = "",
    val name: String? = "",
    val email: String? = "",
    val kanjiWords: Int = 0,
    val jlptWords: Int = 0
)
