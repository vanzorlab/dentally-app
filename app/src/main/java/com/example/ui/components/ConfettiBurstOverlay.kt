package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val angleRad: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

/**
 * Lightweight Compose Canvas confetti celebration burst (zero external dependencies or assets).
 */
@Composable
fun ConfettiBurstOverlay(
    triggerKey: Int,
    modifier: Modifier = Modifier
) {
    if (triggerKey <= 0) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey) {
        val colors = listOf(
            Color(0xFFC81E32),
            Color(0xFFE13347),
            Color(0xFFF59E0B),
            Color(0xFF16A34A),
            Color(0xFFEC4899)
        )
        List(28) { idx ->
            ConfettiParticle(
                angleRad = (idx / 28f) * (2f * Math.PI.toFloat()) + Random.nextFloat() * 0.2f,
                speed = 160f + Random.nextFloat() * 240f,
                size = 6f + Random.nextFloat() * 7f,
                color = colors[idx % colors.size]
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
        )
    }

    val p = progress.value
    if (p < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.35f
            val alpha = (1f - p).coerceIn(0f, 1f)

            particles.forEach { particle ->
                val dist = particle.speed * p
                val gravityY = 180f * p * p
                val x = cx + cos(particle.angleRad) * dist
                val y = cy + sin(particle.angleRad) * dist + gravityY
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}
