package com.prj.data.remote.api

import com.prj.data.remote.dto.JLPTTestDto
import com.prj.data.remote.dto.JLPTTestsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface ExamApiService {
    @GET("exams/{source}/{level}/jlpt_test")
    suspend fun getTestsForLevel(
        @Header("X-API-Key") apiKey: String,
        @Path("source") source: String,
        @Path("level") level: String,
        @Query("page") page: String = "1"
    ) : Response<JLPTTestsResponse>

    @GET("exams/{source}/{level}/jlpt_test/{id}")
    suspend fun getTestQuestions(
        @Header("X-API-Key") apiKey: String,
        @Path("source") source: String,
        @Path("level") level: String,
        @Path("id") id: String,
        @Query("skills") skills: String
    ) : Response<JLPTTestDto>
}