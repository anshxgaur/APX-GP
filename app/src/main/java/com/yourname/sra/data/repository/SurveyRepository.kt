package com.yourname.sra.data.repository

import android.util.Log
import com.yourname.sra.data.model.Survey
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun submitSurvey(survey: Survey): Result<Unit> {
        require(survey.severity in 1..5) { "Severity must be between 1 and 5" }
        require(survey.latitude == null || survey.latitude in -90.0..90.0) { "Invalid latitude" }
        require(survey.longitude == null || survey.longitude in -180.0..180.0) { "Invalid longitude" }

        return try {
            supabaseClient.postgrest.from("surveys").insert(
                mapOf(
                    "volunteer_id" to survey.volunteerId,
                    "category" to survey.category,
                    "severity" to survey.severity,
                    "people_affected" to survey.peopleAffected,
                    "description" to survey.description,
                    "location_name" to survey.locationName,
                    "latitude" to survey.latitude,
                    "longitude" to survey.longitude,
                    "photo_url" to survey.photoUrl,
                    "status" to "pending"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMySurveys(volunteerId: String): Result<List<Survey>> {
        return try {
            val surveys = supabaseClient.postgrest.from("surveys")
                .select {
                    filter {
                        eq("volunteer_id", volunteerId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Survey>()
            Result.success(surveys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentSurveys(volunteerId: String, limit: Int = 2): Result<List<Survey>> {
        return try {
            val surveys = supabaseClient.postgrest.from("surveys")
                .select {
                    filter {
                        eq("volunteer_id", volunteerId)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Survey>()
            Result.success(surveys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSurveys(): Result<List<Survey>> {
        return try {
            val surveys = supabaseClient.postgrest.from("surveys")
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Survey>()
            Result.success(surveys)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhoto(userId: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = supabaseClient.storage.from("survey-photos")
            val path = "$userId/$fileName"
            bucket.upload(path, imageBytes, upsert = true)
            val publicUrl = bucket.publicUrl(path)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload survey photo", e)
            Result.failure(e)
        }
    }

    // Realtime: subscribe to survey changes
    fun subscribeSurveyChanges(): Flow<PostgresAction> {
        val channel = supabaseClient.channel("surveys-changes")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "surveys"
        }
    }

    companion object {
        private const val TAG = "SurveyRepository"
    }
}
