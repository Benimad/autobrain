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

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create new table with userId field
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS car_images_new (
                carKey TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL DEFAULT '',
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
        
        // Step 2: Copy data from old table to new table (userId will be empty for old records)
        database.execSQL(
            """
            INSERT INTO car_images_new (carKey, userId, make, model, year, imageUrl, isTransparent, source, cachedAt, lastAccessedAt)
            SELECT carKey, '', make, model, year, imageUrl, isTransparent, source, cachedAt, lastAccessedAt
            FROM car_images
            """.trimIndent()
        )
        
        // Step 3: Drop old table
        database.execSQL("DROP TABLE car_images")
        
        // Step 4: Rename new table to original name
        database.execSQL("ALTER TABLE car_images_new RENAME TO car_images")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add cacheVersion column with default value 1
        // Setting default to 1 ensures old cache will be invalidated (current version is 2)
        database.execSQL(
            """
            ALTER TABLE car_images ADD COLUMN cacheVersion INTEGER NOT NULL DEFAULT 1
            """.trimIndent()
        )
    }
}
