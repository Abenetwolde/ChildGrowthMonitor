package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {

    @Query("SELECT * FROM follow_up_visits ORDER BY scheduledDate ASC")
    fun getAllFollowUps(): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_up_visits WHERE childId = :childId ORDER BY scheduledDate ASC")
    fun getFollowUpsForChild(childId: String): Flow<List<FollowUpEntity>>

    @Query("SELECT * FROM follow_up_visits WHERE status = 'PENDING' ORDER BY scheduledDate ASC")
    fun getPendingFollowUps(): Flow<List<FollowUpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUpEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUps(followUps: List<FollowUpEntity>)

    @Update
    suspend fun updateFollowUp(followUp: FollowUpEntity)

    @Query("UPDATE follow_up_visits SET status = 'COMPLETED', completedDate = :completedDate WHERE id = :id")
    suspend fun markCompleted(id: String, completedDate: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM follow_up_visits WHERE status = 'PENDING'")
    fun getPendingFollowUpsCount(): Flow<Int>
}
