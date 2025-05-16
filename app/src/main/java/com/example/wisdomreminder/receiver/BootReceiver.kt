package com.example.wisdomreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.wisdomreminder.service.BootStarterService
import com.example.wisdomreminder.service.WisdomDisplayService
import com.example.wisdomreminder.util.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class BootReceiver : BroadcastReceiver() {

    lateinit var workScheduler: WorkScheduler
    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, scheduling work")

            // Instead of direct injection, get the application context
            val appContext = context.applicationContext

            // Start the WorkScheduler through a service or WorkManager directly
            val workIntent = Intent(appContext, BootStarterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(workIntent)
            } else {
                appContext.startService(workIntent)
            }

            // Start foreground service for wisdom display
            try {
                val serviceIntent = Intent(appContext, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_START_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(serviceIntent)
                } else {
                    appContext.startService(serviceIntent)
                }
                Log.d(TAG, "Started wisdom service after boot")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service after boot", e)
            }
        }
    }
}