package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.BrowserCategory
import com.example.data.remote.DentalArticle
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local per-user persistence for custom Berita & Wawasan sources
 * (videos, Reels links, websites, and reading materials added by the user).
 */
class CustomArticleLocalStore(context: Context) {

    private val appContext = context.applicationContext

    private fun prefsFor(uid: String): SharedPreferences {
        val safeUid = uid.ifBlank { "default_guest" }
        return appContext.getSharedPreferences("dentally_custom_articles_$safeUid", Context.MODE_PRIVATE)
    }

    fun getArticles(uid: String): List<DentalArticle> {
        val raw = prefsFor(uid).getString(KEY_ARTICLES_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("id").ifBlank { continue }
                    val title = obj.optString("title").ifBlank { continue }
                    val categoryName = obj.optString("category", BrowserCategory.DENTAL.name)
                    val category = BrowserCategory.entries.firstOrNull { it.name == categoryName }
                        ?: BrowserCategory.DENTAL
                    val takeawaysArr = obj.optJSONArray("takeaways")
                    val takeaways = buildList {
                        if (takeawaysArr != null) {
                            for (j in 0 until takeawaysArr.length()) {
                                takeawaysArr.optString(j).takeIf { it.isNotBlank() }?.let { add(it) }
                            }
                        }
                    }
                    add(
                        DentalArticle(
                            id = id,
                            title = title,
                            summary = obj.optString("summary", ""),
                            contentDetail = obj.optString("contentDetail", ""),
                            takeaways = takeaways,
                            sourceName = obj.optString("sourceName", "Sumber Saya"),
                            publishedAt = obj.optString("publishedAt", "Terkini"),
                            readTime = obj.optString("readTime", "Materi"),
                            category = category,
                            url = obj.optString("url", ""),
                            isFromLiveFeed = false,
                            isCustom = true
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveArticle(uid: String, article: DentalArticle) {
        val current = getArticles(uid).filterNot { it.id == article.id }
        saveAll(uid, listOf(article.copy(isCustom = true)) + current)
    }

    fun mergeRemoteArticles(uid: String, remote: List<DentalArticle>): List<DentalArticle> {
        val local = getArticles(uid)
        val merged = (remote.map { it.copy(isCustom = true) } + local)
            .distinctBy { it.id }
        saveAll(uid, merged)
        return merged
    }

    fun deleteArticle(uid: String, articleId: String): List<DentalArticle> {
        val remaining = getArticles(uid).filterNot { it.id == articleId }
        saveAll(uid, remaining)
        return remaining
    }

    fun clearForUser(uid: String) {
        prefsFor(uid).edit().clear().apply()
    }

    private fun saveAll(uid: String, articles: List<DentalArticle>) {
        val array = JSONArray()
        articles.forEach { article ->
            val obj = JSONObject().apply {
                put("id", article.id)
                put("title", article.title)
                put("summary", article.summary)
                put("contentDetail", article.contentDetail)
                put("sourceName", article.sourceName)
                put("publishedAt", article.publishedAt)
                put("readTime", article.readTime)
                put("category", article.category.name)
                put("url", article.url)
                val tk = JSONArray()
                article.takeaways.forEach { tk.put(it) }
                put("takeaways", tk)
            }
            array.put(obj)
        }
        prefsFor(uid).edit().putString(KEY_ARTICLES_JSON, array.toString()).apply()
    }

    companion object {
        private const val KEY_ARTICLES_JSON = "custom_articles_json"
    }
}
