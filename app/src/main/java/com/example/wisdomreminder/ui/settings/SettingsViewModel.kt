package com.example.wisdomreminder.ui.settings

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.wisdomreminder.util.WisdomAlarmManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wisdomAlarmManager: WisdomAlarmManager
) : ViewModel() {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    // Notification settings
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    // Alarm settings
    private val _alarmsEnabled = MutableStateFlow(prefs.getBoolean("alarms_enabled", true))
    val alarmsEnabled = _alarmsEnabled.asStateFlow()

    private val _morningAlarmEnabled = MutableStateFlow(prefs.getBoolean("morning_alarm_enabled", true))
    val morningAlarmEnabled = _morningAlarmEnabled.asStateFlow()

    private val _morningAlarmTime = MutableStateFlow(prefs.getString("morning_alarm_time", "08:00") ?: "08:00")
    val morningAlarmTime = _morningAlarmTime.asStateFlow()

    private val _eveningAlarmEnabled = MutableStateFlow(prefs.getBoolean("evening_alarm_enabled", true))
    val eveningAlarmEnabled = _eveningAlarmEnabled.asStateFlow()

    private val _eveningAlarmTime = MutableStateFlow(prefs.getString("evening_alarm_time", "20:00") ?: "20:00")
    val eveningAlarmTime = _eveningAlarmTime.asStateFlow()

    // Notification settings methods
    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    // Alarm settings methods
    fun setAlarmsEnabled(enabled: Boolean) {
        _alarmsEnabled.value = enabled
        prefs.edit().putBoolean("alarms_enabled", enabled).apply()

        if (enabled) {
            wisdomAlarmManager.scheduleAlarms()
        } else {
            wisdomAlarmManager.cancelAlarms()
        }
    }

    fun setMorningAlarmEnabled(enabled: Boolean) {
        _morningAlarmEnabled.value = enabled
        prefs.edit().putBoolean("morning_alarm_enabled", enabled).apply()
        wisdomAlarmManager.scheduleAlarms()
    }

    fun setMorningAlarmTime(time: String) {
        _morningAlarmTime.value = time
        prefs.edit().putString("morning_alarm_time", time).apply()
        wisdomAlarmManager.scheduleAlarms()
    }

    fun setEveningAlarmEnabled(enabled: Boolean) {
        _eveningAlarmEnabled.value = enabled
        prefs.edit().putBoolean("evening_alarm_enabled", enabled).apply()
        wisdomAlarmManager.scheduleAlarms()
    }

    fun setEveningAlarmTime(time: String) {
        _eveningAlarmTime.value = time
        prefs.edit().putString("evening_alarm_time", time).apply()
        wisdomAlarmManager.scheduleAlarms()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkExactAlarmPermission(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            // Show dialog explaining the need for exact alarms
            AlertDialog.Builder(context)
                .setTitle("Permission Required")
                .setMessage("This app needs exact alarm permission to schedule wisdom reminders at specific times. Please grant this permission in the next screen.")
                .setPositiveButton("Open Settings") { _, _ ->
                    // Direct user to exact alarm settings
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }
}