package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun InteractiveToothVisual(
    healthPercent: Int,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 88.dp,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tooth_anim")

    // Gentle bounce & breathing animation
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (healthPercent >= 65) -4f else 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Sparkle scale for clean tooth
    val sparkleScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
    )

    // Dirt wobble for dirty tooth
    val dirtWobble by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dirt"
    )

    // Tooth body colors according to health level
    val baseToothColor by animateColorAsState(
        targetValue = when {
            healthPercent >= 85 -> Color(0xFFFFFFFF)
            healthPercent >= 65 -> Color(0xFFFAFBF5)
            healthPercent >= 40 -> Color(0xFFF9EECC) // Yellowish plaque start
            else -> Color(0xFFE8D39E) // Stained dirty tooth
        },
        animationSpec = tween(600),
        label = "tooth_color"
    )

    val shadowColor by animateColorAsState(
        targetValue = when {
            healthPercent >= 85 -> Color(0xFFE0ECE5)
            healthPercent >= 65 -> Color(0xFFE5EDE1)
            healthPercent >= 40 -> Color(0xFFE5D5A8)
            else -> Color(0xFFC7AC70)
        },
        animationSpec = tween(600),
        label = "shadow_color"
    )

    var tapTrigger by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = onClick != null
            ) {
                tapTrigger = !tapTrigger
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val w = size.width
            val h = size.height
            val offsetY = bounceOffset

            // Draw Tooth Path
            val toothPath = Path().apply {
                // Start at top center depression
                moveTo(w * 0.5f, h * (0.16f) + offsetY)
                // Left crown cusp
                cubicTo(
                    w * 0.38f, h * 0.11f + offsetY,
                    w * 0.18f, h * 0.13f + offsetY,
                    w * 0.15f, h * 0.28f + offsetY
                )
                // Left flank
                cubicTo(
                    w * 0.12f, h * 0.44f + offsetY,
                    w * 0.18f, h * 0.62f + offsetY,
                    w * 0.24f, h * 0.78f + offsetY
                )
                // Left root tip
                cubicTo(
                    w * 0.26f, h * 0.88f + offsetY,
                    w * 0.38f, h * 0.88f + offsetY,
                    w * 0.40f, h * 0.74f + offsetY
                )
                // Inter-root crotch (bifurcation)
                cubicTo(
                    w * 0.44f, h * 0.56f + offsetY,
                    w * 0.56f, h * 0.56f + offsetY,
                    w * 0.60f, h * 0.74f + offsetY
                )
                // Right root tip
                cubicTo(
                    w * 0.62f, h * 0.88f + offsetY,
                    w * 0.74f, h * 0.88f + offsetY,
                    w * 0.76f, h * 0.78f + offsetY
                )
                // Right flank
                cubicTo(
                    w * 0.82f, h * 0.62f + offsetY,
                    w * 0.88f, h * 0.44f + offsetY,
                    w * 0.85f, h * 0.28f + offsetY
                )
                // Right crown cusp
                cubicTo(
                    w * 0.82f, h * 0.13f + offsetY,
                    w * 0.62f, h * 0.11f + offsetY,
                    w * 0.5f, h * 0.16f + offsetY
                )
                close()
            }

            // Draw Tooth Drop Shadow / Background Glow
            drawPath(
                path = toothPath,
                color = shadowColor
            )

            // Draw Tooth Body Fill (with subtle gradient)
            val fillBrush = Brush.verticalGradient(
                colors = listOf(
                    baseToothColor,
                    shadowColor
                ),
                startY = 0f,
                endY = h
            )
            drawPath(
                path = toothPath,
                brush = fillBrush
            )

            // Outline Stroke
            val outlineColor = when {
                healthPercent >= 65 -> Color(0xFF5A755D).copy(alpha = 0.25f)
                healthPercent >= 40 -> Color(0xFF9E874D).copy(alpha = 0.4f)
                else -> Color(0xFF6B4A1D).copy(alpha = 0.6f)
            }
            drawPath(
                path = toothPath,
                color = outlineColor,
                style = Stroke(width = w * 0.024f, cap = StrokeCap.Round)
            )

            // Crown Gloss Highlight (When Clean)
            if (healthPercent >= 60) {
                val glossPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.22f + offsetY)
                    cubicTo(
                        w * 0.26f, h * 0.18f + offsetY,
                        w * 0.36f, h * 0.18f + offsetY,
                        w * 0.40f, h * 0.22f + offsetY
                    )
                }
                drawPath(
                    path = glossPath,
                    color = Color.White.copy(alpha = 0.75f),
                    style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
                )
            }

            // --- PLAQUE & DIRT OVERLAYS (WHEN HEALTH IS LOW / BAD HABIT) ---
            if (healthPercent < 70) {
                // Plaque stain at gumline / side flanks
                val plaqueAlpha = ((70 - healthPercent) / 70f).coerceIn(0.2f, 0.9f)

                // Plaque Patch Left
                drawCircle(
                    color = Color(0xFFD6A838).copy(alpha = plaqueAlpha * 0.7f),
                    radius = w * (0.09f + (100 - healthPercent) * 0.0008f),
                    center = Offset(w * 0.24f, h * 0.52f + offsetY + dirtWobble)
                )

                // Plaque Patch Right
                drawCircle(
                    color = Color(0xFFD6A838).copy(alpha = plaqueAlpha * 0.7f),
                    radius = w * (0.08f + (100 - healthPercent) * 0.0006f),
                    center = Offset(w * 0.76f, h * 0.48f + offsetY - dirtWobble)
                )

                // Plaque patch near root crotch
                drawRoundRect(
                    color = Color(0xFFBF8826).copy(alpha = plaqueAlpha * 0.65f),
                    topLeft = Offset(w * 0.42f, h * 0.58f + offsetY),
                    size = Size(w * 0.16f, h * 0.10f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            // HEAVY TARTAR & DIRTY SPECKS (WHEN HEALTH < 40%)
            if (healthPercent < 40) {
                val darkTartar = Color(0xFF805217).copy(alpha = 0.85f)
                // Tartar on left root
                drawCircle(
                    color = darkTartar,
                    radius = w * 0.065f,
                    center = Offset(w * 0.32f, h * 0.76f + offsetY)
                )
                // Tartar on right root
                drawCircle(
                    color = darkTartar,
                    radius = w * 0.07f,
                    center = Offset(w * 0.68f, h * 0.74f + offsetY)
                )
                // Food debris specks on crown
                drawCircle(
                    color = Color(0xFF503512),
                    radius = w * 0.035f,
                    center = Offset(w * 0.34f, h * 0.28f + offsetY)
                )
                drawCircle(
                    color = Color(0xFF503512),
                    radius = w * 0.03f,
                    center = Offset(w * 0.66f, h * 0.30f + offsetY)
                )
            }

            // --- CUTE FACIAL EXPRESSIONS ---
            val eyeCenterY = h * 0.38f + offsetY
            val eyeSpacing = w * 0.15f
            val eyeColor = if (healthPercent >= 40) Color(0xFF2C3E30) else Color(0xFF4A3215)

            if (healthPercent >= 75) {
                // HAPPY SPARKLING EYES (^ ^ or curved arcs)
                val leftEyePath = Path().apply {
                    moveTo(w * 0.5f - eyeSpacing - w * 0.045f, eyeCenterY + h * 0.015f)
                    cubicTo(
                        w * 0.5f - eyeSpacing - w * 0.02f, eyeCenterY - h * 0.035f,
                        w * 0.5f - eyeSpacing + w * 0.02f, eyeCenterY - h * 0.035f,
                        w * 0.5f - eyeSpacing + w * 0.045f, eyeCenterY + h * 0.015f
                    )
                }
                drawPath(
                    path = leftEyePath,
                    color = eyeColor,
                    style = Stroke(width = w * 0.038f, cap = StrokeCap.Round)
                )

                val rightEyePath = Path().apply {
                    moveTo(w * 0.5f + eyeSpacing - w * 0.045f, eyeCenterY + h * 0.015f)
                    cubicTo(
                        w * 0.5f + eyeSpacing - w * 0.02f, eyeCenterY - h * 0.035f,
                        w * 0.5f + eyeSpacing + w * 0.02f, eyeCenterY - h * 0.035f,
                        w * 0.5f + eyeSpacing + w * 0.045f, eyeCenterY + h * 0.015f
                    )
                }
                drawPath(
                    path = rightEyePath,
                    color = eyeColor,
                    style = Stroke(width = w * 0.038f, cap = StrokeCap.Round)
                )

                // Cheerful Open Smile
                val mouthPath = Path().apply {
                    moveTo(w * 0.42f, h * 0.44f + offsetY)
                    quadraticTo(
                        w * 0.50f, h * 0.52f + offsetY,
                        w * 0.58f, h * 0.44f + offsetY
                    )
                }
                drawPath(
                    path = mouthPath,
                    color = Color(0xFFD81B60),
                    style = Fill
                )
                drawPath(
                    path = mouthPath,
                    color = eyeColor,
                    style = Stroke(width = w * 0.025f, cap = StrokeCap.Round)
                )

                // Cute Pink Blushing Cheeks
                drawCircle(
                    color = Color(0xFFFF8A9E).copy(alpha = 0.55f),
                    radius = w * 0.065f,
                    center = Offset(w * 0.28f, h * 0.44f + offsetY)
                )
                drawCircle(
                    color = Color(0xFFFF8A9E).copy(alpha = 0.55f),
                    radius = w * 0.065f,
                    center = Offset(w * 0.72f, h * 0.44f + offsetY)
                )

                // Floating Animated Sparkle Stars (✨)
                drawSparkleStar(
                    center = Offset(w * 0.16f, h * 0.16f + offsetY),
                    radius = w * 0.08f * sparkleScale,
                    color = Color(0xFFF59E0B)
                )
                drawSparkleStar(
                    center = Offset(w * 0.84f, h * 0.20f + offsetY),
                    radius = w * 0.07f * sparkleScale,
                    color = Color(0xFF38A169)
                )

            } else if (healthPercent >= 45) {
                // NEUTRAL / CONCERNED FACE (• •)
                drawCircle(
                    color = eyeColor,
                    radius = w * 0.038f,
                    center = Offset(w * 0.5f - eyeSpacing, eyeCenterY)
                )
                drawCircle(
                    color = eyeColor,
                    radius = w * 0.038f,
                    center = Offset(w * 0.5f + eyeSpacing, eyeCenterY)
                )

                // Gentle wary mouth (—)
                val mouthPath = Path().apply {
                    moveTo(w * 0.44f, h * 0.46f + offsetY)
                    quadraticTo(
                        w * 0.50f, h * 0.44f + offsetY,
                        w * 0.56f, h * 0.46f + offsetY
                    )
                }
                drawPath(
                    path = mouthPath,
                    color = eyeColor,
                    style = Stroke(width = w * 0.03f, cap = StrokeCap.Round)
                )

                // Mild blush
                drawCircle(
                    color = Color(0xFFFFB74D).copy(alpha = 0.4f),
                    radius = w * 0.05f,
                    center = Offset(w * 0.30f, h * 0.44f + offsetY)
                )
                drawCircle(
                    color = Color(0xFFFFB74D).copy(alpha = 0.4f),
                    radius = w * 0.05f,
                    center = Offset(w * 0.70f, h * 0.44f + offsetY)
                )
            } else {
                // SAD / DISTRESSED CRYING FACE (>_< or T_T)
                // Left sad teardrop eye
                val leftSad = Path().apply {
                    moveTo(w * 0.5f - eyeSpacing - w * 0.035f, eyeCenterY - h * 0.015f)
                    lineTo(w * 0.5f - eyeSpacing + w * 0.035f, eyeCenterY + h * 0.015f)
                    moveTo(w * 0.5f - eyeSpacing + w * 0.035f, eyeCenterY - h * 0.015f)
                    lineTo(w * 0.5f - eyeSpacing - w * 0.035f, eyeCenterY + h * 0.015f)
                }
                drawPath(
                    path = leftSad,
                    color = eyeColor,
                    style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
                )

                val rightSad = Path().apply {
                    moveTo(w * 0.5f + eyeSpacing - w * 0.035f, eyeCenterY - h * 0.015f)
                    lineTo(w * 0.5f + eyeSpacing + w * 0.035f, eyeCenterY + h * 0.015f)
                    moveTo(w * 0.5f + eyeSpacing + w * 0.035f, eyeCenterY - h * 0.015f)
                    lineTo(w * 0.5f + eyeSpacing - w * 0.035f, eyeCenterY + h * 0.015f)
                }
                drawPath(
                    path = rightSad,
                    color = eyeColor,
                    style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
                )

                // Frowning wavy mouth (︵)
                val frownPath = Path().apply {
                    moveTo(w * 0.43f, h * 0.48f + offsetY)
                    quadraticTo(
                        w * 0.50f, h * 0.43f + offsetY,
                        w * 0.57f, h * 0.48f + offsetY
                    )
                }
                drawPath(
                    path = frownPath,
                    color = Color(0xFF6B261A),
                    style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
                )

                // Sweat drop on forehead (💧)
                val sweatCenter = Offset(w * 0.78f, h * 0.22f + offsetY)
                drawCircle(
                    color = Color(0xFF42A5F5),
                    radius = w * 0.045f,
                    center = sweatCenter
                )

                // Dirt smudge mark on cheek
                drawCircle(
                    color = Color(0xFF8D6E63).copy(alpha = 0.6f),
                    radius = w * 0.05f,
                    center = Offset(w * 0.30f, h * 0.44f + offsetY)
                )
            }
        }
    }
}

private fun DrawScope.drawSparkleStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path = path, color = color)
}
