package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {

    @Query("SELECT * FROM children WHERE isDraft = 0 ORDER BY createdAt DESC")
    fun getAllChildren(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id LIMIT 1")
    suspend fun getChildById(id: String): ChildEntity?

    @Query("SELECT * FROM children WHERE id = :id LIMIT 1")
    fun observeChildById(id: String): Flow<ChildEntity?>

    @Query("""
        SELECT * FROM children 
        WHERE isDraft = 0 AND (
            firstName LIKE '%' || :query || '%' OR 
            lastName LIKE '%' || :query || '%' OR 
            localId LIKE '%' || :query || '%' OR 
            caregiverName LIKE '%' || :query || '%'
        )
        ORDER BY createdAt DESC
    """)
    fun searchChildren(query: String): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE isDraft = 1 ORDER BY updatedAt DESC")
    fun getDraftChildren(): Flow<List<ChildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<ChildEntity>)

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Query("UPDATE children SET latestCategory = :category, latestMuacCm = :muac, latestScreeningDate = :date, syncStatus = 'pending_sync', updatedAt = :date WHERE id = :childId")
    suspend fun updateChildScreeningSummary(childId: String, category: String, muac: Double, date: Long)

    @Query("SELECT COUNT(*) FROM children WHERE isDraft = 0")
    fun getTotalChildrenCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM children WHERE syncStatus = 'pending_sync'")
    fun getPendingSyncChildrenCount(): Flow<Int>

    @Query("UPDATE children SET syncStatus = 'synced' WHERE syncStatus = 'pending_sync'")
    suspend fun markAllChildrenSynced()
}
