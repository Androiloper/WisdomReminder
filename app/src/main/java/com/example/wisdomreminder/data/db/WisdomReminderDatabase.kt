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
    version = 4, // Incremented version
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wisdom_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`text` TEXT NOT NULL, `source` TEXT NOT NULL, `category` TEXT NOT NULL, " +
                            "`dateCreated` TEXT NOT NULL, `dateCompleted` TEXT, `isActive` INTEGER NOT NULL, " +
                            "`startDate` TEXT, `currentDay` INTEGER NOT NULL, `exposuresTotal` INTEGER NOT NULL, " +
                            "`exposuresToday` INTEGER NOT NULL, `lastExposureTime` TEXT, `isFavorite` INTEGER NOT NULL, " +
                            "`backgroundColor` TEXT, `fontStyle` TEXT, `imageBackground` TEXT)"
                )
                database.execSQL(
                    "INSERT INTO `wisdom_new` (`text`, `source`, `category`, `dateCreated`, `dateCompleted`, `isActive`, `startDate`, `currentDay`, `exposuresTotal`, `exposuresToday`, `lastExposureTime`, `isFavorite`, `backgroundColor`, `fontStyle`, `imageBackground`) " +
                            "SELECT `text`, `source`, `category`, `dateCreated`, `dateCompleted`, `isActive`, `startDate`, `currentDay`, `exposuresTotal`, `exposuresToday`, `lastExposureTime`, `isFavorite`, `backgroundColor`, `fontStyle`, `imageBackground` FROM `wisdom`"
                )
                database.execSQL("DROP TABLE `wisdom`")
                database.execSQL("ALTER TABLE `wisdom_new` RENAME TO `wisdom`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_isActive` ON `wisdom` (`isActive`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_category` ON `wisdom` (`category`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_dateCreated` ON `wisdom` (`dateCreated`)")
            }
        }

        // Migration from version 3 to 4: Add 'orderIndex' column and 'isFavorite' index
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new 'orderIndex' column
                database.execSQL("ALTER TABLE `wisdom` ADD COLUMN `orderIndex` INTEGER NOT NULL DEFAULT 0")
                // Add an index for 'isFavorite' as it might not have been created if MIGRATION_2_3 was a fallback
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_isFavorite` ON `wisdom` (`isFavorite`)")
                // Add an index for the new 'orderIndex' column
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_orderIndex` ON `wisdom` (`orderIndex`)")
            }
        }
    }
}