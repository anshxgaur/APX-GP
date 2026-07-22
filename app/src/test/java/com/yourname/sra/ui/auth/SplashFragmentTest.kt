package com.yourname.sra.ui.auth

import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.utils.UiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for SplashFragment session check functionality.
 * 
 * Tests verify Requirements 3.1, 3.2, 3.3:
 * - 3.1: Check for existing valid session on app startup
 * - 3.2: Navigate to dashboard when valid session exists
 * - 3.3: Navigate to login when no valid session exists
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SplashFragmentTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test: Session check when user is logged in
     * 
     * Validates Requirements:
     * - 3.1: Check for existing valid session
     * - 3.2: Navigate to dashboard when session exists
     * 
     * Expected Behavior:
     * 1. AuthRepository.isLoggedIn() returns true
     * 2. ViewModel emits UiState.Success(true)
     * 3. SplashFragment navigates to DashboardFragment
     */
    @Test
    fun `checkSession when user is logged in should emit success with true`() = runTest {
        // Given: User has valid session
        every { authRepository.isLoggedIn() } returns true
        every { authRepository.observeSessionStatus() } returns MutableStateFlow(true)
        
        authViewModel = AuthViewModel(authRepository)

        // When: Check session
        authViewModel.checkSession()
        advanceUntilIdle()

        // Then: Session state should be Success(true)
        val state = authViewModel.sessionState.value
        assertIs<UiState.Success<Boolean>>(state)
        assertEquals(true, state.data)
        
        // Verify isLoggedIn was called
        verify(exactly = 1) { authRepository.isLoggedIn() }
    }

    /**
     * Test: Session check when user is not logged in
     * 
     * Validates Requirements:
     * - 3.1: Check for existing valid session
     * - 3.3: Navigate to login when no session exists
     * 
     * Expected Behavior:
     * 1. AuthRepository.isLoggedIn() returns false
     * 2. ViewModel emits UiState.Success(false)
     * 3. SplashFragment navigates to LoginFragment
     */
    @Test
    fun `checkSession when user is not logged in should emit success with false`() = runTest {
        // Given: User has no valid session
        every { authRepository.isLoggedIn() } returns false
        every { authRepository.observeSessionStatus() } returns MutableStateFlow(false)
        
        authViewModel = AuthViewModel(authRepository)

        // When: Check session
        authViewModel.checkSession()
        advanceUntilIdle()

        // Then: Session state should be Success(false)
        val state = authViewModel.sessionState.value
        assertIs<UiState.Success<Boolean>>(state)
        assertEquals(false, state.data)
        
        // Verify isLoggedIn was called
        verify(exactly = 1) { authRepository.isLoggedIn() }
    }

    /**
     * Test: Session check when exception occurs
     * 
     * Validates error handling for session check failures.
     * 
     * Expected Behavior:
     * 1. AuthRepository.isLoggedIn() throws exception
     * 2. ViewModel catches exception and treats as not logged in
     * 3. ViewModel emits UiState.Success(false)
     * 4. SplashFragment navigates to LoginFragment
     */
    @Test
    fun `checkSession when exception occurs should emit success with false`() = runTest {
        // Given: Session check throws exception
        every { authRepository.isLoggedIn() } throws RuntimeException("Session check failed")
        every { authRepository.observeSessionStatus() } returns MutableStateFlow(false)
        
        authViewModel = AuthViewModel(authRepository)

        // When: Check session
        authViewModel.checkSession()
        advanceUntilIdle()

        // Then: Session state should be Success(false) - fail safe to login
        val state = authViewModel.sessionState.value
        assertIs<UiState.Success<Boolean>>(state)
        assertEquals(false, state.data)
        
        // Verify isLoggedIn was attempted
        verify(exactly = 1) { authRepository.isLoggedIn() }
    }

    /**
     * Test: Session status is initially Loading
     * 
     * Validates that session state starts in Loading state before check completes.
     * 
     * Expected Behavior:
     * 1. Before checkSession is called, state is Loading
     * 2. This prevents premature navigation
     */
    @Test
    fun `sessionState should initially be Loading`() {
        // Given: Fresh ViewModel
        every { authRepository.observeSessionStatus() } returns MutableStateFlow(true)
        authViewModel = AuthViewModel(authRepository)

        // Then: Initial state should be Loading
        val state = authViewModel.sessionState.value
        assertIs<UiState.Loading>(state)
    }

    /**
     * Test: Session observation for active session
     * 
     * Validates Requirement 3.4: Handle session expiration during app usage
     * 
     * Expected Behavior:
     * 1. observeSessionStatus() returns Flow<Boolean>
     * 2. ViewModel converts to StateFlow for UI observation
     * 3. Active session emits true
     */
    @Test
    fun `isSessionActive should emit true when session is active`() = runTest {
        // Given: Active session
        val sessionFlow = MutableStateFlow(true)
        every { authRepository.observeSessionStatus() } returns sessionFlow
        
        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // Then: isSessionActive should be true
        assertEquals(true, authViewModel.isSessionActive.value)
    }

    /**
     * Test: Session observation for inactive session
     * 
     * Validates Requirement 3.4: Handle session expiration during app usage
     * 
     * Expected Behavior:
     * 1. observeSessionStatus() returns Flow<Boolean>
     * 2. Expired session emits false
     * 3. MainActivity observes this and redirects to login
     */
    @Test
    fun `isSessionActive should emit false when session expires`() = runTest {
        // Given: Session becomes inactive
        val sessionFlow = MutableStateFlow(true)
        every { authRepository.observeSessionStatus() } returns sessionFlow
        
        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: Session expires
        sessionFlow.value = false
        advanceUntilIdle()

        // Then: isSessionActive should be false
        assertEquals(false, authViewModel.isSessionActive.value)
    }
}
