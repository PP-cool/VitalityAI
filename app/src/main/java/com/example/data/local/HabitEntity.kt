package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // hydration, sleep, fitness, mindfulness, nutrition, custom
    val targetValue: Int = 1,
    val currentValue: Int = 0,
    val unit: String = "times",
    val completed: Boolean = false,
    val weeklyDaysMask: String = "0000000", // 7 binary digits for Mon, Tue, Wed, Thu, Fri, Sat, Sun
    val isAiGenerated: Boolean = false,
    val originGoal: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isDayChecked(dayIndex: Int): Boolean {
        if (dayIndex !in 0..6) return false
        val safeMask = if (weeklyDaysMask.length == 7) weeklyDaysMask else "0000000"
        return safeMask.getOrNull(dayIndex) == '1'
    }

    fun toggleDay(dayIndex: Int): HabitEntity {
        if (dayIndex !in 0..6) return this
        val chars = (if (weeklyDaysMask.length == 7) weeklyDaysMask else "0000000").toCharArray()
        chars[dayIndex] = if (chars[dayIndex] == '1') '0' else '1'
        val newMask = String(chars)

        // Today index: Calendar.MONDAY is 2 -> index 0, SUNDAY is 1 -> index 6
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val todayIdx = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        val isTodayDone = chars.getOrNull(todayIdx) == '1'

        return this.copy(
            weeklyDaysMask = newMask,
            completed = isTodayDone
        )
    }
}
