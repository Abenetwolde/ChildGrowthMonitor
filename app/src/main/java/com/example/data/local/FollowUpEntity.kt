package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_up_visits",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["childId"]),
        Index(value = ["scheduledDate"]),
        Index(value = ["status"])
    ]
)
data class FollowUpEntity(
    @PrimaryKey
    val id: String, // UUID
    val childId: String,
    val childFullName: String,
    val screeningId: String? = null,
    val scheduledDate: Long,
    val completedDate: Long? = null,
    val status: String = "PENDING", // PENDING, COMPLETED, MISSED, RESCHEDULED
    val category: String = "MAM",
    val notes: String = "",
    val syncStatus: String = "pending_sync",
    val createdAt: Long = System.currentTimeMillis()
)
