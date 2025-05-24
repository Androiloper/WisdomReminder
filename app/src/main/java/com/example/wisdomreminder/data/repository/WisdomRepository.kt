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
            if (BuildConfig.DEBUG) {
                val addedItem = wisdomDao.getWisdomById(id)
                Log.d(TAG, "Verification - Added wisdom: ${addedItem?.text}, isActive=${addedItem?.isActive}, isFavorite=${addedItem?.isFavorite}, orderIndex=${addedItem?.orderIndex}")
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

    override fun getActiveWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getActiveWisdom()
            .mapToWisdomListAndCatch("Error getting active wisdom", TAG)

    override fun getStrictlyQueuedWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getStrictlyQueuedWisdom()
            .onEach { entities ->
                if (BuildConfig.DEBUG) Log.d(TAG, "Mapping ${entities.size} queued wisdom entities")
            }
            .mapToWisdomListAndCatch("Error getting queued wisdom", TAG)
            .onEach { wisdom ->
                if (BuildConfig.DEBUG) Log.d(TAG, "Emitting ${wisdom.size} queued wisdom items")
            }

    override fun getFavoriteDisplayableWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getFavoriteDisplayableWisdom()
            .mapToWisdomListAndCatch("Error getting favorite queued wisdom", TAG)


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
            val rowsUpdated = wisdomDao.activateWisdom(wisdomId, LocalDateTime.now())
            if (rowsUpdated <= 0) {
                Log.w(TAG, "SQL update affected 0 rows for activateWisdom. Using direct entity update for ID: $wisdomId")
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
                    Log.d(TAG, "Used direct entity update to activate wisdom: $wisdomId")
                } else {
                    return Result.failure(Exception("Wisdom with ID $wisdomId not found for activation"))
                }
            }
            val verifiedWisdom = wisdomDao.getWisdomById(wisdomId)
            if (verifiedWisdom?.isActive != true) {
                return Result.failure(Exception("Failed to verify activation for wisdom $wisdomId"))
            }
            Log.d(TAG, "Successfully activated wisdom: $wisdomId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error activating wisdom: $wisdomId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateFavoriteStatus(wisdomId: Long, isFavorite: Boolean): Result<Boolean> { // New
        return try {
            wisdomDao.updateFavoriteStatus(wisdomId, isFavorite)
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite status for wisdom: $wisdomId", e)
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

    override fun searchWisdom(query: String): Flow<List<Wisdom>> =
        wisdomDao.searchWisdom(query)
            .mapToWisdomListAndCatch("Error searching wisdom with query: $query", TAG)

    override fun getDisplayableWisdomByCategory(category: String): Flow<List<Wisdom>> =
        wisdomDao.getDisplayableWisdomByCategory(category)
            .mapToWisdomListAndCatch("Error getting wisdom for category: $category", TAG)

    override fun getAllCategories(): Flow<List<String>> =
        wisdomDao.getAllCategories()
            .map { categories -> categories.filter { it.isNotBlank() } }
            .catch { e ->
                Log.e(TAG, "Error getting all categories", e)
                emit(emptyList())
            }

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

    @Transaction
    override suspend fun renameCategory(oldName: String, newName: String): Result<Int> {
        return try {
            if (oldName.equals(newName, ignoreCase = true)) {
                Log.d(TAG, "RenameCategory: Old and new names are the same ('$oldName'). No action needed.")
                return Result.success(0)
            }
            val rowsAffected = wisdomDao.updateCategoryName(oldName, newName)
            Log.d(TAG, "Renamed category from '$oldName' to '$newName'. $rowsAffected items updated.")
            Result.success(rowsAffected)
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming category from '$oldName' to '$newName'", e)
            Result.failure(e)
        }
    }

    override suspend fun getWisdomCountForCategory(categoryName: String): Result<Int> {
        return try {
            Result.success(wisdomDao.getWisdomCountForCategory(categoryName))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wisdom count for category: $categoryName", e)
            Result.failure(e)
        }
    }

    @Transaction
    override suspend fun clearCategoryItemsToGeneral(categoryToClear: String, generalCategoryName: String): Result<Int> {
        return try {
            if (categoryToClear.equals(generalCategoryName, ignoreCase = true)) {
                Log.d(TAG, "ClearCategoryItems: Category to clear ('$categoryToClear') is already the general category. No action needed.")
                return Result.success(0)
            }
            val rowsAffected = wisdomDao.reassignWisdomCategoryToGeneral(categoryToClear, generalCategoryName)
            Log.d(TAG, "Cleared category '$categoryToClear' by reassigning $rowsAffected items to '$generalCategoryName'.")
            Result.success(rowsAffected)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing category '$categoryToClear' to '$generalCategoryName'", e)
            Result.failure(e)
        }
    }
}