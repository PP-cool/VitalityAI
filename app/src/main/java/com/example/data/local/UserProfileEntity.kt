package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Alex Morgan",
    val userGoal: String = "Daily fitness, hydration & 8h sleep",
    val avatarIndex: Int = 0,
    val waterTargetMl: Int = 2500,
    val sleepTargetHours: Float = 8.0f,
    val stepTarget: Int = 10000,
    val workoutTargetMinutes: Int = 45,
    val isReminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val lastActiveDate: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
