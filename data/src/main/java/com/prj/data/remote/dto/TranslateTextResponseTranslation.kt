package com.prj.data.remote.dto

data class TranslateTextResponseTranslation(
    val detectedSourceLanguage: String?,
    val model: String?,
    val translatedText: String?,
)
