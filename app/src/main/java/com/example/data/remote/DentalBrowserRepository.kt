package com.example.data.remote

import android.net.Uri
import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

enum class BrowserCategory(val label: String) {
    ALL("Semua"),
    TRENDING("Trending"),
    SEASONAL("Waspada Penyakit"),
    VIDEO_REELS("Video & Reels"),
    DENTAL("Kesehatan Gigi"),
    RESEARCH("Riset & Fakta")
}

data class DentalArticle(
    val id: String,
    val title: String,
    val summary: String,
    val contentDetail: String,
    val takeaways: List<String>,
    val sourceName: String,
    val publishedAt: String,
    val readTime: String,
    val category: BrowserCategory,
    val url: String,
    val isFromLiveFeed: Boolean = false,
    val isCustom: Boolean = false
)

/**
 * Live Multi-Platform Health & Dental Feed Repository.
 * Fetches real-time trending health & dental news, seasonal disease alerts (DBD, ISPA/Flu,
 * gula berlebih, dll), and direct Instagram Reels / YouTube video links via live RSS feeds.
 * - Never emits `google.com/search?q=...` links or generic IG profile pages.
 * - Rotates query pools on every refresh so Refresh produces genuinely new/freshly ordered feeds.
 */
class DentalBrowserRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val refreshCounter = AtomicInteger(0)

    fun normalizeUserSourceUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return ""
        val withScheme = if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else if (trimmed.contains("://")) {
            return null
        } else {
            "https://$trimmed"
        }
        val sanitized = sanitizeUrl(withScheme) ?: return null
        val host = try {
            Uri.parse(sanitized).host
        } catch (_: Exception) {
            null
        }
        if (host.isNullOrBlank() || !host.contains('.')) return null
        return sanitized
    }

    fun inferSourceNameFromUrl(url: String, customSource: String): String {
        val cleanCustom = customSource.trim()
        if (cleanCustom.isNotBlank()) return cleanCustom
        return when {
            url.contains("instagram.com", ignoreCase = true) -> "Instagram Reels • Sumber Saya"
            url.contains("youtube.com", ignoreCase = true) || url.contains("youtu.be", ignoreCase = true) ->
                "YouTube • Sumber Saya"
            url.isNotBlank() -> {
                val host = try {
                    Uri.parse(url).host?.removePrefix("www.")
                } catch (_: Exception) {
                    null
                }
                if (!host.isNullOrBlank()) "$host • Sumber Saya" else "Tautan • Sumber Saya"
            }
            else -> "Materi Pribadi • Sumber Saya"
        }
    }

    fun buildCustomArticle(
        title: String,
        summaryOrContent: String,
        sourceName: String,
        url: String,
        category: BrowserCategory,
        takeawaysRaw: List<String> = emptyList(),
        idOverride: String? = null
    ): DentalArticle {
        val normalizedUrl = if (url.isBlank()) "" else (normalizeUserSourceUrl(url) ?: "")
        val resolvedSource = inferSourceNameFromUrl(normalizedUrl, sourceName)
        val trimmedBody = summaryOrContent.trim()
        val resolvedSummary = when {
            trimmedBody.isNotBlank() -> trimmedBody.take(150).let {
                if (trimmedBody.length > 150) "$it..." else it
            }
            normalizedUrl.isNotBlank() -> "Sumber pilihan kamu dari $resolvedSource."
            else -> "Catatan & materi edukasi pribadi."
        }
        val resolvedDetail = when {
            trimmedBody.isNotBlank() -> trimmedBody
            normalizedUrl.isNotBlank() -> "Tautan video atau artikel pilihan kamu dari $resolvedSource. Ketuk tombol di bawah untuk membuka sumber aslinya secara langsung."
            else -> title.trim()
        }
        val resolvedTakeaways = takeawaysRaw.map { it.trim() }.filter { it.isNotBlank() }.ifEmpty {
            if (normalizedUrl.isNotBlank()) {
                listOf("Disimpan dari sumber pilihanmu: $resolvedSource")
            } else {
                listOf("Materi bacaan pribadi tersimpan di akunmu")
            }
        }
        val readBadge = when {
            normalizedUrl.contains("instagram.com", ignoreCase = true) -> "Reels IG"
            normalizedUrl.contains("youtube.com", ignoreCase = true) ||
                normalizedUrl.contains("youtu.be", ignoreCase = true) -> "Video"
            normalizedUrl.isNotBlank() -> "Tautan"
            else -> "Materi"
        }
        val dateStr = SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(java.util.Date())
        val uniqueSuffix = java.util.UUID.randomUUID().toString().replace("-", "").take(6)
        return DentalArticle(
            id = idOverride ?: "custom_${System.currentTimeMillis()}_$uniqueSuffix",
            title = title.trim(),
            summary = resolvedSummary,
            contentDetail = resolvedDetail,
            takeaways = resolvedTakeaways,
            sourceName = resolvedSource,
            publishedAt = dateStr,
            readTime = readBadge,
            category = category,
            url = normalizedUrl,
            isFromLiveFeed = false,
            isCustom = true
        )
    }

    fun getInitialCuratedArticles(): List<DentalArticle> {
        return getCuratedSeasonalAlerts() +
            getCuratedDentalAndResearch()
    }

    suspend fun fetchLiveBrowserFeed(forceRotate: Boolean = false): Pair<List<DentalArticle>, Boolean> =
        withContext(Dispatchers.IO) {
            val cycle = if (forceRotate) refreshCounter.incrementAndGet() else refreshCounter.get()

            val trendingQueries = listOf(
                "https://news.google.com/rss/search?q=viral+tren+kesehatan+indonesia+terbaru&hl=id&gl=ID&ceid=ID:id",
                "https://news.google.com/rss/search?q=kemenkes+edukasi+kesehatan+masyarakat+terbaru&hl=id&gl=ID&ceid=ID:id",
                "https://news.google.com/rss/search?q=gaya+hidup+sehat+remaja+keluarga+terbaru&hl=id&gl=ID&ceid=ID:id"
            )
            val seasonalQueries = listOf(
                "https://news.google.com/rss/search?q=waspada+DBD+demam+berdarah+influenza+ISPA+indonesia&hl=id&gl=ID&ceid=ID:id",
                "https://news.google.com/rss/search?q=kemenkes+penyakit+musim+hujan+diare+flu+batuk&hl=id&gl=ID&ceid=ID:id",
                "https://news.google.com/rss/search?q=bahaya+minuman+manis+diabetes+anak+remaja+kemenkes&hl=id&gl=ID&ceid=ID:id"
            )
            val dentalQueries = listOf(
                "https://news.google.com/rss/search?q=kesehatan+gigi+mulut+dokter+gigi+PDGI+kemenkes&hl=id&gl=ID&ceid=ID:id",
                "https://news.google.com/rss/search?q=cara+cegah+gigi+berlubang+karang+gigi+gusi+berdarah&hl=id&gl=ID&ceid=ID:id",
                "https://news.google.com/rss/search?q=tips+dokter+gigi+sikat+gigi+malam+gigi+sensitif&hl=id&gl=ID&ceid=ID:id"
            )
            val igReelQueries = listOf(
                "https://www.bing.com/search?q=site%3Ainstagram.com%2Freel+edukasi+kesehatan+gigi+dokter+gigi&format=rss",
                "https://www.bing.com/search?q=site%3Ainstagram.com%2Freel+edukasi+kesehatan+kemenkes+dbd+gula&format=rss"
            )
            val youtubeQueries = listOf(
                "https://www.bing.com/search?q=site%3Ayoutube.com%2Fwatch+cara+sikat+gigi+yang+benar+dokter+gigi&format=rss",
                "https://www.bing.com/search?q=site%3Ayoutube.com%2Fwatch+edukasi+kemenkes+waspada+dbd+influenza&format=rss"
            )

            val (liveTrending, liveSeasonal, liveDental, liveIgReels, liveYt) = coroutineScope {
                val t = async {
                    fetchRssFeed(
                        urls = rotateList(trendingQueries, cycle),
                        category = BrowserCategory.TRENDING,
                        prefixLabel = "Trending",
                        maxCount = 5
                    )
                }
                val s = async {
                    fetchRssFeed(
                        urls = rotateList(seasonalQueries, cycle),
                        category = BrowserCategory.SEASONAL,
                        prefixLabel = "Info Wabah & Kemenkes",
                        maxCount = 5
                    )
                }
                val d = async {
                    fetchRssFeed(
                        urls = rotateList(dentalQueries, cycle),
                        category = BrowserCategory.DENTAL,
                        prefixLabel = "Berita Gigi",
                        maxCount = 5
                    )
                }
                val ig = async {
                    fetchRssFeed(
                        urls = rotateList(igReelQueries, cycle),
                        category = BrowserCategory.VIDEO_REELS,
                        prefixLabel = "Instagram Reels",
                        maxCount = 4,
                        urlFilter = { url ->
                            url.contains("instagram.com/reel/", ignoreCase = true) ||
                                url.contains("instagram.com/p/", ignoreCase = true)
                        }
                    )
                }
                val yt = async {
                    fetchRssFeed(
                        urls = rotateList(youtubeQueries, cycle),
                        category = BrowserCategory.VIDEO_REELS,
                        prefixLabel = "YouTube Video",
                        maxCount = 4,
                        urlFilter = { url ->
                            url.contains("youtube.com/watch", ignoreCase = true) ||
                                url.contains("youtube.com/shorts/", ignoreCase = true) ||
                                url.contains("youtu.be/", ignoreCase = true)
                        }
                    )
                }
                FeedTuple(t.await(), s.await(), d.await(), ig.await(), yt.await())
            }

            val hasAnyLive = liveTrending.isNotEmpty() || liveSeasonal.isNotEmpty() ||
                liveDental.isNotEmpty() || liveIgReels.isNotEmpty() || liveYt.isNotEmpty()

            val curatedSeasonal = rotateList(getCuratedSeasonalAlerts(), cycle)
            val curatedResearch = rotateList(getCuratedDentalAndResearch(), cycle)

            val videosPool = (liveIgReels + liveYt)
            val seasonalPool = liveSeasonal.ifEmpty { curatedSeasonal }
            val dentalPool = liveDental.ifEmpty {
                curatedResearch.filter { it.category == BrowserCategory.DENTAL }
            }
            val trendingPool = liveTrending
            val researchPool = curatedResearch.filter { it.category == BrowserCategory.RESEARCH }

            // Interleave categories so "Semua" feels fresh, dynamic, and varies across refreshes
            val combined = buildList {
                addAll(seasonalPool.take(2))
                addAll(videosPool.take(3))
                addAll(trendingPool.take(2))
                addAll(dentalPool.take(2))
                addAll(researchPool.take(2))
                addAll(seasonalPool.drop(2))
                addAll(videosPool.drop(3))
                addAll(trendingPool.drop(2))
                addAll(dentalPool.drop(2))
                addAll(researchPool.drop(2))
            }.distinctBy { it.url.ifBlank { it.id } }

            combined to hasAnyLive
        }

    /**
     * Searches live RSS sources directly for any health/dental topic typed by the user,
     * returning direct publisher URLs rather than sending the user to Google Search.
     */
    suspend fun searchLiveTopics(rawQuery: String): Pair<List<DentalArticle>, Boolean> =
        withContext(Dispatchers.IO) {
            val query = rawQuery.trim()
            if (query.isBlank()) return@withContext fetchLiveBrowserFeed(forceRotate = false)

            val encoded = Uri.encode(query)
            val encodedReels = Uri.encode("site:instagram.com/reel $query kesehatan")
            val encodedYt = Uri.encode("site:youtube.com/watch $query kesehatan")

            val (newsResults, reelResults, ytResults) = coroutineScope {
                val n = async {
                    fetchRssFeed(
                        urls = listOf(
                            "https://news.google.com/rss/search?q=$encoded+kesehatan&hl=id&gl=ID&ceid=ID:id"
                        ),
                        category = BrowserCategory.TRENDING,
                        prefixLabel = "Sumber Berita",
                        maxCount = 8
                    )
                }
                val r = async {
                    fetchRssFeed(
                        urls = listOf("https://www.bing.com/search?q=$encodedReels&format=rss"),
                        category = BrowserCategory.VIDEO_REELS,
                        prefixLabel = "Instagram Reels",
                        maxCount = 4,
                        urlFilter = { url ->
                            url.contains("instagram.com/reel/", ignoreCase = true) ||
                                url.contains("instagram.com/p/", ignoreCase = true)
                        }
                    )
                }
                val y = async {
                    fetchRssFeed(
                        urls = listOf("https://www.bing.com/search?q=$encodedYt&format=rss"),
                        category = BrowserCategory.VIDEO_REELS,
                        prefixLabel = "YouTube Video",
                        maxCount = 4,
                        urlFilter = { url ->
                            url.contains("youtube.com/watch", ignoreCase = true) ||
                                url.contains("youtube.com/shorts/", ignoreCase = true)
                        }
                    )
                }
                Triple(n.await(), r.await(), y.await())
            }

            val liveCombined = (reelResults + newsResults + ytResults).distinctBy { it.url }
            if (liveCombined.isNotEmpty()) {
                liveCombined to true
            } else {
                // Local smart match over curated verified sources if offline
                val localMatches = getInitialCuratedArticles().filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.summary.contains(query, ignoreCase = true) ||
                        it.contentDetail.contains(query, ignoreCase = true)
                }
                localMatches to false
            }
        }

    private fun <T> rotateList(items: List<T>, offset: Int): List<T> {
        if (items.size <= 1) return items
        val shift = ((offset % items.size) + items.size) % items.size
        return items.drop(shift) + items.take(shift)
    }

    private fun fetchRssFeed(
        urls: List<String>,
        category: BrowserCategory,
        prefixLabel: String,
        maxCount: Int,
        urlFilter: (String) -> Boolean = { true }
    ): List<DentalArticle> {
        val collected = mutableListOf<DentalArticle>()
        for (rssUrl in urls) {
            try {
                val request = Request.Builder()
                    .url(rssUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile) DentallyHealth/2.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    val xmlBody = response.body?.string()
                    if (response.isSuccessful && !xmlBody.isNullOrBlank()) {
                        val parsed = parseRssXml(xmlBody, category, prefixLabel, maxCount, urlFilter)
                        if (parsed.isNotEmpty()) {
                            collected.addAll(parsed)
                            if (collected.size >= maxCount) {
                                return collected.take(maxCount)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("DentalBrowserRepo", "Gagal ambil RSS dari $rssUrl", e)
            }
        }
        return collected.take(maxCount)
    }

    private fun parseRssXml(
        xml: String,
        category: BrowserCategory,
        prefixLabel: String,
        maxCount: Int,
        urlFilter: (String) -> Boolean
    ): List<DentalArticle> {
        val results = mutableListOf<DentalArticle>()
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var insideItem = false
            var title = ""
            var link = ""
            var pubDate = ""
            var source = ""
            var description = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.lowercase() ?: ""
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName == "item") {
                            insideItem = true
                            title = ""
                            link = ""
                            pubDate = ""
                            source = ""
                            description = ""
                        } else if (insideItem) {
                            when (tagName) {
                                "title" -> title = parser.nextText().trim()
                                "link" -> link = parser.nextText().trim()
                                "pubdate" -> pubDate = parser.nextText().trim()
                                "source" -> source = parser.nextText().trim()
                                "description" -> description = parser.nextText().trim()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName == "item" && insideItem) {
                            insideItem = false
                            val safeUrl = sanitizeUrl(link)
                            if (title.isNotBlank() && safeUrl != null && urlFilter(safeUrl)) {
                                val cleanTitle = cleanTitle(title, source)
                                val resolvedSource = source.ifBlank { extractSourceFromTitle(title) ?: prefixLabel }
                                val formattedDate = formatRssDate(pubDate)
                                val cleanDesc = stripHtml(description)
                                val summaryText = if (cleanDesc.length > 24 && !cleanDesc.equals(cleanTitle, ignoreCase = true)) {
                                    cleanDesc.take(150).let { if (cleanDesc.length > 150) "$it..." else it }
                                } else {
                                    "Liputan langsung dari $resolvedSource seputar info & langkah pencegahan."
                                }

                                val readLabel = when (category) {
                                    BrowserCategory.VIDEO_REELS -> if (safeUrl.contains("instagram.com")) "Reels IG" else "Tonton Video"
                                    BrowserCategory.SEASONAL -> "Waspada"
                                    else -> "Sumber Resmi"
                                }

                                results.add(
                                    DentalArticle(
                                        id = "${category.name.lowercase()}_${results.size}_${safeUrl.hashCode()}",
                                        title = cleanTitle,
                                        summary = summaryText,
                                        contentDetail = "${if (cleanDesc.length > 24) cleanDesc else summaryText}\n\nSumber: $resolvedSource ($formattedDate). Ketuk tombol di bawah untuk membuka postingan atau artikel aslinya secara langsung.",
                                        takeaways = listOf(
                                            "Sumber langsung: $resolvedSource",
                                            "Selalu cek gejala awal & jaga kebersihan diri serta gigi setiap hari",
                                            "Ketuk tombol di bawah untuk membuka tautan asli"
                                        ),
                                        sourceName = "$prefixLabel • $resolvedSource",
                                        publishedAt = formattedDate,
                                        readTime = readLabel,
                                        category = category,
                                        url = safeUrl,
                                        isFromLiveFeed = true
                                    )
                                )
                            }
                            if (results.size >= maxCount) break
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("DentalBrowserRepo", "Error parsing RSS XML", e)
        }
        return results
    }

    internal fun sanitizeUrl(raw: String): String? {
        val decoded = raw
            .replace("&amp;", "&")
            .trim()
        if (decoded.isBlank()) return null
        if (!(decoded.startsWith("https://", ignoreCase = true) || decoded.startsWith("http://", ignoreCase = true))) {
            return null
        }
        if (decoded.contains("google.com/search?", ignoreCase = true)) return null
        return decoded
    }

    private fun stripHtml(raw: String): String {
        if (raw.isBlank()) return ""
        return raw
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun cleanTitle(rawTitle: String, source: String): String {
        val cleaned = stripHtml(rawTitle)
        if (source.isNotBlank() && cleaned.endsWith(" - $source")) {
            return cleaned.removeSuffix(" - $source").trim()
        }
        val dashIdx = cleaned.lastIndexOf(" - ")
        return if (dashIdx > 20) cleaned.substring(0, dashIdx).trim() else cleaned
    }

    private fun extractSourceFromTitle(rawTitle: String): String? {
        val dashIdx = rawTitle.lastIndexOf(" - ")
        return if (dashIdx in 15 until rawTitle.length - 3) {
            rawTitle.substring(dashIdx + 3).trim()
        } else null
    }

    private fun formatRssDate(rawDate: String): String {
        if (rawDate.isBlank()) return "Terkini"
        return try {
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val date = inputFormat.parse(rawDate) ?: return "Terkini"
            val outFormat = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
            outFormat.format(date)
        } catch (_: Exception) {
            "Terkini"
        }
    }

    private fun getCuratedSeasonalAlerts(): List<DentalArticle> = listOf(
        DentalArticle(
            id = "seasonal_dbd_who",
            title = "Waspada Demam Berdarah (DBD) Saat Musim Pancaroba & Hujan",
            summary = "Kenali demam mendadak, nyeri di belakang mata, dan bintik merah. Lakukan 3M Plus di rumah.",
            contentDetail = "Peralihan musim membuat nyamuk Aedes aegypti berkembang biak cepat di genangan air. Perhatikan gejala demam tinggi mendadak 2-7 hari, lemas, dan nyeri sendi. Segera periksa ke fasilitas kesehatan bila muncul tanda bahaya.",
            takeaways = listOf(
                "Kuras, tutup, dan daur ulang wadah penampung air minimal seminggu sekali",
                "Pastikan cukup minum cairan agar tidak dehidrasi saat demam",
                "Segera ke dokter/puskesmas bila demam tidak turun di hari ke-3"
            ),
            sourceName = "WHO • Lembar Fakta Resmi",
            publishedAt = "Peringatan Musiman",
            readTime = "2 menit baca",
            category = BrowserCategory.SEASONAL,
            url = "https://www.who.int/news-room/fact-sheets/detail/dengue-and-severe-dengue"
        ),
        DentalArticle(
            id = "seasonal_flu_ispa",
            title = "Antisipasi Influenza & ISPA Saat Cuaca Tak Menentu",
            summary = "Jaga daya tahan tubuh, pakai masker saat batuk-pilek, dan rajin bersihkan mulut serta tangan.",
            contentDetail = "Infeksi saluran pernapasan akut (ISPA) dan influenza mudah menyebar di sekolah maupun tempat kerja. Cuci tangan pakai sabun, cukup tidur, serta sikat gigi rutin membantu mengurangi kuman di rongga mulut dan tenggorokan.",
            takeaways = listOf(
                "Pakai masker ketika sedang flu agar tidak menulari keluarga",
                "Ganti sikat gigi setelah sembuh dari flu atau radang tenggorokan",
                "Perbanyak air putih dan konsumsi buah kaya vitamin"
            ),
            sourceName = "WHO • Influenza Musiman",
            publishedAt = "Waspada Penyakit",
            readTime = "2 menit baca",
            category = BrowserCategory.SEASONAL,
            url = "https://www.who.int/news-room/fact-sheets/detail/influenza-(seasonal)"
        ),
        DentalArticle(
            id = "seasonal_kemenkes_portal",
            title = "Panduan Hidup Sehat & Waspada Penyakit Menular dari Kemenkes RI",
            summary = "Info resmi pola makan seimbang, batasi gula harian, dan deteksi dini penyakit musiman.",
            contentDetail = "Portal Ayo Sehat Kemenkes RI menyajikan panduan praktis bagi anak, remaja, hingga keluarga untuk menjaga kebugaran tubuh, membatasi konsumsi gula berlebih, dan mencegah penyakit infeksi harian.",
            takeaways = listOf(
                "Batasi gula tambahan maksimal 4 sendok makan (50g) per hari",
                "Minum air putih setelah makanan/minuman manis untuk jaga gigi & metabolisme",
                "Rutin aktivitas fisik 30 menit per hari"
            ),
            sourceName = "Kemenkes RI • Ayo Sehat",
            publishedAt = "Sumber Resmi",
            readTime = "3 menit baca",
            category = BrowserCategory.SEASONAL,
            url = "https://ayosehat.kemkes.go.id/"
        ),
        DentalArticle(
            id = "seasonal_emerging_kemkes",
            title = "Pantauan Penyakit Infeksi & Kewaspadaan Kesehatan Terbaru Kemenkes",
            summary = "Rujukan resmi perkembangan infeksi pernapasan, virus musiman, dan langkah pencegahannya.",
            contentDetail = "Kanal Kewaspadaan Penyakit Infeksi Kemenkes RI mempublikasikan pembaruan resmi situasi penyakit menular serta protokol pencegahan dasar bagi masyarakat Indonesia.",
            takeaways = listOf(
                "Biasakan cuci tangan sebelum makan dan sesudah dari luar rumah",
                "Jaga kebersihan lingkungan rumah dan ventilasi udara"
            ),
            sourceName = "Kemenkes RI • Infeksi Emerging",
            publishedAt = "Resmi Kemenkes",
            readTime = "2 menit baca",
            category = BrowserCategory.SEASONAL,
            url = "https://infeksiemerging.kemkes.go.id/"
        )
    )

    private fun getCuratedDentalAndResearch(): List<DentalArticle> = listOf(
        DentalArticle(
            id = "dental_who_oral",
            title = "Fakta Global Kesehatan Mulut WHO: Cara Cegah Karies & Radang Gusi",
            summary = "Karies dan penyakit gusi adalah masalah paling umum namun sangat mudah dicegah setiap hari.",
            contentDetail = "Organisasi Kesehatan Dunia (WHO) menegaskan bahwa menyikat gigi dua kali sehari dengan pasta gigi berfluoride serta mengurangi minuman tinggi gula adalah kunci utama mencegah gigi berlubang dan infeksi gusi.",
            takeaways = listOf(
                "Sikat gigi 2x sehari: pagi sesudah sarapan dan malam sebelum tidur",
                "Kurangi frekuensi ngemil manis di antara jam makan",
                "Periksakan gigi rutin setiap 6 bulan sekali"
            ),
            sourceName = "WHO • Oral Health Fact Sheet",
            publishedAt = "Riset Global",
            readTime = "2 menit baca",
            category = BrowserCategory.RESEARCH,
            url = "https://www.who.int/news-room/fact-sheets/detail/oral-health"
        ),
        DentalArticle(
            id = "dental_nhs_clean",
            title = "Panduan NHS: Kenapa Jangan Langsung Kumur Air Banyak Habis Sikat Gigi?",
            summary = "Meludahkan busa pasta gigi tanpa dibilas habis bikin perlindungan fluoride bertahan lebih lama.",
            contentDetail = "Menurut panduan kesehatan NHS Inggris, langsung berkumur air banyak setelah menyikat gigi akan membilas lapisan pelindung fluoride. Cukup ludahkan busa berlebih dan beri jeda sebelum minum.",
            takeaways = listOf(
                "Cukup ludahkan busa pasta gigi, hindari langsung kumur air banyak",
                "Durasi minimal 2 menit di 4 sisi mulut atas-bawah kanan-kiri"
            ),
            sourceName = "NHS UK • Dental Care Guide",
            publishedAt = "Riset Klinis",
            readTime = "2 menit baca",
            category = BrowserCategory.RESEARCH,
            url = "https://www.nhs.uk/live-well/healthy-teeth-and-gums/how-to-keep-your-teeth-clean/"
        ),
        DentalArticle(
            id = "dental_cdc_prevention",
            title = "Panduan CDC: Lindungi Email Gigi dari Asam Kopi, Soda & Minuman Boba",
            summary = "Jangan langsung sikat gigi setelah minum asam — bilas air putih dulu dan tunggu 30 menit.",
            contentDetail = "Minuman asam dan manis melunakkan lapisan pelindung gigi sementara waktu. CDC menganjurkan berkumur air putih terlebih dahulu setelah minum kopi atau soda, lalu menunggu sekitar 30 menit sebelum menyikat gigi.",
            takeaways = listOf(
                "Bilas air putih setelah minum kopi, teh manis, boba, atau soda",
                "Tunggu 30 menit sebelum sikat gigi bila habis makan/minum asam"
            ),
            sourceName = "CDC • Oral Health Prevention",
            publishedAt = "Sumber Resmi",
            readTime = "2 menit baca",
            category = BrowserCategory.DENTAL,
            url = "https://www.cdc.gov/oral-health/prevention/index.html"
        ),
        DentalArticle(
            id = "dental_cdc_about",
            title = "Hubungan Kesehatan Mulut dengan Kesehatan Jantung & Gula Darah Tubuh",
            summary = "Infeksi gusi yang dibiarkan ternyata berpengaruh ke peradangan tubuh secara menyeluruh.",
            contentDetail = "Rongga mulut adalah pintu masuk utama menuju tubuh. Menjaga gusi tidak mudah berdarah dan bebas plak membantu menekan risiko peradangan sistemik dan menjaga kadar gula darah tetap stabil.",
            takeaways = listOf(
                "Kesehatan mulut cerminan kesehatan tubuh secara menyeluruh",
                "Jangan abaikan gusi bengkak atau sering berdarah saat menyikat"
            ),
            sourceName = "CDC • About Oral Health",
            publishedAt = "Edukasi Medis",
            readTime = "2 menit baca",
            category = BrowserCategory.DENTAL,
            url = "https://www.cdc.gov/oral-health/about/index.html"
        )
    )

    private data class FeedTuple(
        val trending: List<DentalArticle>,
        val seasonal: List<DentalArticle>,
        val dental: List<DentalArticle>,
        val igReels: List<DentalArticle>,
        val ytVideos: List<DentalArticle>
    )
}
