package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_metrics")
data class DailyMetricsEntity(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val waterCurrentMl: Int = 0,
    val waterTargetMl: Int = 2500,
    val sleepHours: Float = 0.0f,
    val sleepTargetHours: Float = 8.0f,
    val stepCurrent: Int = 0,
    val stepTarget: Int = 10000,
    val workoutMinutes: Int = 0,
    val workoutTargetMinutes: Int = 45,
    val aiInsight: String = "Welcome to your day! Start with a fresh glass of water and set your daily intentions.",
    val streakDays: Int = 0,
    val userName: String = "Alex Morgan",
    val userGoal: String = "Daily fitness, hydration & 8h sleep",
    val avatarIndex: Int = 0,
    val isWaterCompleted: Boolean = false,
    val isSleepCompleted: Boolean = false,
    val isStepsCompleted: Boolean = false,
    val isWorkoutCompleted: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
