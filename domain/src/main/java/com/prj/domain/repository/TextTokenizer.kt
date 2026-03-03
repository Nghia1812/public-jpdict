package com.prj.domain.repository

import com.prj.domain.model.dictionaryscreen.Token

/**
 * Interface for tokenizing Japanese text into tokens with furigana
 */
interface TextTokenizer {

    /**
     * Initialize the tokenizer (may involve loading dictionaries)
     * Should be called once during app startup
     */
    suspend fun initialize()

    /**
     * Tokenize Japanese text and add furigana readings
     *
     * @param text Japanese text to tokenize
     * @return List of tokens with surface form and furigana
     */
    suspend fun tokenizeWithFurigana(text: String): List<Token>
}