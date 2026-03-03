package com.prj.domain.model.testscreen

enum class Source(val value: String) {
    CUSTOM("custom"),
    OFFICIAL("official");

    companion object {
        fun fromString(value: String): Source {
            return entries.find {
                it.value.equals(value, ignoreCase = true)
            } ?: throw IllegalArgumentException("Unknown source: $value")
        }
    }
}