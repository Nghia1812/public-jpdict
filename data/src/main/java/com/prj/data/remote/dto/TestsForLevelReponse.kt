package com.prj.data.remote.dto

data class TestInfoDto(
    val id: String,
    val title: String
)

/**
 * DTO for returned Json object from getTestsForLevel API - @ExamApiService
 */
data class JLPTTestsResponse(
    val items: List<TestInfoDto>,
    val page: Int,
    val limit: Int,
    val totalItems: Int,
    val totalPages: Int,
    val hasPrev: Boolean,
    val hasNext: Boolean
)
