package com.example.wisdomreminder.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase // <-- Add this import
import com.example.wisdomreminder.data.InitialData // <-- Import your initial data
import com.example.wisdomreminder.data.db.WisdomReminderDatabase
import com.example.wisdomreminder.data.db.dao.WisdomDao
import com.example.wisdomreminder.data.db.entities.WisdomEntity // <-- Import WisdomEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider // <-- Add this import
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        // Use Provider to avoid circular dependency for the callback
        wisdomDaoProvider: Provider<WisdomDao>
    ): WisdomReminderDatabase {
        return Room.databaseBuilder(
            context,
            WisdomReminderDatabase::class.java,
            "wisdom-reminder-database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Populate the database in a coroutine
                    // We use the Provider to get the DAO instance *after* the DB is created
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        val wisdomDao = wisdomDaoProvider.get()
                        InitialData.getPredefinedWisdom().forEach { wisdom ->
                            wisdomDao.insertWisdom(WisdomEntity.fromWisdom(wisdom))
                        }
                    }
                }
            })
            .addMigrations(
                WisdomReminderDatabase.MIGRATION_1_2,
                WisdomReminderDatabase.MIGRATION_2_3,
                WisdomReminderDatabase.MIGRATION_3_4
            )
            .fallbackToDestructiveMigration() // Keep for development
            .build()
    }

    @Provides
    @Singleton
    fun provideWisdomDao(database: WisdomReminderDatabase): WisdomDao {
        return database.wisdomDao()
    }
}