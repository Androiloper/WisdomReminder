package com.example.wisdomreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.util.NotificationManager
import com.example.wisdomreminder.util.WisdomAlarmManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var wisdomRepository: WisdomRepository



    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == "com.example.wisdomreminder.ALARM_TRIGGER") {
            Timber.d("Alarm triggered")

            val isSnooze = intent.getBooleanExtra("is_snooze", false)
            val wisdomId = intent.getLongExtra("wisdom_id", -1L)
            val alarmTime = intent.getStringExtra("alarm_time") ?: "Scheduled"

            GlobalScope.launch {
                showAlarmNotification(context, wisdomId, isSnooze, alarmTime)
            }
        }
    }

    private suspend fun showAlarmNotification(
        context: Context,
        wisdomId: Long,
        isSnooze: Boolean,
        alarmTime: String
    ) {
        // If specific wisdom ID provided (for snooze)
        if (wisdomId != -1L) {
            val wisdom = wisdomRepository.getWisdomById(wisdomId)
            if (wisdom != null) {
                notificationManager.showAlarmNotification(wisdom, alarmTime)
                return
            }
        }

        // Otherwise, pick a random active wisdom
        val activeWisdom = wisdomRepository.getActiveWisdom().first()
        if (activeWisdom.isNotEmpty()) {
            // Choose wisdom with fewest exposures today
            val wisdom = activeWisdom.minByOrNull { it.exposuresToday }
                ?: activeWisdom.random()

            notificationManager.showAlarmNotification(wisdom, alarmTime)
        }
    }
}


