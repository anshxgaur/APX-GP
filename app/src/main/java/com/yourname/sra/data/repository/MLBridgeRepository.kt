package com.yourname.sra.data.repository

import com.yourname.sra.data.model.AreaRiskScore
import com.yourname.sra.data.model.Survey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Bridge Repository
 * 
 * Convenience wrapper repository that provides a clean, documented interface for external
 * ML systems to interact with the application database. This repository abstracts away
 * database implementation details and provides ML engineers with simple, well-named
 * functions for their workflow.
 * 
 * ## ML Workflow Overview
 * 
 * The typical ML risk calculation workflow follows these steps:
 * 
 * 1. **Data Export**: Fetch all survey data for ML processing
 *    - Call `fetchSurveyDataForML()` to retrieve all survey records
 *    - Survey data includes: location, severity, category, people affected, etc.
 * 
 * 2. **Risk Calculation**: Process survey data through ML models (external to this app)
 *    - ML system analyzes survey patterns
 *    - ML system calculates risk scores per geographic area
 *    - ML system determines risk levels and contributing factors
 * 
 * 3. **Old Data Cleanup**: Clear previous risk calculations
 *    - Call `clearPreviousRiskScores()` to remove outdated risk scores
 *    - This ensures the database only contains current calculations
 * 
 * 4. **Risk Import**: Save calculated risk scores back to the database
 *    - Call `saveCalculatedRiskScores()` with the list of calculated scores
 *    - Scores are validated and batch inserted
 *    - App UI will immediately display updated risk data
 * 
 * ## Usage Example
 * 
 * ```kotlin
 * // Step 1: Fetch survey data for ML processing
 * val surveysResult = mlBridgeRepository.fetchSurveyDataForML()
 * if (surveysResult.isFailure) {
 *     // Handle error
 *     return
 * }
 * val surveys = surveysResult.getOrNull() ?: emptyList()
 * 
 * // Step 2: Process surveys through ML system (external)
 * val calculatedScores = mlSystem.calculateRiskScores(surveys)
 * 
 * // Step 3: Clear previous risk scores
 * mlBridgeRepository.clearPreviousRiskScores()
 * 
 * // Step 4: Save new risk scores
 * val saveResult = mlBridgeRepository.saveCalculatedRiskScores(calculatedScores)
 * if (saveResult.isSuccess) {
 *     // Risk scores successfully updated
 * }
 * ```
 * 
 * ## Data Validation
 * 
 * All risk scores are validated before insertion:
 * - risk_score must be between 0.0 and 10.0
 * - latitude must be between -90.0 and 90.0
 * - longitude must be between -180.0 and -180.0
 * - risk_level must be one of: "low", "medium", "high", "critical"
 * 
 * Invalid data will result in IllegalArgumentException being thrown.
 * 
 * ## Requirements Satisfied
 * 
 * This repository satisfies the following acceptance criteria:
 * - Requirement 15.1, 15.2, 15.3: Survey data export for ML
 * - Requirement 16.1, 16.2, 16.3, 16.4, 16.5, 16.6: Risk score import and validation
 * - Requirement 17.1, 17.2, 17.3: Old score cleanup before recalculation
 * 
 * @see SurveyRepository for underlying survey data access
 * @see RiskScoreRepository for underlying risk score data access
 */
@Singleton
class MLBridgeRepository @Inject constructor(
    private val surveyRepository: SurveyRepository,
    private val riskScoreRepository: RiskScoreRepository
) {

    /**
     * Fetch all survey data for ML processing.
     * 
     * This function retrieves all survey records from the database in a format
     * suitable for ML model consumption. Survey data includes location coordinates,
     * severity levels, affected population counts, and categorical information.
     * 
     * ## Data Fields Returned
     * 
     * Each survey includes:
     * - id: Unique survey identifier
     * - volunteer_id: ID of the volunteer who submitted the survey
     * - category: Type of disaster/issue (e.g., "infrastructure", "medical")
     * - severity: Severity rating (1-5, where 5 is most severe)
     * - people_affected: Number of people impacted
     * - description: Detailed description of the situation
     * - location_name: Human-readable location name
     * - latitude: GPS latitude coordinate (nullable)
     * - longitude: GPS longitude coordinate (nullable)
     * - photo_url: URL to uploaded photo evidence (nullable)
     * - status: Survey review status (e.g., "pending", "approved")
     * - admin_notes: Administrative notes (nullable)
     * - created_at: Timestamp of survey submission
     * 
     * ## ML Processing Considerations
     * 
     * - Surveys may have null latitude/longitude if GPS was unavailable
     * - Severity ranges from 1 (minor) to 5 (critical)
     * - Multiple surveys may exist for the same geographic area
     * - Recent surveys should be weighted more heavily in risk calculations
     * 
     * @return Result containing list of all surveys, or failure with exception
     * @see Survey for data model structure
     */
    suspend fun fetchSurveyDataForML(): Result<List<Survey>> {
        return surveyRepository.getAllSurveys()
    }

    /**
     * Save calculated risk scores to the database.
     * 
     * This function accepts a list of risk scores calculated by external ML systems
     * and persists them to the database. The app UI will automatically display
     * these scores on risk maps and dashboards.
     * 
     * ## Validation Rules
     * 
     * Each AreaRiskScore is validated before insertion:
     * - riskScore must be between 0.0 and 10.0
     * - latitude must be between -90.0 and 90.0
     * - longitude must be between -180.0 and 180.0
     * - riskLevel must be one of: "low", "medium", "high", "critical"
     * 
     * If any score fails validation, the entire batch is rejected and
     * IllegalArgumentException is thrown.
     * 
     * ## Risk Level Guidelines
     * 
     * Recommended mapping from risk_score to risk_level:
     * - "low": 0.0 - 2.5
     * - "medium": 2.5 - 5.0
     * - "high": 5.0 - 7.5
     * - "critical": 7.5 - 10.0
     * 
     * ## Contributing Factors
     * 
     * The contributingFactors field should include human-readable strings
     * explaining why the risk score is high, for example:
     * - "High severity surveys in area"
     * - "Large affected population"
     * - "Multiple infrastructure failures"
     * - "Recent disaster events"
     * 
     * ## Performance
     * 
     * This function uses batch insertion for efficiency. All scores are
     * inserted in a single database transaction.
     * 
     * @param scores List of calculated risk scores to save
     * @return Result indicating success or failure
     * @throws IllegalArgumentException if validation fails
     * @see AreaRiskScore for data model structure
     */
    suspend fun saveCalculatedRiskScores(scores: List<AreaRiskScore>): Result<Unit> {
        return riskScoreRepository.saveRiskScores(scores)
    }

    /**
     * Clear all previous risk score calculations.
     * 
     * This function removes all existing risk scores from the database.
     * It should be called before saving new risk calculations to ensure
     * that only current data is displayed to users.
     * 
     * ## When to Call
     * 
     * Call this function:
     * - Before each new ML risk calculation cycle
     * - When risk calculation methodology changes
     * - When outdated scores need to be removed
     * 
     * ## Side Effects
     * 
     * - All records in area_risk_scores table are deleted
     * - App UI will show "No risk scores calculated yet" until new scores are saved
     * - Realtime subscriptions will notify the UI of the changes
     * 
     * ## Database Implementation
     * 
     * This function calls the clear_risk_scores RPC function on the Supabase backend,
     * which efficiently truncates the area_risk_scores table.
     * 
     * @return Result indicating success or failure
     */
    suspend fun clearPreviousRiskScores(): Result<Unit> {
        return riskScoreRepository.clearOldScores()
    }

    /**
     * Get count of available surveys for ML processing.
     * 
     * This convenience function returns the total number of surveys available
     * for ML processing. Useful for logging, monitoring, and determining if
     * sufficient data exists for meaningful risk calculations.
     * 
     * @return Result containing survey count, or failure with exception
     */
    suspend fun getSurveyCount(): Result<Int> {
        return surveyRepository.getAllSurveys().map { surveys -> surveys.size }
    }

    /**
     * Get current risk scores for validation/monitoring.
     * 
     * This function retrieves all currently stored risk scores, ordered by
     * risk level (highest risk first). Useful for validating ML calculations
     * or monitoring the current state of risk data.
     * 
     * @return Result containing list of risk scores ordered by risk level
     */
    suspend fun getCurrentRiskScores(): Result<List<AreaRiskScore>> {
        return riskScoreRepository.getAllRiskScores()
    }
}
