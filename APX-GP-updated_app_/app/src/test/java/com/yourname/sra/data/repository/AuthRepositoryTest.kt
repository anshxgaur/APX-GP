package com.yourname.sra.data.repository

import com.yourname.sra.data.local.TaskDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for AuthRepository
 * 
 * Tests cover:
 * - User signup with profile creation
 * - User login
 * - User logout
 * - Session state checking
 * - Current user ID retrieval
 * - Error handling scenarios
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository
    private lateinit var mockSupabaseClient: FakeSupabaseClient
    private lateinit var mockTaskDao: TaskDao
    
    @Before
    fun setup() {
        mockSupabaseClient = FakeSupabaseClient()
        mockTaskDao = mockk(relaxed = true)
        repository = AuthRepository(mockSupabaseClient, mockTaskDao)
    }

    @Test
    fun `signUp creates auth user and volunteer profile successfully`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val fullName = "John Doe"
        val phone = "+1234567890"
        val area = "Downtown"
        val skills = listOf("First Aid", "Construction")
        val availability = "weekends"

        // When
        val result = repository.signUp(
            email = email,
            password = password,
            fullName = fullName,
            phone = phone,
            role = "volunteer",
            area = area,
            skills = skills,
            availability = availability
        )

        // Then
        assertTrue(result.isSuccess, "Signup should succeed")
        assertTrue(mockSupabaseClient.authSignUpCalled, "Auth signUp should be called")
        assertTrue(mockSupabaseClient.volunteerProfileCreated, "Volunteer profile should be created")
    }

    @Test
    fun `signUp fails when user ID is not available after auth creation`() = runTest {
        // Given
        mockSupabaseClient.shouldReturnNullUser = true
        
        // When
        val result = repository.signUp(
            email = "test@example.com",
            password = "password123",
            fullName = "John Doe",
            phone = "+1234567890",
            role = "volunteer",
            area = "Downtown",
            skills = listOf("First Aid"),
            availability = "weekends"
        )

        // Then
        assertTrue(result.isFailure, "Signup should fail when user ID is unavailable")
        assertFalse(mockSupabaseClient.volunteerProfileCreated, "Profile should not be created")
    }

    @Test
    fun `login authenticates user successfully`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isSuccess, "Login should succeed")
        assertTrue(mockSupabaseClient.authSignInCalled, "Auth signIn should be called")
    }

    @Test
    fun `login handles authentication errors`() = runTest {
        // Given
        mockSupabaseClient.shouldThrowOnSignIn = true

        // When
        val result = repository.login("test@example.com", "wrongpassword")

        // Then
        assertTrue(result.isFailure, "Login should fail with invalid credentials")
    }

    @Test
    fun `logout signs out user successfully`() = runTest {
        // Given - User is logged in
        mockSupabaseClient.setLoggedIn(true)
        coEvery { mockTaskDao.deleteAll() } returns Unit

        // When
        val result = repository.logout()

        // Then
        assertTrue(result.isSuccess, "Logout should succeed")
        assertTrue(mockSupabaseClient.authSignOutCalled, "Auth signOut should be called")
        coVerify { mockTaskDao.deleteAll() }
    }

    @Test
    fun `logout clears local data even when remote logout fails`() = runTest {
        // Given - User is logged in but remote logout will fail
        mockSupabaseClient.setLoggedIn(true)
        mockSupabaseClient.shouldThrowOnSignOut = true
        coEvery { mockTaskDao.deleteAll() } returns Unit

        // When
        val result = repository.logout()

        // Then
        assertTrue(result.isFailure, "Logout should fail when remote logout fails")
        assertEquals("Failed to logout", result.exceptionOrNull()?.message)
        coVerify { mockTaskDao.deleteAll() } // Local data should still be cleared
    }

    @Test
    fun `isLoggedIn returns true when session exists`() {
        // Given
        mockSupabaseClient.setLoggedIn(true)

        // When
        val isLoggedIn = repository.isLoggedIn()

        // Then
        assertTrue(isLoggedIn, "Should return true when session exists")
    }

    @Test
    fun `isLoggedIn returns false when no session exists`() {
        // Given
        mockSupabaseClient.setLoggedIn(false)

        // When
        val isLoggedIn = repository.isLoggedIn()

        // Then
        assertFalse(isLoggedIn, "Should return false when no session exists")
    }

    @Test
    fun `getCurrentUserId returns user ID when logged in`() {
        // Given
        val expectedUserId = "test-user-id-123"
        mockSupabaseClient.setLoggedIn(true, expectedUserId)

        // When
        val userId = repository.getCurrentUserId()

        // Then
        assertEquals(expectedUserId, userId, "Should return current user ID")
    }

    @Test
    fun `getCurrentUserId returns null when not logged in`() {
        // Given
        mockSupabaseClient.setLoggedIn(false)

        // When
        val userId = repository.getCurrentUserId()

        // Then
        assertNull(userId, "Should return null when not logged in")
    }

    @Test
    fun `signUp handles network errors gracefully`() = runTest {
        // Given
        mockSupabaseClient.shouldThrowOnSignUp = true

        // When
        val result = repository.signUp(
            email = "test@example.com",
            password = "password123",
            fullName = "John Doe",
            phone = "+1234567890",
            role = "volunteer",
            area = "Downtown",
            skills = listOf("First Aid"),
            availability = "weekends"
        )

        // Then
        assertTrue(result.isFailure, "Should fail gracefully on network error")
    }
}

/**
 * Fake SupabaseClient for testing
 */
private class FakeSupabaseClient : SupabaseClient {
    var authSignUpCalled = false
    var authSignInCalled = false
    var authSignOutCalled = false
    var volunteerProfileCreated = false
    var shouldReturnNullUser = false
    var shouldThrowOnSignUp = false
    var shouldThrowOnSignIn = false
    var shouldThrowOnSignOut = false
    
    private var currentUserId: String? = null
    private var hasSession = false

    override val auth: Auth = FakeAuth(this)
    override val postgrest: Postgrest = FakePostgrest(this)
    
    override fun close() {}
    
    fun setLoggedIn(loggedIn: Boolean, userId: String = "test-user-id") {
        hasSession = loggedIn
        currentUserId = if (loggedIn) userId else null
    }

    inner class FakeAuth(private val client: FakeSupabaseClient) : Auth {
        override val config: io.github.jan.supabase.gotrue.AuthConfig
            get() = throw NotImplementedError()
        
        override val pluginKey: String = "auth"
        
        override val sessionStatus: MutableStateFlow<SessionStatus> = 
            MutableStateFlow(SessionStatus.NotAuthenticated)

        suspend fun signUpWith(provider: io.github.jan.supabase.gotrue.providers.builtin.Email, 
                               block: io.github.jan.supabase.gotrue.providers.builtin.Email.Config.() -> Unit) {
            client.authSignUpCalled = true
            if (client.shouldThrowOnSignUp) {
                throw Exception("Signup failed")
            }
            if (!client.shouldReturnNullUser) {
                client.currentUserId = "new-user-id-${System.currentTimeMillis()}"
                client.hasSession = true
            }
        }

        suspend fun signInWith(provider: io.github.jan.supabase.gotrue.providers.builtin.Email,
                              block: io.github.jan.supabase.gotrue.providers.builtin.Email.Config.() -> Unit) {
            client.authSignInCalled = true
            if (client.shouldThrowOnSignIn) {
                throw Exception("Invalid credentials")
            }
            client.currentUserId = "test-user-id"
            client.hasSession = true
        }

        suspend fun signOut() {
            client.authSignOutCalled = true
            if (client.shouldThrowOnSignOut) {
                throw Exception("Network error during logout")
            }
            client.currentUserId = null
            client.hasSession = false
        }

        fun currentSessionOrNull(): UserSession? {
            return if (client.hasSession) {
                FakeUserSession(client.currentUserId ?: "test-user-id")
            } else null
        }

        fun currentUserOrNull(): UserInfo? {
            return if (client.currentUserId != null) {
                FakeUserInfo(client.currentUserId!!)
            } else null
        }
    }

    inner class FakePostgrest(private val client: FakeSupabaseClient) : Postgrest {
        override val config: io.github.jan.supabase.postgrest.PostgrestConfig
            get() = throw NotImplementedError()
        
        override val pluginKey: String = "postgrest"

        fun from(table: String): FakePostgrestQueryBuilder {
            return FakePostgrestQueryBuilder(client, table)
        }
    }

    class FakePostgrestQueryBuilder(
        private val client: FakeSupabaseClient,
        private val table: String
    ) : PostgrestQueryBuilder {
        override val schema: String = "public"
        
        suspend fun insert(data: Map<String, Any?>) {
            if (table == "volunteers") {
                if (client.shouldThrowOnSignUp) {
                    throw Exception("Database error")
                }
                client.volunteerProfileCreated = true
            }
        }
    }

    private class FakeUserSession(private val userId: String) : UserSession {
        override val accessToken: String = "fake-access-token"
        override val expiresAt: Long = System.currentTimeMillis() + 3600000
        override val expiresIn: Int = 3600
        override val providerRefreshToken: String? = null
        override val providerToken: String? = null
        override val refreshToken: String = "fake-refresh-token"
        override val tokenType: String = "bearer"
        override val user: UserInfo = FakeUserInfo(userId)
    }

    private class FakeUserInfo(override val id: String) : UserInfo {
        override val aud: String = "authenticated"
        override val confirmedAt: kotlinx.datetime.Instant? = null
        override val createdAt: kotlinx.datetime.Instant = kotlinx.datetime.Clock.System.now()
        override val email: String? = "test@example.com"
        override val emailConfirmedAt: kotlinx.datetime.Instant? = null
        override val identities: List<io.github.jan.supabase.gotrue.user.Identity>? = null
        override val lastSignInAt: kotlinx.datetime.Instant? = null
        override val phone: String? = null
        override val phoneConfirmedAt: kotlinx.datetime.Instant? = null
        override val role: String = "authenticated"
        override val updatedAt: kotlinx.datetime.Instant = kotlinx.datetime.Clock.System.now()
        override val userMetadata: io.kotlinx.serialization.json.JsonObject? = null
        override val appMetadata: io.kotlinx.serialization.json.JsonObject? = null
        override val factors: List<io.github.jan.supabase.gotrue.user.Factor>? = null
    }
}
