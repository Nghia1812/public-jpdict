package com.prj.domain.model.testscreen

enum class Level(val value: String) {
    N5("N5"),
    N4("N4"),
    N3("N3"),
    N2("N2"),
    N1("N1");
    companion object {
        fun fromString(value: String): Level? =
            entries.find { it.value.equals(value, ignoreCase = true) }
        fun getAllLevels(): List<Level> = entries
    }
}