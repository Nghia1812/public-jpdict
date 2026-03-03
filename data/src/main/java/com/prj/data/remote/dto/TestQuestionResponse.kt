package com.prj.data.remote.dto

/**
 * DTO based on returned Json object from getTestQuestions API - ExamApiService
 * Response is in the following format
 */
data class JLPTTestDto(
    val id: String,
    val title: String,
    val level: String,
    val duration: Int,
    val sections: List<BaseSectionDto>
)

data class BaseSectionDto (
    val mondai: String,
    val part: String,
    val title: String,
    val description: String,
    // 3 kinds of Question: TextOnly, Passage, Audio
    val questions: List<BaseQuestionDto>
)

interface BaseQuestionDto {
    val number: Int
    val text: String
    val options: List<String>
    val answer: Int
}

data class TextOnlyQuestionDto(
    override val number: Int,
    override val text: String,
    override val options: List<String>,
    override val answer: Int
) : BaseQuestionDto


data class PassageQuestionDto(
    override val number: Int,
    override val text: String,
    override val options: List<String>,
    override val answer: Int,
    val passage: String) : BaseQuestionDto


data class AudioQuestionDto(
    override val number: Int,
    override val text: String,
    override val options: List<String>,
    override val answer: Int,
    val audioURL: String) : BaseQuestionDto