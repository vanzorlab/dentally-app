package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundEffectManager
import com.example.data.remote.DeveloperUserDetail
import com.example.data.remote.DeveloperUserSummary
import com.example.ui.components.MonthlyHabitCalendar
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardScreen(
    viewModel: DentallyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val users by viewModel.developerUsers.collectAsStateWithLifecycle()
    val selectedDetail by viewModel.selectedDeveloperUserDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingDeveloper.collectAsStateWithLifecycle()
    val errorMsg by viewModel.developerError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    BackHandler {
        if (selectedDetail != null) {
            viewModel.clearSelectedDeveloperUserDetail()
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllUsersForDeveloper()
    }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (selectedDetail != null) "Detail Progres Pengguna" else "Panel Monitor Developer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalDark,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = if (selectedDetail != null) selectedDetail!!.summary.email else "Semua pengguna Dentally di Cloud",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageMedium,
                            fontSize = 11.5.sp
                        )
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (selectedDetail != null) {
                            viewModel.clearSelectedDeveloperUserDetail()
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.testTag("dev_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = NaturalDark
                    )
                }
            },
            actions = {
                if (selectedDetail == null) {
                    IconButton(
                        onClick = { viewModel.loadAllUsersForDeveloper() },
                        modifier = Modifier.testTag("dev_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang Data",
                            tint = NaturalPrimary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        if (selectedDetail != null) {
            DeveloperUserDetailContent(
                detail = selectedDetail!!,
                isLoading = isLoading
            )
        } else {
            DeveloperUserListContent(
                users = filteredUsers,
                totalUsers = users.size,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                isLoading = isLoading,
                errorMsg = errorMsg,
                onSelectUser = { summary -> viewModel.loadUserDetailForDeveloper(summary) }
            )
        }
    }
}

@Composable
private fun DeveloperUserListContent(
    users: List<DeveloperUserSummary>,
    totalUsers: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isLoading: Boolean,
    errorMsg: String?,
    onSelectUser: (DeveloperUserSummary) -> Unit
) {
    val avgHealth = remember(users) {
        if (users.isEmpty()) 0 else users.sumOf { it.toothHealthPercent } / users.size
    }
    val cleanUsersCount = remember(users) {
        users.count { it.toothHealthPercent >= 85 }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Developer Summary Header Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DevMetricCard(
                    title = "Total Akun",
                    value = "$totalUsers",
                    subtitle = "Pengguna Gmail",
                    accentColor = NaturalPrimary,
                    modifier = Modifier.weight(1f)
                )
                DevMetricCard(
                    title = "Rata Kebersihan",
                    value = "$avgHealth%",
                    subtitle = "Kebersihan Mulut",
                    accentColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                DevMetricCard(
                    title = "Sangat Bersih",
                    value = "$cleanUsersCount",
                    subtitle = "Skor ≥ 85%",
                    accentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari nama atau email Gmail pengguna...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = NaturalMedium
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Pencarian",
                                tint = NaturalMedium
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NaturalPrimary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dev_search_input")
            )
        }

        if (errorMsg != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Text(
                        text = "⚠️ $errorMsg",
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NaturalPrimary)
                }
            }
        } else if (users.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Kosong",
                            tint = SageMedium,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada data pengguna ditemukan.",
                            color = SageDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(users, key = { it.userId }) { user ->
                DeveloperUserCard(
                    user = user,
                    onClick = { onSelectUser(user) }
                )
            }
        }
    }
}

@Composable
private fun DevMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SageMedium,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NaturalDark.copy(alpha = 0.7f),
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DeveloperUserCard(
    user: DeveloperUserSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("dev_user_card_${user.userId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DentallyTintBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageMedium,
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (user.toothHealthPercent >= 70) HealthSuccessBg else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "${user.toothHealthPercent}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (user.toothHealthPercent >= 70) HealthSuccess else Color(0xFFB45309),
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (user.toothHealthPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NaturalPrimary,
                trackColor = DentallyBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Habit: ${user.completedHabitsCount}/${user.totalHabitsCount} • Streak ${user.streakDays}h",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageDark,
                        fontSize = 11.5.sp
                    )
                )
                val statusLabel = when {
                    user.toothHealthPercent >= 85 -> "Sangat Bersih"
                    user.toothHealthPercent >= 65 -> "Cukup Bersih"
                    else -> "Perlu Perawatan"
                }
                Text(
                    text = "Mulut: $statusLabel",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Update: ${user.updatedAtFormatted}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageMedium,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "Lihat Detail →",
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
private fun DeveloperUserDetailContent(
    detail: DeveloperUserDetail,
    isLoading: Boolean
) {
    val summary = detail.summary
    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var selectedDate by remember(summary.userId) { mutableStateOf(todayStr) }
    var filterBySelectedDate by remember(summary.userId) { mutableStateOf(false) }

    val displayedMoods = remember(detail.moods, selectedDate, filterBySelectedDate) {
        if (filterBySelectedDate) detail.moods.filter { it.dateString == selectedDate } else detail.moods
    }
    val displayedHabits = remember(detail.habits, selectedDate, filterBySelectedDate) {
        if (filterBySelectedDate) detail.habits.filter { it.dateString == selectedDate } else detail.habits
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = summary.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = NaturalDark
                            )
                            Text(
                                text = summary.email,
                                fontSize = 13.sp,
                                color = SageDark
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DentallyTintBg
                        ) {
                            Text(
                                text = "${summary.toothHealthPercent}% • Lv.${summary.level}",
                                color = NaturalPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = DentallyBorder)

                    Text(
                        text = "${summary.toothStage} • Streak ${summary.streakDays} hari",
                        fontSize = 12.5.sp,
                        color = NaturalDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = if (summary.isCalendarSynced) Color(0xFF4285F4) else SageMedium,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (summary.isCalendarSynced) "Pengingat Jadwal Aktif" else "Pengingat Belum Disinkron",
                            fontSize = 12.sp,
                            color = if (summary.isCalendarSynced) Color(0xFF1967D2) else SageMedium
                        )
                    }
                }
            }
        }

        // Monthly Calendar Monitoring for Developer (Full / Partial / Missed per date)
        item {
            MonthlyHabitCalendar(
                habits = detail.habits,
                moods = detail.moods,
                selectedDate = selectedDate,
                onDateSelected = { clickedDate ->
                    selectedDate = clickedDate
                    filterBySelectedDate = true
                },
                title = "Kalender Pengisian & Sikat Gigi"
            )
        }

        // Filter toggle chip (Selected Date vs All Dates)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (!filterBySelectedDate) NaturalPrimary else Color.White,
                    modifier = Modifier
                        .clickable {
                            SoundEffectManager.playPop()
                            filterBySelectedDate = false
                        }
                        .testTag("dev_filter_all_dates")
                ) {
                    Text(
                        text = "Semua Hari (${detail.habits.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!filterBySelectedDate) Color.White else NaturalDark,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (filterBySelectedDate) NaturalPrimary else Color.White,
                    modifier = Modifier
                        .clickable {
                            SoundEffectManager.playPop()
                            filterBySelectedDate = true
                        }
                        .testTag("dev_filter_selected_date")
                ) {
                    Text(
                        text = "Tanggal $selectedDate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (filterBySelectedDate) Color.White else NaturalDark,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NaturalPrimary)
                }
            }
        }

        // Tingkat Kebersihan Mulut Summary Card
        item {
            SectionTitleWithIcon(
                icon = Icons.Default.HealthAndSafety,
                title = "Tingkat Kebersihan Mulut"
            )
        }

        item {
            val statusDesc = when {
                summary.toothHealthPercent >= 85 -> "Sangat Bersih — Kebiasaan sikat gigi pengguna terjaga sangat baik."
                summary.toothHealthPercent >= 65 -> "Cukup Bersih — Sebagian besar perawatan rutin sudah dijalankan."
                else -> "Perlu Perawatan — Masih ada kebiasaan harian yang belum diselesaikan."
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${summary.toothHealthPercent}% • ${summary.toothStage}",
                        fontWeight = FontWeight.Bold,
                        color = NaturalPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = statusDesc,
                        fontSize = 12.sp,
                        color = SageDark
                    )
                }
            }
        }

        // Daylio Moods Section
        item {
            SectionTitleWithIcon(
                icon = Icons.Default.Mood,
                title = "Mood Gigi (${displayedMoods.size})"
            )
        }

        if (displayedMoods.isEmpty()) {
            item {
                EmptyDetailCard("Belum ada catatan mood.")
            }
        } else {
            items(displayedMoods) { mood ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${mood.dateString} • ${mood.moodTitle}",
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "Skor ${mood.moodScore}/5",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                fontSize = 12.5.sp
                            )
                        }
                        if (mood.dentalFeel.isNotBlank()) {
                            Text(
                                text = mood.dentalFeel,
                                fontSize = 12.sp,
                                color = SageDark
                            )
                        }
                        if (mood.note.isNotBlank()) {
                            Text(
                                text = "\"${mood.note}\"",
                                fontSize = 12.sp,
                                color = SageMedium
                            )
                        }
                    }
                }
            }
        }

        // Habits Section
        item {
            SectionTitleWithIcon(
                icon = Icons.Default.CheckCircle,
                title = "Kebiasaan (${displayedHabits.size})"
            )
        }

        if (displayedHabits.isEmpty()) {
            item {
                EmptyDetailCard("Belum ada catatan kebiasaan pada filter ini.")
            }
        } else {
            items(displayedHabits) { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.title,
                                fontWeight = FontWeight.SemiBold,
                                color = NaturalDark,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "${habit.dateString} • ${if (habit.isBadHabit) "Pemicu Kotor" else "Rutinitas Baik"}",
                                fontSize = 11.5.sp,
                                color = SageMedium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (habit.isCompleted) {
                                if (habit.isBadHabit) Color(0xFFFEE2E2) else HealthSuccessBg
                            } else Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = if (habit.isCompleted) {
                                    if (habit.isBadHabit) "Terjadi" else "Selesai"
                                } else "Belum",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (habit.isCompleted) {
                                    if (habit.isBadHabit) Color(0xFFDC2626) else HealthSuccess
                                } else SageMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitleWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = NaturalPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = NaturalDark,
                fontSize = 14.5.sp
            )
        )
    }
}

@Composable
private fun EmptyDetailCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = message,
            color = SageMedium,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(14.dp)
        )
    }
}
