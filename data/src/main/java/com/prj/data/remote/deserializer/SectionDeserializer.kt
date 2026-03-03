package com.prj.data.remote.deserializer

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.prj.data.remote.dto.AudioQuestionDto
import com.prj.data.remote.dto.BaseQuestionDto
import com.prj.data.remote.dto.BaseSectionDto
import com.prj.data.remote.dto.PassageQuestionDto
import com.prj.data.remote.dto.TextOnlyQuestionDto
import timber.log.Timber
import java.lang.reflect.Type

class DtoDeserializer : JsonDeserializer<BaseSectionDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BaseSectionDto {
        val obj = json.asJsonObject

        // Ensure no NullPointerException or IllegalStateException is returned at runtime in case any of the json fields are null
        val mondai = runCatching {obj["mondai"].asString}.getOrNull() ?: ""
        val part = runCatching { obj["part"].asString }.getOrNull() ?: ""
        val title = runCatching { obj["title"].asString }.getOrNull() ?: ""
        val description = runCatching {obj["description"].asString}.getOrNull() ?: ""
        val questionsJson = runCatching {obj["questions"].asJsonArray}.getOrNull() ?: JsonArray()

        val questions = mutableListOf<BaseQuestionDto>()

        for (q in questionsJson) {
            val qObj = q.asJsonObject
            try{
                val question: BaseQuestionDto = when {
                    qObj.has("audioURL") -> {
                        context.deserialize(q, AudioQuestionDto::class.java)
                    }
                    qObj.has("passage") -> {
                        context.deserialize(q, PassageQuestionDto::class.java)
                    }
                    else -> {
                        context.deserialize(q, TextOnlyQuestionDto::class.java)
                    }
                }

                questions.add(question)
            } catch (e: JsonParseException){
                Timber.e(e, "Invalid question JSON")
                val fallback = TextOnlyQuestionDto(
                    number = -1,
                    text = "unknown",
                    options = emptyList(),
                    answer = -1
                )
                questions.add(fallback)
            }

        }

        return BaseSectionDto(
            mondai = mondai,
            part = part,
            title = title,
            description = description,
            questions = questions
        )
    }
}
