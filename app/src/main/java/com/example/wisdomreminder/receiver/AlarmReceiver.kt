package com.example.wisdomreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.util.NotificationManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class AlarmReceiver : BroadcastReceiver() {

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface AlarmReceiverEntryPoint {
        fun notificationManager(): NotificationManager
        fun wisdomRepository(): WisdomRepository
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

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

            val pendingResult = goAsync()

            scope.launch {
                try {
                    showAlarmNotification(context, wisdomId, isSnooze, alarmTime, notificationManager, wisdomRepository)
                } finally {
                    pendingResult.finish()
                }
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
        if (wisdomId != -1L) {
            val wisdomResult = wisdomRepository.getWisdomById(wisdomId)
            wisdomResult.onSuccess { wisdom ->
                if (wisdom != null) {
                    notificationManager.showAlarmNotification(wisdom, alarmTime)
                    return
                }
            }.onFailure {
                Timber.e(it, "Failed to get wisdom by ID $wisdomId for alarm")
            }
        }

        // If specific wisdom ID failed or not provided, pick a random active wisdom
        try {
            val activeWisdom = wisdomRepository.getActiveWisdom().first()
            if (activeWisdom.isNotEmpty()) {
                val wisdom = activeWisdom.minByOrNull { it.exposuresToday }
                    ?: activeWisdom.random()
                notificationManager.showAlarmNotification(wisdom, alarmTime)
            } else {
                Timber.d("No active wisdom to show for alarm.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get active wisdom for alarm notification.")
        }
    }
}