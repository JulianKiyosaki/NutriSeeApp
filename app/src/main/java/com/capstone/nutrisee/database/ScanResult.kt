package com.capstone.nutrisee.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val nutritionInfo: String,
    val scanDate: Long
)
