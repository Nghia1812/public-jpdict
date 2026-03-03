package com.prj.domain.model

data class ProgressStats(
    val wordsLearnedToday: Int = 0,
    val wordsLearnedThisWeek: Int = 0,
    val testsCompletedToday: Int = 0,
    val currentStreak: Int = 0,
    val accuracyRate: Float = 0f,
    val dueReviewCount: Int = 0,
    val newAchievements: List<Achievement> = emptyList()
)

data class Achievement(
    val title: String,
    val description: String,
    val icon: String
)
