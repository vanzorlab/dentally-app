package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium

data class ToothMoodInfo(
    val score: Int,         // 1 to 5
    val title: String,      // "Luar Biasa", "Baik", "Biasa Aja", "Buruk", "Sangat Buruk"
    val color: Color,
    val description: String,
    val dentalFeel: String
)

val DAYLIO_TOOTH_MOODS = listOf(
    ToothMoodInfo(
        score = 5,
        title = "Luar Biasa",
        color = Color(0xFF00B09B), // Teal Green (Daylio Rad)
        description = "Gigi super bersih, nafas wangi & percaya diri!",
        dentalFeel = "Gigi Kinclong & Segar Maksimal"
    ),
    ToothMoodInfo(
        score = 4,
        title = "Baik",
        color = Color(0xFF84CC16), // Fresh Light Green (Daylio Good)
        description = "Sikat gigi lancar, gusi nyaman dan tenang.",
        dentalFeel = "Gigi Bersih & Sehat"
    ),
    ToothMoodInfo(
        score = 3,
        title = "Biasa Aja",
        color = Color(0xFF38BDF8), // Sky Blue (Daylio Meh)
        description = "Kondisi normal, jangan lewatkan sikat malam ya!",
        dentalFeel = "Kondisi Netral / Normal"
    ),
    ToothMoodInfo(
        score = 2,
        title = "Buruk",
        color = Color(0xFFFB923C), // Warm Orange (Daylio Bad)
        description = "Makan manis/kopi berlebih, agak terasa lengket.",
        dentalFeel = "Terasa Agak Lengket / Asam"
    ),
    ToothMoodInfo(
        score = 1,
        title = "Sangat Buruk",
        color = Color(0xFFF43F5E), // Red Pink (Daylio Awful)
        description = "Linu pada gigi, gusi berdarah atau lupa sikat gigi.",
        dentalFeel = "Gigi Linu / Gusi Sensitif"
    )
)

/**
 * Individual Tooth Character with Daylio-style Emotions.
 * Rendered using precision vector Canvas.
 */
@Composable
fun DaylioToothEmotion(
    moodScore: Int,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 54.dp,
    subEmotion: String = "default",
    isSelected: Boolean = false,
    showLabel: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val mood = DAYLIO_TOOTH_MOODS.firstOrNull { it.score == moodScore } ?: DAYLIO_TOOTH_MOODS[1]
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.14f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "mood_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scaleAnim)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(
                    if (isSelected) mood.color.copy(alpha = 0.18f) else Color.Transparent
                )
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.5.dp,
                            color = mood.color,
                            shape = CircleShape
                        )
                    } else Modifier
                )
                .padding(if (isSelected) 3.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(sizeDp - if (isSelected) 6.dp else 0.dp)) {
                drawDaylioToothEmotionCanvas(
                    moodScore = moodScore,
                    subEmotion = subEmotion,
                    moodColor = mood.color,
                    canvasSize = size
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mood.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) mood.color else NaturalDark,
                    fontSize = 11.5.sp
                )
            )
        }
    }
}

/**
 * 5-Mood Horizontal Selector Bar (Identical to Daylio reference).
 */
@Composable
fun DaylioMoodSelectorRow(
    selectedMoodScore: Int,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DAYLIO_TOOTH_MOODS.forEach { mood ->
            DaylioToothEmotion(
                moodScore = mood.score,
                isSelected = selectedMoodScore == mood.score,
                showLabel = true,
                sizeDp = 50.dp,
                onClick = { onMoodSelected(mood.score) }
            )
        }
    }
}

/**
 * Core Canvas drawing logic for the 5 distinct Tooth Mood expressions.
 */
fun DrawScope.drawDaylioToothEmotionCanvas(
    moodScore: Int,
    subEmotion: String,
    moodColor: Color,
    canvasSize: Size
) {
    val w = canvasSize.width
    val h = canvasSize.height
    val cx = w / 2f
    val cy = h / 2f

    // 1. Draw circular background body (Daylio style)
    drawCircle(
        color = moodColor,
        radius = w * 0.48f,
        center = Offset(cx, cy)
    )

    // Soft top highlight for 3D depth
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(cx, cy * 0.55f),
            radius = w * 0.35f
        ),
        radius = w * 0.46f,
        center = Offset(cx, cy)
    )

    val faceColor = Color(0xFF1E293B) // Dark expressive facial lines
    val eyeRadius = w * 0.052f

    when (moodScore) {
        // 5: RAD (Luar Biasa) - Joyful Laughing ^ ▽ ^
        5 -> {
            // Left eye curved ^
            val leftEye = Path().apply {
                moveTo(w * 0.28f, h * 0.42f)
                quadraticBezierTo(w * 0.34f, h * 0.33f, w * 0.40f, h * 0.42f)
            }
            drawPath(
                leftEye,
                color = faceColor,
                style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
            )

            // Right eye curved ^
            val rightEye = Path().apply {
                moveTo(w * 0.60f, h * 0.42f)
                quadraticBezierTo(w * 0.66f, h * 0.33f, w * 0.72f, h * 0.42f)
            }
            drawPath(
                rightEye,
                color = faceColor,
                style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
            )

            // Wide open happy mouth ▽
            val mouthPath = Path().apply {
                moveTo(w * 0.32f, h * 0.52f)
                lineTo(w * 0.68f, h * 0.52f)
                quadraticBezierTo(w * 0.50f, h * 0.82f, w * 0.32f, h * 0.52f)
                close()
            }
            drawPath(mouthPath, color = faceColor)

            // Cute pink tongue inside mouth
            val tonguePath = Path().apply {
                moveTo(w * 0.40f, h * 0.64f)
                quadraticBezierTo(w * 0.50f, h * 0.56f, w * 0.60f, h * 0.64f)
                quadraticBezierTo(w * 0.50f, h * 0.80f, w * 0.40f, h * 0.64f)
                close()
            }
            drawPath(tonguePath, color = Color(0xFFFF8FA3))

            // Rosy cheeks
            drawCircle(
                color = Color(0x33FF6B8B),
                radius = w * 0.08f,
                center = Offset(w * 0.24f, h * 0.50f)
            )
            drawCircle(
                color = Color(0x33FF6B8B),
                radius = w * 0.08f,
                center = Offset(w * 0.76f, h * 0.50f)
            )
        }

        // 4: GOOD (Baik) - Cute Friendly Smile • ᴗ •
        4 -> {
            // Left eye dot
            drawCircle(
                color = faceColor,
                radius = eyeRadius,
                center = Offset(w * 0.33f, h * 0.44f)
            )
            // Right eye dot
            drawCircle(
                color = faceColor,
                radius = eyeRadius,
                center = Offset(w * 0.67f, h * 0.44f)
            )

            // Sweet curved smile
            val smilePath = Path().apply {
                moveTo(w * 0.38f, h * 0.58f)
                quadraticBezierTo(w * 0.50f, h * 0.70f, w * 0.62f, h * 0.58f)
            }
            drawPath(
                smilePath,
                color = faceColor,
                style = Stroke(width = w * 0.05f, cap = StrokeCap.Round)
            )

            // Rosy cheeks
            drawCircle(
                color = Color(0x33FF8A80),
                radius = w * 0.065f,
                center = Offset(w * 0.25f, h * 0.52f)
            )
            drawCircle(
                color = Color(0x33FF8A80),
                radius = w * 0.065f,
                center = Offset(w * 0.75f, h * 0.52f)
            )
        }

        // 3: MEH (Biasa Aja) - Calm Straight Mouth • _ •
        3 -> {
            // Left eye dot
            drawCircle(
                color = faceColor,
                radius = eyeRadius,
                center = Offset(w * 0.33f, h * 0.45f)
            )
            // Right eye dot
            drawCircle(
                color = faceColor,
                radius = eyeRadius,
                center = Offset(w * 0.67f, h * 0.45f)
            )

            // Straight horizontal mouth
            drawLine(
                color = faceColor,
                start = Offset(w * 0.39f, h * 0.62f),
                end = Offset(w * 0.61f, h * 0.62f),
                strokeWidth = w * 0.055f,
                cap = StrokeCap.Round
            )
        }

        // 2: BAD (Buruk) - Frown • ︵ •
        2 -> {
            // Left eye dot
            drawCircle(
                color = faceColor,
                radius = eyeRadius,
                center = Offset(w * 0.34f, h * 0.46f)
            )
            // Right eye dot
            drawCircle(
                color = faceColor,
                radius = eyeRadius,
                center = Offset(w * 0.66f, h * 0.46f)
            )

            // Slanted sad eyebrows
            val leftBrow = Path().apply {
                moveTo(w * 0.28f, h * 0.34f)
                lineTo(w * 0.40f, h * 0.38f)
            }
            drawPath(leftBrow, color = faceColor, style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))

            val rightBrow = Path().apply {
                moveTo(w * 0.72f, h * 0.34f)
                lineTo(w * 0.60f, h * 0.38f)
            }
            drawPath(rightBrow, color = faceColor, style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))

            // Downturned sad mouth
            val frownPath = Path().apply {
                moveTo(w * 0.38f, h * 0.66f)
                quadraticBezierTo(w * 0.50f, h * 0.54f, w * 0.62f, h * 0.66f)
            }
            drawPath(
                frownPath,
                color = faceColor,
                style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
            )
        }

        // 1: AWFUL (Sangat Buruk) - Straining / Pain > ︵ <
        1 -> {
            // Left squeezed eye >
            val leftEye = Path().apply {
                moveTo(w * 0.28f, h * 0.38f)
                lineTo(w * 0.38f, h * 0.44f)
                lineTo(w * 0.28f, h * 0.50f)
            }
            drawPath(
                leftEye,
                color = faceColor,
                style = Stroke(width = w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Right squeezed eye <
            val rightEye = Path().apply {
                moveTo(w * 0.72f, h * 0.38f)
                lineTo(w * 0.62f, h * 0.44f)
                lineTo(w * 0.72f, h * 0.50f)
            }
            drawPath(
                rightEye,
                color = faceColor,
                style = Stroke(width = w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Pain frown mouth
            val painMouth = Path().apply {
                moveTo(w * 0.35f, h * 0.68f)
                quadraticBezierTo(w * 0.50f, h * 0.56f, w * 0.65f, h * 0.68f)
            }
            drawPath(
                painMouth,
                color = faceColor,
                style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
            )

            // Blue sweat droplet on forehead
            val sweatPath = Path().apply {
                moveTo(w * 0.76f, h * 0.24f)
                quadraticBezierTo(w * 0.84f, h * 0.34f, w * 0.76f, h * 0.38f)
                quadraticBezierTo(w * 0.68f, h * 0.34f, w * 0.76f, h * 0.24f)
                close()
            }
            drawPath(sweatPath, color = Color(0xFF60A5FA))
        }
    }
}
