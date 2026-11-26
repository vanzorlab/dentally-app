package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ChatLearningProfile(
    val totalMessages: Int = 0,
    val avgWordCount: Double = 6.0,
    val pronounStyle: String = "aku_kamu", // "aku_kamu", "gue_lo"
    val laughStyle: String = "",          // "wkwk", "haha", "hehe", ""
    val usesAbbreviations: Boolean = false,
    val prefersLowercaseCasual: Boolean = false,
    val learnedInterests: List<String> = emptyList(),
    val learnedFacts: List<String> = emptyList(),
    val topicCounts: Map<String, Int> = emptyMap(),
    val recentKeywords: List<String> = emptyList(),
    val adaptedFromSimilarUsers: Boolean = false
) {
    val prefersShortReplies: Boolean
        get() = totalMessages >= 1 && avgWordCount <= 7.0

    val closenessLevel: String
        get() = when {
            totalMessages >= 20 -> "Bestie Akrab"
            totalMessages >= 8 -> "Temen Deket"
            adaptedFromSimilarUsers || totalMessages >= 3 -> "Nyambung Cepat"
            else -> "Kenalan Baru"
        }
}

/**
 * Anonymized collective AI intelligence shared across ALL Dentally users
 * (persisted globally on device + synced to Firestore /ai_knowledge/global_brain).
 * Never stores PII (no names, emails, or UIDs in learned knowledge), so when a new
 * or different user types with similar slang, habits, or interests, Dentally adapts immediately.
 */
data class SharedAiKnowledge(
    val totalCollectiveInteractions: Int = 0,
    val gueLoUsersCount: Int = 0,
    val akuKamuUsersCount: Int = 0,
    val shortStyleCount: Int = 0,
    val wkwkCount: Int = 0,
    val hahaCount: Int = 0,
    val heheCount: Int = 0,
    val knownSlang: List<String> = emptyList(),
    val popularInterests: List<String> = emptyList(),
    val learnedInsights: List<String> = emptyList(),
    val topicCounts: Map<String, Int> = emptyMap()
) {
    val dominantLaugh: String
        get() = listOf(
            "wkwk" to wkwkCount,
            "haha" to hahaCount,
            "hehe" to heheCount
        ).maxByOrNull { it.second }?.takeIf { it.second > 0 }?.first.orEmpty()
}

data class CombinedChatLearning(
    val userProfile: ChatLearningProfile,
    val sharedKnowledge: SharedAiKnowledge
)

/**
 * Dual-layer adaptive chat memory:
 * 1) SharedAiKnowledge (global cross-user & cross-account): transfers slang, typing patterns,
 *    popular interests, and topic intelligence so new users don't start from zero.
 * 2) Active session profile: tracks the active user's personal story without leaking other people's names.
 */
class ChatLearningManager(context: Context) {

    private val appContext = context.applicationContext

    private fun sharedPrefs(): SharedPreferences {
        return appContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun userPrefs(uid: String): SharedPreferences {
        val safeUid = uid.ifBlank { "default_guest" }
        return appContext.getSharedPreferences("dentally_chat_learning_$safeUid", Context.MODE_PRIVATE)
    }

    fun getSharedKnowledge(): SharedAiKnowledge {
        val raw = sharedPrefs().getString(KEY_SHARED_JSON, null) ?: return SharedAiKnowledge()
        return parseSharedKnowledge(raw)
    }

    fun mergeRemoteSharedKnowledge(remote: SharedAiKnowledge?): SharedAiKnowledge {
        val local = getSharedKnowledge()
        if (remote == null) return local
        val mergedTopics = mutableMapOf<String, Int>().apply {
            putAll(local.topicCounts)
            remote.topicCounts.forEach { (k, v) ->
                this[k] = maxOf(this[k] ?: 0, v)
            }
        }
        val merged = SharedAiKnowledge(
            totalCollectiveInteractions = maxOf(local.totalCollectiveInteractions, remote.totalCollectiveInteractions),
            gueLoUsersCount = maxOf(local.gueLoUsersCount, remote.gueLoUsersCount),
            akuKamuUsersCount = maxOf(local.akuKamuUsersCount, remote.akuKamuUsersCount),
            shortStyleCount = maxOf(local.shortStyleCount, remote.shortStyleCount),
            wkwkCount = maxOf(local.wkwkCount, remote.wkwkCount),
            hahaCount = maxOf(local.hahaCount, remote.hahaCount),
            heheCount = maxOf(local.heheCount, remote.heheCount),
            knownSlang = (local.knownSlang + remote.knownSlang).distinct().takeLast(35),
            popularInterests = (local.popularInterests + remote.popularInterests).distinct().takeLast(25),
            learnedInsights = (local.learnedInsights + remote.learnedInsights).distinct().takeLast(25),
            topicCounts = mergedTopics
        )
        saveSharedKnowledge(merged)
        return merged
    }

    fun getProfile(uid: String): ChatLearningProfile {
        val raw = userPrefs(uid).getString(KEY_PROFILE_JSON, null)
        val base = if (raw != null) parseProfile(raw) else ChatLearningProfile()
        val shared = getSharedKnowledge()
        return enrichNewUserFromCollectiveBrain(base, shared)
    }

    fun hydrateFromHistoryIfEmpty(uid: String, pastUserMessages: List<String>): CombinedChatLearning {
        val current = getProfile(uid)
        var shared = getSharedKnowledge()
        if (current.totalMessages > 0 || pastUserMessages.isEmpty()) {
            return CombinedChatLearning(
                userProfile = enrichNewUserFromCollectiveBrain(current, shared),
                sharedKnowledge = shared
            )
        }
        var updatedUser = current
        for (msg in pastUserMessages) {
            if (msg.isNotBlank()) {
                val res = learnBothInternal(updatedUser, shared, msg)
                updatedUser = res.userProfile
                shared = res.sharedKnowledge
            }
        }
        saveUserProfile(uid, updatedUser)
        saveSharedKnowledge(shared)
        return CombinedChatLearning(updatedUser, shared)
    }

    fun learnFromUserMessage(uid: String, userText: String): CombinedChatLearning {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) {
            return CombinedChatLearning(getProfile(uid), getSharedKnowledge())
        }
        val currentUser = userPrefs(uid).getString(KEY_PROFILE_JSON, null)?.let { parseProfile(it) }
            ?: ChatLearningProfile()
        val currentShared = getSharedKnowledge()
        val result = learnBothInternal(currentUser, currentShared, trimmed)
        saveUserProfile(uid, result.userProfile)
        saveSharedKnowledge(result.sharedKnowledge)
        return result
    }

    private fun enrichNewUserFromCollectiveBrain(
        profile: ChatLearningProfile,
        shared: SharedAiKnowledge
    ): ChatLearningProfile {
        if (shared.totalCollectiveInteractions == 0) return profile
        val hasCollectiveMatch = shared.knownSlang.isNotEmpty() || shared.popularInterests.isNotEmpty()
        val inheritedTopics = profile.topicCounts.toMutableMap()
        // Carry collective experience for topics frequently explored by prior users
        shared.topicCounts.forEach { (topic, globalCount) ->
            if ((inheritedTopics[topic] ?: 0) == 0 && globalCount >= 2) {
                inheritedTopics[topic] = 2
            }
        }
        return profile.copy(
            topicCounts = inheritedTopics,
            adaptedFromSimilarUsers = profile.adaptedFromSimilarUsers || hasCollectiveMatch
        )
    }

    internal fun learnBothInternal(
        current: ChatLearningProfile,
        shared: SharedAiKnowledge,
        userText: String
    ): CombinedChatLearning {
        val trimmed = userText.trim()
        val lower = trimmed.lowercase()
        val words = lower.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size.coerceAtLeast(1)

        val nextCount = current.totalMessages + 1
        val nextAvgWords = if (current.totalMessages == 0) {
            wordCount.toDouble()
        } else {
            ((current.avgWordCount * current.totalMessages) + wordCount) / nextCount
        }

        // Detect pronoun vibe & cross-user similarity
        val gueLoTokens = listOf("gw", "gue", "gua", "lu", "lo", "elu", "elo", "bro", "bang")
        val akuKamuTokens = listOf("aku", "kamu", "km", "aq")
        val detectedGueLo = words.any { it in gueLoTokens }
        val detectedAkuKamu = words.any { it in akuKamuTokens }
        val nextPronounStyle = when {
            detectedGueLo -> "gue_lo"
            detectedAkuKamu -> "aku_kamu"
            current.totalMessages == 0 && shared.gueLoUsersCount > shared.akuKamuUsersCount -> "gue_lo"
            else -> current.pronounStyle
        }

        // Detect laugh pattern
        val detectedWkwk = lower.contains("wkwk") || lower.contains("wk")
        val detectedHaha = lower.contains("haha") || lower.contains("hihi")
        val detectedHehe = lower.contains("hehe")
        val nextLaughStyle = when {
            detectedWkwk -> "wkwk"
            detectedHaha -> "haha"
            detectedHehe -> "hehe"
            current.laughStyle.isNotBlank() -> current.laughStyle
            else -> shared.dominantLaugh
        }

        // Detect casual abbreviations / slang
        val casualLexicon = setOf(
            "ga", "gk", "gak", "nggak", "yg", "bgt", "bener", "nih", "sih",
            "dong", "udh", "udah", "blm", "belom", "gmn", "gmna", "emg", "klo",
            "kalau", "jg", "aja", "bikin", "santuy", "gaskeun", "spill", "mabar",
            "gabut", "gw", "gue", "lu", "lo", "kocak", "mantul", "boba", "seblak"
        )
        val matchedSlangInMsg = words.filter { it in casualLexicon }.distinct()
        val hasAbbrev = matchedSlangInMsg.isNotEmpty()
        val isLowercaseCasual = trimmed.length >= 4 && trimmed == lower

        // Check whether incoming typing style matches already-learned shared user archetype
        val matchesPriorSlang = matchedSlangInMsg.any { it in shared.knownSlang }
        val matchesPriorArchetype = (detectedGueLo && shared.gueLoUsersCount > 0) ||
            (detectedWkwk && shared.wkwkCount > 0) ||
            (wordCount <= 6 && shared.shortStyleCount > 0)

        // Extract anonymized interests
        val newInterests = mutableListOf<String>()
        if (lower.contains("boba")) newInterests.add("suka minum boba")
        if (lower.contains("matcha")) newInterests.add("suka rasa matcha")
        if (lower.contains("kopi")) newInterests.add("minum kopi")
        if (lower.contains("seblak")) newInterests.add("suka seblak")
        if (lower.contains("es krim") || lower.contains("eskrim")) newInterests.add("suka es krim")
        if (lower.contains("coklat") || lower.contains("cokelat")) newInterests.add("suka cokelat")
        if (lower.contains("permen")) newInterests.add("ngemil permen")
        if (lower.contains("soda") || lower.contains("cola")) newInterests.add("minum soda")

        if (lower.contains("mobile legends") || words.contains("ml")) newInterests.add("main Mobile Legends")
        if (lower.contains("roblox")) newInterests.add("main Roblox")
        if (lower.contains("minecraft")) newInterests.add("main Minecraft")
        if (lower.contains("free fire") || words.contains("ff")) newInterests.add("main Free Fire")
        if (lower.contains("bola") || lower.contains("futsal")) newInterests.add("hobi main bola/futsal")
        if (lower.contains("musik") || lower.contains("lagu")) newInterests.add("suka dengerin musik")
        if (lower.contains("anime")) newInterests.add("nonton anime")
        if (lower.contains("gambar") || lower.contains("lukis")) newInterests.add("hobi menggambar")

        if (lower.contains("behel") || lower.contains("kawat")) newInterests.add("pakai atau bahas behel")
        if (lower.contains("ngilu") || lower.contains("sensitif")) newInterests.add("gigi gampang ngilu")
        if (lower.contains("gusi") && lower.contains("darah")) newInterests.add("pernah alami gusi berdarah")
        if (lower.contains("sariawan")) newInterests.add("pernah tanya soal sariawan")
        if (lower.contains("ketiduran") || lower.contains("lupa sikat")) newInterests.add("kadang lupa sikat malam")

        val mergedUserInterests = (current.learnedInterests + newInterests).distinct().takeLast(12)
        val mergedSharedInterests = (shared.popularInterests + newInterests).distinct().takeLast(25)

        // Personal self-disclosed facts stay ONLY on current user's profile (no PII leakage to other users)
        val facts = current.learnedFacts.toMutableList()
        extractSelfFact(trimmed)?.let { extracted ->
            if (facts.none { it.equals(extracted, ignoreCase = true) }) {
                facts.add(extracted)
            }
        }

        // Anonymized collective insights (safe to share across all users!)
        val insights = shared.learnedInsights.toMutableList()
        fun addInsight(insight: String) {
            if (!insights.contains(insight)) insights.add(insight)
        }
        if (detectedGueLo && detectedWkwk) {
            addInsight("Banyak user nyaman gaya chat santai (gw/lu + wkwk) dengan kalimat ringkas")
        }
        if (newInterests.contains("suka minum boba") || lower.contains("manis")) {
            addInsight("Topik favorit user: minuman manis/boba & trik kumur air putih setelahnya")
        }
        if (newInterests.contains("pakai atau bahas behel")) {
            addInsight("Sering muncul kebutuhan edukasi perawatan khusus pengguna behel")
        }
        if (newInterests.any { it.startsWith("main ") }) {
            addInsight("User akrab dengan analogi game/mabar (daily quest, XP, push rank)")
        }

        // Update both personal and global topic frequencies
        val userTopics = current.topicCounts.toMutableMap()
        val globalTopics = shared.topicCounts.toMutableMap()
        fun bumpTopic(topic: String) {
            val nextGlobal = (globalTopics[topic] ?: 0) + 1
            globalTopics[topic] = nextGlobal
            val currentLocal = userTopics[topic] ?: 0
            // Transfer collective depth immediately when prior users taught this topic!
            userTopics[topic] = maxOf(currentLocal + 1, if (nextGlobal >= 2) 2 else 1)
        }
        if (lower.contains("sikat") || lower.contains("2 menit") || lower.contains("timer")) bumpTopic("sikat_gigi")
        if (lower.contains("kuis") || lower.contains("quiz") || lower.contains("tebak")) bumpTopic("kuis")
        if (lower.contains("boba") || lower.contains("manis") || lower.contains("jajanan") || lower.contains("makan")) bumpTopic("jajanan")
        if (lower.contains("ngilu") || lower.contains("sakit") || lower.contains("lubang")) bumpTopic("keluhan_gigi")
        if (lower.contains("gusi") || lower.contains("darah") || lower.contains("sariawan")) bumpTopic("gusi_mulut")
        if (lower.contains("behel") || lower.contains("kawat")) bumpTopic("behel")
        if (lower.contains("bau mulut") || lower.contains("nafas") || lower.contains("napas")) bumpTopic("bau_mulut")
        if (lower.contains("game") || lower.contains("roblox") || lower.contains("ml") || lower.contains("ff") || lower.contains("minecraft")) bumpTopic("game")
        if (lower.contains("sekolah") || lower.contains("pr") || lower.contains("tugas") || lower.contains("ujian")) bumpTopic("sekolah")
        if (lower.contains("fakta") || lower.contains("spill")) bumpTopic("fakta")

        val stopWords = setOf(
            "aku", "kamu", "yang", "dan", "di", "ke", "dari", "ini", "itu", "aja", "nih",
            "sih", "dong", "kok", "ya", "ga", "gk", "gak", "mau", "lagi", "buat", "apa", "gimana"
        )
        val recent = current.recentKeywords.toMutableList()
        words.filter { it.length >= 4 && it !in stopWords && it.all { ch -> ch.isLetter() } }
            .take(3)
            .forEach { kw ->
                recent.remove(kw)
                recent.add(kw)
            }

        val updatedUser = current.copy(
            totalMessages = nextCount,
            avgWordCount = nextAvgWords,
            pronounStyle = nextPronounStyle,
            laughStyle = nextLaughStyle,
            usesAbbreviations = current.usesAbbreviations || hasAbbrev,
            prefersLowercaseCasual = current.prefersLowercaseCasual || isLowercaseCasual,
            learnedInterests = mergedUserInterests,
            learnedFacts = facts.takeLast(12),
            topicCounts = userTopics,
            recentKeywords = recent.takeLast(10),
            adaptedFromSimilarUsers = current.adaptedFromSimilarUsers || matchesPriorSlang || matchesPriorArchetype || (shared.totalCollectiveInteractions > 0 && newInterests.any { it in shared.popularInterests })
        )

        val updatedShared = shared.copy(
            totalCollectiveInteractions = shared.totalCollectiveInteractions + 1,
            gueLoUsersCount = shared.gueLoUsersCount + if (detectedGueLo) 1 else 0,
            akuKamuUsersCount = shared.akuKamuUsersCount + if (detectedAkuKamu) 1 else 0,
            shortStyleCount = shared.shortStyleCount + if (wordCount <= 6) 1 else 0,
            wkwkCount = shared.wkwkCount + if (detectedWkwk) 1 else 0,
            hahaCount = shared.hahaCount + if (detectedHaha) 1 else 0,
            heheCount = shared.heheCount + if (detectedHehe) 1 else 0,
            knownSlang = (shared.knownSlang + matchedSlangInMsg).distinct().takeLast(35),
            popularInterests = mergedSharedInterests,
            learnedInsights = insights.takeLast(25),
            topicCounts = globalTopics
        )

        return CombinedChatLearning(updatedUser, updatedShared)
    }

    private fun extractSelfFact(raw: String): String? {
        val patterns = listOf(
            Regex("""(?i)\b(?:aku|gw|gue|gua)\s+(?:suka|doyan|hobi|favorit)\s+([^,.!?\n]{3,36})"""),
            Regex("""(?i)\b(?:hobiku|makanan favoritku|minuman favoritku)\s+(?:itu\s+|adalah\s+)?([^,.!?\n]{3,36})"""),
            Regex("""(?i)\b(?:aku|gw|gue)\s+(?:kelas|umur|tinggal di|sekolah di)\s+([^,.!?\n]{2,32})"""),
            Regex("""(?i)\bpanggil\s+(?:aku|gw|gue)\s+([a-zA-Z0-9_\s]{2,20})""")
        )
        for (regex in patterns) {
            val match = regex.find(raw)
            if (match != null) {
                return match.value.trim().take(48)
            }
        }
        return null
    }

    private fun saveUserProfile(uid: String, profile: ChatLearningProfile) {
        val obj = JSONObject().apply {
            put("totalMessages", profile.totalMessages)
            put("avgWordCount", profile.avgWordCount)
            put("pronounStyle", profile.pronounStyle)
            put("laughStyle", profile.laughStyle)
            put("usesAbbreviations", profile.usesAbbreviations)
            put("prefersLowercaseCasual", profile.prefersLowercaseCasual)
            put("learnedInterests", JSONArray(profile.learnedInterests))
            put("learnedFacts", JSONArray(profile.learnedFacts))
            put("recentKeywords", JSONArray(profile.recentKeywords))
            put("topicCounts", JSONObject(profile.topicCounts))
            put("adaptedFromSimilarUsers", profile.adaptedFromSimilarUsers)
        }
        userPrefs(uid).edit().putString(KEY_PROFILE_JSON, obj.toString()).apply()
    }

    private fun saveSharedKnowledge(shared: SharedAiKnowledge) {
        val obj = JSONObject().apply {
            put("totalCollectiveInteractions", shared.totalCollectiveInteractions)
            put("gueLoUsersCount", shared.gueLoUsersCount)
            put("akuKamuUsersCount", shared.akuKamuUsersCount)
            put("shortStyleCount", shared.shortStyleCount)
            put("wkwkCount", shared.wkwkCount)
            put("hahaCount", shared.hahaCount)
            put("heheCount", shared.heheCount)
            put("knownSlang", JSONArray(shared.knownSlang))
            put("popularInterests", JSONArray(shared.popularInterests))
            put("learnedInsights", JSONArray(shared.learnedInsights))
            put("topicCounts", JSONObject(shared.topicCounts))
        }
        sharedPrefs().edit().putString(KEY_SHARED_JSON, obj.toString()).apply()
    }

    private fun parseProfile(raw: String): ChatLearningProfile {
        return try {
            val obj = JSONObject(raw)
            val interests = obj.optJSONArray("learnedInterests")?.let { arr ->
                List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList()
            val facts = obj.optJSONArray("learnedFacts")?.let { arr ->
                List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList()
            val recent = obj.optJSONArray("recentKeywords")?.let { arr ->
                List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList()
            val topicMap = parseTopicMap(obj.optJSONObject("topicCounts"))
            ChatLearningProfile(
                totalMessages = obj.optInt("totalMessages", 0),
                avgWordCount = obj.optDouble("avgWordCount", 6.0),
                pronounStyle = obj.optString("pronounStyle", "aku_kamu"),
                laughStyle = obj.optString("laughStyle", ""),
                usesAbbreviations = obj.optBoolean("usesAbbreviations", false),
                prefersLowercaseCasual = obj.optBoolean("prefersLowercaseCasual", false),
                learnedInterests = interests,
                learnedFacts = facts,
                topicCounts = topicMap,
                recentKeywords = recent,
                adaptedFromSimilarUsers = obj.optBoolean("adaptedFromSimilarUsers", false)
            )
        } catch (_: Exception) {
            ChatLearningProfile()
        }
    }

    private fun parseSharedKnowledge(raw: String): SharedAiKnowledge {
        return try {
            val obj = JSONObject(raw)
            val slang = obj.optJSONArray("knownSlang")?.let { arr ->
                List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList()
            val interests = obj.optJSONArray("popularInterests")?.let { arr ->
                List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList()
            val insights = obj.optJSONArray("learnedInsights")?.let { arr ->
                List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList()
            val topicMap = parseTopicMap(obj.optJSONObject("topicCounts"))
            SharedAiKnowledge(
                totalCollectiveInteractions = obj.optInt("totalCollectiveInteractions", 0),
                gueLoUsersCount = obj.optInt("gueLoUsersCount", 0),
                akuKamuUsersCount = obj.optInt("akuKamuUsersCount", 0),
                shortStyleCount = obj.optInt("shortStyleCount", 0),
                wkwkCount = obj.optInt("wkwkCount", 0),
                hahaCount = obj.optInt("hahaCount", 0),
                heheCount = obj.optInt("heheCount", 0),
                knownSlang = slang,
                popularInterests = interests,
                learnedInsights = insights,
                topicCounts = topicMap
            )
        } catch (_: Exception) {
            SharedAiKnowledge()
        }
    }

    private fun parseTopicMap(tObj: JSONObject?): Map<String, Int> {
        val topicMap = mutableMapOf<String, Int>()
        if (tObj != null) {
            val keys = tObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                topicMap[k] = tObj.optInt(k, 0)
            }
        }
        return topicMap
    }

    companion object {
        private const val SHARED_PREFS_NAME = "dentally_shared_ai_brain"
        private const val KEY_SHARED_JSON = "shared_ai_knowledge_v1"
        private const val KEY_PROFILE_JSON = "learned_profile_v1"
    }
}
