package com.example.wisdomreminder.di

import android.content.Context
import androidx.room.Room
import com.example.wisdomreminder.data.db.WisdomReminderDatabase
import com.example.wisdomreminder.data.db.dao.WisdomDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WisdomReminderDatabase {
        return Room.databaseBuilder(
            context,
            WisdomReminderDatabase::class.java,
            "wisdom-reminder-database"
        )
            // Add all migrations
            .addMigrations(
                WisdomReminderDatabase.MIGRATION_1_2,
                WisdomReminderDatabase.MIGRATION_2_3,
                WisdomReminderDatabase.MIGRATION_3_4 // Added new migration
            )
            .fallbackToDestructiveMigration() // Keep for development, review for production
            .build()
    }

    @Provides
    @Singleton
    fun provideWisdomDao(database: WisdomReminderDatabase): WisdomDao {
        return database.wisdomDao()
    }
}