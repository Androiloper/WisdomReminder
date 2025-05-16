package com.example.wisdomreminder.data.repository

import android.util.Log
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import com.example.wisdomreminder.model.Wisdom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
            val entity = WisdomEntity.fromWisdom(wisdom)
            wisdomDao.insertWisdom(entity)
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
            .map { entities -> entities.map { it.toWisdom() } }
            .catch { e ->
                Log.e(TAG, "Error getting queued wisdom", e)
                emit(emptyList())
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
            wisdomDao.activateWisdom(wisdomId)
            Log.d(TAG, "Activated wisdom: $wisdomId")
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
}