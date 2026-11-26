package com.example.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundEffectManager
import com.example.ui.components.CustomHabitIcon
import com.example.ui.components.DaylioMoodTrackerSheet
import com.example.ui.components.GiantForestToothHero
import com.example.ui.components.InteractiveToothVisual
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.HealthSuccessBg
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import com.example.ui.viewmodel.DentallyViewModel

data class GamificationBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconKey: String,
    val isUnlocked: Boolean,
    val howToUnlock: String
)

@Composable
fun ToothGamificationScreen(
    viewModel: DentallyViewModel,
    onNavigateToHomeTimer: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentDayMood by viewModel.currentDayMood.collectAsStateWithLifecycle()
    val allMoods by viewModel.allMoods.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val timerCompletedToday by viewModel.timerCompletedToday.collectAsStateWithLifecycle()

    val gamificationStatus = viewModel.getGamificationStatus()

    var showMoodSheet by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<GamificationBadge?>(null) }

    val badges = remember(habits, gamificationStatus, timerCompletedToday) {
        val flossCompleted = habits.any {
            !it.isBadHabit && it.isCompleted &&
                (it.title.contains("Benang", ignoreCase = true) ||
                    it.title.contains("Floss", ignoreCase = true) ||
                    it.title.contains("Sela", ignoreCase = true))
        }
        listOf(
            GamificationBadge(
                id = "badge_streak",
                title = "Rajin 7 Hari",
                subtitle = "${gamificationStatus.streakDays}/7 Hari",
                iconKey = "trofi",
                isUnlocked = gamificationStatus.streakDays >= 7,
                howToUnlock = "Rutin merawat gigi setiap hari selama 7 hari berturut-turut."
            ),
            GamificationBadge(
                id = "badge_timer",
                title = "Juara 2 Menit",
                subtitle = if (timerCompletedToday) "Selesai!" else "Pakai Timer",
                iconKey = "timer",
                isUnlocked = timerCompletedToday,
                howToUnlock = "Selesaikan Timer Sikat Gigi 2 Menit di halaman Beranda."
            ),
            GamificationBadge(
                id = "badge_clean",
                title = "Mulut Super Bersih",
                subtitle = "${gamificationStatus.toothHealthPercent}% Bersih",
                iconKey = "diamond",
                isUnlocked = gamificationStatus.toothHealthPercent >= 85,
                howToUnlock = "Capai Tingkat Kebersihan Mulut minimal 85% hari ini."
            ),
            GamificationBadge(
                id = "badge_floss",
                title = "Andalan Benang Gigi",
                subtitle = if (flossCompleted) "Tercapai" else "Sela Gigi",
                iconKey = "floss",
                isUnlocked = flossCompleted,
                howToUnlock = "Centang kebiasaan membersihkan sela gigi pakai benang gigi hari ini."
            ),
            GamificationBadge(
                id = "badge_clean_habits",
                title = "Bebas Kebiasaan Buruk",
                subtitle = if (gamificationStatus.activeBadHabitsCount == 0) "Aman!" else "Kurangi Manis",
                iconKey = "shield",
                isUnlocked = gamificationStatus.activeBadHabitsCount == 0,
                howToUnlock = "Hindari kebiasaan pemicu gigi kotor (manis/soda/rokok) sepanjang hari."
            ),
            GamificationBadge(
                id = "badge_smile",
                title = "Senyum Berkilau",
                subtitle = "Target 90%",
                iconKey = "brush",
                isUnlocked = gamificationStatus.toothHealthPercent >= 90,
                howToUnlock = "Lengkapi hampir semua kebiasaan baik dan capai skor kebersihan 90% ke atas."
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Minimalist Center Highlighted Header
        item {
            Column {
                Text(
                    text = "Gigi Ku",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NaturalDark,
                        fontSize = 23.sp
                    )
                )
                Text(
                    text = "Status kesehatan gigi & koleksimu hari ini",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SageMedium,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        // 1b. Bold Minimalist Status Banner (Dentally Forest Green Hero Card)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Status Kebersihan Mulut",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "KEBERSIHAN MULUT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.4.sp,
                            fontSize = 11.5.sp
                        )
                    )

                    Text(
                        text = gamificationStatus.toothStage.uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${gamificationStatus.toothHealthPercent}% Bersih • ${gamificationStatus.streakDays} Hari Streak",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
            }
        }

        // 2. Giant Interactive Forest Tooth Hero (Highlighted Center Feature)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.padding(vertical = 16.dp)) {
                    GiantForestToothHero(
                        userName = userProfile.userName,
                        healthPercent = gamificationStatus.toothHealthPercent,
                        streakDays = gamificationStatus.streakDays,
                        stageName = gamificationStatus.toothStage,
                        levelNumber = (gamificationStatus.toothHealthPercent / 20) + 1,
                        todayMoodScore = currentDayMood?.moodScore,
                        onStartTimer = onNavigateToHomeTimer,
                        onOpenMoodSheet = { showMoodSheet = true },
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToBrowser = onNavigateToBrowser
                    )
                }
            }
        }

        // 3. Tingkat Kebersihan Mulut Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cleanliness_status_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tingkat Kebersihan Mulut",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalDark,
                                    fontSize = 16.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gamificationStatus.toothStage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = NaturalPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = gamificationStatus.plaqueLevel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SageMedium,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        InteractiveToothVisual(
                            healthPercent = gamificationStatus.toothHealthPercent,
                            sizeDp = 76.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val animatedPercent by animateIntAsState(
                        targetValue = gamificationStatus.toothHealthPercent,
                        animationSpec = tween(durationMillis = 600),
                        label = "gamif_health_percent"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = gamificationStatus.levelName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SageDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "$animatedPercent%",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = NaturalPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { gamificationStatus.toothHealthPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = NaturalPrimary,
                        trackColor = DentallyBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatusPill(
                            label = "Kebiasaan Baik",
                            value = "${gamificationStatus.completedGoodHabits}/${gamificationStatus.totalGoodHabits}",
                            bgColor = HealthSuccessBg,
                            textColor = HealthSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatusPill(
                            label = "Streak",
                            value = "${gamificationStatus.streakDays} Hari",
                            bgColor = Color(0xFFFFF8E1),
                            textColor = Color(0xFF9A6700),
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatusPill(
                            label = "Pemicu Kotor",
                            value = "${gamificationStatus.activeBadHabitsCount}",
                            bgColor = if (gamificationStatus.activeBadHabitsCount == 0) HealthSuccessBg else Color(0xFFFEE2E2),
                            textColor = if (gamificationStatus.activeBadHabitsCount == 0) HealthSuccess else Color(0xFFDC2626),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Koleksi Lencana Kamu (2-column grid, Zero OHI-S)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Koleksi Lencana Kamu",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalDark,
                            fontSize = 16.5.sp
                        )
                    )
                }

                badges.chunked(2).forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeGridItem(
                                badge = badge,
                                onClick = {
                                    SoundEffectManager.playPop()
                                    selectedBadge = badge
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 5. Everyday Indonesian Education Card 1
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Kesehatan Tubuh",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mulut Bersih, Badan Ikut Bugar",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Merawat gigi dan gusi setiap hari menjaga tubuh tetap sehat. Butuh tips cepat? Tanya langsung ke Dentally!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageDark,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToChat,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanya Dentally",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Everyday Indonesian Education Card 2
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DentallyTintBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Pencegahan",
                                tint = NaturalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "3 Kebiasaan Anti-Masalah Gigi",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 15.sp
                            )
                        )
                    }

                    SimplePreventionRow(
                        title = "1. Cegah Gigi Berlubang",
                        description = "Sikat gigi 2 menit sebelum tidur dan kumur air putih setelah ngemil manis."
                    )
                    SimplePreventionRow(
                        title = "2. Jaga Gusi Tidak Berdarah",
                        description = "Pakai sikat berbulu lembut dan bersihkan sela gigi secara rutin."
                    )
                    SimplePreventionRow(
                        title = "3. Napas Tetap Segar",
                        description = "Sikat lembut permukaan lidah dan cukupi minum air putih."
                    )
                }
            }
        }
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

    if (selectedBadge != null) {
        val badge = selectedBadge!!
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            title = {
                Text(
                    text = badge.title,
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (badge.isUnlocked) "Status: Lencana Sudah Terbuka!" else "Status: Belum Terbuka",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (badge.isUnlocked) NaturalPrimary else Color(0xFFB45309),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = badge.howToUnlock,
                        style = MaterialTheme.typography.bodyMedium.copy(color = SageDark)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedBadge = null
                        onNavigateToHomeTimer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintPrimary)
                ) {
                    Text("Mulai Sikat Gigi (2m)")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("Tutup", color = NaturalMedium)
                }
            }
        )
    }
}

@Composable
private fun MiniStatusPill(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor.copy(alpha = 0.85f),
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BadgeGridItem(
    badge: GamificationBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("badge_${badge.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) Color.White else Color(0xFFF6ECEE)
        ),
        border = BorderStroke(1.dp, if (badge.isUnlocked) DentallyBorder else Color(0xFFEAD9DC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) DentallyTintBg else Color(0xFFEAD9DC)),
                contentAlignment = Alignment.Center
            ) {
                CustomHabitIcon(iconKey = badge.iconKey, sizeDp = 28.dp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (badge.isUnlocked) NaturalDark else SageMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (badge.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) NaturalPrimary else NaturalMedium,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = badge.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (badge.isUnlocked) NaturalPrimary else NaturalMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SimplePreventionRow(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DentallyTintBg)
            .border(1.dp, DentallyBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = NaturalPrimary,
                fontSize = 13.5.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = SageDark,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        )
    }
}
