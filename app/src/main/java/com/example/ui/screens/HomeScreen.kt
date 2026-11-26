package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.audio.SoundEffectManager
import com.example.data.local.DailyMoodEntity
import com.example.data.local.HabitEntity
import com.example.data.repository.GamificationStatus
import com.example.ui.components.CircularProgressSummary
import com.example.ui.components.CustomHabitIcon
import com.example.ui.components.CustomStatusCheckbox
import com.example.ui.components.DAYLIO_TOOTH_MOODS
import com.example.ui.components.DailyQuoteCard
import com.example.ui.components.DaylioMoodSelectorRow
import com.example.ui.components.DaylioMoodTrackerSheet
import com.example.ui.components.DentallyLogoIcon
import com.example.ui.components.HomeSyncSkeletonView
import com.example.ui.components.MonthlyHabitCalendar
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.WeeklyCalendarStrip
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.HealthSuccessBg
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPill
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryDark
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import com.example.ui.viewmodel.BrushingQuadrant
import com.example.ui.viewmodel.DentallyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: DentallyViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToGamification: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToDeveloper: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val allHabits by viewModel.allHabits.collectAsStateWithLifecycle()
    val completedDates by viewModel.completedDates.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentDayMood by viewModel.currentDayMood.collectAsStateWithLifecycle()
    val allMoods by viewModel.allMoods.collectAsStateWithLifecycle()

    val timerSecondsLeft by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val currentQuadrant by viewModel.currentQuadrant.collectAsStateWithLifecycle()
    val timerCompletedToday by viewModel.timerCompletedToday.collectAsStateWithLifecycle()

    val quote by viewModel.dailyQuote.collectAsStateWithLifecycle()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsStateWithLifecycle()
    val gamificationStatus = viewModel.getGamificationStatus()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var addHabitInitialBad by remember { mutableStateOf(false) }
    var showMoodSheet by remember { mutableStateOf(false) }
    var showProgressBreakdownDialog by remember { mutableStateOf(false) }
    var showStreakDetailDialog by remember { mutableStateOf(false) }
    var showMonthlyCalendar by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    val formattedHeaderDate = remember(selectedDate) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(selectedDate) ?: Date()
            val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            formatter.format(date)
        } catch (_: Exception) {
            "Hari ini"
        }
    }

    val isTodaySelected = selectedDate == viewModel.todayDateString

    val isInitialCloudSyncing = isSyncingCloud && habits.isEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isInitialCloudSyncing,
            transitionSpec = {
                fadeIn(tween(300, easing = FastOutSlowInEasing))
                    .togetherWith(fadeOut(tween(220, easing = FastOutSlowInEasing)))
            },
            label = "home_sync_transition"
        ) { syncing ->
            if (syncing) {
                HomeSyncSkeletonView()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NaturalBackground),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 0. Clean Minimal Top Bar (Dentally Logo + Quick Audio/Demo Icon Controls)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DentallyLogoIcon(sizeDp = 28.dp, isSelected = false)
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = NaturalPrimary, fontWeight = FontWeight.ExtraBold)) {
                                            append("DENT")
                                        }
                                        withStyle(SpanStyle(color = NaturalPill, fontWeight = FontWeight.ExtraBold)) {
                                            append("ALLY")
                                        }
                                    },
                                    fontSize = 19.sp,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleSfxEnabled() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("home_sfx_toggle_btn")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, DentallyBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (userProfile.sfxEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                            contentDescription = "Suara SFX",
                                            tint = if (userProfile.sfxEnabled) NaturalPrimary else SageMedium,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.openOnboardingDemo() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("home_demo_guide_btn")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, DentallyBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = "Panduan Demo",
                                            tint = NaturalPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1. Modern Welcome Card with Left Crimson Accent Bar & 2 Interactive Metric Tiles
                    item {
                        StaggeredEntrance(index = 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onNavigateToProfile() }
                                .testTag("user_profile_header_card"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, DentallyBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                // Signature subtle Dentally crimson vertical accent bar
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .fillMaxHeight()
                                        .background(NaturalPrimary)
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 18.dp, vertical = 18.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Halo, ${userProfile.userName}!",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = NaturalDark,
                                                    fontSize = 21.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "SENYUMMU, INVESTASIMU",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = NaturalPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    letterSpacing = 0.8.sp
                                                )
                                            )
                                        }

                                        if (userProfile.avatarUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = userProfile.avatarUrl,
                                                contentDescription = "Foto Profil",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .border(1.5.dp, DentallyBorder, CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(DentallyTintBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Profil",
                                                    tint = NaturalPrimary,
                                                    modifier = Modifier.size(21.dp)
                                                )
                                            }
                                        }
                                    }

                                    // 2 Crisp Interactive Metric Tiles (Tap for Detail Popups!)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val isHealthGreat = gamificationStatus.toothHealthPercent >= 85
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isHealthGreat) HealthSuccessBg else DentallyTintBg)
                                                .clickable {
                                                    SoundEffectManager.playPop()
                                                    showProgressBreakdownDialog = true
                                                }
                                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                                .testTag("health_metric_tile")
                                        ) {
                                            Column {
                                                Text(
                                                    text = "KEBERSIHAN MULUT",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = SageMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${gamificationStatus.toothHealthPercent}%",
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        color = if (isHealthGreat) HealthSuccess else NaturalDark,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 20.sp
                                                    )
                                                )
                                                Text(
                                                    text = "${gamificationStatus.toothStage} →",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isHealthGreat) HealthSuccess else NaturalPrimary,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 11.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0xFFFAF3E6))
                                                .clickable {
                                                    SoundEffectManager.playPop()
                                                    showStreakDetailDialog = true
                                                }
                                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                                .testTag("streak_metric_tile")
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocalFireDepartment,
                                                        contentDescription = "Detail Streak",
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "STREAK SIKAT",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = SageMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp,
                                                            letterSpacing = 0.5.sp
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${gamificationStatus.streakDays} Hari",
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        color = Color(0xFF9A6700),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 20.sp
                                                    )
                                                )
                                                Text(
                                                    text = "Ketuk lihat detail →",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFFB45309),
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 11.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }

            // 1b. Compact Developer Monitoring Pill (Only visible for fight.vanzor30@gmail.com)
            if (userProfile.isDeveloper) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToDeveloper() }
                            .testTag("developer_panel_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A0E17)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Panel Developer",
                                    tint = Color(0xFFFECDD3),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Mode Developer",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.5.sp
                                    )
                                )
                            }
                            Text(
                                text = "Monitor →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFECDD3),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. HERO UTAMA BERANDA: Bold Solid Dentally Green Action Card (Expandable Timer!)
            item {
                StaggeredEntrance(index = 1) {
                    BrushingTimerHeroCard(
                        secondsLeft = timerSecondsLeft,
                        isRunning = isTimerRunning,
                        currentQuadrant = currentQuadrant,
                        completedToday = timerCompletedToday,
                        onToggleStartPause = {
                            if (isTimerRunning) {
                                viewModel.pauseTimer()
                            } else {
                                if (timerSecondsLeft <= 0) {
                                    viewModel.resetTimer()
                                }
                                viewModel.startTimer()
                            }
                        },
                        onReset = { viewModel.resetTimer() },
                        onNavigateToGamification = onNavigateToGamification,
                        onNavigateToChat = onNavigateToChat
                    )
                }
            }

            // 3. Daylio-Style Tooth Mood Tracker Card (Overlap Fixed)
            item {
                StaggeredEntrance(index = 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daylio_mood_quick_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, DentallyBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mood,
                                    contentDescription = "Mood Gigi",
                                    tint = NaturalPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mood Gigi Hari Ini",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalDark,
                                        fontSize = 14.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DentallyTintBg)
                                    .clickable {
                                        SoundEffectManager.playPop()
                                        showMoodSheet = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("mood_recap_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Insights,
                                        contentDescription = "Rekap",
                                        tint = NaturalPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Rekap & Detail",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NaturalPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        DaylioMoodSelectorRow(
                            selectedMoodScore = currentDayMood?.moodScore ?: 4,
                            onMoodSelected = { score ->
                                val moodInfo = DAYLIO_TOOTH_MOODS.first { it.score == score }
                                val entity = DailyMoodEntity(
                                    dateString = selectedDate,
                                    moodScore = score,
                                    moodTitle = moodInfo.title,
                                    dentalFeel = moodInfo.dentalFeel,
                                    note = currentDayMood?.note ?: "",
                                    activityTags = currentDayMood?.activityTags ?: "sikat_pagi,sikat_malam",
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.saveDailyMood(entity)
                            }
                        )

                        val currentMoodInfo = DAYLIO_TOOTH_MOODS.firstOrNull { it.score == (currentDayMood?.moodScore ?: 4) }
                            ?: DAYLIO_TOOTH_MOODS[1]

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(currentMoodInfo.color.copy(alpha = 0.09f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Tips",
                                    tint = currentMoodInfo.color,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Status: ${currentMoodInfo.title} • ${currentMoodInfo.dentalFeel}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = currentMoodInfo.color,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
                }
            }

            // 4. Daily Quote Card
            item {
                StaggeredEntrance(index = 3) {
                    DailyQuoteCard(
                        quote = quote.quote,
                        author = quote.author,
                        tip = quote.tip,
                        onClick = { viewModel.nextQuote() }
                    )
                }
            }

            // 5. Circular Summary Card (Tingkat Kebersihan Mulut)
            item {
                val goodHabits = habits.filter { !it.isBadHabit }
                val completedGoodCount = goodHabits.count { it.isCompleted }
                val totalGoodCount = goodHabits.size.coerceAtLeast(1)
                val activeBadCount = habits.count { it.isBadHabit && it.isCompleted }

                CircularProgressSummary(
                    completedCount = completedGoodCount,
                    totalCount = totalGoodCount,
                    activeBadHabitsCount = activeBadCount,
                    onClick = { showProgressBreakdownDialog = true }
                )
            }

            // 6. Weekly / Monthly Calendar Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kalender Rutinitas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 16.sp
                            )
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                                .border(1.dp, DentallyBorder, RoundedCornerShape(50))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (!showMonthlyCalendar) NaturalPrimary else Color.Transparent)
                                    .clickable {
                                        SoundEffectManager.playPop()
                                        showMonthlyCalendar = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("home_cal_weekly_tab")
                            ) {
                                Text(
                                    text = "Mingguan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (!showMonthlyCalendar) Color.White else SageDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (showMonthlyCalendar) NaturalPrimary else Color.Transparent)
                                    .clickable {
                                        SoundEffectManager.playPop()
                                        showMonthlyCalendar = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("home_cal_monthly_tab")
                            ) {
                                Text(
                                    text = "Bulanan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (showMonthlyCalendar) Color.White else SageDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    val futureDateLockToast: () -> Unit = {
                        Toast.makeText(
                            context,
                            "Tanggal belum tiba — baru bisa diisi hari H ya!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (showMonthlyCalendar) {
                        MonthlyHabitCalendar(
                            habits = allHabits,
                            moods = allMoods,
                            selectedDate = selectedDate,
                            onDateSelected = { viewModel.setSelectedDate(it) },
                            onFutureDateBlocked = futureDateLockToast,
                            title = "Progres Sikat Gigi Bulanan"
                        )
                    } else {
                        WeeklyCalendarStrip(
                            selectedDateString = selectedDate,
                            onDateSelected = {
                                SoundEffectManager.playPop()
                                viewModel.setSelectedDate(it)
                            },
                            completedDates = completedDates,
                            showHeader = false,
                            onFutureDateBlocked = futureDateLockToast
                        )
                    }
                }
            }

            // 7. Habits Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTodaySelected) "Kebiasaan Hari Ini" else formattedHeaderDate,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "Centang perawatan harianmu di sini.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageMedium,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DentallyTintBg)
                            .border(1.dp, DentallyBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                SoundEffectManager.playPop()
                                addHabitInitialBad = false
                                showAddHabitDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("add_custom_habit_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Kebiasaan",
                                tint = NaturalPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tambah",
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

            // Good habits vs Bad habits partition
            val goodHabitsList = habits.filter { !it.isBadHabit }
            val badHabitsList = habits.filter { it.isBadHabit }

            if (goodHabitsList.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        CustomHabitIcon(iconKey = "brush", sizeDp = 16.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kebiasaan Baik (Biar Gigi Bersih & Cerah)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SageDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                items(goodHabitsList, key = { it.id }) { habit ->
                    HabitCardItem(
                        habit = habit,
                        onToggle = { viewModel.toggleHabit(habit) },
                        onDelete = { habitToDelete = habit }
                    )
                }
            }

            // Always show Bad Habits header with direct "+ Tambah" pill so users can add custom habits easily
            item {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            CustomHabitIcon(iconKey = "candy", isBadHabit = true, sizeDp = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kebiasaan yang Bikin Gigi Kotor",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(9.dp))
                                .clickable {
                                    SoundEffectManager.playPop()
                                    addHabitInitialBad = true
                                    showAddHabitDialog = true
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("add_bad_habit_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Kebiasaan Buruk",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Tambah",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFB45309),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tandai atau tambahkan kebiasaan pemicu plak (kopi, boba, ngemil manis, dll).",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageMedium,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            if (badHabitsList.isNotEmpty()) {
                items(badHabitsList, key = { it.id }) { habit ->
                    HabitCardItem(
                        habit = habit,
                        onToggle = { viewModel.toggleHabit(habit) },
                        onDelete = { habitToDelete = habit }
                    )
                }
            }

            if (habits.isEmpty()) {
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
                            CustomHabitIcon(iconKey = "brush", sizeDp = 36.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada kebiasaan untuk tanggal ini",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SageMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
                }
            }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            initialIsBadHabit = addHabitInitialBad,
            onDismiss = { showAddHabitDialog = false },
            onAdd = { title, subtitle, emoji, category, isBad ->
                viewModel.addCustomHabit(
                    title = title,
                    subtitle = subtitle,
                    emoji = emoji,
                    category = category,
                    isBadHabit = isBad
                )
                showAddHabitDialog = false
            }
        )
    }

    if (showStreakDetailDialog) {
        StreakDetailDialog(
            status = gamificationStatus,
            allHabits = allHabits,
            todayDateString = viewModel.todayDateString,
            timerCompletedToday = timerCompletedToday,
            onDismiss = { showStreakDetailDialog = false },
            onStartTimer = {
                showStreakDetailDialog = false
                viewModel.startTimer()
            }
        )
    }

    if (showMoodSheet) {
        DaylioMoodTrackerSheet(
            dateString = selectedDate,
            currentMood = currentDayMood,
            allMoods = allMoods,
            onSaveMood = { mood -> viewModel.saveDailyMood(mood) },
            onDismiss = { showMoodSheet = false }
        )
    }

    if (showProgressBreakdownDialog) {
        val goodHabits = habits.filter { !it.isBadHabit }
        val completedGoodCount = goodHabits.count { it.isCompleted }
        val totalGoodCount = goodHabits.size.coerceAtLeast(1)
        val activeBadCount = habits.count { it.isBadHabit && it.isCompleted }

        AlertDialog(
            onDismissRequest = { showProgressBreakdownDialog = false },
            title = {
                Text(
                    text = "Rangkuman Kebersihan Mulut",
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Status: ${gamificationStatus.toothStage} (${gamificationStatus.toothHealthPercent}%)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary
                        )
                    )
                    Text(
                        text = "• Kebiasaan Baik Selesai: $completedGoodCount dari $totalGoodCount\n" +
                                "• Kebiasaan Pemicu Kotor: $activeBadCount (${if (activeBadCount == 0) "Hebat! Tetap bersih" else "Perlu perhatian!"})\n" +
                                "• Kondisi Kebersihan: ${gamificationStatus.plaqueLevel}\n" +
                                "• Streak Berturut-turut: ${gamificationStatus.streakDays} Hari",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageDark,
                            lineHeight = 18.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProgressBreakdownDialog = false
                        onNavigateToGamification()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
                ) {
                    Text("Lihat Gigi Ku")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProgressBreakdownDialog = false }) {
                    Text("Tutup", color = NaturalMedium)
                }
            }
        )
    }

    if (habitToDelete != null) {
        val target = habitToDelete!!
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = {
                Text(
                    text = "Hapus Kebiasaan?",
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark
                )
            },
            text = {
                Text(
                    text = "Apakah kamu ingin menghapus '${target.title}' dari daftar kebiasaan hari ini?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SageDark)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(target.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Batal", color = NaturalMedium)
                }
            }
        )
    }
}

@Composable
fun BrushingTimerHeroCard(
    secondsLeft: Int,
    isRunning: Boolean,
    currentQuadrant: BrushingQuadrant,
    completedToday: Boolean,
    onToggleStartPause: () -> Unit,
    onReset: () -> Unit,
    onNavigateToGamification: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    var showTips by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val showDetailedRing = isExpanded || isRunning || secondsLeft < 120
    val progress = (120 - secondsLeft).coerceIn(0, 120) / 120f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 450),
        label = "timer_ring_progress"
    )

    val mins = (secondsLeft / 60).coerceAtLeast(0)
    val secs = (secondsLeft % 60).coerceAtLeast(0)
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    val isFinished = secondsLeft <= 0

    // Bold Modern Crimson Hero Card — crisp & clean when idle, expands smoothly without overlap
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_timer_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NaturalPrimaryDark,
                            NaturalPrimary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (!isRunning && secondsLeft == 120) {
                                    isExpanded = !isExpanded
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFinished || completedToday) Icons.Default.CheckCircle else Icons.Default.Timer,
                                contentDescription = "Timer Sikat Gigi",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Mulai Sikat Gigi",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 18.5.sp
                                )
                            )
                            Text(
                                text = if (isFinished || completedToday) {
                                    "Target 2 menit selesai hari ini"
                                } else {
                                    "Timer $formattedTime • Panduan 4 area"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    if (!showDetailedRing) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable {
                                    isExpanded = true
                                    onToggleStartPause()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("toggle_timer_button")
                        ) {
                            Text(
                                text = "Mulai",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = NaturalPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showDetailedRing,
                    enter = fadeIn(tween(240)) + expandVertically(tween(260, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(180)) + shrinkVertically(tween(200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Circular Countdown Ring on clean light surface insert — spacious & zero overlap
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(168.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val stroke = 12.dp.toPx()
                                        drawCircle(
                                            color = DentallyBorder,
                                            style = Stroke(width = stroke)
                                        )
                                        drawArc(
                                            color = if (isFinished) HealthSuccess else NaturalPrimary,
                                            startAngle = -90f,
                                            sweepAngle = 360f * animatedProgress,
                                            useCenter = false,
                                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (isFinished) "Selesai!" else formattedTime,
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isFinished) HealthSuccess else NaturalDark,
                                                fontSize = 32.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "AREA ${currentQuadrant.ordinal + 1}/4",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isFinished) HealthSuccess else NaturalPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quadrant Title moved cleanly below the ring so long names never overlap the ring
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isFinished) HealthSuccessBg else DentallyTintBg)
                                        .testTag("timer_quadrant_pill")
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = currentQuadrant.quadrantName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isFinished) HealthSuccess else NaturalPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = currentQuadrant.instruction,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SageDark,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onToggleStartPause,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isRunning) Color(0xFFD97706) else NaturalPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("toggle_timer_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when {
                                                isRunning -> "Jeda"
                                                isFinished -> "Ulangi (2m)"
                                                secondsLeft < 120 -> "Lanjutkan"
                                                else -> "Mulai"
                                            },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onReset()
                                            isExpanded = false
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, DentallyBorder),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("reset_timer_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reset Timer",
                                            tint = NaturalDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset", color = NaturalDark, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { showTips = !showTips }) {
                                        Text(
                                            text = if (showTips) "Tutup Tips" else "Tips Sikat Gigi",
                                            color = NaturalPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Row {
                                        TextButton(onClick = onNavigateToGamification) {
                                            Text("Gigi Ku →", color = SageDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        TextButton(onClick = onNavigateToChat) {
                                            Text("Tanya AI", color = Color(0xFF9A6700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = showTips) {
                                    Text(
                                        text = "Pakai sikat lembut, pasta sebesar biji jagung, dan sikat memutar pelan ke arah gusi.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SageDark,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DentallyTintBg)
                                            .padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitCardItem(
    habit: HabitEntity,
    onToggle: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isBad = habit.isBadHabit
    val cardScale by animateFloatAsState(
        targetValue = if (habit.isCompleted) 1.015f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 450f),
        label = "habit_card_spring"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable { onToggle() }
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBad && habit.isCompleted) Color(0xFFFEF2F2) else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isBad && habit.isCompleted) Color(0xFFFECACA) else DentallyBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            when {
                                isBad && habit.isCompleted -> Color(0xFFFEE2E2)
                                isBad -> Color(0xFFFEF3C7)
                                habit.isCompleted -> HealthSuccessBg
                                else -> DentallyTintBg
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CustomHabitIcon(
                        iconKey = if (habit.iconEmoji.isNotBlank()) habit.iconEmoji else habit.title,
                        isBadHabit = isBad,
                        sizeDp = 28.dp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBad && habit.isCompleted) Color(0xFF991B1B) else NaturalDark,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = habit.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isBad && habit.isCompleted) Color(0xFFB91C1C) else NaturalMedium,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isBad) {
                    if (habit.isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Gigi Kotor",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                            CustomStatusCheckbox(
                                checked = true,
                                isBadHabit = true,
                                onToggle = onToggle
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Terhindar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HealthSuccess,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                            CustomStatusCheckbox(
                                checked = false,
                                isBadHabit = true,
                                onToggle = onToggle
                            )
                        }
                    }
                } else {
                    if (habit.isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Selesai",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = HealthSuccess,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                CustomHabitIcon(
                                    iconKey = "brush",
                                    sizeDp = 14.dp
                                )
                            }
                            CustomStatusCheckbox(
                                checked = true,
                                isBadHabit = false,
                                onToggle = onToggle
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Belum Selesai",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NaturalMedium,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                            CustomStatusCheckbox(
                                checked = false,
                                isBadHabit = false,
                                onToggle = onToggle
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Kebiasaan",
                            tint = Color(0xFFB8A4A7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakDetailDialog(
    status: GamificationStatus,
    allHabits: List<HabitEntity>,
    todayDateString: String,
    timerCompletedToday: Boolean,
    onDismiss: () -> Unit,
    onStartTimer: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dayShortFormat = remember { SimpleDateFormat("EEEEE", Locale("id", "ID")) }

    val last7Days = remember(allHabits, todayDateString, timerCompletedToday) {
        val list = mutableListOf<Triple<String, String, Boolean>>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val dStr = dateFormat.format(cal.time)
            val shortLabel = dayShortFormat.format(cal.time).uppercase()
            val done = allHabits.any { it.dateString == dStr && !it.isBadHabit && it.isCompleted } ||
                (dStr == todayDateString && timerCompletedToday)
            list.add(Triple(dStr, shortLabel, done))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val nextMilestone = if (status.streakDays < 7) 7 else if (status.streakDays < 14) 14 else 30
    val milestoneProgress = (status.streakDays.toFloat() / nextMilestone.toFloat()).coerceIn(0f, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("streak_detail_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Detail Streak Sikat Gigi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NaturalDark
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFAF3E6))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("STREAK SAAT INI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SageMedium)
                            Text("${status.streakDays} Hari", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF9A6700))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DentallyTintBg)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("TOTAL SESI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SageMedium)
                            Text("${status.totalBrushingSessions} Sesi", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NaturalPrimary)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Berikutnya ($nextMilestone Hari)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NaturalDark
                        )
                        Text(
                            text = "${status.streakDays}/$nextMilestone",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { milestoneProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = NaturalPrimary,
                        trackColor = DentallyBorder
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Konsistensi 7 Hari Terakhir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NaturalDark
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        last7Days.forEach { (_, label, done) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (done) HealthSuccess else DentallyTintBg)
                                        .border(1.dp, if (done) HealthSuccess else DentallyBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (done) "✓" else "•",
                                        color = if (done) Color.White else NaturalMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(label, fontSize = 10.sp, color = SageMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onStartTimer,
                colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
            ) {
                Text("Mulai Sikat Gigi (2m)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = NaturalMedium)
            }
        }
    )
}

@Composable
fun AddHabitDialog(
    initialIsBadHabit: Boolean = false,
    onDismiss: () -> Unit,
    onAdd: (title: String, subtitle: String, emoji: String, category: String, isBadHabit: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var isBadHabit by remember(initialIsBadHabit) { mutableStateOf(initialIsBadHabit) }
    var selectedIconKey by remember(initialIsBadHabit) { mutableStateOf(if (initialIsBadHabit) "candy" else "brush") }

    val goodHabitIcons = listOf(
        "brush" to "Sikat Gigi",
        "night_brush" to "Sikat Malam",
        "floss" to "Benang Gigi",
        "water" to "Kumur Air",
        "tongue" to "Lidah",
        "apple" to "Apel Sehat",
        "milk" to "Susu Kalsium",
        "tracker" to "Catatan",
        "book" to "Baca"
    )

    val badHabitIcons = listOf(
        "candy" to "Makanan Manis",
        "coffee" to "Kopi / Teh",
        "soda" to "Boba / Soda",
        "smoke" to "Merokok"
    )

    val activeIcons = if (isBadHabit) badHabitIcons else goodHabitIcons

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBadHabit) "Tambah Kebiasaan Buruk" else "Tambah Kebiasaan Baik",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isBadHabit) Color(0xFFB45309) else NaturalDark
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DentallyTintBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBadHabit) HealthSuccess else Color.Transparent)
                            .clickable {
                                isBadHabit = false
                                selectedIconKey = "brush"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Baik (Bersih)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (!isBadHabit) Color.White else NaturalDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBadHabit) Color(0xFFDC2626) else Color.Transparent)
                            .clickable {
                                isBadHabit = true
                                selectedIconKey = "candy"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Buruk (Kotor)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isBadHabit) Color.White else NaturalDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(if (isBadHabit) "Nama (cth: Minum Boba / Manis)" else "Nama (cth: Sikat Gigi Siang)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = {
                        Text(if (isBadHabit) "Efek (cth: Bikin gigi mudah berlubang)" else "Keterangan (cth: Sehari-hari)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Pilih Ikon Ilustrasi:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = NaturalDark
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeIcons.take(5).forEach { (iconKey, _) ->
                        val isSelected = selectedIconKey == iconKey
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        if (isBadHabit) Color(0xFFFEE2E2) else HealthSuccessBg
                                    } else DentallyTintBg
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = if (isBadHabit) Color(0xFFDC2626) else HealthSuccess,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else Modifier
                                )
                                .clickable { selectedIconKey = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            CustomHabitIcon(
                                iconKey = iconKey,
                                isBadHabit = isBadHabit,
                                sizeDp = 24.dp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            title,
                            if (subtitle.isBlank()) (if (isBadHabit) "Bikin gigi kotor" else "Sehari-hari") else subtitle,
                            selectedIconKey,
                            if (isBadHabit) "Kebiasaan Buruk" else "Kustom",
                            isBadHabit
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBadHabit) Color(0xFFDC2626) else NaturalPrimary
                ),
                enabled = title.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = NaturalMedium)
            }
        }
    )
}
