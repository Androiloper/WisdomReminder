package com.example.wisdomreminder.data.repository

import android.util.Log
import androidx.room.Transaction
import com.example.wisdomreminder.BuildConfig
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.util.mapToCountAndCatch
import com.example.wisdomreminder.util.mapToWisdomListAndCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WisdomRepository @Inject constructor(
    private val wisdomDao: WisdomDao
) : IWisdomRepository {
    private val TAG = "WisdomRepository"

    // Basic CRUD operations
    override fun getAllWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getAllWisdom()
            .mapToWisdomListAndCatch("Error getting all wisdom", TAG)

    override suspend fun getWisdomById(id: Long): Result<Wisdom?> {
        return try {
            Result.success(wisdomDao.getWisdomById(id)?.toWisdom())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wisdom by ID: $id", e)
            Result.failure(e)
        }
    }

    override suspend fun addWisdom(wisdom: Wisdom): Result<Long> {
        return try {
            if (BuildConfig.DEBUG) Log.d(TAG, "Adding wisdom to database: $wisdom")
            val entity = WisdomEntity.fromWisdom(wisdom)
            val id = wisdomDao.insertWisdom(entity)
            Log.d(TAG, "Successfully added wisdom with ID: $id")

            // Verify in debug mode only
            if (BuildConfig.DEBUG) {
                val addedItem = wisdomDao.getWisdomById(id)
                Log.d(TAG, "Verification - Added wisdom: ${addedItem?.text}, isActive=${addedItem?.isActive}")
            }

            Result.success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding wisdom", e)
            Result.failure(e)
        }
    }

    override suspend fun updateWisdom(wisdom: Wisdom): Result<Boolean> {
        return try {
            val entity = WisdomEntity.fromWisdom(wisdom)
            wisdomDao.updateWisdom(entity)
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating wisdom: ${wisdom.id}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteWisdom(wisdom: Wisdom): Result<Boolean> {
        return try {
            val entity = WisdomEntity.fromWisdom(wisdom)
            wisdomDao.deleteWisdom(entity)
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting wisdom: ${wisdom.id}", e)
            Result.failure(e)
        }
    }

    // 21/21 Rule specific operations
    override fun getActiveWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getActiveWisdom()
            .mapToWisdomListAndCatch("Error getting active wisdom", TAG)

    override fun getQueuedWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getQueuedWisdom()
            .onEach { entities ->
                if (BuildConfig.DEBUG) Log.d(TAG, "Mapping ${entities.size} queued wisdom entities")
            }
            .mapToWisdomListAndCatch("Error getting queued wisdom", TAG)
            .onEach { wisdom ->
                if (BuildConfig.DEBUG) Log.d(TAG, "Emitting ${wisdom.size} queued wisdom items")
            }

    override fun getCompletedWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getCompletedWisdom()
            .mapToWisdomListAndCatch("Error getting completed wisdom", TAG)

    override suspend fun recordExposure(wisdomId: Long): Result<Boolean> {
        return try {
            wisdomDao.incrementExposure(wisdomId)
            Log.d(TAG, "Recorded exposure for wisdom: $wisdomId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording exposure for wisdom: $wisdomId", e)
            Result.failure(e)
        }
    }

    override suspend fun resetDailyExposures(): Result<Boolean> {
        return try {
            wisdomDao.resetDailyExposures()
            Log.d(TAG, "Reset daily exposures for all active wisdom")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting daily exposures", e)
            Result.failure(e)
        }
    }

    override suspend fun completeWisdom(): Result<Boolean> {
        return try {
            val completedCount = wisdomDao.completeWisdom(LocalDateTime.now())
            Log.d(TAG, "Marked $completedCount wisdom items as completed")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking wisdom as completed", e)
            Result.failure(e)
        }
    }

    @Transaction
    override suspend fun activateWisdom(wisdomId: Long): Result<Boolean> {
        return try {
            // Log the state before activation
            if (BuildConfig.DEBUG) {
                val beforeWisdom = wisdomDao.getWisdomById(wisdomId)
                Log.d(TAG, "Before activation: isActive=${beforeWisdom?.isActive}, id=$wisdomId")
            }

            // Perform the activation
            wisdomDao.activateWisdom(wisdomId, LocalDateTime.now())

            // Verify the change in debug mode
            if (BuildConfig.DEBUG) {
                val afterWisdom = wisdomDao.getWisdomById(wisdomId)
                Log.d(TAG, "After activation: isActive=${afterWisdom?.isActive}, id=$wisdomId")

                // If the activation didn't work, use fallback method
                if (afterWisdom?.isActive != true) {
                    Log.w(TAG, "Activation SQL update didn't set isActive=true. Using fallback.")

                    val wisdom = wisdomDao.getWisdomById(wisdomId)
                    if (wisdom != null) {
                        val updatedWisdom = wisdom.copy(
                            isActive = true,
                            startDate = LocalDateTime.now(),
                            currentDay = 1,
                            exposuresToday = 0,
                            exposuresTotal = 0,
                            dateCompleted = null
                        )
                        wisdomDao.updateWisdom(updatedWisdom)
                        Log.d(TAG, "Used fallback update to activate wisdom: $wisdomId")
                    }
                }
            }

            Log.d(TAG, "Activated wisdom: $wisdomId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error activating wisdom: $wisdomId", e)
            Result.failure(e)
        }
    }

    override suspend fun getActiveWisdomDirect(): Result<List<Wisdom>> {
        return try {
            val activeEntities = wisdomDao.getActiveWisdomDirectly()
            Log.d(TAG, "Direct query found ${activeEntities.size} active wisdom items")
            Result.success(activeEntities.map { it.toWisdom() })
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active wisdom directly", e)
            Result.failure(e)
        }
    }

    // Search and categorization
    override fun searchWisdom(query: String): Flow<List<Wisdom>> =
        wisdomDao.searchWisdom(query)
            .mapToWisdomListAndCatch("Error searching wisdom with query: $query", TAG)

    override fun getWisdomByCategory(category: String): Flow<List<Wisdom>> =
        wisdomDao.getWisdomByCategory(category)
            .mapToWisdomListAndCatch("Error getting wisdom for category: $category", TAG)

    override fun getAllCategories(): Flow<List<String>> =
        wisdomDao.getAllCategories()
            .catch { e ->
                Log.e(TAG, "Error getting all categories", e)
                emit(emptyList())
            }

    // Statistics
    override suspend fun getTotalWisdomCount(): Result<Int> {
        return try {
            Result.success(wisdomDao.countWisdom())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total wisdom count", e)
            Result.failure(e)
        }
    }

    override fun getActiveWisdomCount(): Flow<Int> =
        wisdomDao.getActiveCount()
            .mapToCountAndCatch("Error getting active wisdom count", TAG)

    override fun getCompletedWisdomCount(): Flow<Int> =
        wisdomDao.getCompletedCount()
            .mapToCountAndCatch("Error getting completed wisdom count", TAG)
}