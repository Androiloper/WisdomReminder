package com.example.wisdomreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.util.WisdomAlarmManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

// Remove the @AndroidEntryPoint annotation
class NotificationReceiver : BroadcastReceiver() {

    // Add an EntryPoint interface
    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface NotificationReceiverEntryPoint {
        fun wisdomRepository(): WisdomRepository
        fun wisdomAlarmManager(): WisdomAlarmManager
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Get dependencies using EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationReceiverEntryPoint::class.java
        )

        val wisdomRepository = entryPoint.wisdomRepository()
        val wisdomAlarmManager = entryPoint.wisdomAlarmManager()

        val action = intent.action
        val wisdomId = intent.getLongExtra("wisdom_id", -1L)

        if (wisdomId == -1L) return

        when (action) {
            "com.example.wisdomreminder.MARK_READ" -> {
                Timber.d("Marking wisdom $wisdomId as read")
                GlobalScope.launch {
                    wisdomRepository.recordExposure(wisdomId)
                }

                Toast.makeText(
                    context,
                    "Wisdom marked as read",
                    Toast.LENGTH_SHORT
                ).show()
            }
            "com.example.wisdomreminder.SNOOZE_ALARM" -> {
                Timber.d("Snoozing alarm for wisdom $wisdomId")
                wisdomAlarmManager.scheduleSnoozeAlarm(wisdomId)

                // Show snooze confirmation toast
                Toast.makeText(
                    context,
                    "Reminder snoozed for 15 minutes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}