package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.Token
import com.prj.domain.repository.TextTokenizer
import javax.inject.Inject

class TokenizeTextUseCase @Inject constructor(
    private val textTokenizer: TextTokenizer
) {
    suspend operator fun invoke(text: String): List<Token> {
        if (text.isBlank()) {
            return emptyList()
        }
        return textTokenizer.tokenizeWithFurigana(text)
    }
}