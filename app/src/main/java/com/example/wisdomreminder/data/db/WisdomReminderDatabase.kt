package com.example.wisdomreminder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity

@Database(
    entities = [WisdomEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WisdomReminderDatabase : RoomDatabase() {
    abstract fun wisdomDao(): WisdomDao

    companion object {
        // Proper migration that creates missing indices
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create missing indices
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_isActive` ON `wisdom` (`isActive`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_category` ON `wisdom` (`category`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_dateCreated` ON `wisdom` (`dateCreated`)")
            }
        }
    }
}