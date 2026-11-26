package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.DailyMoodEntity
import com.example.data.local.HabitEntity
import com.example.data.local.OhisEntity
import com.example.data.repository.GamificationStatus
import com.example.data.repository.SharedAiKnowledge
import com.example.data.repository.UserProfileData
import org.json.JSONObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

data class DeveloperUserSummary(
    val userId: String,
    val name: String,
    val email: String,
    val avatarUrl: String,
    val streakDays: Int,
    val toothHealthPercent: Int,
    val toothStage: String,
    val level: Int,
    val completedHabitsCount: Int,
    val totalHabitsCount: Int,
    val latestOhisScore: Double?,
    val latestOhisCategory: String,
    val isCalendarSynced: Boolean,
    val updatedAtFormatted: String
)

data class DeveloperUserDetail(
    val summary: DeveloperUserSummary,
    val habits: List<HabitEntity>,
    val moods: List<DailyMoodEntity>,
    val ohisRecords: List<OhisEntity>
)

/**
 * Cloud persistence repository backed by Firebase Firestore.
 * Automatically synchronizes user profiles, daily brushing habits,
 * Daylio tooth emotions, and OHI-S clinical checks per authenticated Gmail user,
 * and allows the developer (fight.vanzor30@gmail.com) to monitor all users.
 */
class DentallyFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna wajib masuk dengan Gmail sebelum mengakses Firestore.")
    }

    fun currentUserId(): String? = try { auth.currentUser?.uid } catch (_: Exception) { null }
    fun isUserSignedIn(): Boolean = try { auth.currentUser != null } catch (_: Exception) { false }

    /**
     * Save or sync UserProfile + current progress metrics to Firestore /users/{userId}
     */
    suspend fun saveUserProfile(
        profile: UserProfileData,
        status: GamificationStatus? = null,
        latestOhis: OhisEntity? = null,
        cleanlinessLabel: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)
        val resolvedCleanliness = cleanlinessLabel
            ?: latestOhis?.category
            ?: when {
                (status?.toothHealthPercent ?: 0) >= 85 -> "Sangat Bersih"
                (status?.toothHealthPercent ?: 0) >= 65 -> "Cukup Bersih"
                else -> "Perlu Perawatan"
            }
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "name" to (profile.userName.ifBlank { "Sahabat Dentally" }),
            "email" to (profile.userEmail.ifBlank { auth.currentUser?.email ?: "user@gmail.com" }),
            "avatarUrl" to profile.avatarUrl,
            "streakDays" to (status?.streakDays ?: 0),
            "toothHealthPercent" to (status?.toothHealthPercent ?: 15),
            "toothStage" to (status?.toothStage ?: "Gigi Baru Mulai"),
            "level" to ((status?.toothHealthPercent ?: 15) / 20 + 1),
            "completedHabitsCount" to (status?.completedGoodHabits ?: 0),
            "totalHabitsCount" to (status?.totalGoodHabits ?: 0),
            "latestOhisCategory" to resolvedCleanliness.take(40),
            "isCalendarSynced" to profile.isCalendarSynced,
            "morningReminderTime" to profile.morningReminderTime,
            "nightReminderTime" to profile.nightReminderTime,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (latestOhis != null) {
            payload["latestOhisScore"] = latestOhis.totalOhis
        }
        try {
            docRef.set(payload, SetOptions.merge()).await()
            Log.d("DentallyFirestore", "User profile saved to Firestore for $uid")
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            false
        }
    }

    /**
     * Load UserProfile from Firestore /users/{userId}
     */
    suspend fun loadUserProfile(): UserProfileData? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: (auth.currentUser?.displayName ?: "Sahabat Dentally")
                val email = snapshot.getString("email") ?: (auth.currentUser?.email ?: "")
                val avatarUrl = snapshot.getString("avatarUrl") ?: (auth.currentUser?.photoUrl?.toString() ?: "")
                val isCalendarSynced = snapshot.getBoolean("isCalendarSynced") ?: false
                val morningTime = snapshot.getString("morningReminderTime") ?: "07:00"
                val nightTime = snapshot.getString("nightReminderTime") ?: "21:00"
                UserProfileData(
                    userId = uid,
                    userName = name,
                    userEmail = email,
                    avatarUrl = avatarUrl,
                    isLoggedIn = true,
                    isGoogleConnected = true,
                    isCalendarSynced = isCalendarSynced,
                    lastSyncTime = if (isCalendarSynced) "Tersimpan di Cloud" else "",
                    morningReminderTime = morningTime,
                    nightReminderTime = nightTime
                )
            } else {
                null
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    /**
     * Save Habit to /users/{userId}/habits/{habitId}
     */
    suspend fun saveHabit(habit: HabitEntity): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val habitDocId = habit.id.toString()
        val path = "users/$uid/habits/$habitDocId"
        val docRef = db.collection("users").document(uid).collection("habits").document(habitDocId)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "title" to habit.title,
            "description" to habit.subtitle,
            "timeOfDay" to habit.targetTime,
            "isCompleted" to habit.isCompleted,
            "date" to habit.dateString,
            "iconKey" to habit.iconEmoji,
            "category" to habit.category,
            "isBadHabit" to habit.isBadHabit,
            "orderIndex" to habit.orderIndex,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            false
        }
    }

    /**
     * Delete Habit from /users/{userId}/habits/{habitId}
     */
    suspend fun deleteHabit(habitId: Long): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val path = "users/$uid/habits/$habitId"
        val docRef = db.collection("users").document(uid).collection("habits").document(habitId.toString())
        try {
            docRef.delete().await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            false
        }
    }

    /**
     * Fetch all habits for the authenticated user from Firestore (across dates or for a specific date)
     */
    suspend fun fetchAllHabits(): List<HabitEntity> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        val path = "users/$uid/habits"
        try {
            val querySnapshot = db.collection("users").document(uid).collection("habits")
                .get()
                .await()
            querySnapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val subtitle = doc.getString("description") ?: ""
                val targetTime = doc.getString("timeOfDay") ?: "Pagi & Malam"
                val isCompleted = doc.getBoolean("isCompleted") ?: false
                val isBadHabit = doc.getBoolean("isBadHabit") ?: false
                val category = doc.getString("category") ?: "Harian"
                val date = doc.getString("date") ?: ""
                if (date.isBlank()) return@mapNotNull null
                val iconEmoji = doc.getString("iconKey") ?: "🪥"
                val orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0
                val habitId = doc.id.toLongOrNull() ?: 0L
                HabitEntity(
                    id = habitId,
                    title = title,
                    subtitle = subtitle,
                    iconEmoji = iconEmoji,
                    category = category,
                    isCompleted = isCompleted,
                    isBadHabit = isBadHabit,
                    dateString = date,
                    targetTime = targetTime,
                    isDefault = false,
                    orderIndex = orderIndex
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            emptyList()
        }
    }

    /**
     * Save DailyMood (Daylio) to /users/{userId}/moods/{date}
     */
    suspend fun saveMood(mood: DailyMoodEntity): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val path = "users/$uid/moods/${mood.dateString}"
        val docRef = db.collection("users").document(uid).collection("moods").document(mood.dateString)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "date" to mood.dateString,
            "moodScore" to mood.moodScore,
            "moodLabel" to mood.moodTitle,
            "note" to mood.note,
            "moodSubEmoji" to mood.moodSubEmoji,
            "dentalFeel" to mood.dentalFeel,
            "activities" to mood.activityTags.split(",").filter { it.isNotBlank() }.take(30),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            false
        }
    }

    /**
     * Fetch all moods from Firestore
     */
    suspend fun fetchAllMoods(): List<DailyMoodEntity> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        val path = "users/$uid/moods"
        try {
            val querySnapshot = db.collection("users").document(uid).collection("moods").get().await()
            querySnapshot.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: return@mapNotNull null
                val score = doc.getLong("moodScore")?.toInt() ?: 4
                val title = doc.getString("moodLabel") ?: "Baik"
                val note = doc.getString("note") ?: ""
                val subEmoji = doc.getString("moodSubEmoji") ?: "smile"
                val feel = doc.getString("dentalFeel") ?: "Gigi Segar & Nyaman"
                val activitiesList = (doc.get("activities") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                DailyMoodEntity(
                    dateString = date,
                    moodScore = score,
                    moodTitle = title,
                    moodSubEmoji = subEmoji,
                    dentalFeel = feel,
                    note = note,
                    activityTags = activitiesList.joinToString(",")
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            emptyList()
        }
    }

    /**
     * Save OHI-S assessment to /users/{userId}/ohis/{ohisId}
     */
    suspend fun saveOhis(ohis: OhisEntity): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val ohisDocId = ohis.id.toString()
        val path = "users/$uid/ohis/$ohisDocId"
        val docRef = db.collection("users").document(uid).collection("ohis").document(ohisDocId)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "date" to ohis.formattedDate,
            "debrisScore" to ohis.debrisIndex,
            "calculusScore" to ohis.calculusIndex,
            "totalScore" to ohis.totalOhis,
            "category" to ohis.category,
            "aiRecommendation" to ohis.feedbackText.take(4000),
            "notes" to ohis.notes.take(500),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            false
        }
    }

    /**
     * Delete OHI-S assessment from /users/{userId}/ohis/{ohisId}
     */
    suspend fun deleteOhis(ohisId: Long): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val path = "users/$uid/ohis/$ohisId"
        val docRef = db.collection("users").document(uid).collection("ohis").document(ohisId.toString())
        try {
            docRef.delete().await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            false
        }
    }

    /**
     * Fetch all OHI-S assessments from Firestore
     */
    suspend fun fetchAllOhis(): List<OhisEntity> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        val path = "users/$uid/ohis"
        try {
            val querySnapshot = db.collection("users").document(uid).collection("ohis").get().await()
            querySnapshot.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: ""
                val debris = doc.getDouble("debrisScore") ?: 0.0
                val calculus = doc.getDouble("calculusScore") ?: 0.0
                val total = doc.getDouble("totalScore") ?: (debris + calculus)
                val cat = doc.getString("category") ?: "Baik"
                val feedback = doc.getString("aiRecommendation") ?: ""
                val notes = doc.getString("notes") ?: ""
                val id = doc.id.toLongOrNull() ?: System.currentTimeMillis()
                OhisEntity(
                    id = id,
                    formattedDate = date,
                    debrisIndex = debris,
                    calculusIndex = calculus,
                    totalOhis = total,
                    category = cat,
                    feedbackText = feedback,
                    notes = notes
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            emptyList()
        }
    }

    // ==================== DEVELOPER MONITORING METHODS ====================
    // Enforced on backend by firestore.rules isDeveloper() for fight.vanzor30@gmail.com

    suspend fun fetchAllUsersForDeveloper(): Result<List<DeveloperUserSummary>> = withContext(Dispatchers.IO) {
        val path = "users"
        try {
            requireUserId()
            val snapshot = db.collection("users").get().await()
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val users = snapshot.documents.mapNotNull { doc ->
                val uid = doc.getString("userId") ?: doc.id
                val name = doc.getString("name") ?: "Pengguna"
                val email = doc.getString("email") ?: "-"
                val avatar = doc.getString("avatarUrl") ?: ""
                val streak = (doc.get("streakDays") as? Number)?.toInt() ?: 0
                val health = (doc.get("toothHealthPercent") as? Number)?.toInt() ?: 0
                val stage = doc.getString("toothStage") ?: "Gigi Baru Mulai"
                val level = (doc.get("level") as? Number)?.toInt() ?: 1
                val doneHabits = (doc.get("completedHabitsCount") as? Number)?.toInt() ?: 0
                val totalHabits = (doc.get("totalHabitsCount") as? Number)?.toInt() ?: 0
                val ohisScore = (doc.get("latestOhisScore") as? Number)?.toDouble()
                val ohisCat = doc.getString("latestOhisCategory") ?: "Belum Cek"
                val calendarSynced = doc.getBoolean("isCalendarSynced") ?: false
                val updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.let { formatter.format(it) } ?: "Baru saja"

                DeveloperUserSummary(
                    userId = uid,
                    name = name,
                    email = email,
                    avatarUrl = avatar,
                    streakDays = streak,
                    toothHealthPercent = health,
                    toothStage = stage,
                    level = level,
                    completedHabitsCount = doneHabits,
                    totalHabitsCount = totalHabits,
                    latestOhisScore = ohisScore,
                    latestOhisCategory = ohisCat,
                    isCalendarSynced = calendarSynced,
                    updatedAtFormatted = updatedAt
                )
            }.sortedByDescending { it.toothHealthPercent }
            Result.success(users)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun fetchUserDetailForDeveloper(summary: DeveloperUserSummary): Result<DeveloperUserDetail> = withContext(Dispatchers.IO) {
        val uid = summary.userId
        val path = "users/$uid"
        try {
            requireUserId()
            val userDoc = db.collection("users").document(uid)
            val habitsSnap = userDoc.collection("habits").get().await()
            val moodsSnap = userDoc.collection("moods").get().await()
            val ohisSnap = userDoc.collection("ohis").get().await()

            val habits = habitsSnap.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                HabitEntity(
                    id = doc.id.toLongOrNull() ?: 0L,
                    title = title,
                    subtitle = doc.getString("description") ?: "",
                    iconEmoji = doc.getString("iconKey") ?: "🪥",
                    category = doc.getString("category") ?: "Harian",
                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                    isBadHabit = doc.getBoolean("isBadHabit") ?: false,
                    dateString = doc.getString("date") ?: "",
                    targetTime = doc.getString("timeOfDay") ?: "",
                    orderIndex = (doc.get("orderIndex") as? Number)?.toInt() ?: 0
                )
            }.sortedByDescending { it.dateString }

            val moods = moodsSnap.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: return@mapNotNull null
                val activities = (doc.get("activities") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                DailyMoodEntity(
                    dateString = date,
                    moodScore = (doc.get("moodScore") as? Number)?.toInt() ?: 4,
                    moodTitle = doc.getString("moodLabel") ?: "Baik",
                    moodSubEmoji = doc.getString("moodSubEmoji") ?: "smile",
                    dentalFeel = doc.getString("dentalFeel") ?: "",
                    note = doc.getString("note") ?: "",
                    activityTags = activities.joinToString(",")
                )
            }.sortedByDescending { it.dateString }

            val ohisRecords = ohisSnap.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: ""
                OhisEntity(
                    id = doc.id.toLongOrNull() ?: 0L,
                    formattedDate = date,
                    debrisIndex = (doc.get("debrisScore") as? Number)?.toDouble() ?: 0.0,
                    calculusIndex = (doc.get("calculusScore") as? Number)?.toDouble() ?: 0.0,
                    totalOhis = (doc.get("totalScore") as? Number)?.toDouble() ?: 0.0,
                    category = doc.getString("category") ?: "Baik",
                    notes = doc.getString("notes") ?: "",
                    feedbackText = doc.getString("aiRecommendation") ?: ""
                )
            }.sortedByDescending { it.id }

            Result.success(
                DeveloperUserDetail(
                    summary = summary,
                    habits = habits,
                    moods = moods,
                    ohisRecords = ohisRecords
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    /**
     * Load collective cross-user AI brain from /ai_knowledge/global_brain.
     */
    suspend fun loadSharedAiKnowledge(): SharedAiKnowledge? = withContext(Dispatchers.IO) {
        if (!isUserSignedIn()) return@withContext null
        val path = "ai_knowledge/global_brain"
        try {
            val snapshot = db.collection("ai_knowledge").document("global_brain").get().await()
            if (!snapshot.exists()) return@withContext null

            val slang = (snapshot.get("knownSlang") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val interests = (snapshot.get("popularInterests") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val insights = (snapshot.get("learnedInsights") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

            val topicMap = mutableMapOf<String, Int>()
            val topicJsonStr = snapshot.getString("topicCountsJson") ?: "{}"
            try {
                val obj = JSONObject(topicJsonStr)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    topicMap[k] = obj.optInt(k, 0)
                }
            } catch (_: Exception) {}

            SharedAiKnowledge(
                totalCollectiveInteractions = (snapshot.get("totalCollectiveInteractions") as? Number)?.toInt() ?: 0,
                gueLoUsersCount = (snapshot.get("gueLoUsersCount") as? Number)?.toInt() ?: 0,
                akuKamuUsersCount = (snapshot.get("akuKamuUsersCount") as? Number)?.toInt() ?: 0,
                shortStyleCount = (snapshot.get("shortStyleCount") as? Number)?.toInt() ?: 0,
                wkwkCount = (snapshot.get("wkwkCount") as? Number)?.toInt() ?: 0,
                hahaCount = (snapshot.get("hahaCount") as? Number)?.toInt() ?: 0,
                heheCount = (snapshot.get("heheCount") as? Number)?.toInt() ?: 0,
                knownSlang = slang,
                popularInterests = interests,
                learnedInsights = insights,
                topicCounts = topicMap
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    /**
     * Save anonymized collective AI learning pool to /ai_knowledge/global_brain
     * so other users benefit immediately from previously learned styles, slang, and topics.
     */
    suspend fun saveSharedAiKnowledge(shared: SharedAiKnowledge): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val path = "ai_knowledge/global_brain"
        val topicJson = JSONObject(shared.topicCounts).toString().take(1900)
        val payload = hashMapOf<String, Any>(
            "lastContributorUid" to uid,
            "totalCollectiveInteractions" to shared.totalCollectiveInteractions.coerceAtLeast(0),
            "gueLoUsersCount" to shared.gueLoUsersCount.coerceAtLeast(0),
            "akuKamuUsersCount" to shared.akuKamuUsersCount.coerceAtLeast(0),
            "shortStyleCount" to shared.shortStyleCount.coerceAtLeast(0),
            "wkwkCount" to shared.wkwkCount.coerceAtLeast(0),
            "hahaCount" to shared.hahaCount.coerceAtLeast(0),
            "heheCount" to shared.heheCount.coerceAtLeast(0),
            "knownSlang" to shared.knownSlang.takeLast(40),
            "popularInterests" to shared.popularInterests.takeLast(30),
            "learnedInsights" to shared.learnedInsights.takeLast(30),
            "topicCountsJson" to topicJson,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("ai_knowledge").document("global_brain")
                .set(payload, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            false
        }
    }

    /**
     * Save a user-added custom article/video/link/material to /users/{uid}/custom_articles/{articleId}
     */
    suspend fun saveCustomArticle(article: DentalArticle): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val path = "users/$uid/custom_articles/${article.id}"
        val docRef = db.collection("users").document(uid)
            .collection("custom_articles")
            .document(article.id)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "title" to article.title.take(160),
            "summary" to article.summary.take(400),
            "contentDetail" to article.contentDetail.take(5000),
            "sourceName" to article.sourceName.take(120),
            "publishedAt" to article.publishedAt.take(40),
            "readTime" to article.readTime.take(40),
            "category" to article.category.name.take(40),
            "url" to article.url.take(500),
            "takeaways" to article.takeaways.take(20),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            false
        }
    }

    /**
     * Fetch all user-added custom articles from /users/{uid}/custom_articles
     */
    suspend fun fetchAllCustomArticles(): List<DentalArticle> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        val path = "users/$uid/custom_articles"
        try {
            val snapshot = db.collection("users").document(uid)
                .collection("custom_articles")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val categoryName = doc.getString("category") ?: BrowserCategory.DENTAL.name
                val category = BrowserCategory.entries.firstOrNull { it.name == categoryName }
                    ?: BrowserCategory.DENTAL
                val takeaways = (doc.get("takeaways") as? List<*>)
                    ?.mapNotNull { it?.toString() }
                    ?: emptyList()
                DentalArticle(
                    id = doc.id,
                    title = title,
                    summary = doc.getString("summary") ?: "",
                    contentDetail = doc.getString("contentDetail") ?: "",
                    takeaways = takeaways,
                    sourceName = doc.getString("sourceName") ?: "Sumber Saya",
                    publishedAt = doc.getString("publishedAt") ?: "Terkini",
                    readTime = doc.getString("readTime") ?: "Materi",
                    category = category,
                    url = doc.getString("url") ?: "",
                    isFromLiveFeed = false,
                    isCustom = true
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            emptyList()
        }
    }

    /**
     * Delete a user-added custom article from /users/{uid}/custom_articles/{articleId}
     */
    suspend fun deleteCustomArticle(articleId: String): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val path = "users/$uid/custom_articles/$articleId"
        val docRef = db.collection("users").document(uid)
            .collection("custom_articles")
            .document(articleId)
        try {
            docRef.delete().await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            false
        }
    }
}
