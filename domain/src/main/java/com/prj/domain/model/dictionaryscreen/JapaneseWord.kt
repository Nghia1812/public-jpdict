package com.prj.domain.model.dictionaryscreen

data class JapaneseWord(
    val id: Int,
    val kanji: String?,
    val reading: String?,
    val meaning: String?,
    val type: String?
)
