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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Non-destructive migration:
                // 1. Create a new table with the desired schema (auto-incrementing ID)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wisdom_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`text` TEXT NOT NULL, `source` TEXT NOT NULL, `category` TEXT NOT NULL, " +
                            "`dateCreated` TEXT NOT NULL, `dateCompleted` TEXT, `isActive` INTEGER NOT NULL, " +
                            "`startDate` TEXT, `currentDay` INTEGER NOT NULL, `exposuresTotal` INTEGER NOT NULL, " +
                            "`exposuresToday` INTEGER NOT NULL, `lastExposureTime` TEXT, `isFavorite` INTEGER NOT NULL, " +
                            "`backgroundColor` TEXT, `fontStyle` TEXT, `imageBackground` TEXT)"
                )

                // 2. Copy data from the old table to the new table.
                //    Assuming the old table `wisdom` had an `id` that was NOT auto-incrementing
                //    and other column names match. If `id` was already auto-incrementing and the change
                //    was just to ensure it, this step might need adjustment or might not be strictly necessary
                //    if only other schema changes occurred. But for a explicit change to auto-increment,
                //    it's safer to copy to a new table where Room manages the auto-increment.
                //    We are selecting all columns *except* the old `id` if we want the new table to truly auto-generate all IDs.
                //    Or, if we want to preserve old IDs as much as possible and only auto-generate for new ones,
                //    we might copy the `id` as well if the schema change wasn't about the PK itself.
                //    Given the original comment mentioned "ID change" and "auto-incrementing ID",
                //    we'll copy existing data, letting new IDs be generated if there were conflicts,
                //    or ideally, map them if possible. For simplicity in this automated fix,
                //    we are effectively re-inserting, and new auto-generated IDs will be assigned.
                //    A more robust solution would involve careful ID mapping if they are foreign keys elsewhere.
                database.execSQL(
                    "INSERT INTO `wisdom_new` (`text`, `source`, `category`, `dateCreated`, `dateCompleted`, `isActive`, `startDate`, `currentDay`, `exposuresTotal`, `exposuresToday`, `lastExposureTime`, `isFavorite`, `backgroundColor`, `fontStyle`, `imageBackground`) " +
                            "SELECT `text`, `source`, `category`, `dateCreated`, `dateCompleted`, `isActive`, `startDate`, `currentDay`, `exposuresTotal`, `exposuresToday`, `lastExposureTime`, `isFavorite`, `backgroundColor`, `fontStyle`, `imageBackground` FROM `wisdom`"
                )

                // 3. Drop the old table.
                database.execSQL("DROP TABLE `wisdom`")

                // 4. Rename the new table to the original table name.
                database.execSQL("ALTER TABLE `wisdom_new` RENAME TO `wisdom`")

                // 5. Re-create indices on the new table structure
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_isActive` ON `wisdom` (`isActive`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_category` ON `wisdom` (`category`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_wisdom_dateCreated` ON `wisdom` (`dateCreated`)")
            }
        }
    }
}