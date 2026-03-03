package com.prj.data.repository


import com.prj.data.BuildConfig
import com.prj.data.mapper.toDomain
import com.prj.data.remote.api.TranslateApiService
import com.prj.data.remote.dto.TranslationRequestBody
import com.prj.domain.model.translatescreen.TranslationResult
import com.prj.domain.repository.ITranslationRepository
import javax.inject.Inject


class TranslationRepository @Inject constructor(
    private val mTranslationApiService: TranslateApiService
) : BaseApiRepository(), ITranslationRepository {
    private val apiKey: String = BuildConfig.TRANSLATE_API_KEY

    override suspend fun translateText(
        text: String,
        source: String,
        target: String
    ): Result<TranslationResult> {
        return safeApiCall {
            mTranslationApiService.getTranslatedText(
                apiKey, TranslationRequestBody(
                    q = text, target = target, source = source
                )
            )
        }.mapCatching { data ->
            data.toDomain()
        }
    }
}