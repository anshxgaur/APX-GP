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

    private val isMockMode = com.yourname.sra.BuildConfig.SUPABASE_URL.contains("placeholder")

    private val mockSurveysList = java.util.concurrent.CopyOnWriteArrayList<Survey>().apply {
        add(
            Survey(
                id = "mock-survey-1",
                volunteerId = "mock-volunteer-id",
                category = "Water supply issue",
                severity = 3,
                peopleAffected = 150,
                description = "Main pipeline ruptured near the school, causing street flooding and loss of drinking water access.",
                locationName = "Oakridge High School",
                latitude = 37.7549,
                longitude = -122.4394,
                status = "approved"
            )
        )
        add(
            Survey(
                id = "mock-survey-2",
                volunteerId = "mock-volunteer-id",
                category = "Power lines down",
                severity = 4,
                peopleAffected = 40,
                description = "Power lines knocked down by strong winds. Sparks observed. Police notified.",
                locationName = "4th and Broadway",
                latitude = 37.7649,
                longitude = -122.4494,
                status = "pending"
            )
        )
    }

    suspend fun submitSurvey(survey: Survey): Result<Unit> {
        if (isMockMode) {
            mockSurveysList.add(survey.copy(id = "mock-survey-${System.currentTimeMillis()}", status = "pending"))
            return Result.success(Unit)
        }
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
        if (isMockMode) {
            return Result.success(mockSurveysList.filter { it.volunteerId == volunteerId })
        }
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
        if (isMockMode) {
            return Result.success(mockSurveysList.filter { it.volunteerId == volunteerId }.take(limit))
        }
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
        if (isMockMode) {
            return Result.success("https://images.unsplash.com/photo-1534528741775-53994a69daeb")
        }
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
