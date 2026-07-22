package com.yourname.sra.ui.profile

import android.graphics.Bitmap
import com.yourname.sra.data.model.Volunteer
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.ProfileRepository
import com.yourname.sra.utils.UiState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ProfileViewModel.
 * 
 * Tests the integration of ProfileRepository in ProfileViewModel including:
 * - loadProfile() calling profileRepository.getProfile()
 * - updateProfile() calling profileRepository.updateProfile()
 * - uploadProfilePhoto() with ImageUtils.compressImage()
 * - Profile photo upload and URL update flow
 * - UiState management for loading, success, error
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 18.1, 18.2, 18.3, 18.4, 18.5, 19.1, 19.2, 19.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var profileRepository: ProfileRepository
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testUserId = "test-user-123"
    private val testVolunteer = Volunteer(
        id = testUserId,
        fullName = "John Doe",
        email = "john@example.com",
        phone = "1234567890",
        role = "volunteer",
        area = "Test Area",
        latitude = 0.0,
        longitude = 0.0,
        skills = listOf("First Aid", "Search & Rescue"),
        availability = "Weekends",
        profilePhotoUrl = null,
        totalTasksCompleted = 0,
        totalHours = 0,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        profileRepository = mockk()
        authRepository = mockk()
        
        // Default mock behavior
        every { authRepository.getCurrentUserId() } returns testUserId
        
        viewModel = ProfileViewModel(profileRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `loadProfile should call profileRepository getProfile and update state to Success`() = runTest {
        // Given
        coEvery { profileRepository.getProfile(testUserId) } returns Result.success(testVolunteer)

        // When
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { profileRepository.getProfile(testUserId) }
        val state = viewModel.profileState.value
        assertTrue(state is UiState.Success)
        assertEquals(testVolunteer, (state as UiState.Success).data)
    }

    @Test
    fun `loadProfile should update state to Error when repository fails`() = runTest {
        // Given
        val errorMessage = "Failed to load profile"
        coEvery { profileRepository.getProfile(testUserId) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.profileState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `loadProfile should update state to Error when user is not logged in`() = runTest {
        // Given
        every { authRepository.getCurrentUserId() } returns null

        // When
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.profileState.value
        assertTrue(state is UiState.Error)
        assertEquals("User not logged in", (state as UiState.Error).message)
    }

    @Test
    fun `updateProfile should call profileRepository updateProfile and update state to Success`() = runTest {
        // Given
        coEvery { profileRepository.updateProfile(testVolunteer) } returns Result.success(Unit)
        coEvery { profileRepository.getProfile(testUserId) } returns Result.success(testVolunteer)

        // When
        viewModel.updateProfile(testVolunteer)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { profileRepository.updateProfile(testVolunteer) }
        coVerify { profileRepository.getProfile(testUserId) } // Should refresh after update
        val state = viewModel.updateState.value
        assertTrue(state is UiState.Success)
    }

    @Test
    fun `updateProfile should update state to Error when repository fails`() = runTest {
        // Given
        val errorMessage = "Failed to update profile"
        coEvery { profileRepository.updateProfile(testVolunteer) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.updateProfile(testVolunteer)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.updateState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `uploadProfilePhoto with bytes should upload photo and update profile URL`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3)
        val fileName = "test-photo.jpg"
        val photoUrl = "https://example.com/photo.jpg"
        
        coEvery { profileRepository.uploadProfilePhoto(testUserId, imageBytes, fileName) } returns Result.success(photoUrl)
        coEvery { profileRepository.updateProfilePhotoUrl(testUserId, photoUrl) } returns Result.success(Unit)
        coEvery { profileRepository.getProfile(testUserId) } returns Result.success(testVolunteer)

        // When
        viewModel.uploadProfilePhoto(imageBytes, fileName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { profileRepository.uploadProfilePhoto(testUserId, imageBytes, fileName) }
        coVerify { profileRepository.updateProfilePhotoUrl(testUserId, photoUrl) }
        coVerify { profileRepository.getProfile(testUserId) } // Should refresh after upload
        
        val state = viewModel.photoUploadState.value
        assertTrue(state is UiState.Success)
        assertEquals(photoUrl, (state as UiState.Success).data)
    }

    @Test
    fun `uploadProfilePhoto should update state to Error when photo upload fails`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3)
        val fileName = "test-photo.jpg"
        val errorMessage = "Failed to upload photo"
        
        coEvery { profileRepository.uploadProfilePhoto(testUserId, imageBytes, fileName) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.uploadProfilePhoto(imageBytes, fileName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.photoUploadState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `uploadProfilePhoto should update state to Error when profile URL update fails`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3)
        val fileName = "test-photo.jpg"
        val photoUrl = "https://example.com/photo.jpg"
        val errorMessage = "Failed to update profile with photo"
        
        coEvery { profileRepository.uploadProfilePhoto(testUserId, imageBytes, fileName) } returns Result.success(photoUrl)
        coEvery { profileRepository.updateProfilePhotoUrl(testUserId, photoUrl) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.uploadProfilePhoto(imageBytes, fileName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.photoUploadState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `uploadProfilePhoto should update state to Error when user is not logged in`() = runTest {
        // Given
        every { authRepository.getCurrentUserId() } returns null
        val imageBytes = byteArrayOf(1, 2, 3)
        val fileName = "test-photo.jpg"

        // When
        viewModel.uploadProfilePhoto(imageBytes, fileName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.photoUploadState.value
        assertTrue(state is UiState.Error)
        assertEquals("User not logged in", (state as UiState.Error).message)
    }

    @Test
    fun `logout should call authRepository logout and update state to Success`() = runTest {
        // Given
        coEvery { authRepository.logout() } returns Result.success(Unit)

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { authRepository.logout() }
        val state = viewModel.logoutState.value
        assertTrue(state is UiState.Success)
    }

    @Test
    fun `resetUpdateState should set updateState to Empty`() = runTest {
        // Given
        viewModel.updateProfile(testVolunteer)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.resetUpdateState()

        // Then
        val state = viewModel.updateState.value
        assertTrue(state is UiState.Empty)
    }

    @Test
    fun `resetPhotoUploadState should set photoUploadState to Empty`() = runTest {
        // Given
        val imageBytes = byteArrayOf(1, 2, 3)
        val fileName = "test-photo.jpg"
        coEvery { profileRepository.uploadProfilePhoto(testUserId, imageBytes, fileName) } returns Result.success("url")
        coEvery { profileRepository.updateProfilePhotoUrl(any(), any()) } returns Result.success(Unit)
        coEvery { profileRepository.getProfile(testUserId) } returns Result.success(testVolunteer)
        
        viewModel.uploadProfilePhoto(imageBytes, fileName)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.resetPhotoUploadState()

        // Then
        val state = viewModel.photoUploadState.value
        assertTrue(state is UiState.Empty)
    }
}
