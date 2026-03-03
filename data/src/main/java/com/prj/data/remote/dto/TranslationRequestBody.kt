package com.prj.data.remote.dto

data class TranslationRequestBody(
    val q: String,
    val target: String,
    val format: String? = "text",
    val source: String? = null,
    val model: String = "base",
)
