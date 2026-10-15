package com.example.clinical

import com.example.model.ClinicalAssessment
import com.example.model.EdemaGrade
import com.example.model.FieldValidation
import com.example.model.MalnutritionCategory

object ClinicalAssessmentEngine {

    const val MUAC_SAM_THRESHOLD_CM = 11.5
    const val MUAC_MAM_THRESHOLD_CM = 12.5
    const val MUAC_MIN_PLAUSIBLE_CM = 6.0
    const val MUAC_MAX_PLAUSIBLE_CM = 25.0

    /**
     * Evaluates acute malnutrition status per Ethiopian MOH and WHO Child Growth Standards.
     */
    fun evaluateScreening(
        muacCm: Double,
        edemaGrade: EdemaGrade,
        hasDangerSigns: Boolean = false,
        ageMonths: Int = 24
    ): ClinicalAssessment {
        val hasEdema = edemaGrade != EdemaGrade.NONE

        val warning = when {
            muacCm < MUAC_MIN_PLAUSIBLE_CM || muacCm > MUAC_MAX_PLAUSIBLE_CM ->
                "MUAC of ${muacCm}cm is outside the typical plausible physiological range (6.0 - 25.0 cm). Please verify measurement."
            ageMonths < 6 ->
                "Child is under 6 months. Standard community MUAC screening applies to 6–59 months; clinical referral for infant feeding assessment recommended."
            ageMonths > 59 ->
                "Child is over 59 months. MUAC thresholds are standardized for children aged 6 to 59 months."
            else -> null
        }

        // Severe Acute Malnutrition (SAM): Bilateral edema OR MUAC < 11.5 cm OR Critical danger signs
        if (hasEdema || muacCm < MUAC_SAM_THRESHOLD_CM || hasDangerSigns) {
            val reason = when {
                hasDangerSigns -> "Presence of critical clinical danger signs"
                hasEdema && muacCm < MUAC_SAM_THRESHOLD_CM -> "Bilateral pitting edema (${edemaGrade.description}) and MUAC ${muacCm}cm (< 11.5cm)"
                hasEdema -> "Presence of bilateral pitting edema (${edemaGrade.description})"
                else -> "MUAC ${muacCm}cm is below the 11.5cm SAM threshold"
            }
            return ClinicalAssessment(
                category = MalnutritionCategory.SAM,
                muacCm = muacCm,
                edemaGrade = edemaGrade,
                actionPlan = "URGENT OTP REFERRAL: Immediately refer child to Health Center OTP / Stabilization Center. Administer initial dose of Vitamin A/Amoxicillin per Kebele protocol if authorized.",
                primaryReason = reason,
                recommendedFollowUpDays = 7,
                requiresUrgentReferral = true,
                warningMessage = warning
            )
        }

        // Moderate Acute Malnutrition (MAM): 11.5 cm <= MUAC < 12.5 cm without edema
        if (muacCm < MUAC_MAM_THRESHOLD_CM) {
            return ClinicalAssessment(
                category = MalnutritionCategory.MAM,
                muacCm = muacCm,
                edemaGrade = edemaGrade,
                actionPlan = "ENROLL IN TSFP: Enroll child in Targeted Supplementary Feeding Program. Provide RUSF / CSB+ rations. Deliver maternal Infant and Young Child Feeding (IYCF) counseling.",
                primaryReason = "MUAC ${muacCm}cm is between 11.5cm and 12.4cm without edema",
                recommendedFollowUpDays = 14,
                requiresUrgentReferral = false,
                warningMessage = warning
            )
        }

        // Well Nourished (Normal): MUAC >= 12.5 cm without edema
        return ClinicalAssessment(
            category = MalnutritionCategory.NORMAL,
            muacCm = muacCm,
            edemaGrade = edemaGrade,
            actionPlan = "ROUTINE GROWTH MONITORING: Child is well-nourished. Encourage exclusive breastfeeding up to 6 months, diversified complementary feeding, and schedule routine monthly screening.",
            primaryReason = "MUAC ${muacCm}cm is at or above 12.5cm without edema",
            recommendedFollowUpDays = 30,
            requiresUrgentReferral = false,
            warningMessage = warning
        )
    }

    /**
     * Validates entered MUAC string for numerical correctness and physiological sanity.
     */
    fun validateMuacInput(rawInput: String): FieldValidation {
        if (rawInput.isBlank()) {
            return FieldValidation(isValid = false, errorMessage = "MUAC measurement is required.")
        }
        val value = rawInput.toDoubleOrNull()
            ?: return FieldValidation(isValid = false, errorMessage = "Enter a valid decimal number.")

        if (value <= 0.0) {
            return FieldValidation(isValid = false, errorMessage = "MUAC must be greater than 0.")
        }

        val warning = if (value < MUAC_MIN_PLAUSIBLE_CM || value > MUAC_MAX_PLAUSIBLE_CM) {
            "Extreme value (${value}cm). Normal range is 6.0cm to 25.0cm. Please recheck tape placement."
        } else null

        return FieldValidation(isValid = true, warningMessage = warning)
    }
}
