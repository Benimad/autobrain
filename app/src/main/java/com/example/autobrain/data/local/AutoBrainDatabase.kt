package com.example.autobrain.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.autobrain.data.local.converter.ListStringConverter
import com.example.autobrain.data.local.converter.MapStringConverter
import com.example.autobrain.data.local.dao.*
import com.example.autobrain.data.local.entity.*

@Database(
    entities = [
        MaintenanceRecordEntity::class,
        CarLogEntity::class,
        AIScoreEntity::class,
        ReminderEntity::class,
        AudioDiagnosticEntity::class,
        VideoDiagnosticEntity::class,
        CarImageEntity::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(ListStringConverter::class, MapStringConverter::class)
abstract class AutoBrainDatabase : RoomDatabase() {
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao
    abstract fun carLogDao(): CarLogDao
    abstract fun aiScoreDao(): AIScoreDao
    abstract fun reminderDao(): ReminderDao
    abstract fun audioDiagnosticDao(): AudioDiagnosticDao
    abstract fun videoDiagnosticDao(): VideoDiagnosticDao
    abstract fun carImageDao(): CarImageDao

    companion object {
        const val DATABASE_NAME = "autobrain_database"
    }
}
