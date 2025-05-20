package com.example.wisdomreminder.data.repository

import android.util.Log
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import com.example.wisdomreminder.model.Wisdom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified repository for Wisdom with only essential operations
 */
@Singleton
class WisdomRepository @Inject constructor(
    private val wisdomDao: WisdomDao
) {
    private val TAG = "WisdomRepository"

    // Convert between domain model and entity
    private fun WisdomEntity.toWisdom() = Wisdom(
        id = id,
        title = title,
        text = text,
        category = category
    )

    private fun Wisdom.toEntity() = WisdomEntity(
        id = id,
        title = title,
        text = text,
        category = category
    )

    // Get all wisdom with Flow
    fun getAllWisdom(): Flow<List<Wisdom>> = wisdomDao.getAllWisdom()
        .map { entities -> entities.map { it.toWisdom() } }
        .catch { e ->
            Log.e(TAG, "Error getting all wisdom", e)
            emit(emptyList())
        }

    // Get wisdom by ID
    suspend fun getWisdomById(id: Long): Result<Wisdom?> {
        return try {
            val entity = wisdomDao.getWisdomById(id)
            Result.success(entity?.toWisdom())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wisdom by ID: $id", e)
            Result.failure(e)
        }
    }

    // Add new wisdom
    suspend fun addWisdom(wisdom: Wisdom): Result<Long> {
        return try {
            val id = wisdomDao.insertWisdom(wisdom.toEntity())
            Log.d(TAG, "Added wisdom with ID: $id")
            Result.success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding wisdom", e)
            Result.failure(e)
        }
    }

    // Update wisdom
    suspend fun updateWisdom(wisdom: Wisdom): Result<Boolean> {
        return try {
            wisdomDao.updateWisdom(wisdom.toEntity())
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating wisdom: ${wisdom.id}", e)
            Result.failure(e)
        }
    }

    // Delete wisdom
    suspend fun deleteWisdom(wisdom: Wisdom): Result<Boolean> {
        return try {
            wisdomDao.deleteWisdom(wisdom.toEntity())
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting wisdom: ${wisdom.id}", e)
            Result.failure(e)
        }
    }

    // Get by category
    fun getWisdomByCategory(category: String): Flow<List<Wisdom>> =
        wisdomDao.getWisdomByCategory(category)
            .map { entities -> entities.map { it.toWisdom() } }
            .catch { e ->
                Log.e(TAG, "Error getting wisdom for category: $category", e)
                emit(emptyList())
            }

    // Get all categories
    fun getAllCategories(): Flow<List<String>> =
        wisdomDao.getAllCategories()
            .catch { e ->
                Log.e(TAG, "Error getting all categories", e)
                emit(emptyList())
            }

    // Get count
    suspend fun getTotalWisdomCount(): Result<Int> {
        return try {
            Result.success(wisdomDao.countWisdom())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total wisdom count", e)
            Result.failure(e)
        }
    }
}