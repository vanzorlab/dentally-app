package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fullscreen kinetic launch intro animation played when opening Dentally.
 * Features breathing concentric sonar waves, sweeping luminous orbital arcs,
 * spring-animated tooth mascot, staggered typography, and optional coordination
 * with initial cloud/auth loading so entering the app feels fluid and natural.
 */
@Composable
fun LaunchSplashOverlay(
    visible: Boolean,
    isBusyLoading: Boolean = false,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestBusy by rememberUpdatedState(isBusyLoading)

    val logoScale = remember { Animatable(0.38f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoTilt = remember { Animatable(-8f) }
    val titleProgress = remember { Animatable(0f) }
    val subtitleProgress = remember { Animatable(0f) }
    val barProgress = remember { Animatable(0f) }
    val ringExpand = remember { Animatable(0.55f) }

    val statusMessages = remember {
        listOf(
            "Menyiapkan ruang sehatmu...",
            "Menyelaraskan data awanmu...",
            "Hampir siap untukmu..."
        )
    }
    var statusIndex by remember { mutableIntStateOf(0) }
    var isExiting by remember { mutableStateOf(false) }

    val exitAlpha by animateFloatAsState(
        targetValue = if (isExiting || !visible) 0f else 1f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "splash_exit_alpha"
    )
    val exitScale by animateFloatAsState(
        targetValue = if (isExiting || !visible) 1.06f else 1f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "splash_exit_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash_ambient")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )
    val pulseWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_wave"
    )
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing_y"
    )

    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        isExiting = false
        statusIndex = 0

        launch {
            ringExpand.animateTo(
                targetValue = 1.16f,
                animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(320))
        }
        launch {
            logoTilt.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            delay(220)
            titleProgress.animateTo(1f, animationSpec = tween(460, easing = FastOutSlowInEasing))
        }
        launch {
            delay(400)
            subtitleProgress.animateTo(1f, animationSpec = tween(460, easing = FastOutSlowInEasing))
        }
        launch {
            delay(240)
            barProgress.animateTo(0.86f, animationSpec = tween(1450, easing = FastOutSlowInEasing))
        }

        delay(1700)
        // Smoothly hold a little longer only if initial sign-in/cloud sync is actively finishing
        if (latestBusy) {
            statusIndex = 1
            var waited = 0L
            while (latestBusy && waited < 1200L) {
                delay(150)
                waited += 150L
                if (waited >= 600L) statusIndex = 2
            }
        }
        barProgress.animateTo(1f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        delay(100)
        isExiting = true
        delay(330)
        onFinished()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = exitAlpha
                    scaleX = exitScale
                    scaleY = exitScale
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5A0A16),
                            Color(0xFF8C1123),
                            Color(0xFFC81E32),
                            Color(0xFF6E0C1A)
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isExiting) {
                        isExiting = true
                        onFinished()
                    }
                }
                .testTag("launch_splash_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Ambient glowing geometric waves, orbital rings & sparkles behind the mascot
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.42f)
                val baseRadius = size.minDimension * 0.24f

                // Expanding sonar breath wave
                drawCircle(
                    color = Color(0xFFFFC2C9).copy(alpha = (1f - pulseWave) * 0.22f),
                    radius = baseRadius * (1.15f + pulseWave * 1.15f),
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Outer expanding soft halo
                drawCircle(
                    color = Color(0xFFFFB3BD).copy(alpha = 0.09f),
                    radius = baseRadius * 1.95f * ringExpand.value,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFFD6DB).copy(alpha = 0.16f),
                    radius = baseRadius * 1.45f * ringExpand.value,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Kinetic sweeping orbital arcs
                drawArc(
                    color = Color(0xFFFFD4DA).copy(alpha = 0.78f),
                    startAngle = orbitAngle,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 1.22f, center.y - baseRadius * 1.22f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.44f, baseRadius * 2.44f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                drawArc(
                    color = Color(0xFFFFE5B4).copy(alpha = 0.52f),
                    startAngle = orbitAngle + 180f,
                    sweepAngle = 72f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 1.42f, center.y - baseRadius * 1.42f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.84f, baseRadius * 2.84f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Orbiting luminous dot particles
                val rad1 = Math.toRadians(orbitAngle.toDouble())
                val rad2 = Math.toRadians((orbitAngle + 165f).toDouble())
                val r1 = baseRadius * 1.22f
                val r2 = baseRadius * 1.42f

                drawCircle(
                    color = Color(0xFFFFF6DC),
                    radius = 4.5.dp.toPx(),
                    center = Offset(
                        center.x + (r1 * cos(rad1)).toFloat(),
                        center.y + (r1 * sin(rad1)).toFloat()
                    )
                )
                drawCircle(
                    color = Color(0xFFFFC8D0),
                    radius = 3.5.dp.toPx(),
                    center = Offset(
                        center.x + (r2 * cos(rad2)).toFloat(),
                        center.y + (r2 * sin(rad2)).toFloat()
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hero Mascot Emblem with subtle glow ring
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value
                            rotationZ = logoTilt.value
                            translationY = bobbingOffset * 2f
                        }
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFECEF)
                                )
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    DentallyLogoIcon(sizeDp = 76.dp, isSelected = false)
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Subtle modern pill badge
                Box(
                    modifier = Modifier
                        .alpha(titleProgress.value)
                        .offset(y = ((1f - titleProgress.value) * 14f).dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SMART ORAL CARE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFFE1E5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Staggered Title Reveal
                Text(
                    text = "Dentally",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 37.sp,
                        letterSpacing = (1.2f + (1f - titleProgress.value) * 7f).sp
                    ),
                    modifier = Modifier
                        .alpha(titleProgress.value)
                        .offset(y = ((1f - titleProgress.value) * 18f).dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline Reveal
                Text(
                    text = "Jaga Gigi & Gusi Sehat Setiap Hari",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFFFD8DD),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(subtitleProgress.value)
                        .offset(y = ((1f - subtitleProgress.value) * 14f).dp)
                )

                Spacer(modifier = Modifier.height(38.dp))

                // Sleek Animated Loading Progress Bar + Smooth Rotating Status Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(subtitleProgress.value)
                ) {
                    Box(
                        modifier = Modifier
                            .width(184.dp)
                            .height(5.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(barProgress.value.coerceIn(0.06f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFB5BF),
                                            Color(0xFFFFFFFF)
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedContent(
                        targetState = statusMessages[statusIndex.coerceIn(0, statusMessages.lastIndex)],
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                        label = "splash_status_text"
                    ) { caption ->
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
