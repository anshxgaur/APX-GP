package com.yourname.sra.data.repository

import com.yourname.sra.data.model.Volunteer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    private val isMockMode = com.yourname.sra.BuildConfig.SUPABASE_URL.contains("placeholder")

    suspend fun getProfile(userId: String): Result<Volunteer> {
        if (isMockMode) {
            return Result.success(
                Volunteer(
                    id = "mock-volunteer-id",
                    fullName = "John Doe",
                    email = "arhaan@example.com",
                    phone = "+91 98765 43210",
                    area = "Delhi NGO Zone",
                    skills = listOf("First Aid", "Logistics", "Rescue Operations"),
                    availability = "flexible",
                    totalTasksCompleted = 12,
                    totalHours = 45,
                    profilePhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
                )
            )
        }
        return try {
            val volunteer = supabaseClient.postgrest.from("volunteers")
                .select {
                    filter {
                        eq("id", userId)
                    }
                    limit(1)
                }
                .decodeSingle<Volunteer>()
            Result.success(volunteer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(volunteer: Volunteer): Result<Unit> {
        if (isMockMode) return Result.success(Unit)
        return try {
            supabaseClient.postgrest.from("volunteers").update(
                {
                    set("full_name", volunteer.fullName)
                    set("phone", volunteer.phone)
                    set("area", volunteer.area)
                    set("skills", volunteer.skills)
                    set("availability", volunteer.availability)
                    set("profile_photo_url", volunteer.profilePhotoUrl)
                    set("updated_at", Instant.now().toString())
                }
            ) {
                filter {
                    eq("id", volunteer.id)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementTasksCompleted(userId: String): Result<Unit> {
        if (isMockMode) return Result.success(Unit)
        return try {
            val volunteer = getProfile(userId).getOrThrow()
            supabaseClient.postgrest.from("volunteers").update(
                {
                    set("total_tasks_completed", volunteer.totalTasksCompleted + 1)
                    set("updated_at", Instant.now().toString())
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePhoto(imageBytes: ByteArray, fileName: String): Result<String> {
        if (isMockMode) return Result.success("https://images.unsplash.com/photo-1534528741775-53994a69daeb")
        return try {
            val bucket = supabaseClient.storage.from("profile-photos")
            bucket.upload(fileName, imageBytes, upsert = true)
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
