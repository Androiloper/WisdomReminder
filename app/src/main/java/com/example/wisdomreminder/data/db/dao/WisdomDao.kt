package com.example.wisdomreminder.data.db.dao

import androidx.room.*
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WisdomDao {
    // Basic CRUD operations
    @Query("SELECT * FROM wisdom ORDER BY orderIndex ASC, dateCreated DESC") // Consider orderIndex
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

    @Query("SELECT * FROM wisdom WHERE isActive = 1 ORDER BY orderIndex ASC, startDate DESC") // Consider orderIndex
    fun getActiveWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE isActive = 0 AND dateCompleted IS NULL ORDER BY orderIndex ASC, dateCreated DESC") // Consider orderIndex
    fun getStrictlyQueuedWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE dateCompleted IS NOT NULL ORDER BY dateCompleted DESC")
    fun getCompletedWisdom(): Flow<List<WisdomEntity>>

    // Favorite items that are NOT completed (can be active or strictly queued)
    @Query("SELECT * FROM wisdom WHERE isFavorite = 1 AND dateCompleted IS NULL ORDER BY orderIndex ASC, dateCreated DESC")
    fun getFavoriteDisplayableWisdom(): Flow<List<WisdomEntity>>


    @Query("UPDATE wisdom SET exposuresToday = exposuresToday + 1, exposuresTotal = exposuresTotal + 1, lastExposureTime = :timestamp WHERE id = :wisdomId")
    suspend fun incrementExposure(wisdomId: Long, timestamp: LocalDateTime = LocalDateTime.now())

    @Query("UPDATE wisdom SET exposuresToday = 0, currentDay = currentDay + 1 WHERE isActive = 1 AND currentDay < 21")
    suspend fun resetDailyExposures()

    @Query("UPDATE wisdom SET isActive = 0, dateCompleted = :completionDate WHERE isActive = 1 AND currentDay >= 21")
    suspend fun completeWisdom(completionDate: LocalDateTime = LocalDateTime.now())

    @Query("UPDATE wisdom SET isActive = 1, startDate = :startDate, currentDay = 1, exposuresToday = 0, exposuresTotal = 0, dateCompleted = NULL, orderIndex = 0 WHERE id = :wisdomId") // Reset orderIndex on activation
    suspend fun activateWisdom(wisdomId: Long, startDate: LocalDateTime): Int

    @Query("SELECT * FROM wisdom WHERE (text LIKE '%' || :query || '%' OR source LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') AND dateCompleted IS NULL") // Search non-completed
    fun searchWisdom(query: String): Flow<List<WisdomEntity>>

    // Wisdom from a category that is NOT completed (can be active or strictly queued)
    @Query("SELECT * FROM wisdom WHERE category = :category AND dateCompleted IS NULL ORDER BY orderIndex ASC, dateCreated DESC")
    fun getDisplayableWisdomByCategory(category: String): Flow<List<WisdomEntity>>


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

    @Query("UPDATE wisdom SET category = :generalCategoryName WHERE category = :categoryToClear")
    suspend fun reassignWisdomCategoryToGeneral(categoryToClear: String, generalCategoryName: String): Int

    // For updating favorite status
    @Query("UPDATE wisdom SET isFavorite = :isFavorite WHERE id = :wisdomId")
    suspend fun updateFavoriteStatus(wisdomId: Long, isFavorite: Boolean)

    // New method for deactivating wisdom
    @Query("UPDATE wisdom SET isActive = 0, startDate = NULL, currentDay = 0, exposuresToday = 0, exposuresTotal = 0, dateCompleted = NULL, lastExposureTime = NULL, orderIndex = (SELECT IFNULL(MAX(orderIndex), -1) + 1 FROM wisdom WHERE isActive = 0 AND dateCompleted IS NULL AND category = (SELECT category FROM wisdom WHERE id = :wisdomId)) WHERE id = :wisdomId")
    suspend fun deactivateWisdom(wisdomId: Long): Int
}