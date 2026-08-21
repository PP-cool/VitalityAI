package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discipline_history")
data class DisciplineDayEntity(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val dayNumber: Int, // Day of month: 1..31
    val monthYear: String, // Format: MMMM yyyy (e.g. "August 2026")
    val completionPercent: Int = 0, // 0..100
    val isCheckedIn: Boolean = false,
    val completedTargetsCount: Int = 0,
    val totalTargetsCount: Int = 0,
    val stepsDone: Boolean = false,
    val summaryNotes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
