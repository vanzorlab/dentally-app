package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPill
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium

@Composable
fun VirtualToothCharacterCard(
    healthPercent: Int,
    streakDays: Int,
    stageName: String,
    plaqueLevel: String = "Bebas Plak",
    activeBadHabitsCount: Int = 0,
    onStartTimer: () -> Unit,
    onChatWithDentally: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClean = healthPercent >= 70
    val isWarning = healthPercent in 40..69
    val isDirty = healthPercent < 40

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated Interactive Tooth Visual Avatar
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when {
                                    isClean -> Color(0xFFEBF7EE)
                                    isWarning -> Color(0xFFFEF9E7)
                                    else -> Color(0xFFFBECEB)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        InteractiveToothVisual(
                            healthPercent = healthPercent,
                            sizeDp = 62.dp,
                            onClick = onStartTimer
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gigi Dentally",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalDark,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isClean) Icons.Default.AutoAwesome else Icons.Default.Warning,
                                contentDescription = if (isClean) "Glowing" else "Warning",
                                tint = if (isClean) AccentGold else if (isWarning) Color(0xFFE65100) else Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = stageName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isClean) NaturalPrimary else if (isWarning) Color(0xFFC07800) else Color(0xFFB93829),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$streakDays Hari Streak • $plaqueLevel",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NaturalMedium,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                // Health percentage badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isClean -> Color(0xFFD1EAB7)
                                isWarning -> Color(0xFFFEF2D0)
                                else -> Color(0xFFFFDCDA)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$healthPercent% Bersih",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when {
                                isClean -> NaturalPrimary
                                isWarning -> Color(0xFFB45309)
                                else -> Color(0xFFB91C1C)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation notice if dirty or clean
            val habitFeedbackText = when {
                activeBadHabitsCount > 0 && isDirty -> "⚠️ Ada $activeBadHabitsCount kebiasaan buruk aktif & habit belum lengkap! Gigi kotor dan berplak tebal. Sikat sekarang untuk membersihkannya!"
                activeBadHabitsCount > 0 -> "⚠️ Kebiasaan buruk memicu noda dan plak pada gigi. Segera sikat gigi dan kumur!"
                isDirty -> "⚠️ Habit menyikat gigi belum diisi/terlewat! Plak dan asam menumpuk membuat gigi kotor."
                isWarning -> "Selesaikan sikat gigi malam & flossing agar gigimu berkilau 100%!"
                else -> "✨ Gigimu putih berkilau dan sehat terlindungi bebas plak!"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isClean -> Color(0xFFF3FAF2)
                            isWarning -> Color(0xFFFFFBEB)
                            else -> Color(0xFFFEF2F2)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = habitFeedbackText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = when {
                            isClean -> NaturalPrimary
                            isWarning -> Color(0xFF92400E)
                            else -> Color(0xFF991B1B)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { healthPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    isClean -> NaturalPrimary
                    isWarning -> Color(0xFFD97706)
                    else -> Color(0xFFDC2626)
                },
                trackColor = Color(0xFFE8EDE1),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartTimer,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("start_brushing_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDirty) Color(0xFFB93829) else NaturalPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isDirty) Icons.Default.CleaningServices else Icons.Default.PlayArrow,
                        contentDescription = "Mulai Bersihkan",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDirty) "Bersihkan Gigi (2 Min)" else "Sikat Gigi 2 Menit",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    )
                }

                Button(
                    onClick = onChatWithDentally,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp)
                        .testTag("chat_dentally_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEBF4EF),
                        contentColor = SageDark
                    )
                ) {
                    Text(
                        text = "💬 Tanya AI",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }
    }
}

