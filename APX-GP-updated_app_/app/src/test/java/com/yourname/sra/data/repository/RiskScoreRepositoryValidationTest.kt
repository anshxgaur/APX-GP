package com.yourname.sra.data.repository

import com.yourname.sra.data.model.AreaRiskScore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PostgrestConfig
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.rpc
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit tests for RiskScoreRepository validation logic
 * 
 * Tests cover Requirements 16.3, 16.4, 16.5, 16.6:
 * - Risk score must be between 0 and 10
 * - Latitude must be between -90 and 90
 * - Longitude must be between -180 and 180
 * - Risk level must be one of: low, medium, high, critical
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RiskScoreRepositoryValidationTest {

    private lateinit var repository: RiskScoreRepository
    private lateinit var mockSupabaseClient: SupabaseClient
    private lateinit var mockPostgrest: Postgrest
    
    @Before
    fun setup() {
        mockSupabaseClient = mockk(relaxed = true)
        mockPostgrest = mockk(relaxed = true)
        
        // Mock the postgrest property
        coEvery { mockSupabaseClient.postgrest } returns mockPostgrest
        
        repository = RiskScoreRepository(mockSupabaseClient)
    }

    // Risk Score Validation Tests

    @Test
    fun `saveRiskScore accepts valid risk score of 0`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(riskScore = 0f)
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Risk score 0 should be valid")
    }

    @Test
    fun `saveRiskScore accepts valid risk score of 10`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(riskScore = 10f)
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Risk score 10 should be valid")
    }

    @Test
    fun `saveRiskScore rejects risk score below 0`() = runTest {
        // Given
        val invalidScore = createValidAreaRiskScore(riskScore = -0.1f)

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScore(invalidScore)
        }
        assertEquals("Risk score must be between 0 and 10", exception.message)
    }

    @Test
    fun `saveRiskScore rejects risk score above 10`() = runTest {
        // Given
        val invalidScore = createValidAreaRiskScore(riskScore = 10.1f)

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScore(invalidScore)
        }
        assertEquals("Risk score must be between 0 and 10", exception.message)
    }

    // Latitude Validation Tests

    @Test
    fun `saveRiskScore accepts valid latitude of -90`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(latitude = -90.0)
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Latitude -90 should be valid")
    }

    @Test
    fun `saveRiskScore accepts valid latitude of 90`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(latitude = 90.0)
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Latitude 90 should be valid")
    }

    @Test
    fun `saveRiskScore rejects latitude below -90`() = runTest {
        // Given
        val invalidScore = createValidAreaRiskScore(latitude = -90.1)

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScore(invalidScore)
        }
        assertEquals("Invalid latitude", exception.message)
    }

    @Test
    fun `saveRiskScore rejects latitude above 90`() = runTest {
        // Given
        val invalidScore = createValidAreaRiskScore(latitude = 90.1)

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScore(invalidScore)
        }
        assertEquals("Invalid latitude", exception.message)
    }

    // Longitude Validation Tests

    @Test
    fun `saveRiskScore accepts valid longitude of -180`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(longitude = -180.0)
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Longitude -180 should be valid")
    }

    @Test
    fun `saveRiskScore accepts valid longitude of 180`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(longitude = 180.0)
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Longitude 180 should be valid")
    }

    @Test
    fun `saveRiskScore rejects longitude below -180`() = runTest {
        // Given
        val invalidScore = createValidAreaRiskScore(longitude = -180.1)

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScore(invalidScore)
        }
        assertEquals("Invalid longitude", exception.message)
    }

    @Test
    fun `saveRiskScore rejects longitude above 180`() = runTest {
        // Given
        val invalidScore = createValidAreaRiskScore(longitude = 180.1)

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScore(invalidScore)
        }
        assertEquals("Invalid longitude", exception.message)
    }

    // Risk Level Validation Tests

    @Test
    fun `saveRiskScore accepts risk level low`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(riskLevel = "low")
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Risk level 'low' should be valid")
    }

    @Test
    fun `saveRiskScore accepts risk level medium`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(riskLevel = "medium")
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Risk level 'medium' should be valid")
    }

    @Test
    fun `saveRiskScore accepts risk level high`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(riskLevel = "high")
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Risk level 'high' should be valid")
    }

    @Test
    fun `saveRiskScore accepts risk level critical`() = runTest {
        // Given
        val validScore = createValidAreaRiskScore(riskLevel = "critical")
        
        // Mock the insert to succeed
        coEvery { 
            mockSupabaseClient.postgrest.from("area_risk_scores").insert(any<Map<String, Any?>>()) 
        } returns Unit

        // When
        val result = repository.saveRiskScore(validScore)

        // Then
        assertTrue(result.isSuccess, "Risk level 'critical' should be valid")
    }

    @Test
    fun `saveRiskScore rejects invalid risk level`() = runTest {
        // Given
        val invalidScore = createValidAreaRiskScore(riskLevel = "severe")

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScore(invalidScore)
        }
        assertEquals("Invalid risk level", exception.message)
    }

    // Batch Validation Tests

    @Test
    fun `saveRiskScores validates all scores before inserting`() = runTest {
        // Given - One valid score and one invalid score (risk score out of range)
        val scores = listOf(
            createValidAreaRiskScore(riskScore = 5f),
            createValidAreaRiskScore(riskScore = 11f) // Invalid
        )

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScores(scores)
        }
        assertEquals("Risk score must be between 0 and 10", exception.message)
    }

    @Test
    fun `saveRiskScores validates latitude for all scores`() = runTest {
        // Given - One valid score and one with invalid latitude
        val scores = listOf(
            createValidAreaRiskScore(latitude = 45.0),
            createValidAreaRiskScore(latitude = 91.0) // Invalid
        )

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScores(scores)
        }
        assertEquals("Invalid latitude", exception.message)
    }

    @Test
    fun `saveRiskScores validates longitude for all scores`() = runTest {
        // Given - One valid score and one with invalid longitude
        val scores = listOf(
            createValidAreaRiskScore(longitude = 120.0),
            createValidAreaRiskScore(longitude = -181.0) // Invalid
        )

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScores(scores)
        }
        assertEquals("Invalid longitude", exception.message)
    }

    @Test
    fun `saveRiskScores validates risk level for all scores`() = runTest {
        // Given - One valid score and one with invalid risk level
        val scores = listOf(
            createValidAreaRiskScore(riskLevel = "low"),
            createValidAreaRiskScore(riskLevel = "extreme") // Invalid
        )

        // When/Then
        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveRiskScores(scores)
        }
        assertEquals("Invalid risk level", exception.message)
    }

    // Helper function to create valid AreaRiskScore with overridable fields
    private fun createValidAreaRiskScore(
        riskScore: Float = 5.0f,
        latitude: Double = 40.7128,
        longitude: Double = -74.0060,
        riskLevel: String = "medium"
    ) = AreaRiskScore(
        id = "test-id",
        areaName = "Test Area",
        latitude = latitude,
        longitude = longitude,
        riskScore = riskScore,
        riskLevel = riskLevel,
        contributingFactors = listOf("factor1", "factor2"),
        calculatedAt = "2024-01-01T00:00:00Z"
    )
}
