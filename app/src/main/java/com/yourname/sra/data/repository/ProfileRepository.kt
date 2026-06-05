package com.yourname.sra.data.repository

import android.util.Log

import com.yourname.sra.data.model.Volunteer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun getProfile(userId: String): Result<Volunteer> {
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
        return try {
            // Use RPC for atomic increment — avoids read-modify-write race condition
            supabaseClient.postgrest.rpc("increment_tasks_completed", mapOf("user_id" to userId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementTotalHours(userId: String, hours: Int): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc("increment_total_hours", mapOf("user_id" to userId, "hours" to hours))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePhoto(userId: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = supabaseClient.storage.from("profile-photos")
            val path = "$userId/$fileName"
            bucket.upload(path, imageBytes, upsert = true)
            val publicUrl = bucket.publicUrl(path)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload profile photo", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfilePhotoUrl(userId: String, photoUrl: String): Result<Unit> {
        return try {
            supabaseClient.postgrest.from("volunteers").update(
                {
                    set("profile_photo_url", photoUrl)
                    set("updated_at", Instant.now().toString())
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update profile photo URL", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "ProfileRepository"
    }
}
