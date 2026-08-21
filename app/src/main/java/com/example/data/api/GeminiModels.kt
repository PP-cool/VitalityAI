package com.example.data.api

data class MicroHabitParsed(
    val title: String,
    val category: String = "custom",
    val targetValue: Int = 1,
    val unit: String = "times",
    val reason: String? = null
)

data class GeminiCoachResult(
    val replyText: String,
    val generatedHabits: List<MicroHabitParsed> = emptyList(),
    val rawJson: String? = null
)
