package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalLight
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayItem(
    val dayName: String,     // "Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"
    val dayNumber: String,   // "18", "19", "20", etc.
    val dateString: String,  // "YYYY-MM-DD"
    val isToday: Boolean,
    val isFuture: Boolean,
    val hasCompletion: Boolean
)

@Composable
fun WeeklyCalendarStrip(
    selectedDateString: String,
    onDateSelected: (String) -> Unit,
    completedDates: Set<String>,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    onFutureDateBlocked: (() -> Unit)? = null
) {
    val calendarDays = rememberWeeklyDays(completedDates)

    Column(modifier = modifier.fillMaxWidth()) {
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kalender Mingguan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = NaturalDark,
                        fontSize = 16.sp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            onDateSelected(todayStr)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Hari Ini",
                        tint = NaturalMedium,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hari Ini",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            calendarDays.forEach { dayItem ->
                val isSelected = dayItem.dateString == selectedDateString && !dayItem.isFuture
                val animatedBg by animateColorAsState(
                    targetValue = when {
                        isSelected -> NaturalPrimary
                        dayItem.isFuture -> Color(0xFFF6F2F3)
                        else -> Color.White
                    },
                    label = "cal_bg"
                )
                val textColor = when {
                    isSelected -> Color.White
                    dayItem.isFuture -> NaturalLight
                    else -> NaturalDark
                }
                val subTextColor = when {
                    isSelected -> Color.White.copy(alpha = 0.9f)
                    dayItem.isFuture -> NaturalLight
                    else -> NaturalMedium
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.5.dp)
                        .alpha(if (dayItem.isFuture) 0.52f else 1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(animatedBg)
                        .then(
                            if (!isSelected) {
                                Modifier.border(
                                    width = if (dayItem.isToday) 1.5.dp else 1.dp,
                                    color = if (dayItem.isToday) NaturalPrimary else DentallyBorder,
                                    shape = RoundedCornerShape(18.dp)
                                )
                            } else Modifier
                        )
                        .clickable {
                            if (dayItem.isFuture) {
                                onFutureDateBlocked?.invoke()
                            } else {
                                onDateSelected(dayItem.dateString)
                            }
                        }
                        .padding(vertical = 12.dp)
                        .testTag("calendar_day_${dayItem.dateString}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayItem.dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = subTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dayItem.dayNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val dotColor = when {
                        isSelected -> Color.White
                        dayItem.isFuture -> Color(0xFFE5DCDF)
                        dayItem.hasCompletion -> HealthSuccess
                        else -> Color(0xFFE2D4D6)
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

internal fun rememberWeeklyDays(completedDates: Set<String>): List<CalendarDayItem> {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dateFormat.format(Date())

    val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val list = mutableListOf<CalendarDayItem>()

    for (i in 0..6) {
        val dateStr = dateFormat.format(cal.time)
        val dayNumber = cal.get(Calendar.DAY_OF_MONTH).toString()
        val isToday = dateStr == todayStr
        val isFuture = dateStr > todayStr
        val hasCompletion = completedDates.contains(dateStr) && !isFuture

        list.add(
            CalendarDayItem(
                dayName = dayNames[i],
                dayNumber = dayNumber,
                dateString = dateStr,
                isToday = isToday,
                isFuture = isFuture,
                hasCompletion = hasCompletion
            )
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }

    return list
}
