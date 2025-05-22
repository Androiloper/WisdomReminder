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
        markWisdomAsCompleted(wisdomId, completionDate)
    }

    @Query("UPDATE wisdom SET isActive = 0, dateCompleted = :completionDate WHERE id = :wisdomId")
    suspend fun markWisdomAsCompleted(wisdomId: Long, completionDate: LocalDateTime)

    @Transaction
    suspend fun performDailyReset() {
        resetDailyExposures()
        completeWisdom()
    }

    @Query("SELECT * FROM wisdom WHERE isActive = 1 ORDER BY lastExposureTime ASC LIMIT :limit")
    suspend fun getPrioritizedActiveWisdom(limit: Int = 5): List<WisdomEntity>

    @Query("SELECT * FROM wisdom WHERE isActive = 1 ORDER BY startDate DESC")
    fun getActiveWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE isActive = 0 AND dateCompleted IS NULL ORDER BY dateCreated DESC")
    fun getQueuedWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE dateCompleted IS NOT NULL ORDER BY dateCompleted DESC")
    fun getCompletedWisdom(): Flow<List<WisdomEntity>>

    @Query("UPDATE wisdom SET exposuresToday = exposuresToday + 1, exposuresTotal = exposuresTotal + 1, lastExposureTime = :timestamp WHERE id = :wisdomId")
    suspend fun incrementExposure(wisdomId: Long, timestamp: LocalDateTime = LocalDateTime.now())

    @Query("UPDATE wisdom SET exposuresToday = 0, currentDay = currentDay + 1 WHERE isActive = 1 AND currentDay < 21")
    suspend fun resetDailyExposures()

    @Query("UPDATE wisdom SET isActive = 0, dateCompleted = :completionDate WHERE isActive = 1 AND currentDay >= 21")
    suspend fun completeWisdom(completionDate: LocalDateTime = LocalDateTime.now())

    @Query("UPDATE wisdom SET isActive = 1, startDate = :startDate, currentDay = 1, exposuresToday = 0, exposuresTotal = 0, dateCompleted = NULL WHERE id = :wisdomId")
    suspend fun activateWisdom(wisdomId: Long, startDate: LocalDateTime): Int

    @Query("SELECT * FROM wisdom WHERE text LIKE '%' || :query || '%' OR source LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchWisdom(query: String): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE category = :category ORDER BY dateCreated DESC")
    fun getWisdomByCategory(category: String): Flow<List<WisdomEntity>>

    @Query("SELECT DISTINCT category FROM wisdom ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM wisdom")
    suspend fun countWisdom(): Int

    @Query("SELECT COUNT(*) FROM wisdom WHERE isActive = 1")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM wisdom WHERE dateCompleted IS NOT NULL")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT * FROM wisdom WHERE isActive = 1")
    suspend fun getActiveWisdomDirectly(): List<WisdomEntity>

    // --- New methods for category management ---
    @Query("UPDATE wisdom SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String): Int

    @Query("SELECT COUNT(*) FROM wisdom WHERE category = :categoryName")
    suspend fun getWisdomCountForCategory(categoryName: String): Int

    // Option 1: Delete wisdom items of a category (more destructive)
    // @Query("DELETE FROM wisdom WHERE category = :categoryName")
    // suspend fun deleteWisdomItemsByCategory(categoryName: String): Int

    // Option 2: Re-assign wisdom items to a general category (safer "delete" of a category label)
    @Query("UPDATE wisdom SET category = :generalCategoryName WHERE category = :categoryToClear")
    suspend fun reassignWisdomCategoryToGeneral(categoryToClear: String, generalCategoryName: String): Int
}