package com.prj.data.local.model

import androidx.room.ColumnInfo

data class ListWordCountWithStateEntity(
    val id: String,
    val name: String,
    @ColumnInfo(name = "total_count") val totalCount: Int,
    @ColumnInfo(name = "remembered_count") val rememberedCount: Int,
    @ColumnInfo(name = "forgot_count") val forgotCount: Int,
    @ColumnInfo(name = "not_learnt_count") val notLearntCount: Int
)
