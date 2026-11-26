package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reusable luminous shimmer brush so loading cards feel fluid and alive rather than frozen.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_wave")
    val translateAnim by transition.animateFloat(
        initialValue = -350f,
        targetValue = 1100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFFF6ECEE),
            Color(0xFFFCF8F8),
            Color(0xFFF6ECEE)
        ),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 320f, 180f)
    )
}

/**
 * Smooth staggered entrance wrapper for cards & sections so screens glide in cleanly
 * instead of snapping statically all at once.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerItemMs: Int = 55,
    content: @Composable () -> Unit
) {
    val progress = remember(index) { Animatable(0f) }

    LaunchedEffect(index) {
        val waitMs = (index.coerceIn(0, 8) * delayPerItemMs).toLong()
        if (waitMs > 0) delay(waitMs)
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
            )
        }
    }

    val p = progress.value
    Box(
        modifier = modifier.graphicsLayer {
            alpha = p
            translationY = (1f - p) * 20.dp.toPx()
            val scale = 0.965f + p * 0.035f
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

/**
 * Sleek shimmering skeleton layout shown on Home while initial cloud sync fetches data,
 * eliminating abrupt blank jumps or stiff frozen spinners.
 */
@Composable
fun HomeSyncSkeletonView(
    modifier: Modifier = Modifier
) {
    val shimmer = rememberShimmerBrush()
    val pulseTransition = rememberInfiniteTransition(label = "sync_badge_pulse")
    val badgeScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sync_pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .testTag("home_sync_skeleton"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        userScrollEnabled = false
    ) {
        // Active sync status pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DentallyTintBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer {
                            scaleX = badgeScale
                            scaleY = badgeScale
                        }
                        .clip(CircleShape)
                        .background(NaturalPrimary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Menyelaraskan data & progres gigimu...",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SageDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        // Profile header skeleton
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(shimmer)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(136.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(shimmer)
                        )
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .height(11.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(shimmer)
                        )
                    }
                }
            }
        }

        // Hero Timer skeleton
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(shimmer)
                    )
                    Box(
                        modifier = Modifier
                            .size(146.dp)
                            .clip(CircleShape)
                            .background(shimmer)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(shimmer)
                    )
                }
            }
        }

        // Mood & Habit card skeletons
        items(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmer)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(shimmer)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.45f)
                                .height(11.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(shimmer)
                        )
                    }
                }
            }
        }
    }
}
