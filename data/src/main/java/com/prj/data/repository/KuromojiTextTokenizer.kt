package com.prj.data.repository

import android.util.LruCache
import com.atilika.kuromoji.TokenizerBase
import com.atilika.kuromoji.ipadic.Tokenizer
import com.prj.domain.model.dictionaryscreen.Token
import com.prj.domain.repository.TextTokenizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KuromojiTextTokenizer @Inject constructor(): TextTokenizer {
    private var tokenizer: Tokenizer? = null

    // Ensure KuromojiHelper tokenizer not init in Main thread (avoid ANR)
    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            if (tokenizer == null) {
                tokenizer = Tokenizer.Builder()
                    .mode(TokenizerBase.Mode.NORMAL)
                    .build()
            }
        }
    }

    private val cache = LruCache<String, List<Token>>(100)

    override suspend fun tokenizeWithFurigana(text: String): List<Token> = withContext(Dispatchers.Default) {
        val t = tokenizer ?: throw IllegalStateException("KuromojiHelper not initialized")
        cache.get(text)?.let { return@withContext it }
        try {
            val tokens = t.tokenize(text)
            val result = tokens.map { token ->
                val kanji = token.surface ?: "" // get kanji
                val katakana = token.reading ?: ""
                val hiragana = katakanaToHiragana(katakana) // convert katakana to hiragana
                Token(surface = kanji, furigana = hiragana)
            }
            if (result.isNotEmpty()) cache.put(text, result)
            result
        } catch (e: Exception) {
            Timber.e("Error tokenizing: ${e.message}")
            emptyList()
        }
    }

    private fun katakanaToHiragana(katakana: String): String {
        return katakana.map { ch ->
            if (ch in 'ァ'..'ン') (ch.code - 0x60).toChar() else ch
        }.joinToString("")
    }

//    fun clearCache() {
//        cache.evictAll()
//    }

}