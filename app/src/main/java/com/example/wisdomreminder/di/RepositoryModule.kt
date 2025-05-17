package com.example.wisdomreminder.di

import com.example.wisdomreminder.data.repository.IWisdomRepository
import com.example.wisdomreminder.data.repository.WisdomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWisdomRepository(
        wisdomRepository: WisdomRepository
    ): IWisdomRepository
}