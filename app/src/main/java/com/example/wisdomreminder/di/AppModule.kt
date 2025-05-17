package com.example.wisdomreminder.di

import android.content.Context
import android.content.SharedPreferences
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.util.NotificationManager
import com.example.wisdomreminder.util.WisdomAlarmManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Add notification and alarm managers
    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        wisdomRepository: WisdomRepository
    ): NotificationManager {
        return NotificationManager(context, wisdomRepository)
    }

    @Provides
    @Singleton
    fun provideWisdomAlarmManager(
        @ApplicationContext context: Context,
        wisdomRepository: WisdomRepository
    ): WisdomAlarmManager {
        return WisdomAlarmManager(context, wisdomRepository)
    }
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("wisdom_preferences", Context.MODE_PRIVATE)
    }
}