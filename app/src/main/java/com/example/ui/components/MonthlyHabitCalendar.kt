package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectManager
import com.example.data.local.DailyMoodEntity
import com.example.data.local.HabitEntity
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.HealthSuccessBg
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalLight
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class DayCompletionState {
    FULL,     // All good habits completed
    PARTIAL,  // Some good habits completed
    MISSED,   // Has habits logged but 0 completed
    EMPTY     // No activity recorded on this date
}

data class MonthlyDaySummary(
    val dateString: String,
    val dayNumber: Int,
    val completedGood: Int,
    val totalGood: Int,
    val brushedDone: Boolean,
    val moodTitle: String?,
    val isFuture: Boolean,
    val state: DayCompletionState
)

@Composable
fun MonthlyHabitCalendar(
    habits: List<HabitEntity>,
    moods: List<DailyMoodEntity>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    title: String = "Kalender Kepatuhan Bulanan",
    modifier: Modifier = Modifier,
    onFutureDateBlocked: (() -> Unit)? = null
) {
    var monthOffset by remember { mutableIntStateOf(0) }

    val targetCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }

    val monthTitle = remember(monthOffset) {
        SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(targetCalendar.time)
            .replaceFirstChar { it.uppercase() }
    }

    val habitsByDate = remember(habits) { habits.groupBy { it.dateString } }
    val moodsByDate = remember(moods) { moods.associateBy { it.dateString } }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayString = remember { dateFormat.format(Calendar.getInstance().time) }

    val daysInMonth = targetCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = targetCalendar.get(Calendar.DAY_OF_WEEK)
    val leadingEmptySlots = ((firstDayOfWeek - Calendar.MONDAY) + 7) % 7

    val daySummaries = remember(monthOffset, habitsByDate, moodsByDate) {
        (1..daysInMonth).map { day ->
            val cal = (targetCalendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, day)
            }
            val dateStr = dateFormat.format(cal.time)
            val isFuture = dateStr > todayString
            val dayHabits = habitsByDate[dateStr].orEmpty()
            val goodHabits = dayHabits.filter { !it.isBadHabit }
            val completedGood = if (isFuture) 0 else goodHabits.count { it.isCompleted }
            val totalGood = if (isFuture) 0 else goodHabits.size
            val brushedDone = !isFuture && goodHabits.any {
                it.isCompleted && it.title.contains("Sikat", ignoreCase = true)
            }
            val mood = if (isFuture) null else moodsByDate[dateStr]

            val state = when {
                isFuture -> DayCompletionState.EMPTY
                totalGood > 0 && completedGood == totalGood -> DayCompletionState.FULL
                completedGood > 0 -> DayCompletionState.PARTIAL
                totalGood > 0 -> DayCompletionState.MISSED
                else -> DayCompletionState.EMPTY
            }
            MonthlyDaySummary(
                dateString = dateStr,
                dayNumber = day,
                completedGood = completedGood,
                totalGood = totalGood,
                brushedDone = brushedDone,
                moodTitle = mood?.moodTitle,
                isFuture = isFuture,
                state = state
            )
        }
    }

    val fullDaysCount = daySummaries.count { !it.isFuture && it.state == DayCompletionState.FULL }
    val partialDaysCount = daySummaries.count { !it.isFuture && it.state == DayCompletionState.PARTIAL }
    val selectedDayDetail = daySummaries.firstOrNull { it.dateString == selectedDate && !it.isFuture }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_habit_calendar"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DentallyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalDark,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            SoundEffectManager.playPop()
                            monthOffset -= 1
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Bulan Sebelumnya",
                            tint = NaturalDark
                        )
                    }
                    IconButton(
                        onClick = {
                            if (monthOffset < 0) {
                                SoundEffectManager.playPop()
                                monthOffset += 1
                            }
                        },
                        enabled = monthOffset < 0,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Bulan Berikutnya",
                            tint = if (monthOffset < 0) NaturalDark else NaturalLight.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarStatBadge(
                    dotColor = HealthSuccess,
                    label = "Penuh: $fullDaysCount",
                    bgColor = HealthSuccessBg,
                    textColor = HealthSuccess
                )
                CalendarStatBadge(
                    dotColor = Color(0xFFD97706),
                    label = "Sebagian: $partialDaysCount",
                    bgColor = Color(0xFFFEF3C7),
                    textColor = Color(0xFF9A6700)
                )
                CalendarStatBadge(
                    dotColor = Color(0xFFD6C7CA),
                    label = "Kosong",
                    bgColor = Color(0xFFF7F2F3),
                    textColor = NaturalMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val weekDays = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NaturalMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val totalCells = leadingEmptySlots + daySummaries.size
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (rowIdx in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (colIdx in 0 until 7) {
                            val cellIndex = rowIdx * 7 + colIdx
                            val dayIndex = cellIndex - leadingEmptySlots
                            if (dayIndex in daySummaries.indices) {
                                val summary = daySummaries[dayIndex]
                                val isSelected = summary.dateString == selectedDate && !summary.isFuture
                                val isToday = summary.dateString == todayString

                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.08f else 1f,
                                    animationSpec = spring(dampingRatio = 0.55f, stiffness = 450f),
                                    label = "cal_day_scale"
                                )

                                val cellBg = when {
                                    isSelected -> NaturalPrimary
                                    summary.isFuture -> Color(0xFFF6F2F3)
                                    summary.state == DayCompletionState.FULL -> HealthSuccess
                                    summary.state == DayCompletionState.PARTIAL -> Color(0xFFFEF3C7)
                                    summary.state == DayCompletionState.MISSED -> Color(0xFFFFE4E7)
                                    else -> Color(0xFFFAF5F6)
                                }

                                val cellTextColor = when {
                                    isSelected -> Color.White
                                    summary.isFuture -> NaturalLight
                                    summary.state == DayCompletionState.FULL -> Color.White
                                    summary.state == DayCompletionState.PARTIAL -> Color(0xFF92400E)
                                    summary.state == DayCompletionState.MISSED -> NaturalPrimary
                                    else -> NaturalDark
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .scale(scale)
                                        .alpha(if (summary.isFuture) 0.42f else 1f)
                                        .clip(CircleShape)
                                        .background(cellBg)
                                        .then(
                                            if (isToday && !isSelected) {
                                                Modifier.border(2.dp, NaturalPrimary, CircleShape)
                                            } else Modifier
                                        )
                                        .clickable {
                                            if (summary.isFuture) {
                                                onFutureDateBlocked?.invoke()
                                            } else {
                                                SoundEffectManager.playPop()
                                                onDateSelected(summary.dateString)
                                            }
                                        }
                                        .testTag("month_day_${summary.dateString}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = summary.dayNumber.toString(),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = cellTextColor,
                                                fontWeight = if (summary.state == DayCompletionState.FULL || isSelected || isToday) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Medium
                                                },
                                                fontSize = 12.sp
                                            )
                                        )
                                        if (summary.state == DayCompletionState.FULL) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Penuh",
                                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.92f),
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = selectedDayDetail != null) {
                selectedDayDetail?.let { info ->
                    val statusText = when (info.state) {
                        DayCompletionState.FULL -> "Penuh! Semua rutinitas & sikat gigi selesai (${info.completedGood}/${info.totalGood})"
                        DayCompletionState.PARTIAL -> "Terisi sebagian (${info.completedGood}/${info.totalGood} kebiasaan)"
                        DayCompletionState.MISSED -> "Belum diselesaikan (${info.completedGood}/${info.totalGood})"
                        DayCompletionState.EMPTY -> "Belum ada catatan pada tanggal ini"
                    }
                    val moodSuffix = if (!info.moodTitle.isNullOrBlank()) " • Mood: ${info.moodTitle}" else ""

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DentallyTintBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Tanggal ${info.dateString}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NaturalDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = statusText + moodSuffix,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NaturalMedium,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarStatBadge(
    dotColor: Color,
    label: String,
    bgColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )
        )
    }
}
