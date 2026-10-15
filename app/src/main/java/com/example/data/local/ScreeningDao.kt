package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreeningDao {

    @Query("SELECT * FROM screening_sessions ORDER BY screeningDate DESC")
    fun getAllScreenings(): Flow<List<ScreeningEntity>>

    @Query("SELECT * FROM screening_sessions WHERE childId = :childId ORDER BY screeningDate DESC")
    fun getScreeningsForChild(childId: String): Flow<List<ScreeningEntity>>

    @Query("SELECT * FROM screening_sessions ORDER BY screeningDate DESC LIMIT :limit")
    fun getRecentScreenings(limit: Int = 10): Flow<List<ScreeningEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreening(screening: ScreeningEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenings(screenings: List<ScreeningEntity>)

    @Query("SELECT COUNT(*) FROM screening_sessions")
    fun getTotalScreeningsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM screening_sessions WHERE syncStatus = 'pending_sync'")
    fun getPendingSyncScreeningsCount(): Flow<Int>

    @Query("UPDATE screening_sessions SET syncStatus = 'synced' WHERE syncStatus = 'pending_sync'")
    suspend fun markAllScreeningsSynced()
}
