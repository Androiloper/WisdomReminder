// Create in the same package as WisdomRepository

package com.example.wisdomreminder.data.repository

import com.example.wisdomreminder.model.Wisdom
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Interface for the Wisdom Repository following dependency inversion principle
 */
interface IWisdomRepository {
    // Basic CRUD operations
    fun getAllWisdom(): Flow<List<Wisdom>>
    suspend fun getWisdomById(id: Long): Result<Wisdom?>
    suspend fun addWisdom(wisdom: Wisdom): Result<Long>
    suspend fun updateWisdom(wisdom: Wisdom): Result<Boolean>
    suspend fun deleteWisdom(wisdom: Wisdom): Result<Boolean>

    // 21/21 Rule specific operations
    fun getActiveWisdom(): Flow<List<Wisdom>>
    fun getQueuedWisdom(): Flow<List<Wisdom>>
    fun getCompletedWisdom(): Flow<List<Wisdom>>
    suspend fun recordExposure(wisdomId: Long): Result<Boolean>
    suspend fun resetDailyExposures(): Result<Boolean>
    suspend fun completeWisdom(): Result<Boolean>
    suspend fun activateWisdom(wisdomId: Long): Result<Boolean>
    suspend fun getActiveWisdomDirect(): Result<List<Wisdom>>

    // Search and categorization
    fun searchWisdom(query: String): Flow<List<Wisdom>>
    fun getWisdomByCategory(category: String): Flow<List<Wisdom>>
    fun getAllCategories(): Flow<List<String>>

    // Statistics
    suspend fun getTotalWisdomCount(): Result<Int>
    fun getActiveWisdomCount(): Flow<Int>
    fun getCompletedWisdomCount(): Flow<Int>
}