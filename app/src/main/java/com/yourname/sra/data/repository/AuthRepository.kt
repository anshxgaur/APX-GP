package com.yourname.sra.data.repository

import com.yourname.sra.data.local.TaskDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for authentication operations.
 * 
 * Session Persistence:
 * - Supabase GoTrue automatically handles session persistence using Android SharedPreferences
 * - Sessions are stored securely and restored on app restart
 * - No manual session management required
 * 
 * Fulfills Requirements:
 * - 3.1: Check for existing valid session on app startup
 * - 3.2: Navigate to dashboard when valid session exists
 * - 3.3: Navigate to login when no valid session exists
 * - 3.4: Redirect to login when session expires during app usage
 */
@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val taskDao: TaskDao
) {

    /**
     * Signs up a new user with authentication and creates volunteer profile.
     * 
     * Error Handling:
     * - Duplicate email: Returns Result.failure with "Email already registered" message
     * - Network errors: Returns Result.failure with network error details
     * - Other errors: Returns Result.failure with exception details
     * 
     * Fulfills Requirements:
     * - 2.1: Create user account and volunteer profile
     * - 2.2: Automatically log in user after signup
     * - 2.3: Handle duplicate email error with user-friendly message
     * 
     * @param email User's email address
     * @param password User's password
     * @param fullName User's full name
     * @param phone User's phone number
     * @param role User role (default: "volunteer")
     * @param area Geographic area
     * @param skills List of user skills
     * @param availability User availability
     * @return Result.success(Unit) on success, Result.failure(Exception) on error
     */
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String = "volunteer",
        area: String,
        skills: List<String>,
        availability: String
    ): Result<Unit> {
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
                    "role" to role,
                    "area" to area,
                    "skills" to skills,
                    "availability" to availability
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            // Map Supabase errors to user-friendly messages
            val errorMessage = when {
                // Duplicate email error
                e.message?.contains("already exists", ignoreCase = true) == true ||
                e.message?.contains("duplicate", ignoreCase = true) == true ||
                e.message?.contains("unique constraint", ignoreCase = true) == true ||
                e.message?.contains("User already registered", ignoreCase = true) == true ->
                    "Email already registered"
                
                // Network errors
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("connection", ignoreCase = true) == true ||
                e is IOException ->
                    "Network error. Please check your connection."
                
                // Timeout errors
                e.message?.contains("timeout", ignoreCase = true) == true ||
                e is SocketTimeoutException ->
                    "Request timed out. Please try again."
                
                // Generic error - preserve original message
                else -> e.message ?: "An unexpected error occurred during signup"
            }
            
            Result.failure(Exception(errorMessage, e))
        }
    }

    /**
     * Authenticates user with email and password.
     * 
     * Error Handling:
     * - Invalid credentials: Returns Result.failure with "Invalid email or password" message
     * - Network errors: Returns Result.failure with network error details
     * - Other errors: Returns Result.failure with exception details
     * 
     * Fulfills Requirements:
     * - 2.4: Authenticate user and establish session
     * - 2.5: Navigate to dashboard on successful login
     * - 2.6: Handle invalid credentials with user-friendly message
     * 
     * @param email User's email address
     * @param password User's password
     * @return Result.success(Unit) on success, Result.failure(Exception) on error
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Map Supabase errors to user-friendly messages
            val errorMessage = when {
                // Invalid credentials errors
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true ||
                e.message?.contains("invalid", ignoreCase = true) == true ||
                e.message?.contains("credentials", ignoreCase = true) == true ||
                e.message?.contains("authentication failed", ignoreCase = true) == true ||
                e.message?.contains("unauthorized", ignoreCase = true) == true ||
                e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                    "Invalid email or password"
                
                // Network errors
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("connection", ignoreCase = true) == true ||
                e is IOException ->
                    "Network error. Please check your connection."
                
                // Timeout errors
                e.message?.contains("timeout", ignoreCase = true) == true ||
                e is SocketTimeoutException ->
                    "Request timed out. Please try again."
                
                // Generic error - preserve original message
                else -> e.message ?: "An unexpected error occurred during login"
            }
            
            Result.failure(Exception(errorMessage, e))
        }
    }

    /**
     * Signs out the current user and clears session.
     * 
     * Error Handling:
     * - Network errors: Returns Result.failure with network error details
     * - Note: Local session is cleared even if remote logout fails
     * 
     * Fulfills Requirements:
     * - 2.7: Terminate session and clear authentication state
     * - 2.8: Navigate to login screen after logout
     * - 27.1: Handle network errors with appropriate messages
     * - 33.1: Clear Supabase session on logout
     * - 33.2: Clear cached user data from memory
     * - 33.3: Navigate to login and clear backstack
     * - 33.4: Display error "Failed to logout" if remote logout fails but still clear local session
     * - 35.2: Clear backstack on logout navigation
     * 
     * @return Result.success(Unit) on success, Result.failure(Exception) on error
     */
    suspend fun logout(): Result<Unit> {
        var remoteLogoutFailed = false
        var logoutException: Exception? = null
        
        return try {
            // Step 1: Try to sign out from Supabase (remote session)
            try {
                supabaseClient.auth.signOut()
            } catch (e: Exception) {
                // Mark that remote logout failed, but continue with local cleanup
                remoteLogoutFailed = true
                logoutException = e
            }
            
            // Step 2: Clear all local cached data regardless of remote logout result
            try {
                taskDao.deleteAll()
            } catch (e: Exception) {
                // Log but don't fail - local cache clearing is best-effort
                android.util.Log.w("AuthRepository", "Failed to clear task cache during logout", e)
            }
            
            // Step 3: Return appropriate result
            if (remoteLogoutFailed) {
                // Remote logout failed, but local session cleared
                Result.failure(
                    Exception(
                        "Failed to logout",
                        logoutException
                    )
                )
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Unexpected error during logout flow
            val errorMessage = when {
                // Network errors
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("connection", ignoreCase = true) == true ||
                e is IOException ->
                    "Failed to logout"
                
                // Timeout errors
                e.message?.contains("timeout", ignoreCase = true) == true ||
                e is SocketTimeoutException ->
                    "Failed to logout"
                
                // Generic error
                else -> "Failed to logout"
            }
            
            Result.failure(Exception(errorMessage, e))
        }
    }

    /**
     * Checks if a valid session exists.
     * 
     * Fulfills Requirement 3.1: Check for existing valid session on app startup
     * 
     * @return true if user has a valid authenticated session, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    /**
     * Gets the current authenticated user's ID.
     * 
     * @return User ID if authenticated, null otherwise
     */
    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    /**
     * Observes session status changes.
     * 
     * Fulfills Requirement 3.4: Handle session expiration during app usage
     * 
     * Emits true when session is valid, false when expired or logged out.
     * Use this to handle session expiration during app usage.
     * 
     * Session States:
     * - Authenticated: User has valid session → emit true
     * - NotAuthenticated: Session expired or logged out → emit false
     * - LoadingFromStorage: Loading session from storage → emit true (assume valid during load)
     * - NetworkError: Network issue checking session → emit true (don't log out on network errors)
     * 
     * @return Flow emitting true when session is active, false when inactive
     */
    fun observeSessionStatus(): Flow<Boolean> {
        return supabaseClient.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> true
                is SessionStatus.NotAuthenticated -> false
                is SessionStatus.LoadingFromStorage -> true // Still loading, assume valid
                is SessionStatus.NetworkError -> true // Network error, don't log out
            }
        }
    }
}
