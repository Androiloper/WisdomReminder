package com.example.wisdomreminder

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.wisdomreminder.util.WisdomAlarmManager
import com.example.wisdomreminder.util.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WisdomReminderApplication : Application(), Configuration.Provider {
    private val TAG = "WisdomReminderApp"

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workScheduler: WorkScheduler

    @Inject
    lateinit var wisdomAlarmManager: WisdomAlarmManager

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Schedule work for reminders and 21/21 rule tracking
        workScheduler.scheduleAllWork()

        // Schedule alarms based on user preferences
        wisdomAlarmManager.scheduleAlarms()

        Log.d("WisdomReminderApp", "Application initialized")
        Log.d(TAG, "Application initialized")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}