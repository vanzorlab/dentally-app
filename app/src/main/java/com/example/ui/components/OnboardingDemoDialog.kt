package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.SoundEffectManager
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import kotlinx.coroutines.launch

private data class DemoSlide(
    val title: String,
    val shortDesc: String,
    val badge: String,
    val accentColor: Color,
    val bgColor: Color,
    val icon: ImageVector,
    val actionLabel: String
)

/**
 * Interactive 4-slide onboarding & feature demo walkthrough for new or curious users.
 */
@Composable
fun OnboardingDemoDialog(
    onFinish: () -> Unit
) {
    val slides = remember {
        listOf(
            DemoSlide(
                title = "Sikat Gigi 2 Menit",
                shortDesc = "Tekan mulai di Beranda. Timer memandu 4 sisi gigi agar bersih merata.",
                badge = "Langkah 1 • Timer",
                accentColor = NaturalPrimary,
                bgColor = DentallyTintBg,
                icon = Icons.Default.Timer,
                actionLabel = "Coba Bunyi Selesai"
            ),
            DemoSlide(
                title = "Rawat Gigi Ku",
                shortDesc = "Centang kebiasaan harian supaya Gigi Ku tetap kinclong, ceria, dan naik level.",
                badge = "Langkah 2 • Karakter Gigi",
                accentColor = Color(0xFF0284C7),
                bgColor = Color(0xFFE0F2FE),
                icon = Icons.Default.AutoAwesome,
                actionLabel = "Sapa Gigi Ku"
            ),
            DemoSlide(
                title = "Kalender & Pengingat Malam",
                shortDesc = "Pantau hari penuh di kalender & isi cek malam sebelum lanjut aktivitas HP.",
                badge = "Langkah 3 • Disiplin",
                accentColor = Color(0xFFD97706),
                bgColor = Color(0xFFFEF3C7),
                icon = Icons.Default.NotificationsActive,
                actionLabel = "Dengar Nada Pengingat"
            ),
            DemoSlide(
                title = "Berita Multi-Platform",
                shortDesc = "Baca info gigi seru dari Google, YouTube, Instagram, Jurnal, dan tanya Dentally AI.",
                badge = "Langkah 4 • Wawasan",
                accentColor = Color(0xFF7C3AED),
                bgColor = Color(0xFFF3E8FF),
                icon = Icons.Default.Public,
                actionLabel = "Tes Efek Suara"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    var demoTaps by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onFinish,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_demo_dialog"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Panduan Cepat",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NaturalDark
                            )
                        )
                        TextButton(
                            onClick = {
                                SoundEffectManager.playPop()
                                onFinish()
                            },
                            modifier = Modifier.testTag("onboarding_skip_btn")
                        ) {
                            Text("Lewati", color = SageMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                    ) { pageIndex ->
                        val slide = slides[pageIndex]
                        var isPressed by remember { mutableStateOf(false) }
                        val iconScale by animateFloatAsState(
                            targetValue = if (isPressed) 1.15f else 1f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = 450f),
                            label = "demo_icon_scale"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(slide.bgColor)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = slide.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = slide.accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .scale(iconScale)
                                    .clip(CircleShape)
                                    .background(slide.bgColor)
                                    .border(2.dp, slide.accentColor.copy(alpha = 0.35f), CircleShape)
                                    .clickable {
                                        isPressed = !isPressed
                                        demoTaps++
                                        when (pageIndex) {
                                            0 -> SoundEffectManager.playFanfare()
                                            1 -> SoundEffectManager.playToothGiggle()
                                            2 -> SoundEffectManager.playSuccessChime()
                                            else -> SoundEffectManager.playPop()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (pageIndex == 1) {
                                    InteractiveToothVisual(
                                        healthPercent = 95,
                                        sizeDp = 64.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = slide.icon,
                                        contentDescription = slide.title,
                                        tint = slide.accentColor,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NaturalDark,
                                    fontSize = 20.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = slide.shortDesc,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SageDark,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DentallyTintBg)
                                    .clickable {
                                        isPressed = !isPressed
                                        demoTaps++
                                        when (pageIndex) {
                                            0 -> SoundEffectManager.playFanfare()
                                            1 -> SoundEffectManager.playToothGiggle()
                                            2 -> SoundEffectManager.playSuccessChime()
                                            else -> SoundEffectManager.playPop()
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = slide.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slide.actionLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = slide.accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Dots Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        repeat(slides.size) { idx ->
                            val isSelected = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (isSelected) 22.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NaturalPrimary else DentallyBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val isLast = pagerState.currentPage == slides.lastIndex
                    Button(
                        onClick = {
                            if (isLast) {
                                SoundEffectManager.playSuccessChime()
                                onFinish()
                            } else {
                                SoundEffectManager.playPop()
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_next_btn")
                    ) {
                        Text(
                            text = if (isLast) "Mulai Pakai Dentally" else "Lanjut",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
