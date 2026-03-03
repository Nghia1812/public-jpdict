package com.prj.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a predefined theme for grouping Japanese words, such as "Food" or "Travel".
 * Each theme has a unique ID, a name, and an associated image.
 *
 * This class defines the schema for the "theme_list" table in the Room database.
 *
 * @property id The unique identifier for the theme. It is the primary key and is generated automatically.
 * @property name The name of the theme (e.g., "Animals", "Daily Life").
 * @property image A byte array storing the image associated with the theme. This is useful for displaying
 *                 a visual representation of the theme in the UI.
 */
@Entity(tableName = "theme_list")
data class ThemeListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "image") val image: ByteArray
)
