package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfileData(
    val userId: String = "",
    val userName: String = "Sahabat Dentally",
    val userEmail: String = "",
    val avatarUrl: String = "",
    val isLoggedIn: Boolean = false,
    val isGoogleConnected: Boolean = false,
    val isCalendarSynced: Boolean = false,
    val lastSyncTime: String = "",
    val morningReminderTime: String = "07:00",
    val nightReminderTime: String = "20:00",
    val sfxEnabled: Boolean = true,
    val eveningNotificationEnabled: Boolean = true
) {
    val isDeveloper: Boolean
        get() = userEmail.equals(DEVELOPER_EMAIL, ignoreCase = true)

    companion object {
        const val DEVELOPER_EMAIL = "fight.vanzor30@gmail.com"
    }
}

data class CalendarSyncEvent(
    val title: String,
    val description: String,
    val timeOfDay: String,
    val recurrence: String = "Setiap Hari"
)

class UserProfileRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dentally_user_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfileData> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfileData {
        return UserProfileData(
            userId = prefs.getString("user_id", "") ?: "",
            userName = prefs.getString("user_name", "Sahabat Dentally") ?: "Sahabat Dentally",
            userEmail = prefs.getString("user_email", "") ?: "",
            avatarUrl = prefs.getString("user_avatar", "") ?: "",
            isLoggedIn = prefs.getBoolean("is_logged_in", false),
            isGoogleConnected = prefs.getBoolean("is_google_connected", false),
            isCalendarSynced = prefs.getBoolean("is_calendar_synced", false),
            lastSyncTime = prefs.getString("last_sync_time", "") ?: "",
            morningReminderTime = prefs.getString("morning_reminder", "07:00") ?: "07:00",
            nightReminderTime = prefs.getString("night_reminder", "20:00") ?: "20:00",
            sfxEnabled = prefs.getBoolean("sfx_enabled", true),
            eveningNotificationEnabled = prefs.getBoolean("evening_notif_enabled", true)
        )
    }

    fun getLastActiveUserId(): String = prefs.getString("last_active_uid", "") ?: ""

    fun setLastActiveUserId(uid: String) {
        prefs.edit().putString("last_active_uid", uid).apply()
    }

    fun hasSeenOnboarding(uid: String): Boolean {
        val key = if (uid.isNotBlank()) "onboarding_seen_$uid" else "onboarding_seen_default"
        return prefs.getBoolean(key, false)
    }

    fun setOnboardingSeen(uid: String, seen: Boolean = true) {
        val key = if (uid.isNotBlank()) "onboarding_seen_$uid" else "onboarding_seen_default"
        prefs.edit().putBoolean(key, seen).apply()
    }

    fun setSfxEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sfx_enabled", enabled).apply()
        _userProfile.value = _userProfile.value.copy(sfxEnabled = enabled)
    }

    fun setEveningNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("evening_notif_enabled", enabled).apply()
        _userProfile.value = _userProfile.value.copy(eveningNotificationEnabled = enabled)
    }

    fun setNightReminderTime(time: String) {
        prefs.edit().putString("night_reminder", time).apply()
        _userProfile.value = _userProfile.value.copy(nightReminderTime = time)
    }

    fun login(email: String, name: String? = null, uid: String = "", avatarUrl: String = "") {
        val finalName = if (!name.isNullOrBlank()) name else if (email.contains("@")) {
            email.substringBefore("@").replace(".", " ").split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        } else "Pengguna Dentally"

        prefs.edit()
            .putString("user_id", uid)
            .putBoolean("is_logged_in", true)
            .putBoolean("is_google_connected", true)
            .putString("user_email", email)
            .putString("user_name", finalName)
            .putString("user_avatar", avatarUrl)
            .apply()

        _userProfile.value = _userProfile.value.copy(
            userId = uid,
            isLoggedIn = true,
            isGoogleConnected = true,
            userEmail = email,
            userName = finalName,
            avatarUrl = avatarUrl
        )
    }

    fun register(name: String, email: String) {
        login(email = email, name = name)
    }

    fun logout() {
        val currentSfx = _userProfile.value.sfxEnabled
        prefs.edit()
            .putString("user_id", "")
            .putBoolean("is_logged_in", false)
            .putBoolean("is_google_connected", false)
            .putString("user_email", "")
            .putString("user_name", "Sahabat Dentally")
            .putString("user_avatar", "")
            .putBoolean("is_calendar_synced", false)
            .putString("last_sync_time", "")
            .apply()

        _userProfile.value = UserProfileData(isLoggedIn = false, sfxEnabled = currentSfx)
    }

    fun updateUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            prefs.edit().putString("user_name", trimmed).apply()
            _userProfile.value = _userProfile.value.copy(userName = trimmed)
        }
    }

    fun setGoogleConnected(connected: Boolean, email: String? = null, name: String? = null, uid: String? = null) {
        val editor = prefs.edit().putBoolean("is_google_connected", connected)
        if (email != null) editor.putString("user_email", email)
        if (name != null) editor.putString("user_name", name)
        if (uid != null) editor.putString("user_id", uid)
        editor.apply()

        _userProfile.value = _userProfile.value.copy(
            userId = uid ?: _userProfile.value.userId,
            isGoogleConnected = connected,
            userEmail = email ?: _userProfile.value.userEmail,
            userName = name ?: _userProfile.value.userName
        )
    }

    fun syncRoutinesToGoogleCalendar(): List<CalendarSyncEvent> {
        val nowFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        prefs.edit()
            .putBoolean("is_calendar_synced", true)
            .putString("last_sync_time", nowFormatted)
            .apply()

        _userProfile.value = _userProfile.value.copy(
            isCalendarSynced = true,
            lastSyncTime = nowFormatted
        )

        return listOf(
            CalendarSyncEvent(
                title = "Sikat Gigi Pagi",
                description = "2 menit setelah sarapan biar mulut bersih segar.",
                timeOfDay = _userProfile.value.morningReminderTime
            ),
            CalendarSyncEvent(
                title = "Sikat Gigi Malam",
                description = "Bersihkan gigi & sela gigi sebelum tidur.",
                timeOfDay = _userProfile.value.nightReminderTime
            ),
            CalendarSyncEvent(
                title = "Cek Dokter Gigi",
                description = "Kontrol rutin setiap 6 bulan sekali.",
                timeOfDay = "09:00",
                recurrence = "Setiap 6 Bulan"
            )
        )
    }
}
