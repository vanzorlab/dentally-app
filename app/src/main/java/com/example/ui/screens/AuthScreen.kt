package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.StaggeredEntrance
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Mandatory Gmail / Google Sign-In Gate via Jetpack Credential Manager + Firebase Auth.
 * Ensures each person has their own isolated private database in Firestore.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. App Mascot & Brand Header
            StaggeredEntrance(index = 0) {
                AuthBrandHeader()
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. Main Gmail Login Card
            StaggeredEntrance(index = 1) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_form_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Masuk dengan Akun Gmail",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalDark,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "Silakan pilih akun Gmail kamu sebelum memakai Dentally. Semua progres sikat gigi, tingkat kebersihan mulut, dan catatan harianmu tersimpan aman khusus untuk akunmu sendiri.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SageDark,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            )
                        }

                        // 3 Key Privacy & Sync Benefits
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DentallyTintBg)
                                .border(1.dp, DentallyBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AuthFeatureRow(
                                icon = Icons.Default.Lock,
                                title = "Data Pribadi Terisolasi",
                                subtitle = "Setiap akun Gmail memiliki ruang database sendiri."
                            )
                            AuthFeatureRow(
                                icon = Icons.Default.CloudDone,
                                title = "Tersimpan Otomatis di Cloud",
                                subtitle = "Progres tidak hilang saat ganti HP atau instal ulang."
                            )
                            AuthFeatureRow(
                                icon = Icons.Default.Security,
                                title = "Login Resmi Google",
                                subtitle = "Tanpa buat sandi baru, cukup 1 ketuk pakai Gmail."
                            )
                        }

                        // Error Message Banner
                        AnimatedVisibility(visible = errorMessage != null) {
                            errorMessage?.let { msg ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFB91C1C),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Primary Google / Gmail Sign-In Button
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    },
                                    scope = coroutineScope,
                                    onAuthCancelled = {
                                        isLoading = false
                                        errorMessage = "Proses pemilihan akun Gmail dibatalkan. Silakan pilih akun Gmail untuk melanjutkan."
                                    }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("google_login_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NaturalPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Menghubungkan Gmail...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(26.dp),
                                        shape = CircleShape,
                                        color = Color.White
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "G",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    color = Color(0xFF4285F4),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Masuk dengan Gmail (Google)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Smooth Animated Sync Feedback Pill during Gmail Authentication
                        AnimatedVisibility(
                            visible = isLoading,
                            enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                            exit = fadeOut(tween(180)) + shrinkVertically(tween(180))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DentallyTintBg)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = NaturalPrimary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Membuka ruang sehat & menyinkronkan cloud...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SageDark,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Dengan masuk, kamu menyetujui penyimpanan progres kesehatan gigi pribadi di cloud Dentally.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageMedium,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer note
            StaggeredEntrance(index = 2) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privasi Terjaga",
                        tint = SageMedium,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Data setiap akun terenkripsi & terlindungi Firebase Auth",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageMedium,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(DentallyTintBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NaturalPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SageDark,
                    fontSize = 11.5.sp
                )
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Konfigurasi Google Sign-In tidak ditemukan.")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activity = context.findActivity()
                ?: throw IllegalStateException("Context bukan Activity yang valid untuk Credential Manager")
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Tipe kredensial Google tidak sesuai.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.w("Auth", "No Google credential available on device: ${e.message}", e)
            onAuthError("Belum ada akun Google yang aktif di perangkat ini. Tambahkan akun Gmail di Pengaturan HP lalu coba lagi.")
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            val detail = e.localizedMessage ?: e.message
            onAuthError(
                if (!detail.isNullOrBlank()) {
                    "Gagal masuk Gmail: $detail"
                } else {
                    "Gagal masuk dengan akun Gmail. Pastikan layanan Google Play & koneksi internet aktif."
                }
            )
        }
    }
}

@Composable
private fun AuthBrandHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_pulse")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )
    val haloRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotate"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .graphicsLayer {
                        scaleX = haloScale
                        scaleY = haloScale
                        rotationZ = haloRotate
                    }
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                NaturalPrimary.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color(0xFFFF8A96),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFF2F3), Color(0xFFFFE4E7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(58.dp)
                        .graphicsLayer { translationY = bounceY.dp.toPx() }
                ) {
                    val w = size.width
                    val h = size.height

                    val toothPath = Path().apply {
                        moveTo(w * 0.28f, h * 0.18f)
                        cubicTo(w * 0.35f, h * 0.08f, w * 0.65f, h * 0.08f, w * 0.72f, h * 0.18f)
                        cubicTo(w * 0.92f, h * 0.26f, w * 0.90f, h * 0.62f, w * 0.78f, h * 0.88f)
                        cubicTo(w * 0.72f, h * 0.98f, w * 0.58f, h * 0.98f, w * 0.52f, h * 0.74f)
                        cubicTo(w * 0.48f, h * 0.74f, w * 0.42f, h * 0.98f, w * 0.28f, h * 0.88f)
                        cubicTo(w * 0.10f, h * 0.62f, w * 0.08f, h * 0.26f, w * 0.28f, h * 0.18f)
                        close()
                    }

                    drawPath(toothPath, Color.White, style = Fill)
                    drawCircle(Color(0xFFFFF9C4), radius = 5f, center = Offset(w * 0.72f, h * 0.25f))
                    drawCircle(NaturalDark, radius = 2.8f, center = Offset(w * 0.38f, h * 0.42f))
                    drawCircle(NaturalDark, radius = 2.8f, center = Offset(w * 0.62f, h * 0.42f))
                    drawCircle(Color(0xFFFFCDD2), radius = 3.5f, center = Offset(w * 0.28f, h * 0.48f))
                    drawCircle(Color(0xFFFFCDD2), radius = 3.5f, center = Offset(w * 0.72f, h * 0.48f))

                    val smilePath = Path().apply {
                        moveTo(w * 0.42f, h * 0.50f)
                        quadraticTo(w * 0.50f, h * 0.60f, w * 0.58f, h * 0.50f)
                    }
                    drawPath(smilePath, NaturalDark, style = Stroke(width = 2.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Dentally",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DentallyTintBg)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "CLOUD SYNC",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NaturalPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sahabat Sehat Gigi, Gusi & Senyum Menawan",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SageDark,
                fontSize = 13.5.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
