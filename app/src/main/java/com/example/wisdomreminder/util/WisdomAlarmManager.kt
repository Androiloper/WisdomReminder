package com.example.wisdomreminder.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WisdomAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wisdomRepository: WisdomRepository
) {
    companion object {
        private const val ALARM_REQUEST_CODE_BASE = 5000
    }

    @SuppressLint("ServiceCast")
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Schedule daily alarms based on user preferences
     */
    fun scheduleAlarms() {
        if (!prefs.getBoolean("alarms_enabled", true)) {
            Timber.d("Alarms disabled, not scheduling")
            return
        }

        // Cancel existing alarms first
        cancelAlarms()

        // Get user's preferred alarm times
        val morningAlarmEnabled = prefs.getBoolean("morning_alarm_enabled", true)
        val morningAlarmTime = prefs.getString("morning_alarm_time", "08:00") ?: "08:00"

        val eveningAlarmEnabled = prefs.getBoolean("evening_alarm_enabled", true)
        val eveningAlarmTime = prefs.getString("evening_alarm_time", "20:00") ?: "20:00"

        // Schedule the alarms if enabled
        if (morningAlarmEnabled) {
            scheduleRepeatingAlarm(
                alarmTime = morningAlarmTime,
                requestCode = ALARM_REQUEST_CODE_BASE + 1
            )
        }

        if (eveningAlarmEnabled) {
            scheduleRepeatingAlarm(
                alarmTime = eveningAlarmTime,
                requestCode = ALARM_REQUEST_CODE_BASE + 2
            )
        }
    }

    /**
     * Schedule a single snooze alarm (one-time)
     */
    fun scheduleSnoozeAlarm(wisdomId: Long, delayMinutes: Int = 15) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.wisdomreminder.ALARM_TRIGGER"
            putExtra("wisdom_id", wisdomId)
            putExtra("is_snooze", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_BASE + wisdomId.toInt() + 100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule for delayMinutes from now
        val triggerTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000)

        // Check permission for exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Timber.w("No permission for exact alarms, using inexact alarm")
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                return
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Timber.d("Scheduled snooze alarm for wisdom $wisdomId in $delayMinutes minutes")
        } catch (e: SecurityException) {
            Timber.e("SecurityException when scheduling exact alarm, falling back to inexact")
            // Fall back to inexact alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /**
     * Schedule a daily repeating alarm at the given time
     */
    private fun scheduleRepeatingAlarm(alarmTime: String, requestCode: Int) {
        val (hour, minute) = parseTimeString(alarmTime)

        // Create calendar for target time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time for today has already passed, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Create intent for alarm broadcast
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.wisdomreminder.ALARM_TRIGGER"
            putExtra("alarm_time", alarmTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Timber.d("Scheduled daily alarm at $alarmTime (${calendar.time})")
        } catch (e: SecurityException) {
            Timber.e("SecurityException when scheduling repeating alarm")
            // Repeating is safer than exact, but still catch any exceptions
        }
    }

    /**
     * Cancel all scheduled alarms
     */
    fun cancelAlarms() {
        // Cancel morning alarm
        val morningIntent = Intent(context, AlarmReceiver::class.java)
        val morningPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_BASE + 1,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(morningPendingIntent)

        // Cancel evening alarm
        val eveningIntent = Intent(context, AlarmReceiver::class.java)
        val eveningPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_BASE + 2,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(eveningPendingIntent)

        Timber.d("Cancelled all scheduled alarms")
    }

    /**
     * Parse time string in format "HH:MM"
     */
    private fun parseTimeString(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            Pair(hour, minute)
        } catch (e: Exception) {
            Timber.e("Error parsing time string: $timeString")
            Pair(8, 0) // Default to 8:00 AM
        }
    }
}