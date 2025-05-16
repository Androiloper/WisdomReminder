package com.example.wisdomreminder.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wisdomreminder.data.repository.WisdomRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Worker that resets daily exposure counters and updates day count
 * Typically run at midnight
 */
@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val wisdomRepository: WisdomRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("Running daily reset")

            // Reset daily exposure counts and increment day counter
            wisdomRepository.resetDailyExposures()

            // Complete any wisdom that has reached 21 days
            wisdomRepository.completeWisdom()

            Timber.d("Daily reset completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in daily reset worker")
            Result.retry()
        }
    }
}