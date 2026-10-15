package com.example

import com.example.clinical.ClinicalAssessmentEngine
import com.example.model.EdemaGrade
import com.example.model.MalnutritionCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalAssessmentEngineTest {

    @Test
    fun testSamClassificationByMuacUnderThreshold() {
        val result = ClinicalAssessmentEngine.evaluateScreening(
            muacCm = 11.2,
            edemaGrade = EdemaGrade.NONE,
            hasDangerSigns = false
        )
        assertEquals(MalnutritionCategory.SAM, result.category)
        assertTrue(result.requiresUrgentReferral)
        assertEquals(7, result.recommendedFollowUpDays)
    }

    @Test
    fun testSamClassificationByBilateralEdemaRegardlessOfMuac() {
        val result = ClinicalAssessmentEngine.evaluateScreening(
            muacCm = 13.8, // Even with normal arm circumference
            edemaGrade = EdemaGrade.PLUS_2,
            hasDangerSigns = false
        )
        assertEquals(MalnutritionCategory.SAM, result.category)
        assertTrue(result.requiresUrgentReferral)
    }

    @Test
    fun testMamClassificationBetweenThresholds() {
        val result = ClinicalAssessmentEngine.evaluateScreening(
            muacCm = 12.0,
            edemaGrade = EdemaGrade.NONE,
            hasDangerSigns = false
        )
        assertEquals(MalnutritionCategory.MAM, result.category)
        assertFalse(result.requiresUrgentReferral)
        assertEquals(14, result.recommendedFollowUpDays)
    }

    @Test
    fun testNormalClassificationAtOrAboveThreshold() {
        val result = ClinicalAssessmentEngine.evaluateScreening(
            muacCm = 12.5,
            edemaGrade = EdemaGrade.NONE,
            hasDangerSigns = false
        )
        assertEquals(MalnutritionCategory.NORMAL, result.category)
        assertFalse(result.requiresUrgentReferral)
        assertEquals(30, result.recommendedFollowUpDays)
    }

    @Test
    fun testPlausibilityWarningOnExtremeValues() {
        val lowValidation = ClinicalAssessmentEngine.validateMuacInput("4.5")
        assertTrue(lowValidation.isValid)
        assertNotNull(lowValidation.warningMessage)

        val invalidAlpha = ClinicalAssessmentEngine.validateMuacInput("abc")
        assertFalse(invalidAlpha.isValid)
    }
}
