package com.prj.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prj.domain.model.testscreen.LearningState
import com.prj.data.local.model.CustomWordEntryCrossRef
import com.prj.data.local.model.CustomWordListEntity
import com.prj.data.local.model.CustomWordListWithEntriesEntity
import com.prj.data.local.model.JapaneseWordEntity
import com.prj.data.local.model.JapaneseWordWithTranslation
import com.prj.data.local.model.JlptWordEntryEntity
import com.prj.data.local.model.KanjiDetailEntity
import com.prj.data.local.model.ListWordCount
import com.prj.data.local.model.ListWordCountWithStateEntity
import com.prj.data.local.model.ThemeEntryCrossRef
import com.prj.data.remote.dto.AudioQuestionDto
import com.prj.data.remote.dto.BaseSectionDto
import com.prj.data.remote.dto.FirebaseUserEntity
import com.prj.data.remote.dto.JLPTTestDto
import com.prj.data.remote.dto.JLPTTestsResponse
import com.prj.data.remote.dto.PassageQuestionDto
import com.prj.data.remote.dto.TextOnlyQuestionDto
import com.prj.data.remote.dto.TranslateTextResponseList
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.domain.model.dictionaryscreen.CustomWordRef
import com.prj.domain.model.dictionaryscreen.JlptWordRef
import com.prj.domain.model.dictionaryscreen.ThemeWordRef
import com.prj.domain.model.testscreen.BaseTestSection
import com.prj.domain.model.testscreen.JLPTTest
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.dictionaryscreen.KanjiDetail
import com.prj.domain.model.testscreen.JlptTestInfo
import com.prj.domain.model.testscreen.Question
import com.prj.domain.model.testscreen.TestItem
import com.prj.domain.model.translatescreen.TranslationResult
import com.prj.domain.model.profilescreen.User
import com.prj.domain.model.testscreen.ListWordCountWithState
import com.prj.domain.model.testscreen.TestSectionType
import com.prj.domain.model.testscreen.TestStatus

/**
 * Extension functions to convert DTO from DB/API server to domain models
 */

fun CustomWordListEntity.toDomain(): WordList =
    WordList(
        name = name,
        count = 0,
        listId = id
    )

fun ListWordCount.toDomain(): WordList =
    WordList(
        name = name,
        count = count,
        listId = id
    )

fun BaseSectionDto.toDomain() = BaseTestSection(
    mondai = mondai,
    type = TestSectionType.fromString(part),
    title = title,
    description = description,
    questions = questions.map { it ->
        when (it) {
            is TextOnlyQuestionDto -> it.toDomain()
            is AudioQuestionDto -> it.toDomain()
            is PassageQuestionDto -> it.toDomain()
            else -> throw IllegalArgumentException("Unknown section type")
        }
    }
)

fun TextOnlyQuestionDto.toDomain() = Question.TextQuestion(
    number = number,
    text = text,
    options = options,
    answer = answer,
)

fun AudioQuestionDto.toDomain() = Question.AudioQuestion(
    number = number,
    text = text,
    options = options,
    answer = answer,
    audioURL = audioURL
)

fun PassageQuestionDto.toDomain() = Question.PassageQuestion(
    number = number,
    text = text,
    options = options,
    answer = answer,
    passage = passage
)

fun JLPTTestDto.toDomain() = JLPTTest(
    id = id,
    title = title,
    level = level,
    duration = duration,
    sections = sections.map { it.toDomain() }
)

fun TranslateTextResponseList.toDomain(): TranslationResult {
    val firstTranslation = data.translations.firstOrNull()
        ?: throw IllegalStateException("translations list is empty")

    val translatedText = firstTranslation.translatedText
        ?: throw IllegalStateException("translatedText is null")

    val sourceLanguage = firstTranslation.detectedSourceLanguage

    val model = firstTranslation.model

    return TranslationResult(
        translatedText = translatedText,
        sourceLanguage = sourceLanguage,
        model = model
    )
}

fun JapaneseWordEntity.toDomain() = JapaneseWord(
    id = id,
    kanji = kanji,
    reading = reading,
    meaning =  null,
    type = null
)

fun JapaneseWordWithTranslation.toDomain() = JapaneseWord(
    id = entry.id,
    kanji = entry.kanji,
    reading = entry.reading,
    meaning = translation.gloss,
    type = translation.position
)

fun JLPTTestsResponse.toDomain(): JlptTestInfo {
    val testList = mutableListOf<TestItem>()
    for (item in items) {
        val testItem = TestItem(item.id, item.title,TestStatus.NOT_STARTED)
        testList.add(testItem)
    }
    return JlptTestInfo(items = testList)
}

fun FirebaseUser.toDomain() = User(
    id = uid,
    email = email,
    name = displayName
)

fun FirebaseUserEntity.toDomain(): User {
    return User(
        id = this.id,
        name = this.name,
        email = this.email,
        kanjiWords = this.kanjiWords,
        jlptWords = this.jlptWords
    )
}

private val gson = Gson()

fun KanjiDetailEntity.toDomain(): KanjiDetail {
    return KanjiDetail(
        kanji = kanji,
        onyomi = parseJsonArray(onyomi),
        kunyomi = parseJsonArray(kunyomi),
        meanings = parseJsonArray(meanings),
        strokeCount = strokeCount,
        grade = grade,
        jlpt = jlpt,
        strokePaths = parseJsonArray(strokes)
    )
}

private fun parseJsonArray(json: String): List<String> {
    return try {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

fun CustomWordEntryCrossRef.toDomain(): CustomWordRef {
    return CustomWordRef(
        listId = listId,
        entryId = entryId,
        learningState = learningState
    )
}

fun CustomWordRef.toEntity(): CustomWordEntryCrossRef {
    return CustomWordEntryCrossRef(
        listId = listId,
        entryId = entryId,
        learningState = learningState
    )
}

fun ListWordCountWithStateEntity.toDomain(): ListWordCountWithState {
    return ListWordCountWithState(
        id = id,
        name = name,
        totalCount = totalCount,
        rememberedCount = rememberedCount,
        forgotCount = forgotCount,
        notLearntCount = notLearntCount
    )
}

fun User.toEntity(): FirebaseUserEntity {
    return FirebaseUserEntity(
        id = this.id,
        name = this.name,
        email = this.email,
        kanjiWords = this.kanjiWords,
        jlptWords = this.jlptWords
    )
}

fun CustomWordListWithEntriesEntity.toDomain(): CustomWordListWithEntries = CustomWordListWithEntries(
    list = list.toDomain(),
    entries = entries.map { it.toDomain() }
)

fun JlptWordRef.toEntity(): JlptWordEntryEntity {
    return JlptWordEntryEntity(
        listId = listId,
        entryId = entryId,
        learningState = learningState
    )
}

fun ThemeWordRef.toEntity(): ThemeEntryCrossRef {
    return ThemeEntryCrossRef(
        themeId = listId.toInt(),
        entryId = entryId,
        learningState = learningState
    )
}

