package com.prj.domain.model.testscreen

data class ListWordCountWithState(
    val id: String,
    val name: String,
    val totalCount: Int,
    val rememberedCount: Int,
    val forgotCount: Int,
    val notLearntCount: Int
)
