package com.example.wisdomreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.util.NotificationManager
import com.example.wisdomreminder.util.WisdomAlarmManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

// Remove the @AndroidEntryPoint annotation
class AlarmReceiver : BroadcastReceiver() {

    // Add an EntryPoint interface
    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface AlarmReceiverEntryPoint {
        fun notificationManager(): NotificationManager
        fun wisdomRepository(): WisdomRepository
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // Get dependencies using EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )

        val notificationManager = entryPoint.notificationManager()
        val wisdomRepository = entryPoint.wisdomRepository()

        if (action == "com.example.wisdomreminder.ALARM_TRIGGER") {
            Timber.d("Alarm triggered")

            val isSnooze = intent.getBooleanExtra("is_snooze", false)
            val wisdomId = intent.getLongExtra("wisdom_id", -1L)
            val alarmTime = intent.getStringExtra("alarm_time") ?: "Scheduled"

            GlobalScope.launch {
                showAlarmNotification(context, wisdomId, isSnooze, alarmTime, notificationManager, wisdomRepository)
            }
        }
    }

    private suspend fun showAlarmNotification(
        context: Context,
        wisdomId: Long,
        isSnooze: Boolean,
        alarmTime: String,
        notificationManager: NotificationManager,
        wisdomRepository: WisdomRepository
    ) {
        // If specific wisdom ID provided (for snooze)
        if (wisdomId != -1L) {
            val wisdomResult = wisdomRepository.getWisdomById(wisdomId)
            wisdomResult.onSuccess { wisdom ->
                if (wisdom != null) {
                    notificationManager.showAlarmNotification(wisdom, alarmTime)
                    return
                }
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