package com.prj.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prj.domain.model.testscreen.LearningState

/**
 * Converter for RoomDB :
 * - As data from kanji_detail table is in form of Json array
 * - As data from custom_word_entry table is in form of LearningState
 * Convert it to List<String> and vice versa
 *
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromLearningState(state: LearningState): String {
        return state.name
    }

    @TypeConverter
    fun toLearningState(value: String): LearningState {
        return try {
            LearningState.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Default fallback
            LearningState.NOT_LEARNT_YET
        }
    }

}