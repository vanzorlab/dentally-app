package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE dateString = :date ORDER BY orderIndex ASC, id ASC")
    fun getHabitsForDate(date: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY dateString DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setHabitCompleted(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("SELECT COUNT(*) FROM habits WHERE dateString = :date")
    suspend fun getHabitCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM habits WHERE dateString = :date AND isCompleted = 1")
    suspend fun getCompletedHabitCountForDate(date: String): Int

    @Query("SELECT DISTINCT dateString FROM habits WHERE isCompleted = 1")
    fun getCompletedDates(): Flow<List<String>>

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()
}

@Dao
interface OhisDao {
    @Query("SELECT * FROM ohis_assessments ORDER BY timestamp DESC")
    fun getAllOhisAssessments(): Flow<List<OhisEntity>>

    @Query("SELECT * FROM ohis_assessments ORDER BY timestamp DESC LIMIT 1")
    fun getLatestOhisAssessment(): Flow<OhisEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOhis(assessment: OhisEntity): Long

    @Query("DELETE FROM ohis_assessments WHERE id = :id")
    suspend fun deleteOhisById(id: Long)

    @Query("DELETE FROM ohis_assessments")
    suspend fun clearAllOhis()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface DailyMoodDao {
    @Query("SELECT * FROM daily_moods WHERE dateString = :date LIMIT 1")
    fun getMoodForDate(date: String): Flow<DailyMoodEntity?>

    @Query("SELECT * FROM daily_moods ORDER BY dateString DESC")
    fun getAllMoods(): Flow<List<DailyMoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMood(mood: DailyMoodEntity)

    @Query("DELETE FROM daily_moods WHERE dateString = :date")
    suspend fun deleteMood(date: String)

    @Query("DELETE FROM daily_moods")
    suspend fun clearAllMoods()
}

