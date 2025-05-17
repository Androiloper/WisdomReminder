package com.example.wisdomreminder.work

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.service.WisdomDisplayService
import com.example.wisdomreminder.util.NotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Worker that triggers wisdom reminders throughout the day
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val wisdomRepository: WisdomRepository,
    private val notificationManager: NotificationManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if notifications are enabled
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            if (!prefs.getBoolean("notifications_enabled", true)) {
                Timber.d("Notifications disabled, skipping")
                return@withContext Result.success()
            }

            // Get active wisdom entries
            val activeWisdom = wisdomRepository.getActiveWisdom().first()

            if (activeWisdom.isNotEmpty()) {
                // Prioritize wisdom that needs more exposures today
                val prioritizedWisdom = activeWisdom.sortedBy { it.exposuresToday }

                // Take the top priority wisdom
                val wisdomToShow = prioritizedWisdom.first()

                // Show notification
                notificationManager.showWisdomNotification(wisdomToShow)

                Timber.d("Displayed wisdom: ${wisdomToShow.text.take(20)}... (${wisdomToShow.exposuresToday + 1}/${wisdomToShow.currentDay}/21)")
            } else {
                Timber.d("No active wisdom to display")
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in reminder worker")
            Result.retry()
        }
    }
}