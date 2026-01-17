package com.example.autobrain.di

import android.content.Context
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.ai.GeminiCarnetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for Gemini AI Integration
 * Provides all Gemini-powered repositories for AutoBrain
 */
@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    fun provideGeminiAiRepository(
        @ApplicationContext context: Context
    ): GeminiAiRepository {
        return GeminiAiRepository(context)
    }

    @Provides
    @Singleton
    fun provideGeminiCarnetRepository(
        @ApplicationContext context: Context
    ): GeminiCarnetRepository {
        return GeminiCarnetRepository(context)
    }
}
