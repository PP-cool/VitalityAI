package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mini_workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val durationMinutes: Int,
    val iconName: String = "fitness", // "stretch", "core", "cardio", "walk", "fitness", "yoga"
    val colorHex: Long = 0xFFFF7043,
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
