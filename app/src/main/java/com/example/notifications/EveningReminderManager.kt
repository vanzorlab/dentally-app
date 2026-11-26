package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EveningReminderScheduler {
    const val CHANNEL_ID = "dentally_evening_checkin_priority"
    const val NOTIFICATION_ID = 2026
    const val EXTRA_OPEN_EVENING_CHECKIN = "open_evening_checkin"
    const val EXTRA_START_BRUSHING_TIMER = "start_brushing_timer"
    private const val PREFS_NAME = "dentally_user_prefs"
    private const val KEY_EVENING_DONE_DATE = "evening_completed_date"
    private const val REQUEST_CODE = 8801

    private val catchyPrompts = listOf(
        "Stop sebentar yuk — Checklist gigi malam menunggu!" to
            "Sebelum lanjut scroll atau main HP, pastikan sikat gigi 2 menit dan checklist malammu sudah beres.",
        "Jaga streak sehatmu hari ini di Dentally!" to
            "Bakteri paling aktif saat tidur malam. Luangkan 2 menit buat sikat gigi & isi checklist malam sekarang.",
        "Waktunya Check-In Malam Dentally!" to
            "Ketuk untuk langsung isi checklist harianmu atau jalankan Timer Sikat Gigi 2 Menit agar mulut tetap segar."
    )

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Wajib Sikat Gigi Malam",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Push notifikasi prioritas tinggi untuk mengisi checklist harian & sikat gigi malam."
                enableVibration(true)
                vibrationPattern = longArrayOf(0L, 260L, 140L, 260L)
                enableLights(true)
                lightColor = 0xFFC81E32.toInt()
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun markTodayEveningCompleted(context: Context, completed: Boolean) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (completed) {
            prefs.edit().putString(KEY_EVENING_DONE_DATE, todayStr).apply()
            cancelActiveNotification(context)
        } else {
            prefs.edit().remove(KEY_EVENING_DONE_DATE).apply()
        }
    }

    fun isTodayEveningCompleted(context: Context): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EVENING_DONE_DATE, "") == todayStr
    }

    fun scheduleDailyEveningReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        try {
            ensureChannel(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, EveningReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
                set(Calendar.MINUTE, minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // Safe fallback on restricted environments
        }
    }

    fun cancelDailyReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, EveningReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    fun cancelActiveNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    fun showEveningReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        ensureChannel(context)

        val openCheckInIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_EVENING_CHECKIN, true)
        }
        val checkInPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE + 1,
            openCheckInIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val startTimerIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_START_BRUSHING_TIMER, true)
        }
        val timerPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE + 2,
            startTimerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dayIdx = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % catchyPrompts.size
        val (title, body) = catchyPrompts[dayIdx]

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(0xFFC81E32.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0L, 260L, 140L, 260L))
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(checkInPendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "Isi Checklist",
                checkInPendingIntent
            )
            .addAction(
                R.mipmap.ic_launcher,
                "Timer 2 Menit",
                timerPendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission revoked at runtime
        }
    }
}

class EveningReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!EveningReminderScheduler.isTodayEveningCompleted(context)) {
            EveningReminderScheduler.showEveningReminderNotification(context)
        }
        // Reschedule for tomorrow if using exact alarm
        val prefs = context.getSharedPreferences("dentally_user_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("evening_notification_enabled", true)
        if (enabled) {
            val savedTime = prefs.getString("night_reminder_time", "20:00") ?: "20:00"
            val parts = savedTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            EveningReminderScheduler.scheduleDailyEveningReminder(context, hour, minute)
        }
    }
}
