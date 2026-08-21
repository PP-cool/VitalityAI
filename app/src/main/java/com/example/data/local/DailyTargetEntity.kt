package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_targets")
data class DailyTargetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val currentValue: Float = 0f,
    val targetValue: Float = 100f,
    val unit: String = "pts",
    val stepDelta: Float = 10f,
    val iconName: String = "water",
    val colorHex: Long = 0xFF00E5FF,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0,
    val lastUpdatedDate: String = ""
)
