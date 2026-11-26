package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyMoodEntity
import com.example.ui.theme.AccentGold
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

data class ActivityTagItem(
    val id: String,
    val label: String,
    val iconKey: String,
    val isBad: Boolean = false
)

val DENTAL_ACTIVITY_TAGS = listOf(
    ActivityTagItem("sikat_pagi", "Sikat Pagi", "brush"),
    ActivityTagItem("sikat_malam", "Sikat Malam", "night_brush"),
    ActivityTagItem("floss", "Benang Gigi", "floss"),
    ActivityTagItem("kumur", "Kumur Air", "kumur"),
    ActivityTagItem("lidah", "Sikat Lidah", "lidah"),
    ActivityTagItem("apel_sehat", "Makan Sehat", "apel"),
    ActivityTagItem("susu", "Susu Kalsium", "susu"),
    ActivityTagItem("manis", "Makan Manis", "candy", isBad = true),
    ActivityTagItem("kopi", "Kopi / Teh", "coffee", isBad = true),
    ActivityTagItem("soda", "Minuman Soda", "soda", isBad = true),
    ActivityTagItem("linu", "Gigi Linu", "shield", isBad = true),
    ActivityTagItem("gusi_darah", "Gusi Sensitif", "shield", isBad = true)
)

/**
 * Complete Daylio Mood Tracker & Recap Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DaylioMoodTrackerSheet(
    dateString: String,
    currentMood: DailyMoodEntity?,
    allMoods: List<DailyMoodEntity>,
    onSaveMood: (DailyMoodEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedMoodScore by remember(currentMood) {
        mutableIntStateOf(currentMood?.moodScore ?: 4) // Default "Baik"
    }

    var selectedTags by remember(currentMood) {
        val initialTags = currentMood?.activityTags?.split(",")?.filter { it.isNotBlank() }?.toSet()
            ?: setOf("sikat_pagi", "sikat_malam")
        mutableStateOf(initialTags)
    }

    var noteText by remember(currentMood) {
        mutableStateOf(currentMood?.note ?: "")
    }

    var showRecapTab by remember { mutableStateOf(false) }

    val activeMoodInfo = DAYLIO_TOOTH_MOODS.first { it.score == selectedMoodScore }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NaturalBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (showRecapTab) "📊 Rekap Mood & Gigi (Daylio)" else "😊 Mood & Sensasi Gigi Hari Ini",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalDark,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "Tanggal: $dateString",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageMedium,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Toggle Recap Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (showRecapTab) NaturalPrimary else DentallyTintBg)
                            .clickable { showRecapTab = !showRecapTab }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (showRecapTab) "Input" else "Rekap",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (showRecapTab) Color.White else NaturalPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = NaturalMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!showRecapTab) {
                // ---------------- 1. DAYLIO 5-MOOD SELECTOR ----------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bagaimana perasaan gigimu hari ini?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = NaturalDark,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5 Tooth Mood Icons
                        DaylioMoodSelectorRow(
                            selectedMoodScore = selectedMoodScore,
                            onMoodSelected = { selectedMoodScore = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mood Description Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(activeMoodInfo.color.copy(alpha = 0.12f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = activeMoodInfo.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = activeMoodInfo.color,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------------- 2. DENTAL ACTIVITY TAGS ----------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Aktivitas & Sensasi Gigi Hari Ini",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 13.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DENTAL_ACTIVITY_TAGS.forEach { tag ->
                                val isSelected = selectedTags.contains(tag.id)
                                val tagBg = when {
                                    isSelected && tag.isBad -> Color(0xFFFEE2E2)
                                    isSelected -> DentallyTintBg
                                    else -> NaturalBackground
                                }
                                val tagBorder = when {
                                    isSelected && tag.isBad -> Color(0xFFE53935)
                                    isSelected -> NaturalPrimary
                                    else -> DentallyBorder
                                }
                                val tagTextColor = when {
                                    isSelected && tag.isBad -> Color(0xFFC62828)
                                    isSelected -> NaturalPrimary
                                    else -> NaturalDark
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tagBg)
                                        .border(1.dp, tagBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedTags = if (isSelected) {
                                                selectedTags - tag.id
                                            } else {
                                                selectedTags + tag.id
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    CustomHabitIcon(
                                        iconKey = tag.iconKey,
                                        isBadHabit = tag.isBad,
                                        sizeDp = 18.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tag.label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = tagTextColor,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------------- 3. JOURNAL NOTE ----------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Catatan Harian Gigi (Opsional)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark,
                                fontSize = 13.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = {
                                Text(
                                    "Contoh: Hari ini mencoba pasta gigi sensitif, rasa linu sudah banyak berkurang...",
                                    fontSize = 12.sp,
                                    color = NaturalMedium
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NaturalPrimary,
                                unfocusedBorderColor = DentallyBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save Button
                Button(
                    onClick = {
                        val entity = DailyMoodEntity(
                            dateString = dateString,
                            moodScore = selectedMoodScore,
                            moodTitle = activeMoodInfo.title,
                            moodSubEmoji = "default",
                            dentalFeel = activeMoodInfo.dentalFeel,
                            note = noteText,
                            activityTags = selectedTags.joinToString(","),
                            timestamp = System.currentTimeMillis()
                        )
                        onSaveMood(entity)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_daylio_mood_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Simpan",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan Mood Gigi Hari Ini",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            } else {
                // ---------------- DAYLIO REKAP / ANALYTICS VIEW ----------------
                DaylioMoodAnalyticsSection(allMoods = allMoods)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Daylio Style Mood Distribution & Habits Correlation Recap.
 */
@Composable
fun DaylioMoodAnalyticsSection(allMoods: List<DailyMoodEntity>) {
    val totalCount = allMoods.size.coerceAtLeast(1)
    val radCount = allMoods.count { it.moodScore == 5 }
    val goodCount = allMoods.count { it.moodScore == 4 }
    val mehCount = allMoods.count { it.moodScore == 3 }
    val badCount = allMoods.count { it.moodScore == 2 }
    val awfulCount = allMoods.count { it.moodScore == 1 }

    val goodPercentage = ((radCount + goodCount) * 100) / totalCount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = "Insights",
                        tint = NaturalPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Distribusi Mood Gigi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalDark,
                            fontSize = 15.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HealthSuccessBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$goodPercentage% Sehat",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HealthSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mood Bars Breakdown (Daylio Style)
            DAYLIO_TOOTH_MOODS.forEach { mood ->
                val count = when (mood.score) {
                    5 -> radCount
                    4 -> goodCount
                    3 -> mehCount
                    2 -> badCount
                    else -> awfulCount
                }
                val ratio = count.toFloat() / totalCount.toFloat()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DaylioToothEmotion(
                        moodScore = mood.score,
                        sizeDp = 30.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mood.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.width(80.dp)
                    )

                    // Horizontal Progress Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(DentallyBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio.coerceAtLeast(0.02f))
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(mood.color)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$count Hari",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Correlation Insight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DentallyTintBg)
                    .border(1.dp, DentallyBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Tips",
                        tint = AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💡 Korelasi: Ketika kamu mencentang Sikat Gigi 2x & Benang Gigi, mood gigimu 85% lebih tinggi masuk kategori 'Luar Biasa' & 'Baik'!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
