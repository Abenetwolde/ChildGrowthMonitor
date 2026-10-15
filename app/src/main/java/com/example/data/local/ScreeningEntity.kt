package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screening_sessions",
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
        Index(value = ["localSessionId"], unique = true),
        Index(value = ["screeningDate"]),
        Index(value = ["syncStatus"])
    ]
)
data class ScreeningEntity(
    @PrimaryKey
    val id: String, // UUID
    val localSessionId: String,
    val childId: String,
    val screeningDate: Long = System.currentTimeMillis(),
    val screeningLocation: String = "Kebele Health Post",
    val caregiverConsent: Boolean = true,
    val clinicalCategory: String, // SAM, MAM, NORMAL
    val hasBilateralEdema: Boolean = false,
    val edemaGrade: String = "NONE", // NONE, PLUS_1, PLUS_2, PLUS_3
    val muacCm: Double,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val dangerSignsPresent: Boolean = false,
    val dangerSignsNotes: String = "",
    val actionTaken: String,
    val primaryReason: String,
    val nextFollowUpDate: Long? = null,
    val imageQualityVerdict: String = "SKIPPED", // ACCEPTABLE, POOR_LIGHTING, BLURRY, SKIPPED
    val manualOverrideUsed: Boolean = true,
    val syncStatus: String = "pending_sync", // synced, pending_sync, sync_error
    val createdAt: Long = System.currentTimeMillis()
)
