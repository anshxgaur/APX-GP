package com.yourname.sra.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    private val isMockMode = com.yourname.sra.BuildConfig.SUPABASE_URL.contains("placeholder")

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        area: String,
        skills: List<String>,
        availability: String
    ): Result<Unit> {
        if (isMockMode) return Result.success(Unit)
        return try {
            // 1. Create auth user
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // 2. Get the user ID
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Failed to get user ID after signup"))

            // 3. Insert volunteer profile
            supabaseClient.postgrest.from("volunteers").insert(
                mapOf(
                    "id" to userId,
                    "full_name" to fullName,
                    "email" to email,
                    "phone" to phone,
                    "area" to area,
                    "skills" to skills,
                    "availability" to availability
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        if (isMockMode) return Result.success(Unit)
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        if (isMockMode) return Result.success(Unit)
        return try {
            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean {
        if (isMockMode) return true
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    fun getCurrentUserId(): String? {
        if (isMockMode) return "mock-volunteer-id"
        return supabaseClient.auth.currentUserOrNull()?.id
    }
}


