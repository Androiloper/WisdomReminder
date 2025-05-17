package com.example.wisdomreminder.data.repository

import android.util.Log
import androidx.room.Transaction
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import com.example.wisdomreminder.model.Wisdom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WisdomRepository @Inject constructor(
    private val wisdomDao: WisdomDao
) {
    private val TAG = "WisdomRepository"

    // Basic CRUD operations
    fun getAllWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getAllWisdom()
            .map { entities -> entities.map { it.toWisdom() } }
            .catch { e ->
                Log.e(TAG, "Error getting all wisdom", e)
                emit(emptyList())
            }

    suspend fun getWisdomById(id: Long): Wisdom? {
        return try {
            wisdomDao.getWisdomById(id)?.toWisdom()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wisdom by ID: $id", e)
            null
        }
    }

    suspend fun addWisdom(wisdom: Wisdom): Long {
        return try {
            Log.d(TAG, "Adding wisdom to database: $wisdom")
            val entity = WisdomEntity.fromWisdom(wisdom)
            Log.d(TAG, "Converted to entity with isActive=${entity.isActive}, dateCompleted=${entity.dateCompleted}")
            val id = wisdomDao.insertWisdom(entity)
            Log.d(TAG, "Successfully added wisdom with ID: $id")

            // Verify the item was added correctly by retrieving it
            val addedItem = wisdomDao.getWisdomById(id)
            Log.d(TAG, "Retrieved added wisdom: ${addedItem?.text}, isActive=${addedItem?.isActive}, dateCompleted=${addedItem?.dateCompleted}")

            id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding wisdom", e)
            -1L
        }
    }

    suspend fun updateWisdom(wisdom: Wisdom) {
        try {
            val entity = WisdomEntity.fromWisdom(wisdom)
            wisdomDao.updateWisdom(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating wisdom: ${wisdom.id}", e)
        }
    }

    suspend fun deleteWisdom(wisdom: Wisdom) {
        try {
            val entity = WisdomEntity.fromWisdom(wisdom)
            wisdomDao.deleteWisdom(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting wisdom: ${wisdom.id}", e)
        }
    }

    // 21/21 Rule specific operations
    fun getActiveWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getActiveWisdom()
            .map { entities -> entities.map { it.toWisdom() } }
            .catch { e ->
                Log.e(TAG, "Error getting active wisdom", e)
                emit(emptyList())
            }

    fun getQueuedWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getQueuedWisdom()
            .map { entities ->
                Log.d(TAG, "Mapping ${entities.size} queued wisdom entities")
                entities.map { it.toWisdom() }
            }
            .catch { e ->
                Log.e(TAG, "Error getting queued wisdom", e)
                emit(emptyList())
            }
            .onEach { wisdom ->
                Log.d(TAG, "Emitting ${wisdom.size} queued wisdom items")
            }

    fun getCompletedWisdom(): Flow<List<Wisdom>> =
        wisdomDao.getCompletedWisdom()
            .map { entities -> entities.map { it.toWisdom() } }
            .catch { e ->
                Log.e(TAG, "Error getting completed wisdom", e)
                emit(emptyList())
            }



    suspend fun recordExposure(wisdomId: Long) {
        try {
            wisdomDao.incrementExposure(wisdomId)
            Log.d(TAG, "Recorded exposure for wisdom: $wisdomId")
        } catch (e: Exception) {
            Log.e(TAG, "Error recording exposure for wisdom: $wisdomId", e)
        }
    }

    suspend fun resetDailyExposures() {
        try {
            wisdomDao.resetDailyExposures()
            Log.d(TAG, "Reset daily exposures for all active wisdom")
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting daily exposures", e)
        }
    }

    suspend fun completeWisdom() {
        try {
            wisdomDao.completeWisdom(LocalDateTime.now())
            Log.d(TAG, "Marked completed wisdom")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking wisdom as completed", e)
        }
    }

    suspend fun activateWisdom(wisdomId: Long) {
        try {
            // Log the state before activation
            val beforeWisdom = wisdomDao.getWisdomById(wisdomId)
            Log.d(TAG, "Before activation: isActive=${beforeWisdom?.isActive}, id=$wisdomId")

            // Activate via DAO
            wisdomDao.activateWisdom(wisdomId, LocalDateTime.now())

            // Verify the change
            val afterWisdom = wisdomDao.getWisdomById(wisdomId)
            Log.d(TAG, "After activation: isActive=${afterWisdom?.isActive}, id=$wisdomId")

            if (afterWisdom?.isActive != true) {
                Log.e(TAG, "Activation failed! Trying fallback method.")

                // Fallback: Use entity update instead of query
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
        } catch (e: Exception) {
            Log.e(TAG, "Error activating wisdom: $wisdomId", e)
        }
    }

    // Search and categorization
    fun searchWisdom(query: String): Flow<List<Wisdom>> =
        wisdomDao.searchWisdom(query)
            .map { entities -> entities.map { it.toWisdom() } }
            .catch { e ->
                Log.e(TAG, "Error searching wisdom with query: $query", e)
                emit(emptyList())
            }

    fun getWisdomByCategory(category: String): Flow<List<Wisdom>> =
        wisdomDao.getWisdomByCategory(category)
            .map { entities -> entities.map { it.toWisdom() } }
            .catch { e ->
                Log.e(TAG, "Error getting wisdom for category: $category", e)
                emit(emptyList())
            }

    fun getAllCategories(): Flow<List<String>> =
        wisdomDao.getAllCategories()
            .catch { e ->
                Log.e(TAG, "Error getting all categories", e)
                emit(emptyList())
            }

    // Statistics
    suspend fun getTotalWisdomCount(): Int {
        return try {
            wisdomDao.countWisdom()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total wisdom count", e)
            0
        }
    }

    fun getActiveWisdomCount(): Flow<Int> =
        wisdomDao.getActiveCount()
            .catch { e ->
                Log.e(TAG, "Error getting active wisdom count", e)
                emit(0)
            }

    fun getCompletedWisdomCount(): Flow<Int> =
        wisdomDao.getCompletedCount()
            .catch { e ->
                Log.e(TAG, "Error getting completed wisdom count", e)
                emit(0)
            }

    suspend fun getActiveWisdomDirect(): List<Wisdom> {
        try {
            val query = "SELECT * FROM wisdom WHERE isActive = 1"
            val activeEntities = wisdomDao.getActiveWisdomDirectly()
            Log.d(TAG, "Direct query found ${activeEntities.size} active wisdom items")
            return activeEntities.map { it.toWisdom() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active wisdom directly", e)
            return emptyList()
        }
    }
}