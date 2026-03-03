package com.prj.japanlib.common.utils

/**
 * Helper function to get distinct list of Kanji from a text
 */
fun extractKanji(text: String): List<String> {
    return text.filter { ch ->
        ch in '\u4E00'..'\u9FFF' ||   // CJK Unified Ideographs
                ch in '\u3400'..'\u4DBF' ||   // Extension A
                ch in '\uF900'..'\uFAFF'      // Compatibility Ideographs
    }.map { it.toString() }.distinct()
}