package com.example.wisdomreminder

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WisdomReminderApplication : Application() {
    private val TAG = "WisdomReminderApp"

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Log.d(TAG, "Application initialized")
    }
}