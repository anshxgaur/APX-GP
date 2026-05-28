package com.yourname.sra.data.repository

import com.yourname.sra.data.model.Survey
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun submitSurvey(survey: Survey): Result<Unit> {
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

    suspend fun uploadPhoto(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = supabaseClient.storage.from("survey-photos")
            bucket.upload(fileName, imageBytes, upsert = true)
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
