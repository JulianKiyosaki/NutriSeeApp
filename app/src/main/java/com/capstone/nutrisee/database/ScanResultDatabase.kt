package com.capstone.nutrisee.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanResult::class], version = 1, exportSchema = false)
abstract class ScanResultDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao

    companion object {
        @Volatile
        private var INSTANCE: ScanResultDatabase? = null

        fun getDatabase(context: Context): ScanResultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScanResultDatabase::class.java,
                    "scan_results_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}