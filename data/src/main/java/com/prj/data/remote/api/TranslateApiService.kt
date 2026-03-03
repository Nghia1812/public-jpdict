package com.prj.data.remote.api

import com.prj.data.remote.dto.TranslateTextResponseList
import com.prj.data.remote.dto.TranslationRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface TranslateApiService {
    @POST("language/translate/v2")
    suspend fun getTranslatedText(
        @Query("key") apiKey: String,
        @Body requestBody: TranslationRequestBody
    ): Response<TranslateTextResponseList>
}
