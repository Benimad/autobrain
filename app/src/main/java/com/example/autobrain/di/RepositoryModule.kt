package com.example.autobrain.di

import com.example.autobrain.data.repository.*
import com.example.autobrain.domain.repository.*
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindCarLogRepository(
        impl: CarLogRepositoryImpl
    ): CarLogRepository

    @Binds
    @Singleton
    abstract fun bindAIScoreRepository(
        impl: AIScoreRepositoryImpl
    ): AIScoreRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository
}
