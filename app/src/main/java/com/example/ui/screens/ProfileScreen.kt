package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.audio.SoundEffectManager
import com.example.data.repository.CalendarSyncEvent
import com.example.ui.components.MonthlyHabitCalendar
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.HealthSuccess
import com.example.ui.theme.HealthSuccessBg
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalDark
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import com.example.ui.viewmodel.DentallyViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: DentallyViewModel,
    onNavigateToDeveloper: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val allHabits by viewModel.allHabits.collectAsStateWithLifecycle()
    val allMoods by viewModel.allMoods.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentDayMood by viewModel.currentDayMood.collectAsStateWithLifecycle()
    val timerCompletedToday by viewModel.timerCompletedToday.collectAsStateWithLifecycle()

    val gamificationStatus = remember(habits, currentDayMood, timerCompletedToday) {
        viewModel.getGamificationStatus()
    }

    var isEditingName by remember(userProfile.userName) { mutableStateOf(false) }
    var draftName by remember(userProfile.userName) { mutableStateOf(userProfile.userName) }
    var syncedEvents by remember { mutableStateOf<List<CalendarSyncEvent>>(emptyList()) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleEveningNotification(true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Page Title
        item {
            Column {
                Text(
                    text = "Profil",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NaturalDark,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "Akun Gmail, kalender, pengingat malam & panduan.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SageMedium,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        // 2. Profile Identity Card (Google Avatar + Name Editor + Email + Cloud Status)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_identity_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (userProfile.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = "Foto Profil Google",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, DentallyBorder, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(DentallyTintBg)
                                .border(2.dp, DentallyBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.userName.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = NaturalPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isEditingName) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userProfile.userName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalDark,
                                    fontSize = 19.sp
                                )
                            )
                            IconButton(
                                onClick = {
                                    draftName = userProfile.userName
                                    isEditingName = true
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("edit_profile_name_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Ubah Nama Panggilan",
                                    tint = NaturalPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draftName,
                                onValueChange = { draftName = it },
                                label = { Text("Nama Panggilan") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NaturalPrimary,
                                    unfocusedBorderColor = DentallyBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (draftName.isNotBlank()) {
                                        viewModel.updateUserName(draftName)
                                    }
                                    isEditingName = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("save_profile_name_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Simpan Nama"
                                )
                            }
                        }
                    }

                    Text(
                        text = userProfile.userEmail.ifBlank { "Akun Google Terhubung" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageMedium,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(HealthSuccessBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Tersimpan di Cloud",
                            tint = HealthSuccess,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Data Tersimpan Aman di Cloud",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HealthSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 3. 3-Column Progress Snapshot
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileSnapshotCard(
                    title = "Kebersihan",
                    value = "${gamificationStatus.toothHealthPercent}%",
                    bgColor = if (gamificationStatus.toothHealthPercent >= 85) HealthSuccessBg else DentallyTintBg,
                    accentColor = if (gamificationStatus.toothHealthPercent >= 85) HealthSuccess else NaturalPrimary,
                    modifier = Modifier.weight(1f)
                )
                ProfileSnapshotCard(
                    title = "Streak",
                    value = "${gamificationStatus.streakDays} Hari",
                    bgColor = Color(0xFFFAF3E6),
                    accentColor = Color(0xFF9A6700),
                    modifier = Modifier.weight(1f)
                )
                ProfileSnapshotCard(
                    title = "Kebiasaan",
                    value = "${gamificationStatus.completedGoodHabits}/${gamificationStatus.totalGoodHabits}",
                    bgColor = Color(0xFFE8F0FE),
                    accentColor = Color(0xFF1967D2),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3b. User Monthly Habit & Brushing Calendar
        item {
            MonthlyHabitCalendar(
                habits = allHabits,
                moods = allMoods,
                selectedDate = selectedDate,
                onDateSelected = { viewModel.setSelectedDate(it) },
                onFutureDateBlocked = {
                    Toast.makeText(
                        context,
                        "Tanggal belum tiba — baru bisa diisi hari H ya!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                title = "Kalender Rutinitas Saya"
            )
        }

        // 3c. Audio SFX, Evening Push Notification & Demo Guide Controls
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_preferences_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Suara, Pengingat Malam & Panduan",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalDark,
                            fontSize = 15.sp
                        )
                    )

                    // SFX Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (userProfile.sfxEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Efek Suara",
                                tint = NaturalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Efek Suara Interaktif (SFX)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = NaturalDark
                                )
                                Text(
                                    text = "Suara seru saat centang & timer selesai",
                                    fontSize = 11.5.sp,
                                    color = SageMedium
                                )
                            }
                        }
                        Switch(
                            checked = userProfile.sfxEnabled,
                            onCheckedChange = { viewModel.toggleSfxEnabled() },
                            colors = SwitchDefaults.colors(checkedTrackColor = NaturalPrimary),
                            modifier = Modifier.testTag("profile_sfx_switch")
                        )
                    }

                    HorizontalDivider(color = DentallyBorder)

                    // Evening Push Notification Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notifikasi Malam",
                                tint = NaturalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pengingat Wajib Malam",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = NaturalDark
                                )
                                Text(
                                    text = "Push notifikasi prioritas & kunci check-in Dentally",
                                    fontSize = 11.5.sp,
                                    color = SageMedium
                                )
                            }
                        }
                        Switch(
                            checked = userProfile.eveningNotificationEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Switch
                                    }
                                }
                                viewModel.toggleEveningNotification(enabled)
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = NaturalPrimary),
                            modifier = Modifier.testTag("profile_evening_notif_switch")
                        )
                    }

                    // Night Reminder Time Chips + Custom TimePicker
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Jam Pengingat Malam:",
                            fontSize = 12.sp,
                            color = SageDark,
                            fontWeight = FontWeight.SemiBold
                        )
                        val presets = listOf("19:30", "20:00", "21:00")
                        val isCustomActive = userProfile.nightReminderTime !in presets

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { timeOpt ->
                                val selected = userProfile.nightReminderTime == timeOpt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) NaturalPrimary else DentallyTintBg)
                                        .border(
                                            1.dp,
                                            if (selected) NaturalPrimary else DentallyBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.updateNightReminderTime(timeOpt) }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = timeOpt,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else NaturalDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCustomActive) NaturalPrimary else Color.White)
                                    .border(
                                        1.dp,
                                        if (isCustomActive) NaturalPrimary else DentallyBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        val parts = userProfile.nightReminderTime.split(":")
                                        val initHour = parts.getOrNull(0)?.toIntOrNull() ?: 20
                                        val initMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        TimePickerDialog(
                                            context,
                                            { _, selectedHour, selectedMinute ->
                                                val formatted = String.format(
                                                    Locale.US,
                                                    "%02d:%02d",
                                                    selectedHour,
                                                    selectedMinute
                                                )
                                                viewModel.updateNightReminderTime(formatted)
                                            },
                                            initHour,
                                            initMinute,
                                            true
                                        ).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                    .testTag("custom_reminder_time_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Pilih Jam Sendiri",
                                        tint = if (isCustomActive) Color.White else NaturalPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isCustomActive) {
                                            "Sendiri: ${userProfile.nightReminderTime}"
                                        } else {
                                            "+ Pilih Jam Sendiri"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCustomActive) Color.White else NaturalPrimary
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DentallyTintBg)
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = "Saat jam tiba, push notifikasi prioritas muncul dengan tombol langsung & aplikasi Dentally terkunci di layar check-in sampai selesai (Android membatasi aplikasi lain ditutup paksa dari luar).",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = SageDark
                            )
                        }
                    }

                    // Quick Test Buttons (Test Notification + Test Check-In Gate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.sendTestEveningNotification() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DentallyBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_test_notif_btn")
                        ) {
                            Text("Tes Notif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalDark)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerEveningCheckInGate(forceOpen = true) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DentallyBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_test_gate_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = NaturalPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cek Malam", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalDark)
                        }
                    }

                    // Interactive Onboarding / App Demo Replay Button
                    Button(
                        onClick = { viewModel.openOnboardingDemo() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DentallyTintBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_open_demo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lihat Demo & Panduan Aplikasi",
                            color = NaturalPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Google Calendar Sync Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F0FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Google Calendar",
                                tint = Color(0xFF1967D2),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengingat Google Calendar",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalDark,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = if (userProfile.isCalendarSynced && userProfile.lastSyncTime.isNotBlank()) {
                                    "Terakhir sinkron: ${userProfile.lastSyncTime}"
                                } else {
                                    "Jadwalkan pengingat sikat gigi pagi, malam & cek dokter"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SageMedium,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { syncedEvents = viewModel.syncGoogleCalendar() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1967D2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sync_google_calendar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (userProfile.isCalendarSynced) "Perbarui Jadwal Pengingat" else "Aktifkan Pengingat Jadwal",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (syncedEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            syncedEvents.forEach { event ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF4F8FE))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "${event.title} • ${event.timeOfDay}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color(0xFF1967D2),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = event.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SageDark,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Developer Monitor Access (Strictly visible only for fight.vanzor30@gmail.com)
        if (userProfile.isDeveloper) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDeveloper() }
                        .testTag("profile_developer_panel_btn"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A0E17)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Panel Developer",
                                    tint = Color(0xFFFECDD3),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Panel Monitor Developer",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "Pantau semua akun Gmail & progres kebersihan mulut pengguna",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFFFE4E7),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFECDD3))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Buka →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF4A0E17),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // 6. Logout / Switch Gmail Account
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DentallyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Keluar atau Ganti Akun Gmail",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NaturalDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Semua kebiasaan, mood, dan tingkat kebersihan mulutmu tetap tersimpan aman di Cloud untuk akun Gmail ini.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageMedium,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            SoundEffectManager.playPop()
                            showLogoutConfirmDialog = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFDC2626)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Keluar Akun",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keluar / Ganti Akun Gmail",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Keluar dari Akun Gmail?",
                    fontWeight = FontWeight.Bold,
                    color = NaturalDark
                )
            },
            text = {
                Text(
                    text = "Progresmu sudah tersimpan di Cloud dan akan muncul lagi saat kamu masuk kembali dengan akun ${userProfile.userEmail}.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SageDark)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Batal", color = NaturalMedium)
                }
            }
        )
    }
}

@Composable
private fun ProfileSnapshotCard(
    title: String,
    value: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DentallyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SageMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
