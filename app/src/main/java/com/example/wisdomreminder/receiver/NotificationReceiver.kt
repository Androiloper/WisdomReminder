package com.example.wisdomreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.util.WisdomAlarmManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationReceiver : BroadcastReceiver() {

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface NotificationReceiverEntryPoint {
        fun wisdomRepository(): WisdomRepository
        fun wisdomAlarmManager(): WisdomAlarmManager
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationReceiverEntryPoint::class.java
        )

        val wisdomRepository = entryPoint.wisdomRepository()
        val wisdomAlarmManager = entryPoint.wisdomAlarmManager()

        val action = intent.action
        val wisdomId = intent.getLongExtra("wisdom_id", -1L)

        if (wisdomId == -1L) return

        val pendingResult = goAsync()

        scope.launch {
            try {
                when (action) {
                    "com.example.wisdomreminder.MARK_READ" -> {
                        Timber.d("Marking wisdom $wisdomId as read")
                        wisdomRepository.recordExposure(wisdomId).onSuccess {
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Wisdom marked as read", Toast.LENGTH_SHORT).show()
                            }
                        }.onFailure {
                            Timber.e(it, "Failed to mark wisdom $wisdomId as read")
                        }
                    }
                    "com.example.wisdomreminder.SNOOZE_ALARM" -> {
                        Timber.d("Snoozing alarm for wisdom $wisdomId")
                        wisdomAlarmManager.scheduleSnoozeAlarm(wisdomId)
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Reminder snoozed for 15 minutes", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}