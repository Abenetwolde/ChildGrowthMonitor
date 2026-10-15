package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "children",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["kebele"]),
        Index(value = ["syncStatus"])
    ]
)
data class ChildEntity(
    @PrimaryKey
    val id: String, // UUID
    val localId: String, // e.g. ETH-OR-24-00123
    val firstName: String,
    val lastName: String,
    val sex: String, // MALE / FEMALE
    val dateOfBirth: String?, // YYYY-MM-DD or null
    val estimatedAgeMonths: Int,
    val caregiverName: String,
    val caregiverPhone: String,
    val region: String,
    val zone: String,
    val woreda: String,
    val kebele: String,
    val gotCommunity: String = "",
    val isDraft: Boolean = false,
    val syncStatus: String = "pending_sync", // synced, pending_sync, sync_error
    val latestCategory: String = "UNSCREENED", // SAM, MAM, NORMAL, UNSCREENED
    val latestMuacCm: Double? = null,
    val latestScreeningDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
