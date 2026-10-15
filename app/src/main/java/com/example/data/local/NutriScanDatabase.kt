package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [ChildEntity::class, ScreeningEntity::class, FollowUpEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NutriScanDatabase : RoomDatabase() {

    abstract fun childDao(): ChildDao
    abstract fun screeningDao(): ScreeningDao
    abstract fun followUpDao(): FollowUpDao

    companion object {
        @Volatile
        private var INSTANCE: NutriScanDatabase? = null

        fun getInstance(context: Context): NutriScanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriScanDatabase::class.java,
                    "nutriscan_health_post.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: NutriScanDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val child1Id = UUID.randomUUID().toString()
            val child2Id = UUID.randomUUID().toString()
            val child3Id = UUID.randomUUID().toString()
            val child4Id = UUID.randomUUID().toString()

            val seedChildren = listOf(
                ChildEntity(
                    id = child1Id,
                    localId = "ETH-OR-24-00101",
                    firstName = "Bekele",
                    lastName = "Tolessa",
                    sex = "MALE",
                    dateOfBirth = "2024-04-10",
                    estimatedAgeMonths = 14,
                    caregiverName = "Chaltu Dibaba",
                    caregiverPhone = "0911234567",
                    region = "Oromia",
                    zone = "East Shewa",
                    woreda = "Ada'a",
                    kebele = "Kebele 03 (Dukam)",
                    gotCommunity = "Got 02",
                    isDraft = false,
                    syncStatus = "synced",
                    latestCategory = "MAM",
                    latestMuacCm = 11.8,
                    latestScreeningDate = now - (3 * dayMs),
                    createdAt = now - (3 * dayMs)
                ),
                ChildEntity(
                    id = child2Id,
                    localId = "ETH-AM-24-00102",
                    firstName = "Selam",
                    lastName = "Mulugeta",
                    sex = "FEMALE",
                    dateOfBirth = "2024-01-15",
                    estimatedAgeMonths = 17,
                    caregiverName = "Tigist Alemu",
                    caregiverPhone = "0922345678",
                    region = "Amhara",
                    zone = "North Shewa",
                    woreda = "Basona Werana",
                    kebele = "Kebele 01",
                    gotCommunity = "Got 01",
                    isDraft = false,
                    syncStatus = "pending_sync",
                    latestCategory = "SAM",
                    latestMuacCm = 11.1,
                    latestScreeningDate = now - (1 * dayMs),
                    createdAt = now - (10 * dayMs)
                ),
                ChildEntity(
                    id = child3Id,
                    localId = "ETH-OR-24-00103",
                    firstName = "Dawit",
                    lastName = "Kenenisa",
                    sex = "MALE",
                    dateOfBirth = "2023-09-02",
                    estimatedAgeMonths = 21,
                    caregiverName = "Hawani Gudeta",
                    caregiverPhone = "0933456789",
                    region = "Oromia",
                    zone = "East Shewa",
                    woreda = "Ada'a",
                    kebele = "Kebele 03 (Dukam)",
                    gotCommunity = "Got 04",
                    isDraft = false,
                    syncStatus = "synced",
                    latestCategory = "NORMAL",
                    latestMuacCm = 13.5,
                    latestScreeningDate = now - (7 * dayMs),
                    createdAt = now - (30 * dayMs)
                ),
                ChildEntity(
                    id = child4Id,
                    localId = "ETH-OR-24-00104",
                    firstName = "Ayantu",
                    lastName = "Gemechu",
                    sex = "FEMALE",
                    dateOfBirth = "2024-06-20",
                    estimatedAgeMonths = 12,
                    caregiverName = "Mestawet Tarekegn",
                    caregiverPhone = "0944567890",
                    region = "Oromia",
                    zone = "East Shewa",
                    woreda = "Ada'a",
                    kebele = "Kebele 03 (Dukam)",
                    gotCommunity = "Got 01",
                    isDraft = false,
                    syncStatus = "synced",
                    latestCategory = "UNSCREENED",
                    latestMuacCm = null,
                    latestScreeningDate = null,
                    createdAt = now - (2 * dayMs)
                )
            )

            db.childDao().insertChildren(seedChildren)

            val seedScreenings = listOf(
                ScreeningEntity(
                    id = UUID.randomUUID().toString(),
                    localSessionId = "SCR-24-00101-01",
                    childId = child1Id,
                    screeningDate = now - (3 * dayMs),
                    screeningLocation = "Dukam Health Post",
                    caregiverConsent = true,
                    clinicalCategory = "MAM",
                    hasBilateralEdema = false,
                    edemaGrade = "NONE",
                    muacCm = 11.8,
                    weightKg = 8.2,
                    heightCm = 74.0,
                    dangerSignsPresent = false,
                    actionTaken = "ENROLL IN TSFP: Supplementary rations distributed. Maternal counseling provided.",
                    primaryReason = "MUAC 11.8cm is between 11.5cm and 12.4cm without edema",
                    nextFollowUpDate = now + (11 * dayMs),
                    imageQualityVerdict = "ACCEPTABLE",
                    manualOverrideUsed = false,
                    syncStatus = "synced"
                ),
                ScreeningEntity(
                    id = UUID.randomUUID().toString(),
                    localSessionId = "SCR-24-00102-01",
                    childId = child2Id,
                    screeningDate = now - (1 * dayMs),
                    screeningLocation = "Kebele 01 Community Outreach",
                    caregiverConsent = true,
                    clinicalCategory = "SAM",
                    hasBilateralEdema = false,
                    edemaGrade = "NONE",
                    muacCm = 11.1,
                    weightKg = 7.4,
                    heightCm = 71.5,
                    dangerSignsPresent = false,
                    actionTaken = "URGENT OTP REFERRAL: Referred to Primary Hospital OTP. First dose amoxicillin given.",
                    primaryReason = "MUAC 11.1cm is below the 11.5cm SAM threshold",
                    nextFollowUpDate = now + (6 * dayMs),
                    imageQualityVerdict = "ACCEPTABLE",
                    manualOverrideUsed = false,
                    syncStatus = "pending_sync"
                ),
                ScreeningEntity(
                    id = UUID.randomUUID().toString(),
                    localSessionId = "SCR-24-00103-01",
                    childId = child3Id,
                    screeningDate = now - (7 * dayMs),
                    screeningLocation = "Dukam Health Post",
                    caregiverConsent = true,
                    clinicalCategory = "NORMAL",
                    hasBilateralEdema = false,
                    edemaGrade = "NONE",
                    muacCm = 13.5,
                    weightKg = 10.1,
                    heightCm = 82.0,
                    dangerSignsPresent = false,
                    actionTaken = "ROUTINE GROWTH MONITORING: Well-nourished. Continue age-appropriate feeding.",
                    primaryReason = "MUAC 13.5cm is at or above 12.5cm without edema",
                    nextFollowUpDate = now + (23 * dayMs),
                    imageQualityVerdict = "SKIPPED",
                    manualOverrideUsed = true,
                    syncStatus = "synced"
                )
            )

            db.screeningDao().insertScreenings(seedScreenings)

            val seedFollowUps = listOf(
                FollowUpEntity(
                    id = UUID.randomUUID().toString(),
                    childId = child2Id,
                    childFullName = "Selam Mulugeta",
                    scheduledDate = now + (6 * dayMs),
                    status = "PENDING",
                    category = "SAM",
                    notes = "Check appetite test, OTP weight gain, and edema check."
                ),
                FollowUpEntity(
                    id = UUID.randomUUID().toString(),
                    childId = child1Id,
                    childFullName = "Bekele Tolessa",
                    scheduledDate = now + (11 * dayMs),
                    status = "PENDING",
                    category = "MAM",
                    notes = "TSFP 2-week ration check and MUAC verification."
                )
            )

            db.followUpDao().insertFollowUps(seedFollowUps)
        }
    }
}
