package com.prj.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a custom list created by the user to group word entries.
 * Each list has a unique ID and a name.
 *
 * @property id The unique identifier for the list, generated automatically.
 * @property name The user-defined name of the custom word list.
 */
@Entity(tableName = "custom_word_list")
data class CustomWordListEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
)
