package com.example.model

enum class MalnutritionCategory(val code: String, val labelEn: String) {
    SAM("SAM", "Severe Acute Malnutrition"),
    MAM("MAM", "Moderate Acute Malnutrition"),
    NORMAL("NORMAL", "Well Nourished")
}

enum class EdemaGrade(val code: String, val description: String) {
    NONE("NONE", "None (Absent)"),
    PLUS_1("PLUS_1", "Grade + (Mild - Both Feet)"),
    PLUS_2("PLUS_2", "Grade ++ (Moderate - Feet & Lower Legs)"),
    PLUS_3("PLUS_3", "Grade +++ (Severe - Generalized / Face)")
}

data class ClinicalAssessment(
    val category: MalnutritionCategory,
    val muacCm: Double,
    val edemaGrade: EdemaGrade,
    val actionPlan: String,
    val primaryReason: String,
    val recommendedFollowUpDays: Int,
    val requiresUrgentReferral: Boolean,
    val warningMessage: String? = null
)

data class ReferralSlip(
    val referralId: String,
    val childId: String,
    val childFullName: String,
    val ageMonths: Int,
    val caregiverName: String,
    val caregiverPhone: String,
    val kebele: String,
    val woreda: String,
    val muacCm: Double,
    val edemaGrade: EdemaGrade,
    val category: MalnutritionCategory,
    val dangerSigns: List<String>,
    val referralReason: String,
    val referringFacility: String,
    val targetFacility: String,
    val issuedDateEpochMs: Long
)

data class FieldValidation(
    val isValid: Boolean,
    val warningMessage: String? = null,
    val errorMessage: String? = null
)
