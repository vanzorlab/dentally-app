package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectManager
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryDark
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Giant Interactive Forest-Style Tooth Hero Character.
 * Features:
 * - Big circular growth stage with concentric rings.
 * - Interactive tap animations, dialogues, and sparkle bursts.
 * - Level badge, Growth progress bars, and quick-action shortcuts.
 */
@Composable
fun GiantForestToothHero(
    userName: String,
    healthPercent: Int,
    streakDays: Int,
    stageName: String,
    levelNumber: Int = 1,
    todayMoodScore: Int? = null,
    onStartTimer: () -> Unit,
    onOpenMoodSheet: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToBrowser: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isTapped by remember { mutableStateOf(false) }
    var showSparkles by remember { mutableStateOf(false) }
    var currentDialogueIndex by remember { mutableStateOf(0) }
    var showBubble by remember { mutableStateOf(true) }

    val dialogues = remember(userName, healthPercent, streakDays) {
        listOf(
            "Halo $userName! Tingkat kebersihan mulutmu hari ini $healthPercent%!",
            "Sikat gigi 2 menit pagi & malam bikin gigiku tetap putih dan kuat ✨",
            "Jangan lupa bersihin sela gigi pakai benang gigi sebelum tidur ya!",
            "Kumur air putih habis makan manis biar kotoran tidak gampang nempel 💧",
            "Sudah $streakDays hari berturut-turut merawat gigi! Keren banget! 🦷"
        )
    }

    // Floating / Breathing subtle animation
    val infiniteTransition = rememberInfiniteTransition(label = "forest_hero_float")
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_y_offset"
    )

    // Tap bounce scale
    val tapScale by animateFloatAsState(
        targetValue = if (isTapped) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
        label = "hero_tap_bounce"
    )

    LaunchedEffect(isTapped) {
        if (isTapped) {
            showSparkles = true
            showBubble = true
            delay(1200)
            isTapped = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Speech Bubble from Big Tooth (Interactive)
        AnimatedVisibility(
            visible = showBubble,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DentallyTintBg)
                    .border(1.dp, DentallyBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        SoundEffectManager.playPop()
                        currentDialogueIndex = (currentDialogueIndex + 1) % dialogues.size
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Tips",
                        tint = NaturalPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"${dialogues[currentDialogueIndex]}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NaturalDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Large Circular Forest Stage Container
        Box(
            modifier = Modifier
                .size(220.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        isTapped = true
                        SoundEffectManager.playToothGiggle()
                        currentDialogueIndex = (currentDialogueIndex + 1) % dialogues.size
                    }
                )
                .testTag("giant_forest_tooth"),
            contentAlignment = Alignment.Center
        ) {
            // Concentric Glow Rings
            Canvas(modifier = Modifier.size(220.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                drawCircle(
                    color = DentallyTintBg.copy(alpha = 0.7f),
                    radius = size.width * 0.49f,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    color = Color(0xFFFFE4E7).copy(alpha = 0.75f),
                    radius = size.width * 0.43f,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFF5F6)),
                        center = Offset(cx, cy * 0.9f),
                        radius = size.width * 0.38f
                    ),
                    radius = size.width * 0.38f,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    color = DentallyBorder,
                    radius = size.width * 0.38f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )
            }

            // The Giant 3D Tooth Character
            Box(
                modifier = Modifier
                    .offset(y = floatOffsetY.dp)
                    .scale(tapScale)
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(130.dp)) {
                    drawBigForestToothCharacter(
                        healthPercent = healthPercent,
                        todayMoodScore = todayMoodScore,
                        isTapped = isTapped,
                        canvasSize = size
                    )
                }
            }

            if (showSparkles) {
                SparkleParticlesOverlay(modifier = Modifier.size(190.dp))
            }

            // Level Badge Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-6).dp)
                    .clip(RoundedCornerShape(50))
                    .background(NaturalPrimaryDark)
                    .border(1.5.dp, Color.White, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "LEVEL $levelNumber",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Stage Growth Title
        Text(
            text = when {
                healthPercent >= 85 -> "Gigi Ku lagi kinclong & super sehat!"
                healthPercent >= 65 -> "Gigi Ku cukup bersih, yuk lengkapi rutinitas!"
                else -> "Gigi Ku butuh disikat biar bersih lagi!"
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SageDark,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Segmented Growth Progress Indicator (5 bars)
        Row(
            modifier = Modifier.fillMaxWidth(0.65f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeSegments = (healthPercent / 20).coerceIn(1, 5)
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (i <= activeSegments) NaturalPrimary else DentallyBorder
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Interactive Quick Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Button 1: Sikat Gigi 2 Menit (Ke Beranda Timer)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NaturalPrimary)
                        .clickable { onStartTimer() }
                        .padding(vertical = 12.dp, horizontal = 10.dp)
                        .testTag("hero_start_timer_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sikat Gigi (2m)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }

                // Quick Button 2: Mood Hari Ini
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DentallyTintBg)
                        .border(1.dp, DentallyBorder, RoundedCornerShape(14.dp))
                        .clickable { onOpenMoodSheet() }
                        .padding(vertical = 12.dp, horizontal = 10.dp)
                        .testTag("hero_mood_sheet_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = "Mood",
                            tint = NaturalPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Mood Hari Ini",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NaturalPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Button 3: Tanya Dentally
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF8E1))
                        .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(14.dp))
                        .clickable { onNavigateToChat() }
                        .padding(vertical = 11.dp, horizontal = 10.dp)
                        .testTag("hero_ask_ai_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Tanya Dentally",
                            tint = Color(0xFFB78103),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tanya Dentally",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF8D6200),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Quick Button 4: Baca Berita & Fun Fact
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F0FE))
                        .border(1.dp, Color(0xFFD2E3FC), RoundedCornerShape(14.dp))
                        .clickable { onNavigateToBrowser() }
                        .padding(vertical = 11.dp, horizontal = 10.dp)
                        .testTag("hero_browser_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Berita & Fun Fact",
                            tint = Color(0xFF1967D2),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Berita & Fakta",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF1967D2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas drawing logic for the Big Forest Tooth Character.
 */
private fun DrawScope.drawBigForestToothCharacter(
    healthPercent: Int,
    todayMoodScore: Int?,
    isTapped: Boolean,
    canvasSize: Size
) {
    val w = canvasSize.width
    val h = canvasSize.height
    val cx = w / 2f

    val isClean = healthPercent >= 70
    val isWarning = healthPercent in 40..69
    val isDirty = healthPercent < 40

    // Tooth Base Body Colors
    val toothMainColor = when {
        isDirty -> Color(0xFFFFF7DB)
        isWarning -> Color(0xFFFCFDF5)
        else -> Color(0xFFFFFFFF)
    }

    val toothOutlineColor = when {
        isDirty -> Color(0xFFD4B160)
        isWarning -> Color(0xFFE58C96)
        else -> Color(0xFFE13347)
    }

    // 1. Tooth Crown & Roots Path
    val toothPath = Path().apply {
        moveTo(w * 0.50f, h * 0.16f) // Top center dip
        cubicTo(w * 0.36f, h * 0.08f, w * 0.14f, h * 0.16f, w * 0.14f, h * 0.40f) // Left crown cusp
        cubicTo(w * 0.14f, h * 0.62f, w * 0.22f, h * 0.88f, w * 0.32f, h * 0.94f) // Left root tip
        cubicTo(w * 0.40f, h * 0.98f, w * 0.44f, h * 0.72f, w * 0.50f, h * 0.68f) // Middle root valley
        cubicTo(w * 0.56f, h * 0.72f, w * 0.60f, h * 0.98f, w * 0.68f, h * 0.94f) // Right root tip
        cubicTo(w * 0.78f, h * 0.88f, w * 0.86f, h * 0.62f, w * 0.86f, h * 0.40f) // Right root to crown
        cubicTo(w * 0.86f, h * 0.16f, w * 0.64f, h * 0.08f, w * 0.50f, h * 0.16f) // Right crown cusp
        close()
    }

    // Soft drop shadow
    drawCircle(
        color = Color(0x22000000),
        radius = w * 0.32f,
        center = Offset(cx, h * 0.92f)
    )

    // Draw main filled tooth body with gradient
    drawPath(
        toothPath,
        brush = Brush.verticalGradient(
            colors = listOf(toothMainColor, toothMainColor.copy(alpha = 0.92f), Color(0xFFFFF2F3)),
            startY = h * 0.10f,
            endY = h * 0.95f
        )
    )

    // Draw tooth border outline
    drawPath(
        toothPath,
        color = toothOutlineColor,
        style = Stroke(width = w * 0.038f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Enamel Gloss Highlights
    val leftGloss = Path().apply {
        moveTo(w * 0.22f, h * 0.28f)
        cubicTo(w * 0.20f, h * 0.38f, w * 0.22f, h * 0.52f, w * 0.28f, h * 0.60f)
    }
    drawPath(
        leftGloss,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
    )

    // Plak/Stains overlay if dirty
    if (isDirty || isWarning) {
        val plaqueColor = if (isDirty) Color(0x66D4B160) else Color(0x44E0C068)
        drawCircle(plaqueColor, radius = w * 0.09f, center = Offset(w * 0.28f, h * 0.82f))
        drawCircle(plaqueColor, radius = w * 0.08f, center = Offset(w * 0.72f, h * 0.84f))
    }

    // 2. Cute Expressive Face
    val faceColor = Color(0xFF1F2937)

    // Cheerful Eyes
    if (isClean || isTapped || todayMoodScore == 5) {
        // Joyful curved eyes ^ ^
        val leftJoyEye = Path().apply {
            moveTo(w * 0.34f, h * 0.42f)
            quadraticTo(w * 0.40f, h * 0.34f, w * 0.46f, h * 0.42f)
        }
        drawPath(leftJoyEye, color = faceColor, style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))

        val rightJoyEye = Path().apply {
            moveTo(w * 0.54f, h * 0.42f)
            quadraticTo(w * 0.60f, h * 0.34f, w * 0.66f, h * 0.42f)
        }
        drawPath(rightJoyEye, color = faceColor, style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))
    } else {
        // Round cute eyes • •
        drawCircle(faceColor, radius = w * 0.048f, center = Offset(w * 0.38f, h * 0.42f))
        drawCircle(Color.White, radius = w * 0.016f, center = Offset(w * 0.37f, h * 0.41f))

        drawCircle(faceColor, radius = w * 0.048f, center = Offset(w * 0.62f, h * 0.42f))
        drawCircle(Color.White, radius = w * 0.016f, center = Offset(w * 0.61f, h * 0.41f))
    }

    // Rosy Cheeks
    drawCircle(
        color = Color(0x44FF8A80),
        radius = w * 0.07f,
        center = Offset(w * 0.28f, h * 0.48f)
    )
    drawCircle(
        color = Color(0x44FF8A80),
        radius = w * 0.07f,
        center = Offset(w * 0.72f, h * 0.48f)
    )

    // Happy Smile or Mood Mouth
    if (isClean || isTapped || todayMoodScore in 4..5) {
        val smilePath = Path().apply {
            moveTo(w * 0.42f, h * 0.50f)
            quadraticTo(w * 0.50f, h * 0.62f, w * 0.58f, h * 0.50f)
        }
        drawPath(smilePath, color = faceColor, style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))
    } else if (todayMoodScore in 1..2 || isDirty) {
        val sadPath = Path().apply {
            moveTo(w * 0.42f, h * 0.56f)
            quadraticTo(w * 0.50f, h * 0.48f, w * 0.58f, h * 0.56f)
        }
        drawPath(sadPath, color = faceColor, style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))
    } else {
        // Straight cute mouth
        drawLine(
            color = faceColor,
            start = Offset(w * 0.44f, h * 0.52f),
            end = Offset(w * 0.56f, h * 0.52f),
            strokeWidth = w * 0.04f,
            cap = StrokeCap.Round
        )
    }

    // Sparkle diamond on tooth crown
    if (isClean) {
        drawSparkleStar(cx + w * 0.28f, h * 0.22f, w * 0.08f, AccentGold)
    }
}

/**
 * Helper to draw 4-pointed sparkle star.
 */
private fun DrawScope.drawSparkleStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val starPath = Path().apply {
        moveTo(cx, cy - radius)
        quadraticTo(cx, cy, cx + radius, cy)
        quadraticTo(cx, cy, cx, cy + radius)
        quadraticTo(cx, cy, cx - radius, cy)
        quadraticTo(cx, cy, cx, cy - radius)
        close()
    }
    drawPath(starPath, color = color)
}

/**
 * Animated Sparkles Overlay when Big Tooth is tapped.
 */
@Composable
private fun SparkleParticlesOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val positions = listOf(
            Offset(size.width * 0.2f, size.height * 0.25f),
            Offset(size.width * 0.8f, size.height * 0.22f),
            Offset(size.width * 0.15f, size.height * 0.7f),
            Offset(size.width * 0.85f, size.height * 0.68f),
            Offset(size.width * 0.5f, size.height * 0.1f)
        )
        positions.forEachIndexed { idx, pos ->
            val starSize = if (idx % 2 == 0) 14f else 9f
            val starColor = if (idx % 2 == 0) AccentGold else Color(0xFFC81E32)
            drawSparkleStar(pos.x, pos.y, starSize, starColor)
        }
    }
}
