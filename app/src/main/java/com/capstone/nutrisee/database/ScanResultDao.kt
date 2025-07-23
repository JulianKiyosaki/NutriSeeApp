package com.capstone.nutrisee.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScanResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanResult)

    @Query("SELECT * FROM scan_results ORDER BY scanDate DESC")
    fun getAllScanResults(): LiveData<List<ScanResult>>

    @Query("DELETE FROM scan_results WHERE id = :scanId")
    suspend fun deleteScanResultById(scanId: Int)
}
