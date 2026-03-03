package com.prj.domain.model.translatescreen

/**
 * Represents the result of a text translation operation.
 *
 * This data class encapsulates the output from a translation api
 *
 * @property translatedText The resulting text after translation.
 * @property sourceLanguage The language code (e.g., "en", "ja") of the original text.
 * @property model The identifier of the translation model that performed the operation.
 */
data class TranslationResult(
    val translatedText: String?,
    val sourceLanguage: String?,
    val model: String?
)