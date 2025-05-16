package com.example.wisdomreminder.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.wisdomreminder.util.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootStarterService : Service() {

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Schedule the work
        workScheduler.scheduleAllWork()

        // Stop the service after work is scheduled
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}