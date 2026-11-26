package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundEffectManager
import com.example.data.remote.BrowserCategory
import com.example.data.remote.DentalArticle
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.HealthSuccessBg
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import com.example.ui.viewmodel.DentallyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DentalBrowserScreen(
    viewModel: DentallyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val articles by viewModel.browserArticles.collectAsStateWithLifecycle()
    val isLoadingNews by viewModel.isLoadingNews.collectAsStateWithLifecycle()
    val isLiveGoogleNews by viewModel.isLiveGoogleNews.collectAsStateWithLifecycle()
    val lastRefreshTime by viewModel.lastBrowserRefreshTime.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BrowserCategory.ALL) }
    var selectedArticle by remember { mutableStateOf<DentalArticle?>(null) }
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var pendingDeleteArticle by remember { mutableStateOf<DentalArticle?>(null) }

    val filteredArticles = remember(articles, searchQuery, selectedCategory) {
        articles.filter { article ->
            val matchesCategory = selectedCategory == BrowserCategory.ALL || article.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                article.title.contains(searchQuery, ignoreCase = true) ||
                article.summary.contains(searchQuery, ignoreCase = true) ||
                article.sourceName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val spotlightArticle = remember(articles, selectedCategory, searchQuery) {
        if (searchQuery.isBlank() && selectedCategory == BrowserCategory.ALL) {
            articles.firstOrNull {
                it.category == BrowserCategory.SEASONAL || it.category == BrowserCategory.VIDEO_REELS
            } ?: articles.firstOrNull()
        } else null
    }

    fun openUrlInBrowser(rawUrl: String) {
        val normalized = rawUrl.trim()
        if (!(normalized.startsWith("https://", ignoreCase = true) || normalized.startsWith("http://", ignoreCase = true))) {
            Toast.makeText(context, "Tautan sumber tidak tersedia untuk artikel ini", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Tidak ada browser atau aplikasi pendukung di perangkat", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Gagal membuka tautan sumber", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Browser Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DentallyTintBg)
                            .border(1.dp, DentallyBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Berita & Wawasan Kesehatan",
                            tint = NaturalPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Berita & Wawasan",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 21.sp
                            )
                        )
                        Text(
                            text = "Trending, Waspada Penyakit, Reels IG, YouTube & Riset",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageMedium,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            SoundEffectManager.playPop()
                            showAddSourceDialog = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_custom_news_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Sumber Sendiri",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sumber",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            SoundEffectManager.playPop()
                            viewModel.refreshDentalBrowserNews(forceRotate = true)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, DentallyBorder, CircleShape)
                            .testTag("refresh_browser_btn")
                    ) {
                        if (isLoadingNews) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = NaturalPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Muat Ulang Feed Terbaru",
                                tint = NaturalPrimary
                            )
                        }
                    }
                }
            }
        }

        // 2. Live Topic Search Bar (searches live publisher & Reels feeds directly without Google Search dumps)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Cari tren kesehatan, DBD, flu, Reels, gigi...",
                        fontSize = 13.5.sp,
                        color = NaturalMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = NaturalPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NaturalPrimary)
                                    .clickable {
                                        focusManager.clearFocus()
                                        SoundEffectManager.playPop()
                                        viewModel.searchDentalBrowserTopics(searchQuery)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("browser_live_search_btn")
                            ) {
                                Text(
                                    text = "Cari",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    viewModel.refreshDentalBrowserNews(forceRotate = false)
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Pencarian",
                                    tint = NaturalMedium
                                )
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        if (searchQuery.isNotBlank()) {
                            viewModel.searchDentalBrowserTopics(searchQuery)
                        }
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = NaturalPrimary,
                    unfocusedBorderColor = DentallyBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("browser_search_input")
            )
        }

        // 3. Category Filter Chips + Live Status Pill
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BrowserCategory.entries) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) NaturalPrimary else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NaturalPrimary else DentallyBorder,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable {
                                    SoundEffectManager.playPop()
                                    selectedCategory = category
                                }
                                .padding(horizontal = 15.dp, vertical = 10.dp)
                                .testTag("filter_chip_${category.name}")
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) Color.White else NaturalDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                // Live feed status pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLiveGoogleNews) HealthSuccessBg else DentallyTintBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        val updateSuffix = if (lastRefreshTime.isNotBlank()) " • Update $lastRefreshTime" else ""
                        Text(
                            text = when {
                                isLoadingNews -> "Memuat berita, tren & Reels terbaru..."
                                isLiveGoogleNews -> "Feed Live Langsung ke Sumber$updateSuffix"
                                else -> "Sumber Resmi Terkurasi$updateSuffix"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLiveGoogleNews) HealthSuccess else NaturalPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Text(
                        text = "${filteredArticles.size} Konten",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SageMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // 4. Spotlight Card (Waspada Penyakit / Trending Pilihan)
        if (spotlightArticle != null) {
            item {
                val isSeasonal = spotlightArticle.category == BrowserCategory.SEASONAL
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            SoundEffectManager.playPop()
                            selectedArticle = spotlightArticle
                        }
                        .testTag("spotlight_fun_fact_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSeasonal) Color(0xFFFFF8EB) else DentallyTintBg
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSeasonal) Color(0xFFFDE68A) else DentallyBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSeasonal) Icons.Default.WarningAmber else Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "Sorotan Pilihan",
                                tint = if (isSeasonal) Color(0xFFB45309) else NaturalPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSeasonal) "WASPADA PENYAKIT & INFO MUSIMAN" else "TREN & VIDEO PILIHAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSeasonal) Color(0xFFB45309) else NaturalPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.7.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = spotlightArticle.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = spotlightArticle.summary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageDark,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Ketuk baca ringkasan & sumber langsung →",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSeasonal) Color(0xFFB45309) else NaturalPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 5. Article Feed List
        if (filteredArticles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DentallyBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada hasil untuk filter atau kata kunci ini",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cari langsung ke sumber resmi & postingan edukasi tanpa dialihkan ke halaman pencarian.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SageMedium)
                        )
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    selectedCategory = BrowserCategory.ALL
                                    viewModel.searchDentalBrowserTopics(searchQuery)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cari \"$searchQuery\" di Sumber Resmi")
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedCategory = BrowserCategory.ALL
                                    searchQuery = ""
                                    viewModel.refreshDentalBrowserNews(forceRotate = true)
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, DentallyBorder),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Tampilkan Semua Feed", color = NaturalPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredArticles, key = { it.id }) { article ->
                BrowserArticleCard(
                    article = article,
                    onClick = {
                        SoundEffectManager.playPop()
                        selectedArticle = article
                    },
                    onDeleteCustom = if (article.isCustom) {
                        { pendingDeleteArticle = article }
                    } else null
                )
            }
        }
    }

    // Article Reader Bottom Sheet
    if (selectedArticle != null) {
        val article = selectedArticle!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedArticle = null },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category = article.category)
                    Text(
                        text = "${article.publishedAt} • ${article.readTime}",
                        style = MaterialTheme.typography.labelSmall.copy(color = SageMedium)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NaturalDark,
                        fontSize = 19.sp,
                        lineHeight = 26.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = article.sourceName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = NaturalPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = article.contentDetail,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = NaturalDark,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                )

                if (article.takeaways.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DentallyTintBg)
                            .border(1.dp, DentallyBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Poin Penting yang Bisa Langsung Dipakai:",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalPrimary
                                )
                            )
                            article.takeaways.forEach { point ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = HealthSuccess,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = point,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SageDark,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (article.url.isNotBlank()) {
                    val (ctaLabel, ctaColor) = when {
                        article.url.contains("instagram.com", ignoreCase = true) ->
                            "Buka Postingan / Reels IG" to Color(0xFFC026D3)
                        article.url.contains("youtube.com", ignoreCase = true) ||
                            article.url.contains("youtu.be", ignoreCase = true) ->
                            "Tonton Video di YouTube" to Color(0xFFDC2626)
                        else ->
                            "Baca di Sumber Resmi" to NaturalPrimary
                    }

                    Button(
                        onClick = {
                            SoundEffectManager.playPop()
                            openUrlInBrowser(article.url)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("open_in_google_browser_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ctaColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ctaLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DentallyTintBg)
                            .border(1.dp, DentallyBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Materi bacaan pribadi tersimpan di akun Dentally kamu",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NaturalDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                if (article.isCustom) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val target = article
                            selectedArticle = null
                            pendingDeleteArticle = target
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sheet_delete_custom_source_btn"),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Sumber Saya",
                            tint = NaturalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Sumber Ini", color = NaturalPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { selectedArticle = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, DentallyBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Tutup", color = NaturalDark)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showAddSourceDialog) {
        AddCustomSourceDialog(
            validateUrl = { raw -> viewModel.validateCustomSourceUrl(raw) },
            onDismiss = { showAddSourceDialog = false },
            onSave = { title, content, source, url, category, takeaways ->
                val created = viewModel.addCustomBrowserSource(
                    title = title,
                    summaryOrContent = content,
                    sourceName = source,
                    url = url,
                    category = category,
                    takeawaysRaw = takeaways
                )
                if (created != null) {
                    showAddSourceDialog = false
                    selectedCategory = BrowserCategory.ALL
                    Toast.makeText(context, "Sumber berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (pendingDeleteArticle != null) {
        val target = pendingDeleteArticle!!
        AlertDialog(
            onDismissRequest = { pendingDeleteArticle = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Hapus Sumber Ini?",
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark
                )
            },
            text = {
                Text(
                    text = "\"${target.title}\" akan dihapus dari daftar Berita & Wawasan akunmu.",
                    color = SageDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomBrowserSource(target.id)
                        if (selectedArticle?.id == target.id) {
                            selectedArticle = null
                        }
                        pendingDeleteArticle = null
                        Toast.makeText(context, "Sumber dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                    modifier = Modifier.testTag("confirm_delete_custom_source_btn")
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteArticle = null }) {
                    Text("Batal", color = NaturalMedium)
                }
            }
        )
    }
}

@Composable
private fun BrowserArticleCard(
    article: DentalArticle,
    onClick: () -> Unit,
    onDeleteCustom: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("browser_article_${article.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.dp,
            color = if (article.isCustom) NaturalPrimary.copy(alpha = 0.35f) else DentallyBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryBadge(category = article.category)
                    if (article.isCustom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DentallyTintBg)
                                .border(1.dp, DentallyBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                                .testTag("custom_source_badge_${article.id}")
                        ) {
                            Text(
                                text = "Sumber Saya",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NaturalPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.publishedAt,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SageMedium,
                            fontSize = 11.sp
                        )
                    )
                    if (onDeleteCustom != null) {
                        IconButton(
                            onClick = onDeleteCustom,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("delete_custom_article_${article.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus sumber ${article.title}",
                                tint = NaturalMedium,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark,
                    fontSize = 15.5.sp,
                    lineHeight = 21.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SageDark,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.sourceName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NaturalMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${article.readTime} →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NaturalPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun AddCustomSourceDialog(
    validateUrl: (String) -> String?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        content: String,
        sourceName: String,
        url: String,
        category: BrowserCategory,
        takeaways: List<String>
    ) -> Unit
) {
    val categoryChoices = remember {
        listOf(
            BrowserCategory.VIDEO_REELS,
            BrowserCategory.DENTAL,
            BrowserCategory.TRENDING,
            BrowserCategory.SEASONAL,
            BrowserCategory.RESEARCH
        )
    }

    var selectedCategory by remember { mutableStateOf(BrowserCategory.VIDEO_REELS) }
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var sourceName by remember { mutableStateOf("") }
    var contentDetail by remember { mutableStateOf("") }
    var takeawaysText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val detectedHint = remember(url) {
        val trimmed = url.trim()
        when {
            trimmed.isBlank() -> "Kosongkan tautan jika hanya menyimpan materi / catatan bacaan"
            trimmed.contains("instagram.com", ignoreCase = true) -> "Terdeteksi: Postingan / Reels Instagram"
            trimmed.contains("youtube.com", ignoreCase = true) ||
                trimmed.contains("youtu.be", ignoreCase = true) -> "Terdeteksi: Video YouTube"
            else -> "Terdeteksi: Tautan Artikel / Web Eksternal"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_custom_source_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DentallyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tambah Sumber Sendiri",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Simpan video YouTube/Reels IG, link berita, atau materi kesehatan pilihanmu.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageMedium,
                                fontSize = 12.sp
                            )
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = NaturalMedium
                        )
                    }
                }

                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NaturalDark
                    )
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryChoices) { cat ->
                        val active = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (active) NaturalPrimary else NaturalBackground)
                                .border(
                                    1.dp,
                                    if (active) NaturalPrimary else DentallyBorder,
                                    RoundedCornerShape(50)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("custom_cat_${cat.name}")
                        ) {
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (active) Color.White else NaturalDark,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Judul Video / Tautan / Materi *") },
                    placeholder = { Text("Misal: Cara Flossing Gigi yang Benar") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_source_title_input")
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        errorMessage = null
                    },
                    label = { Text("Tautan Video / Website (Opsional)") },
                    placeholder = { Text("https://instagram.com/reel/... atau youtube.com/...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = NaturalPrimary
                        )
                    },
                    supportingText = {
                        Text(
                            text = detectedHint,
                            fontSize = 11.sp,
                            color = SageMedium
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_source_url_input")
                )

                OutlinedTextField(
                    value = sourceName,
                    onValueChange = { sourceName = it },
                    label = { Text("Nama Sumber / Kreator (Opsional)") },
                    placeholder = { Text("Misal: @drg.rachel atau Kemenkes RI") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_source_author_input")
                )

                OutlinedTextField(
                    value = contentDetail,
                    onValueChange = {
                        contentDetail = it
                        errorMessage = null
                    },
                    label = { Text("Ringkasan / Isi Materi") },
                    placeholder = { Text("Tulis isi materi, catatan penting, atau rangkuman video...") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_source_content_input")
                )

                OutlinedTextField(
                    value = takeawaysText,
                    onValueChange = { takeawaysText = it },
                    label = { Text("Poin Penting (Opsional, pisahkan baris baru)") },
                    placeholder = { Text("Sikat 2 menit pagi & malam\nJangan langsung kumur air banyak") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_source_takeaways_input")
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.testTag("custom_source_error_text")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DentallyBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Batal", color = NaturalDark)
                    }
                    Button(
                        onClick = {
                            val cleanTitle = title.trim()
                            val cleanUrl = url.trim()
                            val cleanContent = contentDetail.trim()
                            if (cleanTitle.isBlank()) {
                                errorMessage = "Judul wajib diisi dulu ya."
                                return@Button
                            }
                            if (cleanUrl.isNotBlank() && validateUrl(cleanUrl) == null) {
                                errorMessage = "Tautan tidak valid. Masukkan URL langsung (bukan link pencarian Google)."
                                return@Button
                            }
                            if (cleanUrl.isBlank() && cleanContent.isBlank()) {
                                errorMessage = "Isi tautan atau ringkasan materi agar sumber bisa dibuka."
                                return@Button
                            }
                            val takeaways = takeawaysText
                                .lines()
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            onSave(
                                cleanTitle,
                                cleanContent,
                                sourceName.trim(),
                                cleanUrl,
                                selectedCategory,
                                takeaways
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("save_custom_source_btn")
                    ) {
                        Text("Simpan Sumber", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: BrowserCategory) {
    val (bg, textColor, icon) = when (category) {
        BrowserCategory.TRENDING -> Triple(Color(0xFFE8F0FE), Color(0xFF1D4ED8), Icons.AutoMirrored.Filled.TrendingUp)
        BrowserCategory.SEASONAL -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.WarningAmber)
        BrowserCategory.VIDEO_REELS -> Triple(Color(0xFFFAE8FF), Color(0xFFC026D3), Icons.Default.PlayCircle)
        BrowserCategory.DENTAL -> Triple(DentallyTintBg, NaturalPrimary, Icons.Default.HealthAndSafety)
        BrowserCategory.RESEARCH -> Triple(HealthSuccessBg, HealthSuccess, Icons.AutoMirrored.Filled.MenuBook)
        BrowserCategory.ALL -> Triple(Color(0xFFF6ECEE), SageDark, Icons.Default.Public)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}
