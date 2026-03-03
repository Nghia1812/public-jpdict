package com.prj.domain.model.dictionaryscreen

data class KanjiDetail(
    val kanji: String,
    val onyomi: List<String>,
    val kunyomi: List<String>,
    val meanings: List<String>,
    val strokeCount: Int?,
    val grade: Int?,
    val jlpt: Int?,
    val strokePaths: List<String>
)
