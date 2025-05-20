package com.example.wisdomreminder.data.db.dao

import androidx.room.*
import com.example.wisdomreminder.data.db.entities.WisdomEntity
import kotlinx.coroutines.flow.Flow

/**
 * Simplified DAO for Wisdom with only basic operations
 */
@Dao
interface WisdomDao {
    // Basic CRUD operations
    @Query("SELECT * FROM wisdom ORDER BY id DESC")
    fun getAllWisdom(): Flow<List<WisdomEntity>>

    @Query("SELECT * FROM wisdom WHERE id = :id")
    suspend fun getWisdomById(id: Long): WisdomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWisdom(wisdom: WisdomEntity): Long

    @Update
    suspend fun updateWisdom(wisdom: WisdomEntity)

    @Delete
    suspend fun deleteWisdom(wisdom: WisdomEntity)

    // Get by category
    @Query("SELECT * FROM wisdom WHERE category = :category ORDER BY id DESC")
    fun getWisdomByCategory(category: String): Flow<List<WisdomEntity>>

    // Get all categories
    @Query("SELECT DISTINCT category FROM wisdom ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    // Count total wisdom
    @Query("SELECT COUNT(*) FROM wisdom")
    suspend fun countWisdom(): Int
}