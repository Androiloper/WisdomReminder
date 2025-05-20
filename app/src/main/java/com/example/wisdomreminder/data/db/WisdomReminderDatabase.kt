package com.example.wisdomreminder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity

@Database(
    entities = [WisdomEntity::class],
    version = 1,
    exportSchema = true
)
abstract class WisdomReminderDatabase : RoomDatabase() {
    abstract fun wisdomDao(): WisdomDao
}