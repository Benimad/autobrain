package com.example.autobrain.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.local.AutoBrainDatabase
import com.example.autobrain.data.local.MIGRATION_4_5
import com.example.autobrain.data.local.MIGRATION_5_6
import com.example.autobrain.data.local.MIGRATION_6_7
import com.example.autobrain.data.local.MIGRATION_7_8
import com.example.autobrain.data.local.dao.*
import com.example.autobrain.data.remote.BackgroundRemovalService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideAutoBrainDatabase(
        @ApplicationContext context: Context
    ): AutoBrainDatabase {
        return Room.databaseBuilder(
            context,
            AutoBrainDatabase::class.java,
            AutoBrainDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .build()
    }

    @Provides
    @Singleton
    fun provideMaintenanceRecordDao(database: AutoBrainDatabase): MaintenanceRecordDao {
        return database.maintenanceRecordDao()
    }

    @Provides
    @Singleton
    fun provideCarLogDao(database: AutoBrainDatabase): CarLogDao {
        return database.carLogDao()
    }

    @Provides
    @Singleton
    fun provideAIScoreDao(database: AutoBrainDatabase): AIScoreDao {
        return database.aiScoreDao()
    }

    @Provides
    @Singleton
    fun provideReminderDao(database: AutoBrainDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    @Singleton
    fun provideAudioDiagnosticDao(database: AutoBrainDatabase): AudioDiagnosticDao {
        return database.audioDiagnosticDao()
    }

    @Provides
    @Singleton
    fun provideVideoDiagnosticDao(database: AutoBrainDatabase): VideoDiagnosticDao {
        return database.videoDiagnosticDao()
    }

    @Provides
    @Singleton
    fun provideCarImageDao(database: AutoBrainDatabase): CarImageDao {
        return database.carImageDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBackgroundRemovalService(
        okHttpClient: OkHttpClient,
        firebaseStorage: FirebaseStorage
    ): BackgroundRemovalService {
        return BackgroundRemovalService(okHttpClient, firebaseStorage)
    }
    
    // Note: GeminiAiRepository, GeminiCarnetRepository, and GeminiCarImageService
    // are provided by GeminiModule.kt to keep AI dependencies organized
}
