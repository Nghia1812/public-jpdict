package com.prj.domain.model.dictionaryscreen

data class ExampleWithFurigana(
    val japanese: String,
    val english: String,
    val tokens: List<Token> = emptyList()
)
