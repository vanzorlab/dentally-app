package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_moods")
data class DailyMoodEntity(
    @PrimaryKey
    val dateString: String,      // YYYY-MM-DD
    val moodScore: Int,          // 1: Awful, 2: Bad, 3: Meh, 4: Good, 5: Rad
    val moodTitle: String,       // "Luar Biasa", "Baik", "Biasa Aja", "Buruk", "Sangat Buruk"
    val moodSubEmoji: String = "smile", // smile, laughing, flat, sad, hurt, angel, blushing, tongue, masked
    val dentalFeel: String = "Gigi Segar & Nyaman", // "Gigi Segar", "Gigi Bersih", "Sedikit Linu", "Gusi Sensitif", "Sakit/Berlubang"
    val note: String = "",
    val activityTags: String = "sikat_pagi,sikat_malam", // Comma separated tags
    val timestamp: Long = System.currentTimeMillis()
)
