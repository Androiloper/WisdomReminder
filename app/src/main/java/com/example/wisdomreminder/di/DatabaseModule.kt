package com.example.wisdomreminder.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // You could populate initial data here if needed
                }
            })
            // Add migrations when needed
             //.addMigrations(WisdomReminderDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton // Added singleton scope for the DAO
    fun provideWisdomDao(database: WisdomReminderDatabase): WisdomDao {
        return database.wisdomDao()
    }
}