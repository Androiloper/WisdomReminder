package com.example.wisdomreminder.util

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wisdomreminder.work.DailyResetWorker
import com.example.wisdomreminder.work.ReminderWorker
import com.example.wisdomreminder.work.WisdomServiceMonitorWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "WorkScheduler"

    fun scheduleAllWork() {
        scheduleReminders()
        scheduleDailyReset()
        scheduleServiceMonitor()
    }

    /**
     * Schedule the wisdom reminders that run throughout the day
     */
    private fun scheduleReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Run every 45 minutes to display reminders
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            45, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "wisdom_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )

        Timber.d("Reminder work scheduled")
    }

    /**
     * Schedule daily reset work - runs at midnight to reset daily counters
     * and update the day counter for the 21/21 rule
     */
    private fun scheduleDailyReset() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Run daily (every 24 hours)
        val resetRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reset",
            ExistingPeriodicWorkPolicy.KEEP,
            resetRequest
        )

        Timber.d("Daily reset work scheduled")
    }

    /**
     * Schedule service monitor to ensure the Wisdom service stays running
     */
    private fun scheduleServiceMonitor() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Run every 15 minutes to check if service is running
        val monitorRequest = PeriodicWorkRequestBuilder<WisdomServiceMonitorWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "service_monitor",
            ExistingPeriodicWorkPolicy.KEEP,
            monitorRequest
        )

        Timber.d("Service monitor work scheduled")
    }
}