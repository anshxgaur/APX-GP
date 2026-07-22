package com.yourname.sra.data.repository

import com.yourname.sra.data.model.AreaRiskScore
import com.yourname.sra.data.model.Survey
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for MLBridgeRepository
 * 
 * Tests cover Requirements 15.1, 15.2, 15.3, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 17.1, 17.2, 17.3:
 * - Survey data export for ML processing
 * - Risk score import and validation
 * - Old score cleanup workflow
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MLBridgeRepositoryTest {

    private lateinit var mlBridgeRepository: MLBridgeRepository
    private lateinit var mockSurveyRepository: SurveyRepository
    private lateinit var mockRiskScoreRepository: RiskScoreRepository

    @Before
    fun setup() {
        mockSurveyRepository = mockk(relaxed = true)
        mockRiskScoreRepository = mockk(relaxed = true)
        mlBridgeRepository = MLBridgeRepository(mockSurveyRepository, mockRiskScoreRepository)
    }

    // Survey Data Export Tests (Requirements 15.1, 15.2, 15.3)

    @Test
    fun `fetchSurveyDataForML calls surveyRepository getAllSurveys`() = runTest {
        // Given
        val mockSurveys = listOf(
            createTestSurvey(id = "survey1", category = "infrastructure"),
            createTestSurvey(id = "survey2", category = "medical")
        )
        coEvery { mockSurveyRepository.getAllSurveys() } returns Result.success(mockSurveys)

        // When
        val result = mlBridgeRepository.fetchSurveyDataForML()

        // Then
        coVerify(exactly = 1) { mockSurveyRepository.getAllSurveys() }
        assertTrue(result.isSuccess, "fetchSurveyDataForML should succeed")
        assertEquals(2, result.getOrNull()?.size, "Should return 2 surveys")
        assertEquals("survey1", result.getOrNull()?.get(0)?.id, "First survey ID should match")
    }

    @Test
    fun `fetchSurveyDataForML returns failure when survey fetch fails`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { mockSurveyRepository.getAllSurveys() } returns Result.failure(exception)

        // When
        val result = mlBridgeRepository.fetchSurveyDataForML()

        // Then
        assertTrue(result.isFailure, "fetchSurveyDataForML should fail")
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchSurveyDataForML returns empty list when no surveys exist`() = runTest {
        // Given
        coEvery { mockSurveyRepository.getAllSurveys() } returns Result.success(emptyList())

        // When
        val result = mlBridgeRepository.fetchSurveyDataForML()

        // Then
        assertTrue(result.isSuccess, "fetchSurveyDataForML should succeed")
        assertEquals(0, result.getOrNull()?.size, "Should return empty list")
    }

    // Risk Score Import Tests (Requirements 16.1, 16.2)

    @Test
    fun `saveCalculatedRiskScores calls riskScoreRepository saveRiskScores`() = runTest {
        // Given
        val mockScores = listOf(
            createTestRiskScore(areaName = "Area A", riskScore = 5.0f),
            createTestRiskScore(areaName = "Area B", riskScore = 7.5f)
        )
        coEvery { mockRiskScoreRepository.saveRiskScores(mockScores) } returns Result.success(Unit)

        // When
        val result = mlBridgeRepository.saveCalculatedRiskScores(mockScores)

        // Then
        coVerify(exactly = 1) { mockRiskScoreRepository.saveRiskScores(mockScores) }
        assertTrue(result.isSuccess, "saveCalculatedRiskScores should succeed")
    }

    @Test
    fun `saveCalculatedRiskScores returns failure when save fails`() = runTest {
        // Given
        val mockScores = listOf(createTestRiskScore())
        val exception = Exception("Database error")
        coEvery { mockRiskScoreRepository.saveRiskScores(mockScores) } returns Result.failure(exception)

        // When
        val result = mlBridgeRepository.saveCalculatedRiskScores(mockScores)

        // Then
        assertTrue(result.isFailure, "saveCalculatedRiskScores should fail")
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveCalculatedRiskScores handles empty list`() = runTest {
        // Given
        val emptyScores = emptyList<AreaRiskScore>()
        coEvery { mockRiskScoreRepository.saveRiskScores(emptyScores) } returns Result.success(Unit)

        // When
        val result = mlBridgeRepository.saveCalculatedRiskScores(emptyScores)

        // Then
        assertTrue(result.isSuccess, "saveCalculatedRiskScores should succeed with empty list")
    }

    // Old Score Cleanup Tests (Requirements 17.1, 17.2, 17.3)

    @Test
    fun `clearPreviousRiskScores calls riskScoreRepository clearOldScores`() = runTest {
        // Given
        coEvery { mockRiskScoreRepository.clearOldScores() } returns Result.success(Unit)

        // When
        val result = mlBridgeRepository.clearPreviousRiskScores()

        // Then
        coVerify(exactly = 1) { mockRiskScoreRepository.clearOldScores() }
        assertTrue(result.isSuccess, "clearPreviousRiskScores should succeed")
    }

    @Test
    fun `clearPreviousRiskScores returns failure when clear fails`() = runTest {
        // Given
        val exception = Exception("RPC error")
        coEvery { mockRiskScoreRepository.clearOldScores() } returns Result.failure(exception)

        // When
        val result = mlBridgeRepository.clearPreviousRiskScores()

        // Then
        assertTrue(result.isFailure, "clearPreviousRiskScores should fail")
        assertEquals("RPC error", result.exceptionOrNull()?.message)
    }

    // ML Workflow Integration Tests

    @Test
    fun `typical ML workflow - fetch, clear, save succeeds`() = runTest {
        // Given - Mock successful survey fetch
        val mockSurveys = listOf(
            createTestSurvey(id = "survey1", severity = 4),
            createTestSurvey(id = "survey2", severity = 5)
        )
        coEvery { mockSurveyRepository.getAllSurveys() } returns Result.success(mockSurveys)

        // Mock successful clear
        coEvery { mockRiskScoreRepository.clearOldScores() } returns Result.success(Unit)

        // Mock successful save
        val calculatedScores = listOf(
            createTestRiskScore(areaName = "Test Area", riskScore = 8.0f, riskLevel = "high")
        )
        coEvery { mockRiskScoreRepository.saveRiskScores(calculatedScores) } returns Result.success(Unit)

        // When - Execute typical ML workflow
        val fetchResult = mlBridgeRepository.fetchSurveyDataForML()
        val clearResult = mlBridgeRepository.clearPreviousRiskScores()
        val saveResult = mlBridgeRepository.saveCalculatedRiskScores(calculatedScores)

        // Then - All steps should succeed
        assertTrue(fetchResult.isSuccess, "Survey fetch should succeed")
        assertTrue(clearResult.isSuccess, "Clear old scores should succeed")
        assertTrue(saveResult.isSuccess, "Save risk scores should succeed")
        
        // Verify call order
        coVerify(exactly = 1) { mockSurveyRepository.getAllSurveys() }
        coVerify(exactly = 1) { mockRiskScoreRepository.clearOldScores() }
        coVerify(exactly = 1) { mockRiskScoreRepository.saveRiskScores(calculatedScores) }
    }

    @Test
    fun `workflow continues if clear fails but save succeeds`() = runTest {
        // Given - Clear fails
        coEvery { mockRiskScoreRepository.clearOldScores() } returns Result.failure(Exception("Clear failed"))

        // But save succeeds
        val calculatedScores = listOf(createTestRiskScore())
        coEvery { mockRiskScoreRepository.saveRiskScores(calculatedScores) } returns Result.success(Unit)

        // When
        val clearResult = mlBridgeRepository.clearPreviousRiskScores()
        val saveResult = mlBridgeRepository.saveCalculatedRiskScores(calculatedScores)

        // Then - Save can still succeed independently
        assertTrue(clearResult.isFailure, "Clear should fail")
        assertTrue(saveResult.isSuccess, "Save should still succeed")
    }

    // Convenience Method Tests

    @Test
    fun `getSurveyCount returns correct count`() = runTest {
        // Given
        val mockSurveys = listOf(
            createTestSurvey(id = "survey1"),
            createTestSurvey(id = "survey2"),
            createTestSurvey(id = "survey3")
        )
        coEvery { mockSurveyRepository.getAllSurveys() } returns Result.success(mockSurveys)

        // When
        val result = mlBridgeRepository.getSurveyCount()

        // Then
        assertTrue(result.isSuccess, "getSurveyCount should succeed")
        assertEquals(3, result.getOrNull(), "Should return count of 3")
    }

    @Test
    fun `getSurveyCount returns 0 for empty surveys`() = runTest {
        // Given
        coEvery { mockSurveyRepository.getAllSurveys() } returns Result.success(emptyList())

        // When
        val result = mlBridgeRepository.getSurveyCount()

        // Then
        assertTrue(result.isSuccess, "getSurveyCount should succeed")
        assertEquals(0, result.getOrNull(), "Should return count of 0")
    }

    @Test
    fun `getCurrentRiskScores calls riskScoreRepository getAllRiskScores`() = runTest {
        // Given
        val mockScores = listOf(
            createTestRiskScore(areaName = "Area A", riskScore = 8.0f),
            createTestRiskScore(areaName = "Area B", riskScore = 3.0f)
        )
        coEvery { mockRiskScoreRepository.getAllRiskScores() } returns Result.success(mockScores)

        // When
        val result = mlBridgeRepository.getCurrentRiskScores()

        // Then
        coVerify(exactly = 1) { mockRiskScoreRepository.getAllRiskScores() }
        assertTrue(result.isSuccess, "getCurrentRiskScores should succeed")
        assertEquals(2, result.getOrNull()?.size, "Should return 2 risk scores")
    }

    // Helper Functions

    private fun createTestSurvey(
        id: String = "test-survey-id",
        volunteerId: String = "volunteer-123",
        category: String = "infrastructure",
        severity: Int = 3,
        peopleAffected: Int = 100,
        description: String = "Test survey description",
        locationName: String = "Test Location",
        latitude: Double = 40.7128,
        longitude: Double = -74.0060,
        photoUrl: String? = null,
        status: String = "pending",
        adminNotes: String? = null,
        createdAt: String = "2024-01-01T00:00:00Z"
    ) = Survey(
        id = id,
        volunteerId = volunteerId,
        category = category,
        severity = severity,
        peopleAffected = peopleAffected,
        description = description,
        locationName = locationName,
        latitude = latitude,
        longitude = longitude,
        photoUrl = photoUrl,
        status = status,
        adminNotes = adminNotes,
        createdAt = createdAt
    )

    private fun createTestRiskScore(
        id: String = "test-risk-id",
        areaName: String = "Test Area",
        latitude: Double = 40.7128,
        longitude: Double = -74.0060,
        riskScore: Float = 5.0f,
        riskLevel: String = "medium",
        contributingFactors: List<String> = listOf("High severity surveys", "Large affected population"),
        calculatedAt: String = "2024-01-01T00:00:00Z"
    ) = AreaRiskScore(
        id = id,
        areaName = areaName,
        latitude = latitude,
        longitude = longitude,
        riskScore = riskScore,
        riskLevel = riskLevel,
        contributingFactors = contributingFactors,
        calculatedAt = calculatedAt
    )
}
