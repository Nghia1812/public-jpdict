package com.prj.japanlib.common

object TextUtils {
    fun containsKanji(s: String): Boolean {
        return s.any { ch ->
            ch in '\u4E00'..'\u9FFF' ||  // CJK Unified Ideographs
                    ch in '\u3400'..'\u4DBF'     // CJK Extension A
        }
    }
}