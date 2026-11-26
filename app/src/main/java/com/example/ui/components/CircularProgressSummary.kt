package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPill
import com.example.ui.theme.NaturalPrimary

@Composable
fun CircularProgressSummary(
    completedCount: Int,
    totalCount: Int,
    activeBadHabitsCount: Int = 0,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700),
        label = "summary_progress"
    )

    val remainingCount = (totalCount - completedCount).coerceAtLeast(0)
    val percentage = (progress * 100).toInt()
    val animatedPercentage by animateIntAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 650),
        label = "summary_percent_int"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, DentallyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Kebersihan Mulut",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NaturalDark,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$completedCount/$totalCount rutinitas selesai",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = NaturalMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (activeBadHabitsCount > 0) {
                    Text(
                        text = "$activeBadHabitsCount pemicu gigi kotor aktif",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB93829),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                } else {
                    Text(
                        text = if (remainingCount == 0) "Sempurna hari ini!" else "$remainingCount langkah lagi menuju 100%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalMedium.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 7.dp.toPx()
                    drawCircle(
                        color = DentallyBorder,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = if (percentage == 100) HealthSuccess else NaturalPrimary,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "$animatedPercentage%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (percentage == 100) HealthSuccess else NaturalDark,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

