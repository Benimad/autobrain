package com.example.autobrain.di

import android.content.Context
import com.example.autobrain.BuildConfig
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.ai.GeminiCarnetRepository
import com.example.autobrain.data.remote.GeminiCarImageGenerator
import com.example.autobrain.data.remote.GeminiCarImageService
import com.example.autobrain.data.remote.GeminiImageGenerationService
import com.example.autobrain.data.repository.AudioDiagnosticRepository
import com.example.autobrain.data.repository.GeminiChatRepository
import com.example.autobrain.data.repository.VideoDiagnosticRepository
import com.example.autobrain.domain.repository.AIScoreRepository
import com.example.autobrain.domain.repository.CarLogRepository
import com.example.autobrain.domain.repository.UserRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
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
            modelName = "models/gemini-3-flash-preview",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 1.0f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 65536
            }
        )
    }
    
    @Provides
    @Singleton
    @javax.inject.Named("imageGeneration")
    fun provideImageGenerationModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 8192
            }
        )
    }
    
    @Provides
    @Singleton
    fun provideGeminiCarImageGenerator(
        @javax.inject.Named("imageGeneration") imageGenModel: GenerativeModel,
        okHttpClient: okhttp3.OkHttpClient,
        firebaseStorage: com.google.firebase.storage.FirebaseStorage
    ): GeminiCarImageGenerator {
        return GeminiCarImageGenerator(imageGenModel, okHttpClient, firebaseStorage)
    }
    
    @Provides
    @Singleton
    fun provideGeminiCarImageService(
        generativeModel: GenerativeModel,
        okHttpClient: okhttp3.OkHttpClient
    ): GeminiCarImageService {
        return GeminiCarImageService(generativeModel, okHttpClient)
    }
    
    @Provides
    @Singleton
    fun provideGeminiImageGenerationService(
        generativeModel: GenerativeModel
    ): GeminiImageGenerationService {
        return GeminiImageGenerationService(generativeModel)
    }
    
    @Provides
    @Singleton
    fun provideGeminiChatRepository(
        geminiModel: GenerativeModel,
        userRepository: UserRepository,
        carLogRepository: CarLogRepository,
        aiScoreRepository: AIScoreRepository,
        audioDiagnosticRepository: AudioDiagnosticRepository,
        videoDiagnosticRepository: VideoDiagnosticRepository,
        auth: FirebaseAuth
    ): GeminiChatRepository {
        return GeminiChatRepository(
            geminiModel = geminiModel,
            userRepository = userRepository,
            carLogRepository = carLogRepository,
            aiScoreRepository = aiScoreRepository,
            audioDiagnosticRepository = audioDiagnosticRepository,
            videoDiagnosticRepository = videoDiagnosticRepository,
            auth = auth
        )
    }
}