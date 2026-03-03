package com.prj.domain.repository

import com.prj.domain.model.translatescreen.TranslationResult

interface ITranslationRepository {
    suspend fun translateText(text: String, source: String, target: String): Result<TranslationResult>
}