package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ohis_assessments")
data class OhisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String, // e.g. "14 Agu 2026"
    val debrisIndex: Double,
    val calculusIndex: Double,
    val totalOhis: Double,
    val category: String, // "Baik", "Sedang", "Buruk"
    val notes: String = "",
    val feedbackText: String = "",
    // Individual tooth debris scores (16, 11, 26, 36, 31, 46)
    val di16: Int = 0,
    val di11: Int = 0,
    val di26: Int = 0,
    val di36: Int = 0,
    val di31: Int = 0,
    val di46: Int = 0,
    // Individual tooth calculus scores (16, 11, 26, 36, 31, 46)
    val ci16: Int = 0,
    val ci11: Int = 0,
    val ci26: Int = 0,
    val ci36: Int = 0,
    val ci31: Int = 0,
    val ci46: Int = 0
)
