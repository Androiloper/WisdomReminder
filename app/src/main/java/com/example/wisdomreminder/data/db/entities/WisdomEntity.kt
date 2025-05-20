package com.example.wisdomreminder.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Simplified Wisdom entity with only essential fields
 */
@Entity(tableName = "wisdom")
data class WisdomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val text: String,
    val category: String = "General"
)