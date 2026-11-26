package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.repository.ChatLearningProfile
import com.example.data.repository.SharedAiKnowledge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class UserChatContext(
    val userName: String = "Sobat",
    val toothHealthPercent: Int = 75,
    val streakDays: Int = 1,
    val levelName: String = "Sahabat Gigi Bersih",
    val completedGoodHabits: Int = 0,
    val totalGoodHabits: Int = 4,
    val timerDoneToday: Boolean = false,
    val currentMood: String? = null,
    val learnedProfile: ChatLearningProfile = ChatLearningProfile(),
    val sharedKnowledge: SharedAiKnowledge = SharedAiKnowledge()
)

class GeminiChatService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun buildSystemPrompt(ctx: UserChatContext): String {
        val nick = ctx.userName.trim().split(" ").firstOrNull().takeUnless { it.isNullOrBlank() } ?: "Sobat"
        val moodNote = if (!ctx.currentMood.isNullOrBlank()) "- Mood hari ini: ${ctx.currentMood}" else ""
        val timerNote = if (ctx.timerDoneToday) "Sudah beres timer 2 menit hari ini." else "Belum jalanin timer 2 menit hari ini."

        val learned = ctx.learnedProfile
        val shared = ctx.sharedKnowledge
        val typingGuide = buildString {
            append("- Tingkat kedekatan saat ini: ${learned.closenessLevel} (${learned.totalMessages} interaksi user ini, ${shared.totalCollectiveInteractions} total obrolan lintas pengguna).\n")
            when (learned.pronounStyle) {
                "gue_lo" -> append("- Gaya panggilan user santai pakai gue/lo atau bro/bang. Kamu boleh sesekali ikutan santai tapi tetap sopan dan ramah.\n")
                else -> append("- Gaya panggilan user hangat pakai aku/kamu.\n")
            }
            if (learned.laughStyle.isNotBlank()) {
                append("- User suka ketawa pakai '${learned.laughStyle}'. Sesekali samain gayanya biar terasa akrab.\n")
            }
            if (learned.prefersShortReplies) {
                append("- User biasa ngetik singkat-singkat. Jawab yang padat, to-the-point, maksimal 2 paragraf pendek biar nggak bikin malas baca.\n")
            }
            if (learned.usesAbbreviations) {
                append("- User nyaman pakai bahasa chat sehari-hari yang ringkas (kayak 'gak', 'yg', 'nih', 'sih').\n")
            }
            if (learned.learnedInterests.isNotEmpty()) {
                append("- Minat/kebiasaan user ini: ${learned.learnedInterests.joinToString(", ")}.\n")
            }
            if (learned.learnedFacts.isNotEmpty()) {
                append("- Fakta yang pernah diceritain user ini: ${learned.learnedFacts.joinToString("; ")}.\n")
            }
            if (shared.knownSlang.isNotEmpty()) {
                append("- Kosakata santai yang sudah kamu pelajari dari pengguna sebelumnya: ${shared.knownSlang.takeLast(20).joinToString(", ")}.\n")
            }
            if (shared.popularInterests.isNotEmpty()) {
                append("- Topik & minat populer dari pengguna serupa sebelumnya: ${shared.popularInterests.takeLast(12).joinToString(", ")}.\n")
            }
            if (shared.learnedInsights.isNotEmpty()) {
                append("- Insight adaptasi kolektif dari riwayat banyak user: ${shared.learnedInsights.joinToString(" | ")}.\n")
            }
            val deepTopics = learned.topicCounts.filterValues { it >= 2 }.keys
            if (deepTopics.isNotEmpty()) {
                append("- Topik yang sudah matang di memorimu (${deepTopics.joinToString(", ")}): langsung kasih info lanjutan yang makin pintar & fresh, tidak usah bertele-tele dari nol.\n")
            }
        }.trimEnd()

        val localeId = Locale.forLanguageTag("id-ID")
        val now = Date()
        val liveClock = SimpleDateFormat("HH:mm", localeId).format(now)
        val liveDate = SimpleDateFormat("EEEE, dd MMMM yyyy", localeId).format(now)

        return """
        Kamu adalah "Dentally", temen ngobrol AI yang asyik, pintar, nyambung, dan membawa kepintaran kolektif dari seluruh pengguna sebelumnya supaya user baru yang punya kemiripan gaya atau topik langsung nyambung dari awal.

        INFO WAKTU & PROFIL TEMAN NGOBROL KAMU SAAT INI:
        - Jam & tanggal perangkat sekarang: pukul $liveClock, hari $liveDate (gunakan info ini kalau user tanya jam berapa atau tanggal berapa sekarang!)
        - Nama panggilan: $nick
        - Tingkat kebersihan mulut hari ini: ${ctx.toothHealthPercent}% (${ctx.levelName})
        - Kebiasaan baik selesai hari ini: ${ctx.completedGoodHabits} dari ${ctx.totalGoodHabits}
        - Streak rajin: ${ctx.streakDays} hari berturut-turut
        - Status timer sikat gigi: $timerNote
        $moodNote

        MEMORI KOLEKTIF & GAYA KETIKAN YANG SUDAH KAMU PELAJARI:
        $typingGuide

        ATURAN GAYA BAHASA & KEPRIBADIAN (WAJIB IKUTI!):
        1. JANGAN KEBANYAKAN EMOTE / EMOJI:
           - Ini aturan keras: JANGAN spam emoji! Pakai 0 atau maksimal 1 emoji saja dalam satu pesan, hanya kalau bener-bener pas. Lebih baik teks mengalir natural daripada penuh simbol.

        2. MENGALIR, ADAPTIF & MAKIN DEKET:
           - Pakai bahasa Indonesia sehari-hari yang santai, luwes, dan terasa kayak temen deket beneran.
           - Manfaatkan apa yang udah kamu pelajari dari gaya ketikan, minat, dan cerita sebelumnya secara halus biar user ngerasa kamu makin paham dia.
           - Nggak usah selalu bikin daftar bullet kaku kalau obrolannya santai. Tulis pendek-pendek 1-3 kalimat per paragraf.

        3. BISA JAWAB APA PUN:
           - Kalau $nick nanya soal pelajaran, game, hobi, makanan, teka-teki, curhat, atau topik random apa pun, jawab dulu dengan serius-tapi-santai dan nyambung. Jangan tiap kali dipaksa belok ke topik sikat gigi.

        4. JAGOAN KESEHATAN GIGI & MULUT:
           - Saat $nick bahas gigi, mulut, behel, sariawan, gusi berdarah, gigi ngilu, atau jajanan manis, jelasin pakai perumpamaan gampang (pasukan gula, selimut lengket, kerak batu, tameng pelindung).
           - DILARANG pakai istilah medis rumit (OHI-S, Debris Index, Calculus Index, karies, gingivitis, halitosis, periodontitis, aterosklerosis). Pakai kata awam: "tingkat kebersihan mulut", "gigi berlubang", "gusi meradang/berdarah", "bau mulut", "sisa makanan/plak", "karang gigi".

        5. INTERAKTIF & KEAMANAN:
           - Tutup sebagian besar balasan dengan pancingan obrolan singkat yang asyik.
           - Kalau keluhan terdengar berat (sakit parah berhari-hari, bengkak besar, demam), ingatkan dengan ramah untuk periksa ke dokter gigi.
        """.trimIndent()
    }

    suspend fun sendMessage(
        userMessage: String,
        history: List<Pair<String, Boolean>> = emptyList(),
        userContext: UserChatContext = UserChatContext()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getSmartFallbackResponse(userMessage, history, userContext)
        }

        val modelsToTry = listOf("gemini-2.5-flash", "gemini-3.5-flash")
        for (modelName in modelsToTry) {
            try {
                val jsonBody = JSONObject().apply {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", buildSystemPrompt(userContext))
                            })
                        })
                    })

                    val contentsArray = JSONArray()
                    val recentHistory = history.takeLast(14)
                    for ((msg, isUser) in recentHistory) {
                        contentsArray.put(JSONObject().apply {
                            put("role", if (isUser) "user" else "model")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", msg)
                                })
                            })
                        })
                    }

                    contentsArray.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userMessage)
                            })
                        })
                    })

                    put("contents", contentsArray)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.85)
                        put("topP", 0.95)
                        put("topK", 40)
                    })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseString = response.body?.string()
                    if (response.isSuccessful && responseString != null) {
                        val root = JSONObject(responseString)
                        val candidates = root.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text")
                                if (text.isNotBlank()) {
                                    return@withContext text.trim()
                                }
                            }
                        }
                    } else {
                        Log.w("DentallyAI", "Gemini ($modelName) returned ${response.code}: $responseString")
                    }
                }
            } catch (e: Exception) {
                Log.e("DentallyAI", "Gemini ($modelName) call failed", e)
            }
        }

        getSmartFallbackResponse(userMessage, history, userContext)
    }

    suspend fun getOhisRecommendation(
        diScore: Double,
        ciScore: Double,
        ohisScore: Double,
        category: String
    ): String = withContext(Dispatchers.IO) {
        "Tingkat kebersihan mulut kamu tercatat $category. Terus jaga sikat gigi 2 menit pagi dan malam ya."
    }

    private data class QuizItem(
        val idTag: String,
        val question: String,
        val correctLetter: String,
        val correctKeywords: List<String>,
        val explanation: String
    )

    private val quizzes = listOf(
        QuizItem(
            idTag = "[Kuis #1]",
            question = "**Tantangan Kuis #1**\n\nMenurutmu, bagian mana yang paling keras di seluruh tubuh manusia — bahkan lebih keras daripada tulang?\n\nA) Lapisan luar gigi\nB) Tulang lutut\nC) Kuku jari\n\nKetik **A**, **B**, atau **C** ya.",
            correctLetter = "A",
            correctKeywords = listOf("a", "lapisan", "luar", "gigi", "enamel"),
            explanation = "Yup, **A) Lapisan luar gigi**. Meski paling keras di tubuh kita, dia tetap bisa melemah kena asam dari sisa gula kalau dibiarkan nggak disikat."
        ),
        QuizItem(
            idTag = "[Kuis #2]",
            question = "**Tantangan Kuis #2**\n\nAbis sikat gigi pakai pasta ber-fluoride, cara paling oke biar perlindungan giginya maksimal itu gimana?\n\nA) Kumur berkali-kali sampai rasa mint hilang total\nB) Buang busanya aja, kumur sedikit atau nggak usah\nC) Langsung minum es teh manis\n\nPilih **A**, **B**, atau **C** nih.",
            correctLetter = "B",
            correctKeywords = listOf("b", "buang", "busa", "dikit", "sedikit"),
            explanation = "Betul banget, jawabannya **B**. Kalau cuma buang busanya tanpa kumur kebanyakan air, 'tameng pelindung' pasta gigi masih nempel dan kerja jagain gigimu."
        ),
        QuizItem(
            idTag = "[Kuis #3]",
            question = "**Tantangan Kuis #3**\n\nDurasi pas buat sikat gigi pakai timer di Beranda itu berapa lama sih biar seluruh 4 sudut gigi bersih maksimal?\n\nA) 20 detik asal cepet\nB) 2 menit (30 detik tiap bagian)\nC) 15 menit nonstop\n\nGas jawab **A**, **B**, atau **C**.",
            correctLetter = "B",
            correctKeywords = listOf("b", "2 menit", "dua menit", "120"),
            explanation = "Mantap, jawabannya **B) 2 menit**. Cukup 30 detik per sisi gigi udah bikin sisa makanan kabur semua tanpa bikin gusi lecet."
        ),
        QuizItem(
            idTag = "[Kuis #4]",
            question = "**Tantangan Kuis #4**\n\nKalau kita habis minum boba, soda, atau makan yang asam, sebaiknya langsung sikat gigi atau tunggu dulu?\n\nA) Langsung sikat detik itu juga\nB) Kumur air putih dulu dan tunggu sekitar 30 menit\nC) Biarin aja sampai besok\n\nPilih **A**, **B**, atau **C** ya.",
            correctLetter = "B",
            correctKeywords = listOf("b", "kumur", "30 menit", "tunggu"),
            explanation = "Bener banget, **B**. Minuman manis-asam bikin lapisan gigi sementara agak lunak, jadi lebih aman bilas pakai air putih dulu baru sikat 30 menit kemudian."
        )
    )

    internal fun getSmartFallbackResponse(
        query: String,
        history: List<Pair<String, Boolean>> = emptyList(),
        userContext: UserChatContext = UserChatContext()
    ): String {
        val trimmed = query.trim()
        val lower = trimmed.lowercase()
        val learned = userContext.learnedProfile
        val nick = userContext.userName.trim().split(" ").firstOrNull().takeUnless { it.isNullOrBlank() } ?: "Sobat"
        val variantSeed = abs(history.size + trimmed.length)
        val laugh = learned.laughStyle.ifBlank { "hehe" }

        // Helper for subtle personal recall
        val rememberedInterest = learned.learnedInterests.lastOrNull()
        val rememberedFact = learned.learnedFacts.lastOrNull()

        // 1. Check if replying to an active mini-quiz in the latest bot message
        val lastBotMsg = history.lastOrNull { !it.second }?.first.orEmpty()
        val activeQuiz = quizzes.firstOrNull { lastBotMsg.contains(it.idTag) }
        if (activeQuiz != null && isQuizAnswerAttempt(lower)) {
            val isCorrect = isAnswerMatchingQuiz(lower, activeQuiz)
            return if (isCorrect) {
                adaptTone(
                    "Wihh **BENAR BANGET**, $nick! ${activeQuiz.explanation}\n\n" +
                        "Skor kebersihanmu aja sekarang udah **${userContext.toothHealthPercent}%**, makin jago nih. Mau lanjut **kuis lagi** atau ngobrol topik lain?",
                    learned
                )
            } else {
                adaptTone(
                    "$laugh hampir kena jebakan nih $nick! Jawaban yang paling pas itu **${activeQuiz.correctLetter}**.\n\n" +
                        "${activeQuiz.explanation}\n\nSantuy, salah sekali wajar biar makin inget. Mau coba **kuis berikutnya**?",
                    learned
                )
            }
        }

        // 2. Simple math helper if a kid asks Dentally to calculate something
        trySolveSimpleMath(lower)?.let { mathAnswer ->
            return adaptTone(
                "Lagi ngerjain soal hitungan nih $nick? Hasilnya adalah **$mathAnswer**.\n\n" +
                    "Semangat ngerjain tugasnya ya. Kalau butuh temen ngobrol pas istirahat belajar, tinggal chat aja.",
                learned
            )
        }

        // 2b. Live device clock & date awareness
        if (lower.contains("jam berapa") || lower.contains("jam brp") || lower.contains("jam brr") ||
            lower.contains("pukul berapa") || lower.contains("tanggal berapa") || lower.contains("hari apa")) {
            val localeId = Locale.forLanguageTag("id-ID")
            val now = Date()
            val timeNow = SimpleDateFormat("HH:mm", localeId).format(now)
            val dateNow = SimpleDateFormat("EEEE, dd MMMM yyyy", localeId).format(now)
            return adaptTone(
                "Sekarang pukul **$timeNow** ya $nick, hari **$dateNow**. Lagi santai atau lagi ada kegiatan nih?",
                learned
            )
        }

        // 3. Quiz launcher
        if (lower.contains("kuis") || lower.contains("quiz") || lower.contains("tebak")) {
            val nextQuizIndex = quizzes.indexOfFirst { lastBotMsg.contains(it.idTag) }
                .let { if (it >= 0) (it + 1) % quizzes.size else variantSeed % quizzes.size }
            val chosen = quizzes[nextQuizIndex]
            return "${chosen.idTag} Siap-siap ya $nick.\n\n${chosen.question}"
        }

        // 4. Progress / personal score check
        if (lower.contains("progres") || lower.contains("skor") || lower.contains("nilai") || lower.contains("status")) {
            val comment = when {
                userContext.toothHealthPercent >= 85 -> "Udah kinclong banget ini sih."
                userContext.toothHealthPercent >= 65 -> "Udah cakep nih, tinggal gas dikit lagi ke 85%+."
                else -> "Yuk pelan-pelan kita kejar lagi bareng, mulai dari sikat 2 menit."
            }
            val memoryNote = if (rememberedInterest != null) "\n\nCatatan obrolan kita: aku inget kamu $rememberedInterest, jadi tetep jaga keseimbangan ya." else ""
            return adaptTone(
                "**Cek Rapor Gigi $nick Hari Ini**\n\n" +
                    "Kebersihan mulut: **${userContext.toothHealthPercent}%** (${userContext.levelName})\n" +
                    "Kebiasaan baik kelar: **${userContext.completedGoodHabits}/${userContext.totalGoodHabits}**\n" +
                    "Streak rajin: **${userContext.streakDays} hari** | Timer 2 menit: **${if (userContext.timerDoneToday) "udah kelar" else "belum nih"}**\n\n" +
                    "$comment Menurutmu kebiasaan mana yang paling gampang dikelarin abis ini?$memoryNote",
                learned
            )
        }

        // 5. Fun facts / jokes / spill
        if (lower.contains("fakta") || lower.contains("spill") || lower.contains("unik") || lower.contains("tau ga") || lower.contains("tahu gak")) {
            val facts = listOf(
                "**Fun Fact buat $nick**\n\nTau nggak sih, **sidik gigi** setiap orang di dunia itu unik banget kayak sidik jari? Bahkan anak kembar identik pun susunan giginya nggak 100% sama persis. Jadi senyummu itu beneran *limited edition*.\n\nMau fakta lain atau lanjut main kuis?",
                "**Fakta Dunia Hewan**\n\nHiu bisa ganti gigi terus-terusan seumur hidupnya—totalnya sampai **30.000 gigi**. Beda sama kita yang gigi tetapnya cuma satu set seumur hidup, makanya wajib dijaga.\n\nKira-kira kalau kamu punya 30.000 gigi, abis berapa botol odol tuh sebulan? $laugh",
                "**Fakta Kecil tapi Nyata**\n\nSetiap 1 milimeter plak yang nempel di gigi dihuni jutaan bakteri yang 'pesta' kalau kita lupa sikat malam. Untungnya cuma butuh **2 menit** sikat gigi buat bubarin pesta mereka.\n\nKamu tim langsung sikat sebelum tidur atau sering ketiduran dulu nih?",
                "**Fun Fact Random**\n\nJerapah ternyata nggak punya gigi depan atas, cuma gigi bawah dan geraham belakang. Tapi giginya tetep bersih karena sering ngunyah daun segar berair.\n\nSama kayak kita kalau makan buah renyah kayak apel atau pir, itu bantu bersihin gigi secara alami. Buah favoritmu apa?"
            )
            return adaptTone(facts[variantSeed % facts.size], learned)
        }

        if (lower.contains("joke") || lower.contains("lucu") || lower.contains("pantun") || lower.contains("ketawa") || lower.contains("ngakak")) {
            val jokes = listOf(
                "Dengerin pantun kilat dulu nih $nick:\n\n*Beli boba di pinggir jalan,*\n*Minumnya bareng si ikan hiu.*\n*Biar senyum makin menawan,*\n*Jangan lupa sikat gigi dulu.*\n\nReceh dikit nggak apa-apa kan $laugh. Hari ini lagi seru atau lagi capek?",
                "Tebak-tebakan receh nih $nick.\n\nGigi apa yang paling rajin sekolah dan nggak pernah telat?\n...\n**Gigi-h belajar**!\n\nGaring dikit ya $laugh. Kalau kamu punya tebak-tebakan lucu, lempar ke aku sini."
            )
            return adaptTone(jokes[variantSeed % jokes.size], learned)
        }

        // 6. Specific Topics (adapting depth from both personal and collective cross-user memory)
        val sharedTopics = userContext.sharedKnowledge.topicCounts
        val snackCount = maxOf(learned.topicCounts["jajanan"] ?: 0, sharedTopics["jajanan"] ?: 0)
        val brushCount = maxOf(learned.topicCounts["sikat_gigi"] ?: 0, sharedTopics["sikat_gigi"] ?: 0)
        val bracesCount = maxOf(learned.topicCounts["behel"] ?: 0, sharedTopics["behel"] ?: 0)

        val baseReply = when {
            lower.contains("boba") || lower.contains("manis") || lower.contains("permen") || lower.contains("coklat") || lower.contains("cokelat") ||
                lower.contains("soda") || lower.contains("kopi") || lower.contains("teh") || lower.contains("es krim") || lower.contains("seblak") -> {
                if (snackCount >= 2) {
                    "Soal jajanan manis lagi nih $nick—karena kita udah pernah bahas dasarnya, ini trik lanjutannya: waktu paling bahaya buat gigi itu kalau ngemil manisnya **dicicil lama-lama** (misal minum es manis sedikit-sedikit selama 2 jam), karena mulut jadi asam terus.\n\n" +
                        "Lebih aman habisin sekaligus dalam satu waktu, terus tutup pakai kumur air putih atau minum air mineral ya. Hari ini lagi ngidam jajan apa emangnya?"
                } else {
                    "Wah ngomongin jajanan enak nih. Sebenernya nggak harus musuhan sama boba, cokelat, atau es manis kok, asal tau triknya: abis makan atau minum yang manis, langsung **kumur pakai air putih** biar gulanya nggak kelamaan nongkrong di sela gigi. Kalau mau nyikat, tunggu sekitar 30 menit ya.\n\nJajanan favorit yang paling susah kamu tolak apa sih $nick?"
                }
            }

            lower.contains("ngilu") || lower.contains("sensitif") || lower.contains("dingin") -> {
                "Gigi ngilu pas kena es emang bikin kaget ya. Biasanya itu tanda lapisan pelindung gigi agak menipis atau pinggir gusi sedikit turun, jadi sinyal dingin gampang nyentuh saraf.\n\n" +
                    "Coba pakai sikat bulu lembut (*soft*) dengan gerakan pelan memutar tanpa ditekan keras, dan hindari langsung sikat habis minum yang asam. Kalau ngilunya sering banget di satu titik, cek ke dokter gigi ya. Paling sering ngilu pas minum es atau makan manis?"
            }

            lower.contains("sakit gigi") || lower.contains("lubang") || lower.contains("nyeri") -> {
                "Duh, kalau udah sakit gigi emang bikin mood anjlok banget ya $nick. Biasanya kalau nyut-nyutan, ada lubang yang udah deket ke bagian sensitif gigi atau sisa makanan nyangkut yang bikin radang.\n\n" +
                    "Sementara bisa kumur air hangat campur sedikit garam dan hindari ngunyah di sisi yang sakit. Tapi yang paling penting, ajak orang tua buat periksa ke dokter gigi secepatnya ya soalnya aku nggak bisa gantiin dokter. Udah berapa hari kerasa sakitnya?"
            }

            lower.contains("gusi") || lower.contains("darah") || lower.contains("bengkak") -> {
                "Tenang dulu $nick, gusi gampang berdarah pas nyikat itu tanda pinggiran gusi lagi agak meradang gara-gara kotoran nyelip, atau bulu sikatnya udah terlalu keras.\n\n" +
                    "Triknya: tetap sikat rutin pakai bulu *soft*, gerakin pelan di batas gusi, plus bersihin sela gigi pakai benang gigi. Kalau beberapa hari masih sering berdarah atau bengkak, wajib periksa ke dokter gigi ya. Sikat gigimu udah diganti dalam 3 bulan terakhir belum?"
            }

            lower.contains("sariawan") -> {
                "Sariawan emang kecil tapi perihnya bikin makan nggak nikmat ya. Biasanya muncul gara-gara nggak sengaja kegigit, kurang minum air putih/buah, stres, atau lagi kecapekan.\n\n" +
                    "Biar cepet reda, perbanyak minum air putih, makan buah bervitamin C, jangan dikopek, dan hindari pedas-asam dulu. Kalau 2 minggu belum sembuh juga, baru cek ke dokter. Lagi kena di bibir atau lidah nih?"
            }

            lower.contains("bau mulut") || lower.contains("nafas") || lower.contains("napas") -> {
                "Rahasia nafas segar seharian tuh bukan cuma dari permen mint, $nick. Penyebab bau mulut nomor satu justru **lapisan putih di lidah** dan sisa makanan di sela gigi yang nggak kejangkau sikat.\n\n" +
                    "Jadi pas sikat gigi 2 menit, sikat lembut permukaan lidah dari belakang ke depan, pakai benang gigi malam hari, dan rajin minum air putih biar mulut nggak kering. Biasanya paling kerasa pas bangun tidur atau siang hari?"
            }

            lower.contains("behel") || lower.contains("kawat") -> {
                if (bracesCount >= 2) {
                    "Buat pengguna behel yang udah paham sikat sela, level berikutnya adalah pakai **lilin ortodonti** kalau kawatnya nusuk pipi dan jaga biar nggak sering gigit makanan keras kayak es batu atau kerupuk alot biar bracket nggak copot.\n\n" +
                        "Oh iya, kalau habis kontrol kawat biasanya agak pegal, makan yang lembut dulu ya. Kontrol berikutnya kapan nih?"
                } else {
                    "Pejuang behel butuh kesabaran ekstra soalnya sela-sela bracket gampang banget jadi tempat ngumpet sisa makanan.\n\n" +
                        "Biar gigimu tetep kinclong sampai behelnya dilepas, sikatnya agak dimiringin dari arah atas dan bawah kawat, pakai sikat sela kecil (interdental), dan rajin kumur air putih habis ngemil. Udah berapa lama pakai behelnya, atau baru rencana?"
                }
            }

            lower.contains("karang") || lower.contains("plak") || lower.contains("kuning") || lower.contains("putih") -> {
                "Biar gampang bedainnya ya $nick: **sisa lunak (plak)** itu kayak selimut lengket tak kasatmata yang gampang disapu bersih sikat gigi. Nah, **karang gigi** itu plak yang kelamaan dicuekin sampai mengeras kayak kerak batu—kalau udah jadi karang, harus dibersihin dokter gigi.\n\n" +
                    "Rutin sikat 2 menit pagi dan malam itu kunci paling aman bikin senyum tetep cerah alami. Penasaran soal cara aman bikin gigi kelihatan lebih bersih nggak?"
            }

            lower.contains("sikat") || lower.contains("2 menit") || lower.contains("timer") -> {
                if (brushCount >= 2) {
                    "Karena kamu udah tau aturan 2 menitnya, ini **tips pro** biar makin maksimal $nick: arahkan bulu sikat miring **45 derajat** ke garis pertemuan gusi dan gigi (di situ tempat kotoran paling sering ngumpet!), getarkan pelan lalu sapu ke arah gigi.\n\n" +
                        "Abis nyikat, cukup buang busanya tanpa kebanyakan kumur air biar fluoride pasta gigi tetap kerja. Udah nyoba trik miring 45 derajat belum?"
                } else {
                    "Aturan sikat gigi yang paling oke tapi tetep santai tuh **2 menit buat 4 area** (30 detik per area): kanan atas, kiri atas, kiri bawah, terakhir kanan bawah plus lidah.\n\n" +
                        "Nggak usah buru-buru apalagi ditekan keras, cukup gerakin memutar pelan. Pencet aja Timer di Beranda kalau mau aku temenin hitung mundur. Udah sikat gigi belum hari ini?"
                }
            }

            lower.contains("kebersihan") || lower.contains("bersih") || lower.contains("tingkat") -> {
                "Tingkat Kebersihan Mulut kamu sekarang di angka **${userContext.toothHealthPercent}%**, $nick. Biar ngebut ke 85%–100%, kuncinya simpel: beresin sikat pagi & malam 2 menit, kumur air putih abis ngemil, dan hindari manis sebelum tidur.\n\n" +
                    "Menurutmu kebiasaan mana yang paling sering kelupaan: sikat malam atau pakai benang gigi?"
            }

            lower.contains("tubuh") || lower.contains("jantung") || lower.contains("badan") || lower.contains("kesehatan") -> {
                "Mulut kita tuh gerbang utama masuk ke seluruh tubuh. Kalau gigi nyaman dan mulut bersih, kamu bisa ngunyah makanan bergizi maksimal, tidur lebih nyenyak, dan badan nggak gampang kena radang dari bakteri mulut.\n\n" +
                    "Cuma modal 4 menit sehari, seluruh tubuh ikut kebagian bonus sehatnya. Belakangan lagi rutin olahraga atau jaga pola makan juga nggak?"
            }

            lower.contains("level") || lower.contains("gigi ku") || lower.contains("gamifikasi") || lower.contains("lencana") -> {
                "Sekarang status Gigi Ku kamu lagi di **${userContext.levelName}** (${userContext.toothHealthPercent}%). Cara paling cepet naik level: kelarin Timer 2 Menit di Beranda, centang semua kebiasaan baik, dan jaga streakmu yang udah **${userContext.streakDays} hari**.\n\n" +
                    "Mau aku kasih tantangan kecil hari ini?"
            }

            // 7. Games, School, Hobbies, Mood & Personal Recall
            lower.contains("game") || lower.contains("roblox") || lower.contains("mobile legends") ||
                lower.contains("ml") || lower.contains("ff") || lower.contains("minecraft") -> {
                "Anak game nih! Push rank atau mabar emang seru banget apalagi pas lagi winstreak. Jangan lupa minum air putih di sela-sela match, dan kalau main sampai malam jangan langsung ketiduran sebelum sikat gigi ya—anggap aja daily quest 2 menit.\n\n" +
                    "Lagi sering main game apa sekarang, role favoritmu apa?"
            }

            lower.contains("sekolah") || lower.contains("pr") || lower.contains("tugas") || lower.contains("ujian") || lower.contains("belajar") -> {
                "Semangat pejuang tugas, $nick! Kalau lagi capek belajar, coba metode 25 menit fokus lalu 5 menit istirahat buat minum air putih dan gerak bentar biar otak fresh lagi.\n\n" +
                    "Kalau mau tanya soal ringan, brainstorming ide tugas, atau sekadar butuh penyemangat, lempar aja ke sini. Lagi sibuk mapel apa emangnya?"
            }

            lower.contains("bosan") || lower.contains("gabut") || lower.contains("bosen") || lower.contains("random") || lower.contains("ngobrol") -> {
                val recallLine = if (rememberedInterest != null) "Oh iya, masih inget nggak terakhir kita sempet bahas soal $rememberedInterest? " else ""
                val options = listOf(
                    "${recallLine}Pas banget mampir kalau lagi gabut. Kita bisa main tebak-tebakan kuis gigi, dengerin fakta unik yang jarang orang tau, atau cerita topik random apa aja (game, makanan, sekolah).\n\nMau pilih yang mana nih $nick?",
                    "Ayo ngobrol santai aja. Kalau bisa milih satu kemampuan super paling aneh di dunia, kamu pilih bisa ngomong sama hewan atau nggak pernah ngantuk seumur hidup?"
                )
                options[variantSeed % options.size]
            }

            lower.contains("sedih") || lower.contains("capek") || lower.contains("lelah") || lower.contains("stres") || lower.contains("galau") -> {
                "Nggak apa-apa banget kok kalau hari ini rasanya capek atau lagi nggak oke, $nick. Setiap orang pasti punya hari yang berat.\n\n" +
                    "Coba tarik napas pelan sebentar, minum segelas air dingin biar agak tenang, dan nggak usah terlalu keras ke diri sendiri. Mau cerita kenapa lagi capek, atau pengin aku hibur pakai fakta random?"
            }

            lower.contains("hai") || lower.contains("halo") || lower.contains("hey") || lower.contains("pagi") ||
                lower.contains("siang") || lower.contains("sore") || lower.contains("malam") || lower.contains("apa kabar") || lower == "p" -> {
                val memoryBit = when {
                    rememberedFact != null -> " Aku masih inget lho kamu pernah cerita '$rememberedFact'."
                    rememberedInterest != null -> " Masih sering $rememberedInterest nggak nih?"
                    else -> ""
                }
                val greetings = listOf(
                    "Halooo $nick! Kabarku aman banget hari ini. Kebersihan mulutmu udah di **${userContext.toothHealthPercent}%** nih.$memoryBit Lagi santai atau pengin bahas sesuatu yang seru?",
                    "Yo $nick, seneng disapa lagi. Status kita udah **${learned.closenessLevel}** lho ($laugh). Hari ini mau bahas soal gigi, main kuis kilat, atau ngobrol topik bebas?"
                )
                greetings[variantSeed % greetings.size]
            }

            lower.contains("makasih") || lower.contains("terima kasih") || lower.contains("thanks") || lower.contains("thx") -> {
                "Sama-sama banget, $nick! Kapan aja butuh temen ngobrol, nanya soal kesehatan mulut, atau pengin main kuis gabut, langsung chat aja. Ada hal seru lain yang lagi kepikiran?"
            }

            lower.contains("haha") || lower.contains("wkwk") || lower.contains("hehe") || lower.contains("xixi") -> {
                "Ikutan ketawa $laugh! Seru banget suasananya. Ngomong-ngomong, mumpung lagi mood asyik gini mau lanjut ngobrol apa lagi nih $nick?"
            }

            lower.contains("siapa kamu") || lower.contains("kamu siapa") || lower.contains("namamu") -> {
                "Kenalin lagi dong! Aku **Dentally**, sahabat AI kamu yang terus belajar dari gaya ngobrol kita supaya makin nyambung—bisa diajak ngobrol apa aja, main tebak-tebakan, sampai kasih tips bikin gigi kuat tanpa ribet.\n\nLangsung ketik aja apa pun yang lagi pengin kamu bahas."
            }

            else -> {
                val shortTopic = trimmed.take(40)
                val recallTie = if (rememberedInterest != null && learned.totalMessages >= 3) {
                    " Kebetulan dari obrolan kita kemarin aku tau kamu juga $rememberedInterest."
                } else ""
                val openResponses = listOf(
                    "Menarik tuh soal \"$shortTopic\", $nick!$recallTie Ceritain lebih banyak dong, bagian paling seru atau yang bikin kamu kepikiran dari hal itu apa?",
                    "Nyambung juga tuh bahas \"$shortTopic\". Aku catat biar makin kenal sama gaya ceritamu nih. Pengin aku kasih pendapat santai, bahas lanjutannya, atau selingin main kuis biar nggak bosen?",
                    "Wihh oke juga topiknya (\"$shortTopic\"). Sambil ngobrol santai gini, kira-kira hal apa yang lagi paling bikin kamu semangat minggu ini $nick?"
                )
                openResponses[variantSeed % openResponses.size]
            }
        }

        return adaptTone(baseReply, learned)
    }

    /**
     * Adapts wording subtly to mirror the user's learned typing style and brevity preference,
     * while strictly keeping emoji count minimal (0-1 max).
     */
    private fun adaptTone(text: String, learned: ChatLearningProfile): String {
        var result = text
        if (learned.pronounStyle == "gue_lo") {
            result = result
                .replace("kamu", "lu")
                .replace("Kamu", "Lu")
                .replace("aku", "gw")
                .replace("Aku", "Gw")
                .replace("mu ", " lu ")
        }
        // If user types very short bursts and response has multiple paragraphs, keep it snappy on short chat
        if (learned.prefersShortReplies && result.length > 360) {
            val parts = result.split("\n\n")
            if (parts.size >= 3) {
                result = "${parts.first()}\n\n${parts.last()}"
            }
        }
        return result.trim()
    }

    private fun isQuizAnswerAttempt(lower: String): Boolean {
        val clean = lower.trim().removePrefix("jawab").trim()
        return clean in listOf("a", "b", "c", "a)", "b)", "c)") ||
            lower.startsWith("jawab a") || lower.startsWith("jawab b") || lower.startsWith("jawab c") ||
            lower.contains("2 menit") || lower.contains("lapisan luar") || lower.contains("kumur") || lower.contains("buang busa")
    }

    private fun isAnswerMatchingQuiz(lower: String, quiz: QuizItem): Boolean {
        val targetLetter = quiz.correctLetter.lowercase()
        val clean = lower.trim().removePrefix("jawab").trim().removeSuffix(")")
        if (clean == targetLetter) return true
        if (clean.length <= 2 && clean.startsWith(targetLetter)) return true
        return quiz.correctKeywords.any { kw -> kw.length > 1 && lower.contains(kw) }
    }

    private fun trySolveSimpleMath(lower: String): String? {
        val regex = Regex("""(-?\d+)\s*([+\-*/x×÷])\s*(-?\d+)""")
        val match = regex.find(lower) ?: return null
        val a = match.groupValues[1].toLongOrNull() ?: return null
        val op = match.groupValues[2]
        val b = match.groupValues[3].toLongOrNull() ?: return null
        return when (op) {
            "+" -> (a + b).toString()
            "-" -> (a - b).toString()
            "*", "x", "×" -> (a * b).toString()
            "/", "÷" -> if (b != 0L) {
                if (a % b == 0L) (a / b).toString() else String.format("%.2f", a.toDouble() / b.toDouble())
            } else "Nggak bisa dibagi nol nih"
            else -> null
        }
    }
}
