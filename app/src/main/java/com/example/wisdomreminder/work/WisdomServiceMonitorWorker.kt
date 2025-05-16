package com.example.wisdomreminder.work

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wisdomreminder.service.WisdomDisplayService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Worker to ensure the WisdomDisplayService keeps running
 */
@HiltWorker
class WisdomServiceMonitorWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if the service is running using the static flag
            val isServiceRunning = WisdomDisplayService.isServiceRunning

            if (!isServiceRunning) {
                Timber.d("WisdomDisplayService not running, starting it")

                // Start the service
                val serviceIntent = Intent(applicationContext, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_START_SERVICE
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    applicationContext.startForegroundService(serviceIntent)
                } else {
                    applicationContext.startService(serviceIntent)
                }
            } else {
                Timber.d("WisdomDisplayService already running")
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in service monitor worker")
            Result.retry()
        }
    }
}