package com.example.wisdomreminder.data.db.dao

import androidx.room.*
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WisdomDao {
    // Basic CRUD operations
    @Query("SELECT * FROM wisdom ORDER BY dateCreated DESC")
    fun getAllWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE id = :id")
    suspend fun getWisdomById(id: Long): WisdomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWisdom(wisdom: WisdomEntity): Long

    @Update
    suspend fun updateWisdom(wisdom: WisdomEntity)

    @Delete
    suspend fun deleteWisdom(wisdom: WisdomEntity)

    // Transaction for completing the 21-day cycle
    @Transaction
    suspend fun completeWisdomCycle(wisdomId: Long, completionDate: LocalDateTime = LocalDateTime.now()) {
        // Update the wisdom to mark it as completed
        markWisdomAsCompleted(wisdomId, completionDate)

        // Optionally update related stats or perform other operations
        // updateRelatedStatistics(wisdomId)
    }

    @Query("UPDATE wisdom SET isActive = 0, dateCompleted = :completionDate WHERE id = :wisdomId")
    suspend fun markWisdomAsCompleted(wisdomId: Long, completionDate: LocalDateTime)

    // Transaction for daily resets - more efficient than separate calls
    @Transaction
    suspend fun performDailyReset() {
        // Reset daily exposures
        resetDailyExposures()

        // Check and complete wisdom that reached 21 days
        completeWisdom()
    }

    // More efficient queries with LIMIT for pagination
    @Query("SELECT * FROM wisdom WHERE isActive = 1 ORDER BY lastExposureTime ASC LIMIT :limit")
    suspend fun getPrioritizedActiveWisdom(limit: Int = 5): List<WisdomEntity>

    // Add count-only query for statistics (more efficient)
   // @Query("SELECT COUNT(*) FROM wisdom WHERE isActive = 1")
    //fun getActiveCount(): Flow<Int>

    // 21/21 Rule specific queries

    @Query("SELECT * FROM wisdom WHERE isActive = 1 ORDER BY startDate DESC")
    fun getActiveWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE isActive = 0 AND dateCompleted IS NULL ORDER BY dateCreated DESC")
    fun getQueuedWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE dateCompleted IS NOT NULL ORDER BY dateCompleted DESC")
    fun getCompletedWisdom(): Flow<List<WisdomEntity>>

    // Update exposure count
    @Query("UPDATE wisdom SET exposuresToday = exposuresToday + 1, exposuresTotal = exposuresTotal + 1, lastExposureTime = :timestamp WHERE id = :wisdomId")
    suspend fun incrementExposure(wisdomId: Long, timestamp: LocalDateTime = LocalDateTime.now())

    // Reset daily exposures (called at midnight)
    @Query("UPDATE wisdom SET exposuresToday = 0, currentDay = currentDay + 1 WHERE isActive = 1 AND currentDay < 21")
    suspend fun resetDailyExposures()

    // Mark wisdom as completed when 21 days are done
    @Query("UPDATE wisdom SET isActive = 0, dateCompleted = :completionDate WHERE isActive = 1 AND currentDay >= 21")
    suspend fun completeWisdom(completionDate: LocalDateTime = LocalDateTime.now())

    // Activate a wisdom
    @Query("UPDATE wisdom SET isActive = 1, startDate = :startDate, currentDay = 1, exposuresToday = 0, exposuresTotal = 0 WHERE id = :wisdomId")
    suspend fun activateWisdom(wisdomId: Long, startDate: LocalDateTime = LocalDateTime.now())

    // Search
    @Query("SELECT * FROM wisdom WHERE text LIKE '%' || :query || '%' OR source LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchWisdom(query: String): Flow<List<WisdomEntity>>

    // Get by category
    @Query("SELECT * FROM wisdom WHERE category = :category ORDER BY dateCreated DESC")
    fun getWisdomByCategory(category: String): Flow<List<WisdomEntity>>

    // Get all categories
    @Query("SELECT DISTINCT category FROM wisdom ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    // Count total wisdom
    @Query("SELECT COUNT(*) FROM wisdom")
    suspend fun countWisdom(): Int

    // Get statistics
    @Query("SELECT COUNT(*) FROM wisdom WHERE isActive = 1")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM wisdom WHERE dateCompleted IS NOT NULL")
    fun getCompletedCount(): Flow<Int>
}