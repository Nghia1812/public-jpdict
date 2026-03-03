package com.prj.domain.model.settingsscreen


enum class AppLanguage(val code: String, val displayName: String = "") {
    VIETNAMESE("vi", "Tiếng Việt"),
    ENGLISH("en", "English");

    companion object {
        fun fromString(language: String?): AppLanguage {
            return when (language) {
                "vi" -> VIETNAMESE
                else -> ENGLISH
            }
        }
    }
}