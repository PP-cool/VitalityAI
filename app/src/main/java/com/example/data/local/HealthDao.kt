package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // Daily Metrics
    @Query("SELECT * FROM daily_metrics WHERE date = :date LIMIT 1")
    fun getDailyMetrics(date: String): Flow<DailyMetricsEntity?>

    @Query("SELECT * FROM daily_metrics ORDER BY date DESC LIMIT 1")
    fun getLatestMetrics(): Flow<DailyMetricsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetrics(metrics: DailyMetricsEntity)

    // Habits
    @Query("SELECT * FROM habits ORDER BY completed ASC, id DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()

    // Mini Workouts
    @Query("SELECT * FROM mini_workouts ORDER BY id ASC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntity>)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM mini_workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query("UPDATE mini_workouts SET isCompletedToday = :isCompleted, lastCompletedDate = :date WHERE id = :id")
    suspend fun setWorkoutCompleted(id: Long, isCompleted: Boolean, date: String)

    @Query("UPDATE mini_workouts SET isCompletedToday = 0 WHERE lastCompletedDate != :today")
    suspend fun resetWorkoutsIfNotToday(today: String)

    @Query("UPDATE mini_workouts SET isCompletedToday = 0")
    suspend fun resetAllWorkoutsCompletion()

    // Dynamic Daily Targets
    @Query("SELECT * FROM daily_targets ORDER BY orderIndex ASC, id ASC")
    fun getAllDailyTargets(): Flow<List<DailyTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTarget(target: DailyTargetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTargets(targets: List<DailyTargetEntity>)

    @Update
    suspend fun updateDailyTarget(target: DailyTargetEntity)

    @Query("UPDATE daily_targets SET currentValue = 0.0, isCompleted = 0, lastUpdatedDate = :today WHERE lastUpdatedDate != :today")
    suspend fun resetDailyTargetsIfNotToday(today: String)

    @Query("DELETE FROM daily_targets WHERE id = :id")
    suspend fun deleteDailyTarget(id: Long)

    @Query("DELETE FROM daily_targets")
    suspend fun clearDailyTargets()

    // User Profile (Permanent single source of truth for user data)
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)

    // Discipline History (Permanent records for monthly heatmap & streak)
    @Query("SELECT * FROM discipline_history ORDER BY date ASC")
    fun getAllDisciplineHistory(): Flow<List<DisciplineDayEntity>>

    @Query("SELECT * FROM discipline_history WHERE monthYear = :monthYear ORDER BY dayNumber ASC")
    fun getDisciplineForMonth(monthYear: String): Flow<List<DisciplineDayEntity>>

    @Query("SELECT * FROM discipline_history WHERE date = :date LIMIT 1")
    suspend fun getDisciplineForDate(date: String): DisciplineDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDisciplineDay(day: DisciplineDayEntity)
}
