package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectManager
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DailyMoodEntity
import com.example.data.local.HabitEntity
import com.example.data.remote.BrowserCategory
import com.example.data.remote.DentalArticle
import com.example.data.remote.DentalBrowserRepository
import com.example.data.remote.DentallyFirestoreRepository
import com.example.data.remote.DeveloperUserDetail
import com.example.data.remote.DeveloperUserSummary
import com.example.data.remote.GeminiChatService
import com.example.data.remote.UserChatContext
import com.example.data.repository.CalendarSyncEvent
import com.example.data.repository.ChatLearningManager
import com.example.data.repository.ChatLearningProfile
import com.example.data.repository.CustomArticleLocalStore
import com.example.data.repository.DailyQuote
import com.example.data.repository.DentallyRepository
import com.example.data.repository.GamificationStatus
import com.example.data.repository.SharedAiKnowledge
import com.example.data.repository.UserProfileData
import com.example.data.repository.UserProfileRepository
import com.example.notifications.EveningReminderScheduler
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BrushingQuadrant(val quadrantName: String, val instruction: String, val seconds: Int) {
    UPPER_RIGHT(
        "Bagian 1: Gigi Kanan Atas",
        "Sikat gigi kanan atas (luar, dalam, dan permukaan kunyah) dengan gerakan memutar lembut.",
        30
    ),
    UPPER_LEFT(
        "Bagian 2: Gigi Kiri Atas",
        "Pindah ke gigi kiri atas. Arahkan bulu sikat miring lembut dari arah gusi ke gigi.",
        30
    ),
    LOWER_LEFT(
        "Bagian 3: Gigi Kiri Bawah",
        "Lanjut ke gigi kiri bawah. Sikat pelan bagian depan, sisi dalam dekat lidah, dan bagian kunyah.",
        30
    ),
    LOWER_RIGHT(
        "Bagian 4: Gigi Kanan Bawah & Lidah",
        "Terakhir, bersihkan gigi kanan bawah dan sikat lembut permukaan lidah biar nafas makin segar!",
        30
    )
}

class DentallyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val geminiService = GeminiChatService()
    private val repository = DentallyRepository(db, geminiService)
    private val userProfileRepo = UserProfileRepository(application)
    private val firestoreRepo = DentallyFirestoreRepository(application)
    private val browserRepo = DentalBrowserRepository()
    private val customArticleStore = CustomArticleLocalStore(application)
    private val chatLearningManager = ChatLearningManager(application)

    val userProfile: StateFlow<UserProfileData> = userProfileRepo.userProfile

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    // Active selected date for the habit tracker
    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Flow of habits for the selected date
    val habits: StateFlow<List<HabitEntity>> = _selectedDate.flatMapLatest { date ->
        repository.getHabitsForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All habits across all dates (for Monthly Calendar consistency view)
    val allHabits: StateFlow<List<HabitEntity>> = db.habitDao().getAllHabits().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Today's habits (for Evening Check-In Gate regardless of selected calendar date)
    val todayHabits: StateFlow<List<HabitEntity>> = repository.getHabitsForDate(todayDateString).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current selected date mood (Daylio)
    val currentDayMood: StateFlow<DailyMoodEntity?> = _selectedDate.flatMapLatest { date ->
        db.dailyMoodDao().getMoodForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // All recorded moods (for Daylio recap analytics)
    val allMoods: StateFlow<List<DailyMoodEntity>> = db.dailyMoodDao().getAllMoods().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Completed dates for calendar indicators
    val completedDates: StateFlow<Set<String>> = db.habitDao().getCompletedDates().combine(_selectedDate) { dates, _ ->
        dates.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Chat messages
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.getChatMessages().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _chatLearningProfile = MutableStateFlow(ChatLearningProfile())
    val chatLearningProfile: StateFlow<ChatLearningProfile> = _chatLearningProfile.asStateFlow()

    private val _sharedAiKnowledge = MutableStateFlow(chatLearningManager.getSharedKnowledge())
    val sharedAiKnowledge: StateFlow<SharedAiKnowledge> = _sharedAiKnowledge.asStateFlow()

    // 2-Minute Brushing Timer State (Featured on Beranda)
    private val _timerSecondsLeft = MutableStateFlow(120)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _currentQuadrant = MutableStateFlow(BrushingQuadrant.UPPER_RIGHT)
    val currentQuadrant: StateFlow<BrushingQuadrant> = _currentQuadrant.asStateFlow()

    private val _timerCompletedToday = MutableStateFlow(false)
    val timerCompletedToday: StateFlow<Boolean> = _timerCompletedToday.asStateFlow()

    // Confetti Celebration Trigger counter
    private val _celebrationTrigger = MutableStateFlow(0)
    val celebrationTrigger: StateFlow<Int> = _celebrationTrigger.asStateFlow()

    // Onboarding & Demo Guide State
    private val _showOnboardingDemo = MutableStateFlow(false)
    val showOnboardingDemo: StateFlow<Boolean> = _showOnboardingDemo.asStateFlow()

    // Evening Check-In Mandatory Gate State
    private val _showEveningCheckInGate = MutableStateFlow(false)
    val showEveningCheckInGate: StateFlow<Boolean> = _showEveningCheckInGate.asStateFlow()

    private var timerJob: Job? = null

    // Browser / Multi-Platform Live Feed State + User Custom Sources
    private val _customBrowserArticles = MutableStateFlow(
        customArticleStore.getArticles(userProfileRepo.userProfile.value.userId)
    )
    private var lastNonCustomFeedArticles: List<DentalArticle> = browserRepo.getInitialCuratedArticles()

    private val _browserArticles = MutableStateFlow(
        mergeFeedWithCustom(_customBrowserArticles.value, lastNonCustomFeedArticles)
    )
    val browserArticles: StateFlow<List<DentalArticle>> = _browserArticles.asStateFlow()

    private val _isLoadingNews = MutableStateFlow(false)
    val isLoadingNews: StateFlow<Boolean> = _isLoadingNews.asStateFlow()

    private val _isLiveGoogleNews = MutableStateFlow(false)
    val isLiveGoogleNews: StateFlow<Boolean> = _isLiveGoogleNews.asStateFlow()

    private val _lastBrowserRefreshTime = MutableStateFlow("")
    val lastBrowserRefreshTime: StateFlow<String> = _lastBrowserRefreshTime.asStateFlow()

    // Sync & User Session State
    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    private var initializedUid: String? = null

    // Developer Monitoring Dashboard State (only for fight.vanzor30@gmail.com)
    private val _developerUsers = MutableStateFlow<List<DeveloperUserSummary>>(emptyList())
    val developerUsers: StateFlow<List<DeveloperUserSummary>> = _developerUsers.asStateFlow()

    private val _selectedDeveloperUserDetail = MutableStateFlow<DeveloperUserDetail?>(null)
    val selectedDeveloperUserDetail: StateFlow<DeveloperUserDetail?> = _selectedDeveloperUserDetail.asStateFlow()

    private val _isLoadingDeveloper = MutableStateFlow(false)
    val isLoadingDeveloper: StateFlow<Boolean> = _isLoadingDeveloper.asStateFlow()

    private val _developerError = MutableStateFlow<String?>(null)
    val developerError: StateFlow<String?> = _developerError.asStateFlow()

    init {
        SoundEffectManager.isEnabled = userProfileRepo.userProfile.value.sfxEnabled
        refreshDentalBrowserNews(forceRotate = false)
        scheduleEveningNotificationIfEnabled()
    }

    private fun mergeFeedWithCustom(
        custom: List<DentalArticle>,
        feed: List<DentalArticle>
    ): List<DentalArticle> {
        return (custom + feed).distinctBy { it.id }
    }

    fun validateCustomSourceUrl(rawUrl: String): String? {
        return browserRepo.normalizeUserSourceUrl(rawUrl)
    }

    fun addCustomBrowserSource(
        title: String,
        summaryOrContent: String,
        sourceName: String,
        url: String,
        category: BrowserCategory,
        takeawaysRaw: List<String> = emptyList()
    ): DentalArticle? {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return null
        val normalizedUrl = browserRepo.normalizeUserSourceUrl(url) ?: return null

        val article = browserRepo.buildCustomArticle(
            title = cleanTitle,
            summaryOrContent = summaryOrContent,
            sourceName = sourceName,
            url = normalizedUrl,
            category = category,
            takeawaysRaw = takeawaysRaw
        )
        val uid = userProfile.value.userId
        customArticleStore.saveArticle(uid, article)
        val updatedCustom = customArticleStore.getArticles(uid)
        _customBrowserArticles.value = updatedCustom
        _browserArticles.value = mergeFeedWithCustom(updatedCustom, lastNonCustomFeedArticles)
        SoundEffectManager.playSuccessChime()

        viewModelScope.launch(Dispatchers.IO) {
            firestoreRepo.saveCustomArticle(article)
        }
        return article
    }

    fun deleteCustomBrowserSource(articleId: String) {
        val uid = userProfile.value.userId
        val remaining = customArticleStore.deleteArticle(uid, articleId)
        _customBrowserArticles.value = remaining
        _browserArticles.value = mergeFeedWithCustom(remaining, lastNonCustomFeedArticles)
        SoundEffectManager.playUncheck()

        viewModelScope.launch(Dispatchers.IO) {
            firestoreRepo.deleteCustomArticle(articleId)
        }
    }

    fun refreshDentalBrowserNews(forceRotate: Boolean = true) {
        viewModelScope.launch {
            _isLoadingNews.value = true
            try {
                val (articles, isLive) = browserRepo.fetchLiveBrowserFeed(forceRotate = forceRotate)
                lastNonCustomFeedArticles = articles
                _browserArticles.value = mergeFeedWithCustom(_customBrowserArticles.value, articles)
                _isLiveGoogleNews.value = isLive
                _lastBrowserRefreshTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            } catch (e: Exception) {
                Log.w("DentalBrowser", "Menggunakan artikel bawaan karena gagal memuat berita", e)
                _isLiveGoogleNews.value = false
            } finally {
                _isLoadingNews.value = false
            }
        }
    }

    fun searchDentalBrowserTopics(query: String) {
        viewModelScope.launch {
            _isLoadingNews.value = true
            try {
                val (articles, isLive) = browserRepo.searchLiveTopics(query)
                val matchingCustom = _customBrowserArticles.value.filter {
                    query.isBlank() ||
                        it.title.contains(query, ignoreCase = true) ||
                        it.summary.contains(query, ignoreCase = true) ||
                        it.contentDetail.contains(query, ignoreCase = true) ||
                        it.sourceName.contains(query, ignoreCase = true)
                }
                lastNonCustomFeedArticles = articles
                _browserArticles.value = mergeFeedWithCustom(matchingCustom, articles)
                _isLiveGoogleNews.value = isLive
                _lastBrowserRefreshTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            } catch (e: Exception) {
                Log.w("DentalBrowser", "Gagal mencari topik langsung", e)
            } finally {
                _isLoadingNews.value = false
            }
        }
    }

    /**
     * Called when MainActivity AuthGate detects an authenticated FirebaseUser.
     * Guarantees strict per-user isolation on local Room DB and syncs cloud Firestore progress.
     */
    fun onAuthenticatedUserReady(firebaseUser: FirebaseUser) {
        val uid = firebaseUser.uid
        if (initializedUid == uid) return
        initializedUid = uid

        viewModelScope.launch {
            _isSyncingCloud.value = true
            try {
                val previousUid = userProfileRepo.getLastActiveUserId()
                if (previousUid.isNotBlank() && previousUid != uid) {
                    withContext(Dispatchers.IO) {
                        repository.clearAllLocalUserData()
                    }
                    _timerCompletedToday.value = false
                }
                userProfileRepo.setLastActiveUserId(uid)

                val email = firebaseUser.email ?: ""
                val displayName = firebaseUser.displayName
                val avatarUrl = firebaseUser.photoUrl?.toString() ?: ""

                userProfileRepo.login(
                    email = email,
                    name = displayName,
                    uid = uid,
                    avatarUrl = avatarUrl
                )

                syncCloudDataToLocalInternal()

                if (!userProfileRepo.hasSeenOnboarding(uid)) {
                    _showOnboardingDemo.value = true
                }
                checkEveningGateCondition()
            } finally {
                _isSyncingCloud.value = false
            }
        }
    }

    private suspend fun syncCloudDataToLocalInternal() = withContext(Dispatchers.IO) {
        try {
            val cloudProfile = firestoreRepo.loadUserProfile()
            if (cloudProfile != null) {
                val resolvedName = if (cloudProfile.userName.isNotBlank() && cloudProfile.userName != "Sahabat Dentally") {
                    cloudProfile.userName
                } else {
                    userProfileRepo.userProfile.value.userName
                }
                userProfileRepo.login(
                    email = cloudProfile.userEmail.ifBlank { userProfileRepo.userProfile.value.userEmail },
                    name = resolvedName,
                    uid = cloudProfile.userId.ifBlank { userProfileRepo.userProfile.value.userId },
                    avatarUrl = cloudProfile.avatarUrl.ifBlank { userProfileRepo.userProfile.value.avatarUrl }
                )
                if (cloudProfile.isCalendarSynced) {
                    userProfileRepo.syncRoutinesToGoogleCalendar()
                }
            }

            val cloudHabits = firestoreRepo.fetchAllHabits()
            if (cloudHabits.isNotEmpty()) {
                db.habitDao().insertHabits(cloudHabits)
            }

            if (db.habitDao().getHabitCountForDate(todayDateString) == 0) {
                val createdDefaults = repository.ensureDefaultHabitsForDate(todayDateString)
                createdDefaults.forEach { firestoreRepo.saveHabit(it) }
            }

            val cloudMoods = firestoreRepo.fetchAllMoods()
            for (cloudMood in cloudMoods) {
                db.dailyMoodDao().saveMood(cloudMood)
            }

            val existingChats = db.chatMessageDao().getAllMessages().firstOrNull()
            if (existingChats.isNullOrEmpty()) {
                repository.clearChatHistory(userProfileRepo.userProfile.value.userName)
            }
            val remoteShared = firestoreRepo.loadSharedAiKnowledge()
            val mergedShared = chatLearningManager.mergeRemoteSharedKnowledge(remoteShared)
            _sharedAiKnowledge.value = mergedShared

            val uid = userProfileRepo.userProfile.value.userId
            val pastUserTexts = existingChats.orEmpty().filter { it.isUser }.map { it.text }
            val combined = chatLearningManager.hydrateFromHistoryIfEmpty(uid, pastUserTexts)
            _chatLearningProfile.value = combined.userProfile
            _sharedAiKnowledge.value = combined.sharedKnowledge

            val remoteCustomArticles = firestoreRepo.fetchAllCustomArticles()
            val mergedCustomArticles = customArticleStore.mergeRemoteArticles(uid, remoteCustomArticles)
            _customBrowserArticles.value = mergedCustomArticles
            _browserArticles.value = mergeFeedWithCustom(mergedCustomArticles, lastNonCustomFeedArticles)

            syncProfileStatsFromDb()
            Log.d("DentallySync", "Semua progres akun berhasil disinkronisasi dari Cloud Firestore!")
        } catch (e: Exception) {
            Log.e("DentallySync", "Gagal sinkronisasi data cloud Firestore", e)
        }
    }

    private suspend fun syncProfileStatsFromDb() = withContext(Dispatchers.IO) {
        if (!firestoreRepo.isUserSignedIn()) return@withContext
        val todayHabits = db.habitDao().getHabitsForDate(todayDateString).firstOrNull() ?: emptyList()
        val todayMood = db.dailyMoodDao().getMoodForDate(todayDateString).firstOrNull()
        val status = computeGamificationStatus(todayHabits, todayMood?.moodScore, _timerCompletedToday.value)
        val cleanlinessLabel = when {
            status.toothHealthPercent >= 85 -> "Sangat Bersih"
            status.toothHealthPercent >= 65 -> "Cukup Bersih"
            else -> "Perlu Perawatan"
        }
        firestoreRepo.saveUserProfile(
            profile = userProfileRepo.userProfile.value,
            status = status,
            latestOhis = null,
            cleanlinessLabel = cleanlinessLabel
        )
    }

    fun isFutureDate(date: String): Boolean = date > todayDateString

    fun setSelectedDate(date: String) {
        if (isFutureDate(date)) return
        _selectedDate.value = date
        viewModelScope.launch {
            val createdDefaults = repository.ensureDefaultHabitsForDate(date)
            if (firestoreRepo.isUserSignedIn()) {
                createdDefaults.forEach { firestoreRepo.saveHabit(it) }
            }
        }
    }

    fun toggleHabit(habit: HabitEntity) {
        if (isFutureDate(habit.dateString)) return
        viewModelScope.launch {
            val updatedStatus = !habit.isCompleted
            when {
                habit.isBadHabit && updatedStatus -> SoundEffectManager.playWarningBlip()
                updatedStatus -> SoundEffectManager.playSuccessChime()
                else -> SoundEffectManager.playUncheck()
            }
            repository.toggleHabitCompleted(habit.id, habit.isCompleted)
            firestoreRepo.saveHabit(habit.copy(isCompleted = updatedStatus))

            val todayHabits = db.habitDao().getHabitsForDate(todayDateString).firstOrNull().orEmpty()
            val goodToday = todayHabits.filter { !it.isBadHabit }
            val allGoodDone = goodToday.isNotEmpty() && goodToday.all {
                if (it.id == habit.id) updatedStatus else it.isCompleted
            }
            EveningReminderScheduler.markTodayEveningCompleted(getApplication(), allGoodDone)
            if (allGoodDone && !habit.isBadHabit && updatedStatus) {
                _celebrationTrigger.value += 1
            }

            syncProfileStatsFromDb()
        }
    }

    fun addCustomHabit(
        title: String,
        subtitle: String,
        emoji: String,
        category: String,
        isBadHabit: Boolean = false
    ) {
        val targetDate = _selectedDate.value
        if (isFutureDate(targetDate)) return
        viewModelScope.launch {
            val inserted = repository.addNewHabit(
                title = title,
                subtitle = subtitle,
                iconEmoji = emoji,
                category = category,
                dateString = targetDate,
                isBadHabit = isBadHabit
            )
            firestoreRepo.saveHabit(inserted)
            syncProfileStatsFromDb()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            firestoreRepo.deleteHabit(habitId)
            syncProfileStatsFromDb()
        }
    }

    private val _quoteOffset = MutableStateFlow(0)
    val dailyQuote: StateFlow<DailyQuote> = _quoteOffset.combine(_selectedDate) { offset, _ ->
        repository.getDailyQuote(offset)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.getDailyQuote(0)
    )

    fun nextQuote() {
        _quoteOffset.value += 1
    }

    fun getGamificationStatus(): GamificationStatus {
        return computeGamificationStatus(
            allHabits = habits.value,
            moodScore = currentDayMood.value?.moodScore,
            timerDone = _timerCompletedToday.value
        )
    }

    private fun computeRealStreakDays(historyHabits: List<HabitEntity>): Int {
        if (historyHabits.isEmpty()) {
            return if (_timerCompletedToday.value) 1 else 0
        }
        val goodCompletedDates = historyHabits
            .filter { !it.isBadHabit && it.isCompleted && !isFutureDate(it.dateString) }
            .map { it.dateString }
            .toMutableSet()
        if (_timerCompletedToday.value) {
            goodCompletedDates.add(todayDateString)
        }
        if (goodCompletedDates.isEmpty()) return 0

        val cal = Calendar.getInstance()
        val todayHasProgress = goodCompletedDates.contains(todayDateString)
        if (!todayHasProgress) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var streak = 0
        for (i in 0 until 60) {
            val dStr = dateFormat.format(cal.time)
            if (goodCompletedDates.contains(dStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun computeGamificationStatus(
        allHabits: List<HabitEntity>,
        moodScore: Int?,
        timerDone: Boolean
    ): GamificationStatus {
        val goodHabits = allHabits.filter { !it.isBadHabit }
        val badHabits = allHabits.filter { it.isBadHabit }

        val completedGoodCount = goodHabits.count { it.isCompleted }
        val totalGoodCount = goodHabits.size.coerceAtLeast(1)
        val goodCompletionRatio = completedGoodCount.toFloat() / totalGoodCount.toFloat()

        val activeBadHabitCount = badHabits.count { it.isCompleted }

        val baseScore = 20
        val habitScore = (goodCompletionRatio * 65).toInt()
        val moodBonus = when {
            moodScore == null -> 5
            moodScore >= 4 -> 10
            moodScore == 3 -> 5
            else -> 0
        }
        val timerBonus = if (timerDone) 10 else 0
        val badHabitPenalty = activeBadHabitCount * 20

        val rawHealth = baseScore + habitScore + moodBonus + timerBonus - badHabitPenalty
        val healthPercent = rawHealth.coerceIn(10, 100)

        val stage = when {
            healthPercent >= 85 -> "Sangat Bersih & Berkilau"
            healthPercent >= 65 -> "Cukup Bersih & Segar"
            healthPercent >= 40 -> "Mulai Kotor & Lengket"
            else -> "Butuh Sikat Gigi Sekarang"
        }

        val mood = when {
            healthPercent >= 85 -> "Mulut Segar & Terlindungi"
            healthPercent >= 65 -> "Bagus! Tinggal Lengkapi Rutinitas"
            healthPercent >= 40 -> "Agak Lengket, Yuk Sikat Gigi"
            else -> "Awas Gigi Berlubang & Bau Mulut"
        }

        val plaqueLevel = when {
            healthPercent >= 85 -> "Mulut Sangat Bersih"
            healthPercent >= 65 -> "Sedikit Sisa Makanan"
            healthPercent >= 40 -> "Mulai Banyak Kotoran"
            else -> "Kotoran Menumpuk"
        }

        val levelNum = (healthPercent / 20) + 1
        val levelTitle = when {
            healthPercent >= 85 -> "Level $levelNum - Senyum Kinclong"
            healthPercent >= 65 -> "Level $levelNum - Sahabat Gigi Bersih"
            healthPercent >= 40 -> "Level $levelNum - Pejuang Kebersihan"
            else -> "Level $levelNum - Mulai Merawat"
        }

        val realStreak = computeRealStreakDays(this.allHabits.value.ifEmpty { allHabits })
        val totalSessions = this.allHabits.value
            .count { !it.isBadHabit && it.isCompleted }
            .coerceAtLeast(completedGoodCount) + if (timerDone) 1 else 0

        return GamificationStatus(
            toothHealthPercent = healthPercent,
            toothStage = stage,
            toothMood = mood,
            plaqueLevel = plaqueLevel,
            streakDays = realStreak,
            totalBrushingSessions = totalSessions,
            levelName = levelTitle,
            completedGoodHabits = completedGoodCount,
            totalGoodHabits = totalGoodCount,
            activeBadHabitsCount = activeBadHabitCount
        )
    }

    // Timer controls
    fun startTimer() {
        if (_isTimerRunning.value) return
        SoundEffectManager.playPop()
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timerSecondsLeft.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timerSecondsLeft.value -= 1
                val elapsed = 120 - _timerSecondsLeft.value
                val newQuadrant = when {
                    elapsed < 30 -> BrushingQuadrant.UPPER_RIGHT
                    elapsed < 60 -> BrushingQuadrant.UPPER_LEFT
                    elapsed < 90 -> BrushingQuadrant.LOWER_LEFT
                    else -> BrushingQuadrant.LOWER_RIGHT
                }
                if (newQuadrant != _currentQuadrant.value) {
                    _currentQuadrant.value = newQuadrant
                    SoundEffectManager.playQuadrantChime()
                }
            }
            if (_timerSecondsLeft.value <= 0) {
                _isTimerRunning.value = false
                _timerCompletedToday.value = true
                _celebrationTrigger.value += 1
                SoundEffectManager.playFanfare()
                val currentHabits = habits.value
                val brushingHabit = currentHabits.firstOrNull {
                    !it.isBadHabit && it.title.contains("Sikat", ignoreCase = true) && !it.isCompleted
                }
                if (brushingHabit != null) {
                    toggleHabit(brushingHabit)
                } else {
                    syncProfileStatsFromDb()
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSecondsLeft.value = 120
        _currentQuadrant.value = BrushingQuadrant.UPPER_RIGHT
    }

    // Chat actions
    fun sendMessageToDentally(userText: String, topic: String? = null) {
        if (userText.isBlank()) return
        SoundEffectManager.playPop()
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val uid = userProfile.value.userId
                val combinedLearning = withContext(Dispatchers.IO) {
                    chatLearningManager.learnFromUserMessage(uid, userText)
                }
                _chatLearningProfile.value = combinedLearning.userProfile
                _sharedAiKnowledge.value = combinedLearning.sharedKnowledge

                val status = getGamificationStatus()
                val ctx = UserChatContext(
                    userName = userProfile.value.userName,
                    toothHealthPercent = status.toothHealthPercent,
                    streakDays = status.streakDays,
                    levelName = status.levelName,
                    completedGoodHabits = status.completedGoodHabits,
                    totalGoodHabits = status.totalGoodHabits,
                    timerDoneToday = _timerCompletedToday.value,
                    currentMood = currentDayMood.value?.moodTitle,
                    learnedProfile = combinedLearning.userProfile,
                    sharedKnowledge = combinedLearning.sharedKnowledge
                )
                repository.sendChatMessage(userText, topic, ctx)
                SoundEffectManager.playSuccessChime()

                // Push updated anonymized collective intelligence to Firestore for other users
                viewModelScope.launch(Dispatchers.IO) {
                    firestoreRepo.saveSharedAiKnowledge(combinedLearning.sharedKnowledge)
                }
            } catch (e: Exception) {
                Log.e("DentallyChat", "Error sending chat message", e)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun clearChat() {
        SoundEffectManager.playPop()
        viewModelScope.launch {
            repository.clearChatHistory(userProfile.value.userName)
        }
    }

    // Mood & Daylio Actions
    fun saveDailyMood(mood: DailyMoodEntity) {
        if (isFutureDate(mood.dateString)) return
        viewModelScope.launch {
            db.dailyMoodDao().saveMood(mood)
            firestoreRepo.saveMood(mood)
            syncProfileStatsFromDb()
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    val activeUid = userProfileRepo.userProfile.value.userId
                    withContext(Dispatchers.IO) {
                        repository.clearAllLocalUserData()
                        customArticleStore.clearForUser(activeUid)
                    }
                    _customBrowserArticles.value = emptyList()
                    lastNonCustomFeedArticles = browserRepo.getInitialCuratedArticles()
                    _browserArticles.value = lastNonCustomFeedArticles
                    userProfileRepo.setLastActiveUserId("")
                    userProfileRepo.logout()
                    initializedUid = null
                } catch (e: Exception) {
                    Log.w("Auth", "Gagal membersihkan data lokal", e)
                }

                try {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w("Auth", "Gagal membersihkan kredensial", e)
                } finally {
                    Firebase.auth.signOut()
                }
            }
        }
    }

    fun updateUserName(name: String) {
        userProfileRepo.updateUserName(name)
        viewModelScope.launch {
            syncProfileStatsFromDb()
        }
    }

    fun syncGoogleCalendar(): List<CalendarSyncEvent> {
        SoundEffectManager.playSuccessChime()
        val events = userProfileRepo.syncRoutinesToGoogleCalendar()
        scheduleEveningNotificationIfEnabled()
        viewModelScope.launch {
            syncProfileStatsFromDb()
        }
        return events
    }

    // ==================== SFX, ONBOARDING & EVENING REMINDER ====================

    fun toggleSfxEnabled() {
        val next = !userProfile.value.sfxEnabled
        userProfileRepo.setSfxEnabled(next)
        SoundEffectManager.isEnabled = next
        if (next) {
            SoundEffectManager.playSuccessChime()
        }
    }

    fun openOnboardingDemo() {
        SoundEffectManager.playPop()
        _showOnboardingDemo.value = true
    }

    fun dismissOnboardingDemo() {
        val uid = userProfile.value.userId
        userProfileRepo.setOnboardingSeen(uid, true)
        _showOnboardingDemo.value = false
    }

    fun toggleEveningNotification(enabled: Boolean) {
        userProfileRepo.setEveningNotificationEnabled(enabled)
        if (enabled) {
            scheduleEveningNotificationIfEnabled()
            SoundEffectManager.playPop()
        } else {
            EveningReminderScheduler.cancelDailyReminder(getApplication())
        }
    }

    fun updateNightReminderTime(time: String) {
        userProfileRepo.setNightReminderTime(time)
        scheduleEveningNotificationIfEnabled()
        SoundEffectManager.playPop()
    }

    fun sendTestEveningNotification() {
        SoundEffectManager.playPop()
        EveningReminderScheduler.showEveningReminderNotification(getApplication())
    }

    fun triggerEveningCheckInGate(forceOpen: Boolean = false) {
        viewModelScope.launch {
            if (forceOpen) {
                _selectedDate.value = todayDateString
                _showEveningCheckInGate.value = true
            } else {
                checkEveningGateCondition()
            }
        }
    }

    fun dismissEveningGateTemporarilyForTimer() {
        _showEveningCheckInGate.value = false
        _selectedDate.value = todayDateString
    }

    fun completeEveningGateIfDone() {
        viewModelScope.launch {
            val todayHabits = db.habitDao().getHabitsForDate(todayDateString).firstOrNull().orEmpty()
            val goodToday = todayHabits.filter { !it.isBadHabit }
            if (goodToday.isNotEmpty() && goodToday.all { it.isCompleted }) {
                EveningReminderScheduler.markTodayEveningCompleted(getApplication(), true)
                _showEveningCheckInGate.value = false
                _celebrationTrigger.value += 1
            }
        }
    }

    private suspend fun checkEveningGateCondition() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isEvening = hour >= 19
        if (!isEvening) return
        if (EveningReminderScheduler.isTodayEveningCompleted(getApplication())) return

        val todayHabits = db.habitDao().getHabitsForDate(todayDateString).firstOrNull().orEmpty()
        val goodToday = todayHabits.filter { !it.isBadHabit }
        if (goodToday.isNotEmpty() && goodToday.any { !it.isCompleted }) {
            _showEveningCheckInGate.value = true
        }
    }

    private fun scheduleEveningNotificationIfEnabled() {
        if (!userProfile.value.eveningNotificationEnabled) return
        val parts = userProfile.value.nightReminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        EveningReminderScheduler.scheduleDailyEveningReminder(getApplication(), hour, minute)
    }

    // ==================== DEVELOPER DASHBOARD ACTIONS ====================

    fun loadAllUsersForDeveloper() {
        if (!userProfile.value.isDeveloper) {
            _developerError.value = "Akses ditolak: Hanya akun developer (${UserProfileData.DEVELOPER_EMAIL}) yang dapat melihat seluruh data."
            return
        }
        viewModelScope.launch {
            _isLoadingDeveloper.value = true
            _developerError.value = null
            val result = firestoreRepo.fetchAllUsersForDeveloper()
            result.onSuccess { list ->
                _developerUsers.value = list
            }.onFailure { err ->
                _developerError.value = err.localizedMessage ?: "Gagal memuat data seluruh pengguna dari Cloud."
            }
            _isLoadingDeveloper.value = false
        }
    }

    fun loadUserDetailForDeveloper(summary: DeveloperUserSummary) {
        if (!userProfile.value.isDeveloper) return
        viewModelScope.launch {
            _isLoadingDeveloper.value = true
            _developerError.value = null
            val result = firestoreRepo.fetchUserDetailForDeveloper(summary)
            result.onSuccess { detail ->
                _selectedDeveloperUserDetail.value = detail
            }.onFailure { err ->
                _developerError.value = err.localizedMessage ?: "Gagal memuat detail progres pengguna."
            }
            _isLoadingDeveloper.value = false
        }
    }

    fun clearSelectedDeveloperUserDetail() {
        _selectedDeveloperUserDetail.value = null
    }
}
