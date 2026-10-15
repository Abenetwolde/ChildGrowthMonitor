package com.example.data

import com.example.data.local.ChildDao
import com.example.data.local.ChildEntity
import com.example.data.local.FollowUpDao
import com.example.data.local.FollowUpEntity
import com.example.data.local.ScreeningDao
import com.example.data.local.ScreeningEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.util.UUID

data class DashboardMetrics(
    val totalChildren: Int,
    val completedScreenings: Int,
    val pendingFollowUps: Int,
    val pendingSyncCount: Int
)

class NutriScanRepository(
    private val childDao: ChildDao,
    private val screeningDao: ScreeningDao,
    private val followUpDao: FollowUpDao
) {

    val allChildren: Flow<List<ChildEntity>> = childDao.getAllChildren()
    val allScreenings: Flow<List<ScreeningEntity>> = screeningDao.getAllScreenings()
    val recentScreenings: Flow<List<ScreeningEntity>> = screeningDao.getRecentScreenings(8)
    val pendingFollowUps: Flow<List<FollowUpEntity>> = followUpDao.getPendingFollowUps()
    val allFollowUps: Flow<List<FollowUpEntity>> = followUpDao.getAllFollowUps()

    fun searchChildren(query: String): Flow<List<ChildEntity>> {
        return if (query.isBlank()) {
            childDao.getAllChildren()
        } else {
            childDao.searchChildren(query.trim())
        }
    }

    fun observeChild(id: String): Flow<ChildEntity?> = childDao.observeChildById(id)

    fun getScreeningsForChild(childId: String): Flow<List<ScreeningEntity>> =
        screeningDao.getScreeningsForChild(childId)

    val metrics: Flow<DashboardMetrics> = combine(
        childDao.getTotalChildrenCount(),
        screeningDao.getTotalScreeningsCount(),
        followUpDao.getPendingFollowUpsCount(),
        childDao.getPendingSyncChildrenCount(),
        screeningDao.getPendingSyncScreeningsCount()
    ) { children, screenings, followUps, pendingChildren, pendingScreenings ->
        DashboardMetrics(
            totalChildren = children,
            completedScreenings = screenings,
            pendingFollowUps = followUps,
            pendingSyncCount = pendingChildren + pendingScreenings
        )
    }

    suspend fun registerChild(
        firstName: String,
        lastName: String,
        sex: String,
        dateOfBirth: String?,
        estimatedAgeMonths: Int,
        caregiverName: String,
        caregiverPhone: String,
        region: String,
        zone: String,
        woreda: String,
        kebele: String,
        gotCommunity: String,
        isDraft: Boolean = false
    ): ChildEntity = withContext(Dispatchers.IO) {
        val childId = UUID.randomUUID().toString()
        val randomSuffix = (1000..9999).random()
        val regionPrefix = if (region.contains("Oromia", ignoreCase = true)) "OR" else "AM"
        val localId = "ETH-$regionPrefix-24-$randomSuffix"

        val entity = ChildEntity(
            id = childId,
            localId = localId,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            sex = sex,
            dateOfBirth = dateOfBirth,
            estimatedAgeMonths = estimatedAgeMonths,
            caregiverName = caregiverName.trim(),
            caregiverPhone = caregiverPhone.trim(),
            region = region,
            zone = zone,
            woreda = woreda,
            kebele = kebele,
            gotCommunity = gotCommunity.trim(),
            isDraft = isDraft,
            syncStatus = "pending_sync"
        )
        childDao.insertChild(entity)
        entity
    }

    suspend fun saveScreening(
        childId: String,
        childFullName: String,
        clinicalCategory: String,
        hasEdema: Boolean,
        edemaGrade: String,
        muacCm: Double,
        weightKg: Double?,
        heightCm: Double?,
        dangerSignsPresent: Boolean,
        dangerSignsNotes: String,
        actionTaken: String,
        primaryReason: String,
        nextFollowUpDate: Long?,
        imageVerdict: String,
        manualOverride: Boolean
    ): ScreeningEntity = withContext(Dispatchers.IO) {
        val screeningId = UUID.randomUUID().toString()
        val randomNum = (1000..9999).random()
        val localSessionId = "SCR-24-$randomNum"
        val now = System.currentTimeMillis()

        val screening = ScreeningEntity(
            id = screeningId,
            localSessionId = localSessionId,
            childId = childId,
            screeningDate = now,
            clinicalCategory = clinicalCategory,
            hasBilateralEdema = hasEdema,
            edemaGrade = edemaGrade,
            muacCm = muacCm,
            weightKg = weightKg,
            heightCm = heightCm,
            dangerSignsPresent = dangerSignsPresent,
            dangerSignsNotes = dangerSignsNotes,
            actionTaken = actionTaken,
            primaryReason = primaryReason,
            nextFollowUpDate = nextFollowUpDate,
            imageQualityVerdict = imageVerdict,
            manualOverrideUsed = manualOverride,
            syncStatus = "pending_sync"
        )
        screeningDao.insertScreening(screening)
        childDao.updateChildScreeningSummary(childId, clinicalCategory, muacCm, now)

        if (nextFollowUpDate != null) {
            val followUp = FollowUpEntity(
                id = UUID.randomUUID().toString(),
                childId = childId,
                childFullName = childFullName,
                screeningId = screeningId,
                scheduledDate = nextFollowUpDate,
                category = clinicalCategory,
                notes = actionTaken
            )
            followUpDao.insertFollowUp(followUp)
        }

        screening
    }

    suspend fun completeFollowUp(id: String) = withContext(Dispatchers.IO) {
        followUpDao.markCompleted(id)
    }

    suspend fun syncAllRecords(): Boolean = withContext(Dispatchers.IO) {
        // Simulates secure HTTPS batch synchronization to server endpoint
        kotlinx.coroutines.delay(1200)
        childDao.markAllChildrenSynced()
        screeningDao.markAllScreeningsSynced()
        true
    }
}
