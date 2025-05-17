package com.example.wisdomreminder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity

// Renamed from WisdomReminderDatabse.kt to WisdomReminderDatabase.kt
@Database(
    entities = [WisdomEntity::class],
    version = 1,
    exportSchema = true // Changed to true to track schema history
)
@TypeConverters(Converters::class)
abstract class WisdomReminderDatabase : RoomDatabase() {
    abstract fun wisdomDao(): WisdomDao

    companion object {
        // Migration from 1 to 2 when you need to update schema
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration: Add a new column
                // database.execSQL("ALTER TABLE wisdom ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}