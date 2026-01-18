package com.example.autobrain.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database Migrations for AutoBrain
 * 
 * Handles schema changes between versions to preserve user data
 * during app updates instead of using fallbackToDestructiveMigration()
 */

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE audio_diagnostics ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE audio_diagnostics ADD COLUMN syncAttempts INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE audio_diagnostics ADD COLUMN lastSyncAttempt INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE video_diagnostics ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE video_diagnostics ADD COLUMN syncAttempts INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE video_diagnostics ADD COLUMN lastSyncAttempt INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop the unused diagnostics table
        database.execSQL("DROP TABLE IF EXISTS diagnostics")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS car_images (
                carKey TEXT PRIMARY KEY NOT NULL,
                make TEXT NOT NULL,
                model TEXT NOT NULL,
                year INTEGER NOT NULL,
                imageUrl TEXT NOT NULL,
                isTransparent INTEGER NOT NULL DEFAULT 0,
                source TEXT NOT NULL,
                cachedAt INTEGER NOT NULL,
                lastAccessedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE audio_diagnostics ADD COLUMN syncError TEXT
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE audio_diagnostics ADD COLUMN localModifiedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE video_diagnostics ADD COLUMN syncError TEXT
            """.trimIndent()
        )
        
        database.execSQL(
            """
            ALTER TABLE video_diagnostics ADD COLUMN localModifiedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
            """.trimIndent()
        )
    }
}
