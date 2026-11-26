package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.HabitEntity
import com.example.data.local.OhisEntity
import com.example.data.remote.GeminiChatService
import com.example.data.remote.UserChatContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyQuote(
    val quote: String,
    val author: String,
    val tip: String = ""
)

data class GamificationStatus(
    val toothHealthPercent: Int, // 0 - 100
    val toothStage: String,      // "Gigi Putih Berkilau", "Gigi Bersih & Segar", "Gigi Mulai Berplak", "Gigi Kotor & Asam"
    val toothMood: String,       // "Sangat Bahagia & Kinclong 🌟", "Bersih & Segar 🪥", "Mulai Lengket & Berplak ⚠️", "Kotor & Terancam Karies 🦠"
    val plaqueLevel: String,     // "Bebas Plak", "Plak Tipis", "Plak Sedang", "Plak & Karang Tebal"
    val streakDays: Int,
    val totalBrushingSessions: Int,
    val levelName: String,
    val completedGoodHabits: Int = 0,
    val totalGoodHabits: Int = 0,
    val activeBadHabitsCount: Int = 0,
    val plantHealthPercent: Int = toothHealthPercent,
    val plantStage: String = toothStage,
    val plantMood: String = toothMood
)

class DentallyRepository(
    private val db: AppDatabase,
    private val geminiService: GeminiChatService
) {
    private val habitDao = db.habitDao()
    private val ohisDao = db.ohisDao()
    private val chatDao = db.chatMessageDao()

    private val quotes = listOf(
        DailyQuote(
            "Kebiasaan kecil yang dilakukan rutin jauh lebih hebat daripada niat besar yang cuma sesekali.",
            "Jim Rohn",
            "Sikat gigi 2 menit sebelum tidur menjaga gigimu tetap bersih sepanjang malam!"
        ),
        DailyQuote(
            "Senyum yang bersih dan segar bikin kamu jauh lebih percaya diri saat ngobrol sama siapa saja.",
            "Dentally Wisdom",
            "Sisa makanan yang menempel seharian bisa mengeras jadi karang gigi. Yuk sikat rutin!"
        ),
        DailyQuote(
            "Merawat hal kecil setiap hari bakal menyelamatkanmu dari rasa sakit yang mahal di masa depan.",
            "James Clear",
            "Sikat gigi 2x sehari dan bersihkan sela gigi pakai benang bikin gusi tetap kuat & sehat."
        ),
        DailyQuote(
            "Mulut yang bersih adalah pintu utama tubuh yang sehat dan bugar setiap hari.",
            "Edukasi Kesehatan Harian",
            "Rajin minum air putih dan sikat lidah membantu nafas tetap segar seharian."
        )
    )

    fun getDailyQuote(dayOffset: Int = 0): DailyQuote {
        val index = (Math.abs(dayOffset + Calendar.getInstance().get(Calendar.DAY_OF_YEAR))) % quotes.size
        return quotes[index]
    }

    fun getHabitsForDate(dateString: String): Flow<List<HabitEntity>> =
        habitDao.getHabitsForDate(dateString)

    fun getAllOhisAssessments(): Flow<List<OhisEntity>> =
        ohisDao.getAllOhisAssessments()

    fun getLatestOhis(): Flow<OhisEntity?> =
        ohisDao.getLatestOhisAssessment()

    fun getChatMessages(): Flow<List<ChatMessageEntity>> =
        chatDao.getAllMessages()

    suspend fun toggleHabitCompleted(id: Long, currentStatus: Boolean) {
        habitDao.setHabitCompleted(id, !currentStatus)
    }

    suspend fun deleteHabit(id: Long) {
        habitDao.deleteHabit(id)
    }

    suspend fun deleteOhis(id: Long) {
        ohisDao.deleteOhisById(id)
    }

    suspend fun clearAllLocalUserData() {
        habitDao.clearAllHabits()
        ohisDao.clearAllOhis()
        db.dailyMoodDao().clearAllMoods()
        chatDao.clearHistory()
    }

    suspend fun addNewHabit(
        title: String,
        subtitle: String,
        iconEmoji: String,
        category: String,
        dateString: String,
        isBadHabit: Boolean = false
    ): HabitEntity {
        val habit = HabitEntity(
            title = title,
            subtitle = subtitle,
            iconEmoji = iconEmoji,
            category = category,
            dateString = dateString,
            isCompleted = false,
            isBadHabit = isBadHabit
        )
        val generatedId = habitDao.insertHabit(habit)
        return habit.copy(id = generatedId)
    }

    suspend fun ensureDefaultHabitsForDate(dateString: String): List<HabitEntity> {
        val count = habitDao.getHabitCountForDate(dateString)
        if (count == 0) {
            val defaultHabits = listOf(
                // Kebiasaan Baik (Membersihkan Gigi)
                HabitEntity(
                    title = "Sikat Gigi Pagi",
                    subtitle = "Setelah sarapan selama 2 menit",
                    iconEmoji = "🌅",
                    category = "Wajib",
                    isCompleted = false,
                    isBadHabit = false,
                    dateString = dateString,
                    targetTime = "Pagi",
                    isDefault = true,
                    orderIndex = 0
                ),
                HabitEntity(
                    title = "Sikat Gigi Malam",
                    subtitle = "Sebelum tidur malam (2 menit)",
                    iconEmoji = "🌙",
                    category = "Wajib",
                    isCompleted = false,
                    isBadHabit = false,
                    dateString = dateString,
                    targetTime = "Malam",
                    isDefault = true,
                    orderIndex = 1
                ),
                HabitEntity(
                    title = "Pakai Benang Gigi",
                    subtitle = "Bersihkan sela gigi yang sulit dijangkau sikat",
                    iconEmoji = "🧵",
                    category = "Pendukung",
                    isCompleted = false,
                    isBadHabit = false,
                    dateString = dateString,
                    targetTime = "Malam",
                    isDefault = true,
                    orderIndex = 2
                ),
                HabitEntity(
                    title = "Kumur Air Putih",
                    subtitle = "Bilas sisa makanan setelah makan & minum",
                    iconEmoji = "💧",
                    category = "Pendukung",
                    isCompleted = false,
                    isBadHabit = false,
                    dateString = dateString,
                    targetTime = "Kapan saja",
                    isDefault = true,
                    orderIndex = 3
                ),
                // Kebiasaan Buruk (Mengotori Gigi / Pemicu Kotoran & Noda)
                HabitEntity(
                    title = "Makan Manis / Ngemil Malam",
                    subtitle = "Sisa gula mudah menempel dan bikin gigi berlubang",
                    iconEmoji = "🍬",
                    category = "Kebiasaan Buruk",
                    isCompleted = false,
                    isBadHabit = true,
                    dateString = dateString,
                    targetTime = "Malam",
                    isDefault = true,
                    orderIndex = 4
                ),
                HabitEntity(
                    title = "Kopi / Teh / Minuman Soda",
                    subtitle = "Bisa bikin gigi kusam, bernoda, dan gampang ngilu",
                    iconEmoji = "☕",
                    category = "Kebiasaan Buruk",
                    isCompleted = false,
                    isBadHabit = true,
                    dateString = dateString,
                    targetTime = "Siang/Sore",
                    isDefault = true,
                    orderIndex = 5
                )
            )
            habitDao.insertHabits(defaultHabits)
            return habitDao.getHabitsForDate(dateString).firstOrNull() ?: emptyList()
        }
        return emptyList()
    }

    suspend fun calculateAndSaveOhis(
        diScores: List<Int>,
        ciScores: List<Int>,
        notes: String = ""
    ): OhisEntity {
        val diSum = diScores.sum()
        val ciSum = ciScores.sum()
        val diIndex = Math.round((diSum.toDouble() / 6.0) * 10.0) / 10.0
        val ciIndex = Math.round((ciSum.toDouble() / 6.0) * 10.0) / 10.0
        val totalOhis = Math.round((diIndex + ciIndex) * 10.0) / 10.0

        val category = when {
            totalOhis <= 1.2 -> "Baik"
            totalOhis <= 3.0 -> "Sedang"
            else -> "Buruk"
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val formattedDate = dateFormat.format(Date())

        val feedback = geminiService.getOhisRecommendation(diIndex, ciIndex, totalOhis, category)

        val entity = OhisEntity(
            formattedDate = formattedDate,
            debrisIndex = diIndex,
            calculusIndex = ciIndex,
            totalOhis = totalOhis,
            category = category,
            notes = notes,
            feedbackText = feedback,
            di16 = diScores.getOrElse(0) { 0 },
            di11 = diScores.getOrElse(1) { 0 },
            di26 = diScores.getOrElse(2) { 0 },
            di36 = diScores.getOrElse(3) { 0 },
            di31 = diScores.getOrElse(4) { 0 },
            di46 = diScores.getOrElse(5) { 0 },
            ci16 = ciScores.getOrElse(0) { 0 },
            ci11 = ciScores.getOrElse(1) { 0 },
            ci26 = ciScores.getOrElse(2) { 0 },
            ci36 = ciScores.getOrElse(3) { 0 },
            ci31 = ciScores.getOrElse(4) { 0 },
            ci46 = ciScores.getOrElse(5) { 0 }
        )

        val id = ohisDao.insertOhis(entity)
        return entity.copy(id = id)
    }

    suspend fun sendChatMessage(
        userText: String,
        topic: String? = null,
        userContext: UserChatContext = UserChatContext()
    ): String {
        val messagesBefore = chatDao.getAllMessages().firstOrNull() ?: emptyList()
        val history = messagesBefore.map { it.text to it.isUser }

        chatDao.insertMessage(
            ChatMessageEntity(
                text = userText,
                isUser = true,
                quickTopic = topic
            )
        )

        val response = geminiService.sendMessage(userText, history, userContext)

        chatDao.insertMessage(
            ChatMessageEntity(
                text = response,
                isUser = false
            )
        )

        return response
    }

    suspend fun clearChatHistory(userName: String = "Sobat") {
        chatDao.clearHistory()
        val nick = userName.trim().split(" ").firstOrNull().takeUnless { it.isNullOrBlank() } ?: "Sobat"
        val initialGreeting = "Hai $nick! Aku **Dentally**, temen ngobrol kamu yang siap bahas apa aja — mulai dari rahasia bikin gigi bersih, tanya soal jajanan & hobi, curhat santai, sampai main kuis seru.\n\nHari ini lagi pengin ngobrolin apa nih?"
        chatDao.insertMessage(
            ChatMessageEntity(
                text = initialGreeting,
                isUser = false
            )
        )
    }
}
