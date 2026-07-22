package com.yourname.sra.data.repository

import com.yourname.sra.data.local.TaskDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for AuthRepository error handling.
 * 
 * Validates Requirements:
 * - 2.3: Handle duplicate email error ("Email already registered")
 * - 2.6: Handle invalid credentials error ("Invalid email or password")
 * - 27.1, 27.2, 27.3: Handle network errors with appropriate messages
 */
class AuthRepositoryErrorHandlingTest {

    private lateinit var supabaseClient: SupabaseClient
    private lateinit var auth: Auth
    private lateinit var postgrest: Postgrest
    private lateinit var taskDao: TaskDao
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        supabaseClient = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        postgrest = mockk(relaxed = true)
        taskDao = mockk(relaxed = true)
        
        every { supabaseClient.auth } returns auth
        every { supabaseClient.postgrest } returns postgrest
        
        coEvery { taskDao.deleteAll() } returns Unit
        
        authRepository = AuthRepository(supabaseClient, taskDao)
    }

    // ===== SIGNUP ERROR HANDLING TESTS =====

    @Test
    fun `signUp handles duplicate email error correctly`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val exception = Exception("User already registered")
        
        coEvery { auth.signUpWith(Email, any()) } throws exception
        
        // Act
        val result = authRepository.signUp(
            email = email,
            password = password,
            fullName = "Test User",
            phone = "1234567890",
            area = "Test Area",
            skills = listOf("Skill1"),
            availability = "Full-time"
        )
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Email already registered", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signUp handles network error correctly`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val exception = IOException("Network connection failed")
        
        coEvery { auth.signUpWith(Email, any()) } throws exception
        
        // Act
        val result = authRepository.signUp(
            email = email,
            password = password,
            fullName = "Test User",
            phone = "1234567890",
            area = "Test Area",
            skills = listOf("Skill1"),
            availability = "Full-time"
        )
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signUp handles timeout error correctly`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val exception = SocketTimeoutException("Connection timed out")
        
        coEvery { auth.signUpWith(Email, any()) } throws exception
        
        // Act
        val result = authRepository.signUp(
            email = email,
            password = password,
            fullName = "Test User",
            phone = "1234567890",
            area = "Test Area",
            skills = listOf("Skill1"),
            availability = "Full-time"
        )
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Request timed out. Please try again.", result.exceptionOrNull()?.message)
    }

    // ===== LOGIN ERROR HANDLING TESTS =====

    @Test
    fun `login handles invalid credentials error correctly`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrongpassword"
        val exception = Exception("Invalid login credentials")
        
        coEvery { auth.signInWith(Email, any()) } throws exception
        
        // Act
        val result = authRepository.login(email, password)
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid email or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login handles network error correctly`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val exception = IOException("No network connection")
        
        coEvery { auth.signInWith(Email, any()) } throws exception
        
        // Act
        val result = authRepository.login(email, password)
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login handles timeout error correctly`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val exception = SocketTimeoutException("Request timeout")
        
        coEvery { auth.signInWith(Email, any()) } throws exception
        
        // Act
        val result = authRepository.login(email, password)
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Request timed out. Please try again.", result.exceptionOrNull()?.message)
    }

    // ===== LOGOUT ERROR HANDLING TESTS =====

    @Test
    fun `logout handles network error correctly`() = runTest {
        // Arrange
        val exception = IOException("Network connection failed")
        
        coEvery { auth.signOut() } throws exception
        
        // Act
        val result = authRepository.logout()
        
        // Assert
        assertTrue(result.isFailure)
        // Local session is cleared even when remote logout fails
        assertEquals("Failed to logout", result.exceptionOrNull()?.message)
        // Verify that local cache was still cleared
        coVerify { taskDao.deleteAll() }
    }

    @Test
    fun `logout handles timeout error correctly`() = runTest {
        // Arrange
        val exception = SocketTimeoutException("Connection timed out")
        
        coEvery { auth.signOut() } throws exception
        
        // Act
        val result = authRepository.logout()
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Failed to logout", result.exceptionOrNull()?.message)
        // Verify that local cache was still cleared
        coVerify { taskDao.deleteAll() }
    }

    // ===== SUCCESS CASE TESTS =====

    @Test
    fun `signUp succeeds with valid data`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val userId = "user-123"
        val mockUser = mockk<UserInfo> {
            every { id } returns userId
        }
        
        coEvery { auth.signUpWith(Email, any()) } just Runs
        every { auth.currentUserOrNull() } returns mockUser
        coEvery { postgrest.from("volunteers").insert(any<Map<String, Any>>()) } just Runs
        
        // Act
        val result = authRepository.signUp(
            email = email,
            password = password,
            fullName = "Test User",
            phone = "1234567890",
            area = "Test Area",
            skills = listOf("Skill1"),
            availability = "Full-time"
        )
        
        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `login succeeds with valid credentials`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        
        coEvery { auth.signInWith(Email, any()) } just Runs
        
        // Act
        val result = authRepository.login(email, password)
        
        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `logout succeeds when called`() = runTest {
        // Arrange
        coEvery { auth.signOut() } just Runs
        
        // Act
        val result = authRepository.logout()
        
        // Assert
        assertTrue(result.isSuccess)
    }
}
