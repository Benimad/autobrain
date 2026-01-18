package com.example.autobrain.di

import android.content.Context
import com.example.autobrain.BuildConfig
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.ai.GeminiCarnetRepository
import com.example.autobrain.data.remote.GeminiCarImageService
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
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
    
    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-2.0-flash-lite-001",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 1.0f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 8192
            }
        )
    }
    
    @Provides
    @Singleton
    fun provideGeminiCarImageService(
        generativeModel: GenerativeModel
    ): GeminiCarImageService {
        return GeminiCarImageService(generativeModel)
    }
}