package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary

/**
 * Custom modern Checkbox:
 * - Kebiasaan Baik Selesai: Semantic Health Green square with white checkmark.
 * - Kebiasaan Buruk Aktif: Alert Crimson/Red square with warning icon.
 * - Belum Selesai: Crisp rose-tinted border square.
 */
@Composable
fun CustomStatusCheckbox(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isBadHabit: Boolean = false,
    sizeDp: Dp = 26.dp
) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                when {
                    checked && isBadHabit -> Color(0xFFDC2626)
                    checked -> HealthSuccess
                    else -> Color.White
                }
            )
            .then(
                if (!checked) {
                    Modifier.border(
                        width = 1.75.dp,
                        color = Color(0xFFB8A4A7),
                        shape = RoundedCornerShape(7.dp)
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            if (isBadHabit) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Aktif",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selesai",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Custom high-resolution vector illustrations for all dental and lifestyle habits.
 * No emojis used!
 */
@Composable
fun CustomHabitIcon(
    iconKey: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 32.dp,
    isBadHabit: Boolean = false
) {
    val normalizedKey = iconKey.lowercase().trim()

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            when {
                // Sikat Gigi / Toothbrush
                normalizedKey.contains("sikat") && normalizedKey.contains("malam") || normalizedKey == "night_brush" || normalizedKey == "🌙" -> {
                    drawNightToothbrush(size)
                }
                normalizedKey.contains("sikat") || normalizedKey.contains("brush") || normalizedKey == "toothbrush" || normalizedKey == "🪥" -> {
                    drawCleanToothbrush(size)
                }
                // Dental Floss
                normalizedKey.contains("floss") || normalizedKey.contains("benang") || normalizedKey == "thread" || normalizedKey == "🧵" -> {
                    drawDentalFloss(size)
                }
                // Kumur Air / Mouthwash
                normalizedKey.contains("kumur") || normalizedKey.contains("air") || normalizedKey.contains("water") || normalizedKey == "💧" -> {
                    drawWaterMouthwash(size)
                }
                // Lidah / Tongue
                normalizedKey.contains("lidah") || normalizedKey.contains("tongue") || normalizedKey == "👅" -> {
                    drawTongueCleaner(size)
                }
                // Cemilan Manis / Candy
                normalizedKey.contains("manis") || normalizedKey.contains("candy") || normalizedKey.contains("gula") || normalizedKey == "🍬" || normalizedKey == "🍫" || normalizedKey == "🍩" -> {
                    drawSweetCandy(size)
                }
                // Kopi / Teh / Coffee
                normalizedKey.contains("kopi") || normalizedKey.contains("teh") || normalizedKey.contains("coffee") || normalizedKey == "☕" -> {
                    drawCoffeeCup(size)
                }
                // Boba / Soda
                normalizedKey.contains("soda") || normalizedKey.contains("boba") || normalizedKey.contains("minuman") || normalizedKey == "🥤" -> {
                    drawSodaCup(size)
                }
                // Merokok / Smoke
                normalizedKey.contains("rokok") || normalizedKey.contains("smoke") || normalizedKey == "🚬" -> {
                    drawCigaretteWarning(size)
                }
                // Tracker / Pekerjaan / Catatan (Pencil & Notepad like reference image!)
                normalizedKey.contains("tracker") || normalizedKey.contains("kerjaan") || normalizedKey.contains("tugas") || normalizedKey.contains("pencil") || normalizedKey == "📝" -> {
                    drawNotepadWithPencil(size)
                }
                // Baca Buku (Open Book like reference image!)
                normalizedKey.contains("buku") || normalizedKey.contains("book") || normalizedKey.contains("baca") || normalizedKey == "📖" -> {
                    drawOpenBook(size)
                }
                // Apel Sehat
                normalizedKey.contains("apel") || normalizedKey.contains("apple") || normalizedKey == "🍎" -> {
                    drawFreshApple(size)
                }
                // Susu Kalsium
                normalizedKey.contains("susu") || normalizedKey.contains("milk") || normalizedKey == "🥛" -> {
                    drawMilkGlass(size)
                }
                // Jam / Timer
                normalizedKey.contains("jam") || normalizedKey.contains("timer") || normalizedKey.contains("clock") || normalizedKey == "⏰" || normalizedKey == "⏱️" -> {
                    drawTimerClock(size)
                }
                // Trofi / Juara
                normalizedKey.contains("trofi") || normalizedKey.contains("juara") || normalizedKey == "🏆" -> {
                    drawTrophy(size)
                }
                // Berlian / Diamond
                normalizedKey.contains("diamond") || normalizedKey.contains("berlian") || normalizedKey == "💎" -> {
                    drawDiamondGem(size)
                }
                // Perisai / Shield
                normalizedKey.contains("shield") || normalizedKey.contains("perisai") || normalizedKey == "🛡️" -> {
                    drawShieldIcon(size)
                }
                // Default fallback
                isBadHabit -> {
                    drawSweetCandy(size)
                }
                else -> {
                    drawCleanToothbrush(size)
                }
            }
        }
    }
}

// ---------------------- VECTOR DRAWING FUNCTIONS ----------------------

/** 📝 Illustrated Notepad with Yellow Pencil (Exactly matching reference screenshot) */
private fun DrawScope.drawNotepadWithPencil(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // 1. Notepad paper body (light blue-grey lined notepad)
    val paperPath = Path().apply {
        moveTo(w * 0.15f, h * 0.20f)
        lineTo(w * 0.72f, h * 0.20f)
        lineTo(w * 0.72f, h * 0.88f)
        lineTo(w * 0.15f, h * 0.88f)
        close()
    }
    drawPath(paperPath, color = Color(0xFFF0F5F8))
    drawPath(
        paperPath,
        color = Color(0xFF90A4AE),
        style = Stroke(width = w * 0.04f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Notepad spiral top bindings
    val ringSpacing = w * 0.11f
    for (i in 0..4) {
        val rx = w * 0.22f + (i * ringSpacing)
        drawLine(
            color = Color(0xFF607D8B),
            start = Offset(rx, h * 0.14f),
            end = Offset(rx, h * 0.24f),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )
    }

    // Notepad horizontal lines
    drawLine(
        color = Color(0xFFCFD8DC),
        start = Offset(w * 0.25f, h * 0.40f),
        end = Offset(w * 0.62f, h * 0.40f),
        strokeWidth = w * 0.035f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFCFD8DC),
        start = Offset(w * 0.25f, h * 0.55f),
        end = Offset(w * 0.62f, h * 0.55f),
        strokeWidth = w * 0.035f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFCFD8DC),
        start = Offset(w * 0.25f, h * 0.70f),
        end = Offset(w * 0.50f, h * 0.70f),
        strokeWidth = w * 0.035f,
        cap = StrokeCap.Round
    )

    // 2. Diagonal Yellow Pencil overlay
    val pencilBody = Path().apply {
        moveTo(w * 0.45f, h * 0.70f)
        lineTo(w * 0.82f, h * 0.26f)
        lineTo(w * 0.92f, h * 0.35f)
        lineTo(w * 0.55f, h * 0.79f)
        close()
    }
    drawPath(pencilBody, color = Color(0xFFFFB300)) // Yellow wood
    drawPath(
        pencilBody,
        color = Color(0xFF5D4037),
        style = Stroke(width = w * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Pencil Tip (Triangle)
    val pencilTip = Path().apply {
        moveTo(w * 0.45f, h * 0.70f)
        lineTo(w * 0.55f, h * 0.79f)
        lineTo(w * 0.36f, h * 0.88f)
        close()
    }
    drawPath(pencilTip, color = Color(0xFFFFE082))
    drawPath(
        pencilTip,
        color = Color(0xFF5D4037),
        style = Stroke(width = w * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Graphite Lead Tip
    val leadTip = Path().apply {
        moveTo(w * 0.40f, h * 0.84f)
        lineTo(w * 0.44f, h * 0.87f)
        lineTo(w * 0.36f, h * 0.88f)
        close()
    }
    drawPath(leadTip, color = Color(0xFF263238))

    // Pencil Eraser (Pink top)
    val eraser = Path().apply {
        moveTo(w * 0.82f, h * 0.26f)
        lineTo(w * 0.92f, h * 0.35f)
        lineTo(w * 0.96f, h * 0.30f)
        lineTo(w * 0.86f, h * 0.21f)
        close()
    }
    drawPath(eraser, color = Color(0xFFFF80AB))
    drawPath(
        eraser,
        color = Color(0xFF5D4037),
        style = Stroke(width = w * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/** 📖 Illustrated Open Book (Matching reference screenshot) */
private fun DrawScope.drawOpenBook(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Spine bottom point
    val spineX = w * 0.50f
    val spineY = h * 0.76f

    // Left Page
    val leftPage = Path().apply {
        moveTo(spineX, spineY)
        cubicTo(w * 0.36f, h * 0.73f, w * 0.20f, h * 0.76f, w * 0.12f, h * 0.70f)
        lineTo(w * 0.12f, h * 0.30f)
        cubicTo(w * 0.20f, h * 0.36f, w * 0.36f, h * 0.33f, spineX, h * 0.38f)
        close()
    }
    // Right Page
    val rightPage = Path().apply {
        moveTo(spineX, spineY)
        cubicTo(w * 0.64f, h * 0.73f, w * 0.80f, h * 0.76f, w * 0.88f, h * 0.70f)
        lineTo(w * 0.88f, h * 0.30f)
        cubicTo(w * 0.80f, h * 0.36f, w * 0.64f, h * 0.33f, spineX, h * 0.38f)
        close()
    }

    // Book Cover Underlay (Deep Slate / Burgundy)
    val coverPath = Path().apply {
        moveTo(w * 0.10f, h * 0.74f)
        lineTo(w * 0.10f, h * 0.32f)
        lineTo(spineX, h * 0.41f)
        lineTo(w * 0.90f, h * 0.32f)
        lineTo(w * 0.90f, h * 0.74f)
        lineTo(spineX, h * 0.82f)
        close()
    }
    drawPath(coverPath, color = Color(0xFFC26D60))

    // Draw Pages (Cream white)
    drawPath(leftPage, color = Color(0xFFFAF8F5))
    drawPath(rightPage, color = Color(0xFFF3EFEA))

    val borderStroke = Stroke(width = w * 0.038f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(leftPage, color = Color(0xFF4A3B32), style = borderStroke)
    drawPath(rightPage, color = Color(0xFF4A3B32), style = borderStroke)

    // Center Spine Line
    drawLine(
        color = Color(0xFF4A3B32),
        start = Offset(spineX, h * 0.38f),
        end = Offset(spineX, spineY),
        strokeWidth = w * 0.04f,
        cap = StrokeCap.Round
    )

    // Left Page Text Lines
    drawLine(
        color = Color(0xFFBCAAA4),
        start = Offset(w * 0.20f, h * 0.44f),
        end = Offset(w * 0.42f, h * 0.46f),
        strokeWidth = w * 0.03f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFBCAAA4),
        start = Offset(w * 0.20f, h * 0.54f),
        end = Offset(w * 0.42f, h * 0.56f),
        strokeWidth = w * 0.03f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFBCAAA4),
        start = Offset(w * 0.20f, h * 0.64f),
        end = Offset(w * 0.36f, h * 0.65f),
        strokeWidth = w * 0.03f,
        cap = StrokeCap.Round
    )

    // Right Page Text Lines
    drawLine(
        color = Color(0xFFBCAAA4),
        start = Offset(w * 0.58f, h * 0.46f),
        end = Offset(w * 0.80f, h * 0.44f),
        strokeWidth = w * 0.03f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFBCAAA4),
        start = Offset(w * 0.58f, h * 0.56f),
        end = Offset(w * 0.80f, h * 0.54f),
        strokeWidth = w * 0.03f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFBCAAA4),
        start = Offset(w * 0.58f, h * 0.65f),
        end = Offset(w * 0.74f, h * 0.64f),
        strokeWidth = w * 0.03f,
        cap = StrokeCap.Round
    )
}

/** 🪥 Illustrated Clean Toothbrush */
private fun DrawScope.drawCleanToothbrush(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Toothbrush Handle (Mint Green / Sage)
    val handlePath = Path().apply {
        moveTo(w * 0.20f, h * 0.85f)
        cubicTo(w * 0.35f, h * 0.70f, w * 0.48f, h * 0.55f, w * 0.60f, h * 0.42f)
        lineTo(w * 0.68f, h * 0.48f)
        cubicTo(w * 0.56f, h * 0.62f, w * 0.42f, h * 0.78f, w * 0.26f, h * 0.90f)
        close()
    }
    drawPath(handlePath, color = Color(0xFFC81E32))

    // Toothbrush Head (White bristles base)
    val headPath = Path().apply {
        moveTo(w * 0.60f, h * 0.42f)
        lineTo(w * 0.80f, h * 0.22f)
        lineTo(w * 0.88f, h * 0.28f)
        lineTo(w * 0.68f, h * 0.48f)
        close()
    }
    drawPath(headPath, color = Color(0xFFE13347))

    // Bristles (Crisp Sky Blue / White)
    val bristlePath = Path().apply {
        moveTo(w * 0.66f, h * 0.36f)
        lineTo(w * 0.58f, h * 0.25f)
        lineTo(w * 0.76f, h * 0.10f)
        lineTo(w * 0.84f, h * 0.20f)
        close()
    }
    drawPath(bristlePath, color = Color(0xFFE1F5FE))
    drawPath(
        bristlePath,
        color = Color(0xFF29B6F6),
        style = Stroke(width = w * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Sparkle star on top of bristles
    drawSparkleStar(Offset(w * 0.78f, h * 0.16f), w * 0.10f, Color(0xFFF59E0B))
}

/** 🌙 Illustrated Night Toothbrush */
private fun DrawScope.drawNightToothbrush(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Crescent Moon in Background
    val moonPath = Path().apply {
        moveTo(w * 0.68f, h * 0.15f)
        cubicTo(w * 0.88f, h * 0.22f, w * 0.92f, h * 0.48f, w * 0.76f, h * 0.64f)
        cubicTo(w * 0.58f, h * 0.44f, w * 0.60f, h * 0.24f, w * 0.68f, h * 0.15f)
        close()
    }
    drawPath(moonPath, color = Color(0xFFFFCA28))

    // Night Toothbrush Handle (Deep Indigo / Slate)
    val handlePath = Path().apply {
        moveTo(w * 0.16f, h * 0.86f)
        cubicTo(w * 0.30f, h * 0.72f, w * 0.44f, h * 0.58f, w * 0.54f, h * 0.46f)
        lineTo(w * 0.62f, h * 0.52f)
        cubicTo(w * 0.50f, h * 0.66f, w * 0.36f, h * 0.80f, w * 0.22f, h * 0.92f)
        close()
    }
    drawPath(handlePath, color = Color(0xFF3F51B5))

    // Bristles
    val bristlePath = Path().apply {
        moveTo(w * 0.52f, h * 0.40f)
        lineTo(w * 0.44f, h * 0.30f)
        lineTo(w * 0.62f, h * 0.16f)
        lineTo(w * 0.70f, h * 0.26f)
        close()
    }
    drawPath(bristlePath, color = Color(0xFFC5CAE9))
    drawPath(
        bristlePath,
        color = Color(0xFF283593),
        style = Stroke(width = w * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Star sparkle
    drawSparkleStar(Offset(w * 0.25f, h * 0.28f), w * 0.08f, Color(0xFFFFD54F))
}

/** 🧵 Illustrated Dental Floss Spool */
private fun DrawScope.drawDentalFloss(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Floss Case Dispenser (Rounded Teal Container)
    drawRoundRect(
        color = Color(0xFF26A69A),
        topLeft = Offset(w * 0.22f, h * 0.34f),
        size = Size(w * 0.56f, h * 0.52f),
        cornerRadius = CornerRadius(w * 0.20f, w * 0.20f)
    )

    // Window Circle showing floss spool inside
    drawCircle(
        color = Color(0xFFE0F2F1),
        radius = w * 0.16f,
        center = Offset(w * 0.50f, h * 0.60f)
    )
    drawCircle(
        color = Color(0xFF00695C),
        radius = w * 0.06f,
        center = Offset(w * 0.50f, h * 0.60f)
    )

    // Top metal cutter tab
    drawRoundRect(
        color = Color(0xFFCFD8DC),
        topLeft = Offset(w * 0.44f, h * 0.26f),
        size = Size(w * 0.12f, h * 0.09f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Curving Dental Floss String pulling out
    val stringPath = Path().apply {
        moveTo(w * 0.50f, h * 0.26f)
        cubicTo(w * 0.52f, h * 0.12f, w * 0.74f, h * 0.14f, w * 0.82f, h * 0.22f)
        cubicTo(w * 0.90f, h * 0.30f, w * 0.85f, h * 0.42f, w * 0.92f, h * 0.48f)
    }
    drawPath(
        stringPath,
        color = Color(0xFF00796B),
        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
    )
}

/** 💧 Illustrated Water Droplet & Rinse Glass */
private fun DrawScope.drawWaterMouthwash(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Water Droplet (Vibrant Sky Blue)
    val dropPath = Path().apply {
        moveTo(w * 0.50f, h * 0.14f)
        cubicTo(w * 0.68f, h * 0.38f, w * 0.82f, h * 0.58f, w * 0.82f, h * 0.70f)
        cubicTo(w * 0.82f, h * 0.88f, w * 0.68f, h * 0.92f, w * 0.50f, h * 0.92f)
        cubicTo(w * 0.32f, h * 0.92f, w * 0.18f, h * 0.88f, w * 0.18f, h * 0.70f)
        cubicTo(w * 0.18f, h * 0.58f, w * 0.32f, h * 0.38f, w * 0.50f, h * 0.14f)
        close()
    }
    drawPath(dropPath, color = Color(0xFF29B6F6))

    // Water Drop Highlight / Reflection
    val shinePath = Path().apply {
        moveTo(w * 0.34f, h * 0.52f)
        cubicTo(w * 0.30f, h * 0.64f, w * 0.32f, h * 0.78f, w * 0.42f, h * 0.84f)
    }
    drawPath(
        shinePath,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(width = w * 0.05f, cap = StrokeCap.Round)
    )

    // Mini Splash Droplets
    drawCircle(color = Color(0xFF039BE5), radius = w * 0.05f, center = Offset(w * 0.78f, h * 0.30f))
    drawCircle(color = Color(0xFF039BE5), radius = w * 0.04f, center = Offset(w * 0.22f, h * 0.36f))
}

/** 👅 Illustrated Tongue Cleaner Tool */
private fun DrawScope.drawTongueCleaner(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Curved loop head
    val loopPath = Path().apply {
        moveTo(w * 0.25f, h * 0.55f)
        cubicTo(w * 0.15f, h * 0.35f, w * 0.30f, h * 0.15f, w * 0.50f, h * 0.15f)
        cubicTo(w * 0.70f, h * 0.15f, w * 0.85f, h * 0.35f, w * 0.75f, h * 0.55f)
    }
    drawPath(
        loopPath,
        color = Color(0xFFEC407A),
        style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
    )

    // Handle
    drawLine(
        color = Color(0xFFD81B60),
        start = Offset(w * 0.50f, h * 0.50f),
        end = Offset(w * 0.50f, h * 0.90f),
        strokeWidth = w * 0.09f,
        cap = StrokeCap.Round
    )
}

/** 🍬 Illustrated Wrapped Candy (Bad Habit) */
private fun DrawScope.drawSweetCandy(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Left Wrapper Twist
    val leftTwist = Path().apply {
        moveTo(w * 0.10f, h * 0.30f)
        lineTo(w * 0.32f, h * 0.50f)
        lineTo(w * 0.10f, h * 0.70f)
        close()
    }
    drawPath(leftTwist, color = Color(0xFFEF5350))

    // Right Wrapper Twist
    val rightTwist = Path().apply {
        moveTo(w * 0.90f, h * 0.30f)
        lineTo(w * 0.68f, h * 0.50f)
        lineTo(w * 0.90f, h * 0.70f)
        close()
    }
    drawPath(rightTwist, color = Color(0xFFEF5350))

    // Center Round Candy Body
    drawCircle(
        color = Color(0xFFE53935),
        radius = w * 0.24f,
        center = Offset(w * 0.50f, h * 0.50f)
    )

    // Sweet Swirl / Stripes on Candy
    val swirl = Path().apply {
        moveTo(w * 0.36f, h * 0.40f)
        cubicTo(w * 0.46f, h * 0.34f, w * 0.54f, h * 0.66f, w * 0.64f, h * 0.60f)
    }
    drawPath(
        swirl,
        color = Color(0xFFFFEBEE),
        style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
    )
}

/** ☕ Illustrated Steaming Coffee Cup (Bad Habit) */
private fun DrawScope.drawCoffeeCup(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Cup Body (Warm Terracotta / Dark Roast)
    val cupPath = Path().apply {
        moveTo(w * 0.22f, h * 0.40f)
        lineTo(w * 0.72f, h * 0.40f)
        lineTo(w * 0.66f, h * 0.80f)
        cubicTo(w * 0.64f, h * 0.88f, w * 0.30f, h * 0.88f, w * 0.28f, h * 0.80f)
        close()
    }
    drawPath(cupPath, color = Color(0xFF6D4C41))

    // Cup Handle
    val handlePath = Path().apply {
        moveTo(w * 0.70f, h * 0.46f)
        cubicTo(w * 0.88f, h * 0.46f, w * 0.88f, h * 0.74f, w * 0.66f, h * 0.74f)
    }
    drawPath(
        handlePath,
        color = Color(0xFF6D4C41),
        style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
    )

    // Steam Swirls
    val steam1 = Path().apply {
        moveTo(w * 0.36f, h * 0.32f)
        cubicTo(w * 0.32f, h * 0.22f, w * 0.42f, h * 0.16f, w * 0.38f, h * 0.10f)
    }
    val steam2 = Path().apply {
        moveTo(w * 0.54f, h * 0.32f)
        cubicTo(w * 0.50f, h * 0.22f, w * 0.60f, h * 0.16f, w * 0.56f, h * 0.10f)
    }
    drawPath(
        steam1,
        color = Color(0xFF8D6E63),
        style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
    )
    drawPath(
        steam2,
        color = Color(0xFF8D6E63),
        style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
    )
}

/** 🥤 Illustrated Soda Cup with Straw */
private fun DrawScope.drawSodaCup(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Straw (Red diagonal)
    val straw = Path().apply {
        moveTo(w * 0.48f, h * 0.40f)
        lineTo(w * 0.66f, h * 0.12f)
        lineTo(w * 0.78f, h * 0.14f)
    }
    drawPath(
        straw,
        color = Color(0xFFE53935),
        style = Stroke(width = w * 0.05f, cap = StrokeCap.Round)
    )

    // Cup Lid
    drawRoundRect(
        color = Color(0xFFECEFF1),
        topLeft = Offset(w * 0.22f, h * 0.34f),
        size = Size(w * 0.56f, h * 0.10f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Cup Body
    val cup = Path().apply {
        moveTo(w * 0.26f, h * 0.44f)
        lineTo(w * 0.74f, h * 0.44f)
        lineTo(w * 0.68f, h * 0.88f)
        lineTo(w * 0.32f, h * 0.88f)
        close()
    }
    drawPath(cup, color = Color(0xFFFF7043))
}

/** 🚬 Cigarette Warning Icon */
private fun DrawScope.drawCigaretteWarning(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Cigarette Filter (Brown/Orange)
    drawRoundRect(
        color = Color(0xFFFB8C00),
        topLeft = Offset(w * 0.15f, h * 0.44f),
        size = Size(w * 0.22f, h * 0.12f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Cigarette Body (White)
    drawRoundRect(
        color = Color(0xFFECEFF1),
        topLeft = Offset(w * 0.37f, h * 0.44f),
        size = Size(w * 0.44f, h * 0.12f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Red prohibition slash
    drawLine(
        color = Color(0xFFD32F2F),
        start = Offset(w * 0.20f, h * 0.80f),
        end = Offset(w * 0.80f, h * 0.20f),
        strokeWidth = w * 0.08f,
        cap = StrokeCap.Round
    )
}

/** 🍎 Fresh Healthy Apple */
private fun DrawScope.drawFreshApple(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Apple Body
    drawCircle(color = Color(0xFFE53935), radius = w * 0.32f, center = Offset(w * 0.50f, h * 0.56f))

    // Leaf
    val leaf = Path().apply {
        moveTo(w * 0.52f, h * 0.28f)
        quadraticTo(w * 0.72f, h * 0.16f, w * 0.78f, h * 0.28f)
        quadraticTo(w * 0.62f, h * 0.34f, w * 0.52f, h * 0.28f)
        close()
    }
    drawPath(leaf, color = Color(0xFF43A047))

    // Stem
    drawLine(
        color = Color(0xFF5D4037),
        start = Offset(w * 0.50f, h * 0.32f),
        end = Offset(w * 0.46f, h * 0.18f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round
    )
}

/** 🥛 Fresh Milk Glass */
private fun DrawScope.drawMilkGlass(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    val glass = Path().apply {
        moveTo(w * 0.26f, h * 0.22f)
        lineTo(w * 0.74f, h * 0.22f)
        lineTo(w * 0.68f, h * 0.86f)
        lineTo(w * 0.32f, h * 0.86f)
        close()
    }
    drawPath(glass, color = Color(0xFFECEFF1))
    drawPath(glass, color = Color(0xFF90A4AE), style = Stroke(width = w * 0.04f))

    // Milk Fill
    val milk = Path().apply {
        moveTo(w * 0.28f, h * 0.42f)
        lineTo(w * 0.72f, h * 0.42f)
        lineTo(w * 0.67f, h * 0.84f)
        lineTo(w * 0.33f, h * 0.84f)
        close()
    }
    drawPath(milk, color = Color.White)
}

/** ⏰ Modern Timer Clock */
private fun DrawScope.drawTimerClock(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Outer Clock Circle
    drawCircle(
        color = Color(0xFFFFF2F3),
        radius = w * 0.38f,
        center = Offset(w * 0.50f, h * 0.54f)
    )
    drawCircle(
        color = Color(0xFFC81E32),
        radius = w * 0.38f,
        center = Offset(w * 0.50f, h * 0.54f),
        style = Stroke(width = w * 0.07f)
    )

    // Clock Hands (2 minutes / sharp)
    drawLine(
        color = Color(0xFF1A1113),
        start = Offset(w * 0.50f, h * 0.54f),
        end = Offset(w * 0.50f, h * 0.30f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFC81E32),
        start = Offset(w * 0.50f, h * 0.54f),
        end = Offset(w * 0.68f, h * 0.54f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round
    )

    // Top Stopwatch Button
    drawRoundRect(
        color = Color(0xFFC81E32),
        topLeft = Offset(w * 0.44f, h * 0.06f),
        size = Size(w * 0.12f, h * 0.08f),
        cornerRadius = CornerRadius(3f, 3f)
    )
}

/** 🏆 Golden Trophy */
private fun DrawScope.drawTrophy(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    // Trophy Cup
    val cup = Path().apply {
        moveTo(w * 0.28f, h * 0.18f)
        lineTo(w * 0.72f, h * 0.18f)
        cubicTo(w * 0.72f, h * 0.52f, w * 0.58f, h * 0.62f, w * 0.50f, h * 0.62f)
        cubicTo(w * 0.42f, h * 0.62f, w * 0.28f, h * 0.52f, w * 0.28f, h * 0.18f)
        close()
    }
    drawPath(cup, color = Color(0xFFFFB300))

    // Handles
    val handleL = Path().apply {
        moveTo(w * 0.28f, h * 0.24f)
        cubicTo(w * 0.12f, h * 0.24f, w * 0.12f, h * 0.46f, w * 0.32f, h * 0.46f)
    }
    val handleR = Path().apply {
        moveTo(w * 0.72f, h * 0.24f)
        cubicTo(w * 0.88f, h * 0.24f, w * 0.88f, h * 0.46f, w * 0.68f, h * 0.46f)
    }
    drawPath(handleL, color = Color(0xFFFFA000), style = Stroke(width = w * 0.05f))
    drawPath(handleR, color = Color(0xFFFFA000), style = Stroke(width = w * 0.05f))

    // Base Stand
    drawLine(
        color = Color(0xFFFFA000),
        start = Offset(w * 0.50f, h * 0.62f),
        end = Offset(w * 0.50f, h * 0.78f),
        strokeWidth = w * 0.07f
    )
    drawRoundRect(
        color = Color(0xFF6D4C41),
        topLeft = Offset(w * 0.32f, h * 0.78f),
        size = Size(w * 0.36f, h * 0.12f),
        cornerRadius = CornerRadius(4f, 4f)
    )
}

/** 💎 Gemstone Diamond */
private fun DrawScope.drawDiamondGem(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    val diamond = Path().apply {
        moveTo(w * 0.26f, h * 0.24f)
        lineTo(w * 0.74f, h * 0.24f)
        lineTo(w * 0.90f, h * 0.44f)
        lineTo(w * 0.50f, h * 0.86f)
        lineTo(w * 0.10f, h * 0.44f)
        close()
    }
    drawPath(diamond, color = Color(0xFF4FC3F7))
    drawPath(diamond, color = Color(0xFF0288D1), style = Stroke(width = w * 0.04f))

    // Inner facet lines
    drawLine(color = Color(0xFFE1F5FE), start = Offset(w * 0.10f, h * 0.44f), end = Offset(w * 0.90f, h * 0.44f), strokeWidth = w * 0.035f)
    drawLine(color = Color(0xFFE1F5FE), start = Offset(w * 0.36f, h * 0.24f), end = Offset(w * 0.30f, h * 0.44f), strokeWidth = w * 0.035f)
    drawLine(color = Color(0xFFE1F5FE), start = Offset(w * 0.64f, h * 0.24f), end = Offset(w * 0.70f, h * 0.44f), strokeWidth = w * 0.035f)
}

/** 🛡️ Medical Dental Shield */
private fun DrawScope.drawShieldIcon(canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height

    val shield = Path().apply {
        moveTo(w * 0.50f, h * 0.14f)
        lineTo(w * 0.82f, h * 0.26f)
        lineTo(w * 0.82f, h * 0.56f)
        cubicTo(w * 0.82f, h * 0.76f, w * 0.50f, h * 0.90f, w * 0.50f, h * 0.90f)
        cubicTo(w * 0.50f, h * 0.90f, w * 0.18f, h * 0.76f, w * 0.18f, h * 0.56f)
        lineTo(w * 0.18f, h * 0.26f)
        close()
    }
    drawPath(shield, color = Color(0xFFC81E32))

    // Plus Cross in center
    drawLine(color = Color.White, start = Offset(w * 0.50f, h * 0.34f), end = Offset(w * 0.50f, h * 0.66f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    drawLine(color = Color.White, start = Offset(w * 0.34f, h * 0.50f), end = Offset(w * 0.66f, h * 0.50f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
}

/** ✨ Helper to draw sparkle stars */
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

/** 💡 Illustrated Lightbulb (Matching the top quote card in user screenshot!) */
@Composable
fun CustomIllustratedLightbulb(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 32.dp
) {
    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val w = size.width
            val h = size.height

            // Yellow glowing Bulb Glass
            val bulbPath = Path().apply {
                moveTo(w * 0.35f, h * 0.68f)
                cubicTo(w * 0.16f, h * 0.54f, w * 0.16f, h * 0.24f, w * 0.50f, h * 0.16f)
                cubicTo(w * 0.84f, h * 0.24f, w * 0.84f, h * 0.54f, w * 0.65f, h * 0.68f)
                close()
            }
            drawPath(bulbPath, color = Color(0xFFFFD54F))

            // Bulb Glow ring
            drawCircle(
                color = Color(0xFFFFE082).copy(alpha = 0.4f),
                radius = w * 0.40f,
                center = Offset(w * 0.50f, h * 0.42f)
            )

            // Inner Filament reflection
            drawLine(
                color = Color(0xFFFFA000),
                start = Offset(w * 0.42f, h * 0.52f),
                end = Offset(w * 0.50f, h * 0.32f),
                strokeWidth = w * 0.05f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFFA000),
                start = Offset(w * 0.50f, h * 0.32f),
                end = Offset(w * 0.58f, h * 0.52f),
                strokeWidth = w * 0.05f,
                cap = StrokeCap.Round
            )

            // Blue / Slate Metal Base (Screw threads matching the image!)
            drawRoundRect(
                color = Color(0xFF0288D1),
                topLeft = Offset(w * 0.34f, h * 0.68f),
                size = Size(w * 0.32f, h * 0.16f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF01579B),
                topLeft = Offset(w * 0.40f, h * 0.84f),
                size = Size(w * 0.20f, h * 0.08f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}

/**
 * Custom Dentally Brand Logo (Smiling mint tooth with sparkle & fresh green mint leaf).
 * Used in the Bottom Navigation Bar and Dentally AI headers.
 */
@Composable
fun DentallyLogoIcon(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 24.dp,
    isSelected: Boolean = false
) {
    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val w = size.width
            val h = size.height

            // Tooth Silhouette matching ic_dentally_logo.jpg
            val toothPath = Path().apply {
                moveTo(w * 0.48f, h * 0.22f)
                cubicTo(w * 0.34f, h * 0.14f, w * 0.14f, h * 0.20f, w * 0.14f, h * 0.44f)
                cubicTo(w * 0.14f, h * 0.68f, w * 0.24f, h * 0.90f, w * 0.34f, h * 0.92f)
                cubicTo(w * 0.42f, h * 0.93f, w * 0.44f, h * 0.72f, w * 0.49f, h * 0.70f)
                cubicTo(w * 0.54f, h * 0.72f, w * 0.56f, h * 0.93f, w * 0.64f, h * 0.92f)
                cubicTo(w * 0.74f, h * 0.90f, w * 0.84f, h * 0.68f, w * 0.84f, h * 0.44f)
                cubicTo(w * 0.84f, h * 0.20f, w * 0.64f, h * 0.14f, w * 0.48f, h * 0.22f)
                close()
            }

            drawPath(
                path = toothPath,
                brush = Brush.linearGradient(
                    colors = if (isSelected) {
                        listOf(Color(0xFFFFFFFF), Color(0xFFB2EBF2), Color(0xFF80DEEA))
                    } else {
                        listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF4DD0E1))
                    },
                    start = Offset(w * 0.2f, h * 0.2f),
                    end = Offset(w * 0.8f, h * 0.9f)
                )
            )

            drawPath(
                path = toothPath,
                color = if (isSelected) Color.White else Color(0xFF26A69A),
                style = Stroke(width = w * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Cute eyes
            val eyeColor = Color(0xFF1E293B)
            drawCircle(eyeColor, radius = w * 0.055f, center = Offset(w * 0.37f, h * 0.47f))
            drawCircle(eyeColor, radius = w * 0.055f, center = Offset(w * 0.60f, h * 0.47f))
            drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.355f, h * 0.455f))
            drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.585f, h * 0.455f))

            // Rosy cheeks
            drawCircle(Color(0x66FF8A80), radius = w * 0.06f, center = Offset(w * 0.27f, h * 0.55f))
            drawCircle(Color(0x66FF8A80), radius = w * 0.06f, center = Offset(w * 0.70f, h * 0.55f))

            // Cute open smile
            val smilePath = Path().apply {
                moveTo(w * 0.41f, h * 0.55f)
                lineTo(w * 0.56f, h * 0.55f)
                quadraticBezierTo(w * 0.485f, h * 0.67f, w * 0.41f, h * 0.55f)
                close()
            }
            drawPath(smilePath, color = eyeColor)

            // Sparkle star top-left
            drawSparkleStar(Offset(w * 0.26f, h * 0.32f), w * 0.065f, Color(0xFFFFD54F))

            // Mint Leaf top-right
            val leafPath = Path().apply {
                moveTo(w * 0.68f, h * 0.34f)
                cubicTo(w * 0.64f, h * 0.16f, w * 0.80f, h * 0.06f, w * 0.94f, h * 0.08f)
                cubicTo(w * 0.94f, h * 0.24f, w * 0.82f, h * 0.36f, w * 0.68f, h * 0.34f)
                close()
            }
            drawPath(leafPath, color = Color(0xFF4CAF50))
            drawPath(
                leafPath,
                color = Color(0xFF1B5E20),
                style = Stroke(width = w * 0.03f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawLine(
                color = Color(0xFFA5D6A7),
                start = Offset(w * 0.70f, h * 0.32f),
                end = Offset(w * 0.88f, h * 0.13f),
                strokeWidth = w * 0.03f,
                cap = StrokeCap.Round
            )
        }
    }
}

