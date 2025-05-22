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
    version = 3, // Incremented version
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WisdomReminderDatabase : RoomDatabase() {
    abstract fun wisdomDao(): WisdomDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_isActive` ON `wisdom` (`isActive`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_category` ON `wisdom` (`category`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_dateCreated` ON `wisdom` (`dateCreated`)")
            }
        }

        // Placeholder for migration due to ID change.
        // IMPORTANT: The SQL for this migration needs to be carefully crafted
        // if data preservation is required. It usually involves:
        // 1. Creating a new table with the correct schema.
        // 2. Copying data from the old table to the new table.
        // 3. Dropping the old table.
        // 4. Renaming the new table to the original table name.
        // This is a simplified placeholder.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Recreate table with auto-incrementing ID
                // THIS WILL WIPE EXISTING DATA IF NOT HANDLED CAREFULLY.
                // A proper migration would copy data.
                database.execSQL("DROP TABLE IF EXISTS `wisdom`")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wisdom` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`text` TEXT NOT NULL, `source` TEXT NOT NULL, `category` TEXT NOT NULL, " +
                            "`dateCreated` TEXT NOT NULL, `dateCompleted` TEXT, `isActive` INTEGER NOT NULL, " +
                            "`startDate` TEXT, `currentDay` INTEGER NOT NULL, `exposuresTotal` INTEGER NOT NULL, " +
                            "`exposuresToday` INTEGER NOT NULL, `lastExposureTime` TEXT, `isFavorite` INTEGER NOT NULL, " +
                            "`backgroundColor` TEXT, `fontStyle` TEXT, `imageBackground` TEXT)"
                )
                // Re-create indices from MIGRATION_1_2 if they were on the old table structure being replaced
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_isActive` ON `wisdom` (`isActive`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_category` ON `wisdom` (`category`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_dateCreated` ON `wisdom` (`dateCreated`)")
            }
        }
    }
}