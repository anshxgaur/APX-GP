package com.yourname.sra.data.repository

import com.yourname.sra.data.model.AreaRiskScore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML ↔ DB Bridge Repository
 *
 * Provides a clean interface between ML team and database.
 * ML team MUST NOT access database logic directly — use these functions instead.
 */
@Singleton
class RiskScoreRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    /**
     * fetch_survey_data() — ML Bridge Function
     * Returns all survey data for ML processing.
     */
    suspend fun fetchSurveyDataForML(): Result<List<Map<String, Any?>>> {
        return try {
            val surveys = supabaseClient.postgrest.from("surveys")
                .select()
                .decodeList<Map<String, Any?>>()
            Result.success(surveys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * save_risk_score() — ML Bridge Function
     * Writes a single risk score calculated by the ML engine.
     */
    suspend fun saveRiskScore(score: AreaRiskScore): Result<Unit> {
        require(score.riskScore in 0f..10f) { "Risk score must be between 0 and 10" }
        require(score.latitude in -90.0..90.0) { "Invalid latitude" }
        require(score.longitude in -180.0..180.0) { "Invalid longitude" }
        require(score.riskLevel in listOf("low", "medium", "high", "critical")) { "Invalid risk level" }

        return try {
            supabaseClient.postgrest.from("area_risk_scores").insert(
                mapOf(
                    "area_name" to score.areaName,
                    "latitude" to score.latitude,
                    "longitude" to score.longitude,
                    "risk_score" to score.riskScore,
                    "risk_level" to score.riskLevel,
                    "contributing_factors" to score.contributingFactors,
                    "calculated_at" to score.calculatedAt
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Batch save multiple risk scores at once.
     */
    suspend fun saveRiskScores(scores: List<AreaRiskScore>): Result<Unit> {
        return try {
            val records = scores.map { score ->
                mapOf(
                    "area_name" to score.areaName,
                    "latitude" to score.latitude,
                    "longitude" to score.longitude,
                    "risk_score" to score.riskScore,
                    "risk_level" to score.riskLevel,
                    "contributing_factors" to score.contributingFactors,
                    "calculated_at" to score.calculatedAt
                )
            }
            supabaseClient.postgrest.from("area_risk_scores").insert(records)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * clear_old_scores() — ML Bridge Function
     * Removes all previous risk calculations before recalculation.
     */
    suspend fun clearOldScores(): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc("clear_risk_scores")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all risk scores, ordered by highest risk first.
     */
    suspend fun getRiskScores(): Result<List<AreaRiskScore>> {
        return try {
            val scores = supabaseClient.postgrest.from("area_risk_scores")
                .select {
                    order("risk_score", Order.DESCENDING)
                }
                .decodeList<AreaRiskScore>()
            Result.success(scores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get risk score for a specific area.
     */
    suspend fun getRiskScoreByArea(areaName: String): Result<AreaRiskScore?> {
        return try {
            val scores = supabaseClient.postgrest.from("area_risk_scores")
                .select {
                    filter {
                        eq("area_name", areaName)
                    }
                    limit(1)
                }
                .decodeList<AreaRiskScore>()
            Result.success(scores.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Realtime: subscribe to risk score changes
    fun subscribeRiskScoreChanges(): Flow<PostgresAction> {
        val channel = supabaseClient.channel("risk-scores-changes")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "area_risk_scores"
        }
    }
}
