package com.prj.data.remote.dto

data class TranslateTextResponseList(
    val data: TranslationData
)

data class TranslationData(
    val translations: List<TranslateTextResponseTranslation>
)
