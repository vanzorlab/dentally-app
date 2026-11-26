package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String,
    val iconEmoji: String = "🪥",
    val category: String = "Harian",
    val isCompleted: Boolean = false,
    val isBadHabit: Boolean = false, // true = bad habit that dirties the tooth when done
    val dateString: String, // Format: YYYY-MM-DD
    val targetTime: String = "Pagi & Malam",
    val isDefault: Boolean = false,
    val orderIndex: Int = 0
)

