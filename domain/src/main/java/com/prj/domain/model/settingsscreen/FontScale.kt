package com.prj.domain.model.settingsscreen

enum class FontScale(val scale: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f);

    companion object {
        fun fromScaleFactor(value: Float?): FontScale {
            return when (value) {
                0.85f -> SMALL
                1.15f -> LARGE
                else -> MEDIUM
            }
        }
    }
}