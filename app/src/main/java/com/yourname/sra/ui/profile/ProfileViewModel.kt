package com.yourname.sra.ui.profile

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.Volunteer
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.ProfileRepository
import com.yourname.sra.utils.ImageUtils
import com.yourname.sra.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<Volunteer>>(UiState.Loading)
    val profileState: StateFlow<UiState<Volunteer>> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val updateState: StateFlow<UiState<Unit>> = _updateState.asStateFlow()

    private val _photoUploadState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val photoUploadState: StateFlow<UiState<String>> = _photoUploadState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _profileState.value = UiState.Error("User not logged in")
                return@launch
            }

            val result = profileRepository.getProfile(userId)
            result.fold(
                onSuccess = { volunteer ->
                    _profileState.value = UiState.Success(volunteer)
                },
                onFailure = { e ->
                    _profileState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load profile"
                    )
                }
            )
        }
    }

    fun updateProfile(volunteer: Volunteer) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = profileRepository.updateProfile(volunteer)
            result.fold(
                onSuccess = {
                    _updateState.value = UiState.Success(Unit)
                    loadProfile() // Refresh
                },
                onFailure = { e ->
                    _updateState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to update profile"
                    )
                }
            )
        }
    }

    fun uploadProfilePhoto(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _photoUploadState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _photoUploadState.value = UiState.Error("User not logged in")
                return@launch
            }
            
            // Step 1: Upload photo to storage
            val uploadResult = profileRepository.uploadProfilePhoto(userId, imageBytes, fileName)
            uploadResult.fold(
                onSuccess = { url ->
                    // Step 2: Update profile with the new photo URL
                    val updateResult = profileRepository.updateProfilePhotoUrl(userId, url)
                    updateResult.fold(
                        onSuccess = {
                            _photoUploadState.value = UiState.Success(url)
                            loadProfile() // Refresh profile to show new photo
                        },
                        onFailure = { e ->
                            _photoUploadState.value = UiState.Error(
                                e.localizedMessage ?: "Failed to update profile with photo"
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _photoUploadState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to upload photo"
                    )
                }
            )
        }
    }

    /**
     * Upload profile photo with automatic compression using ImageUtils.
     * Compresses bitmap to under 2MB before uploading.
     * 
     * @param bitmap The bitmap to upload
     * @param fileName The file name to use for storage
     * 
     * Requirements: 18.1, 18.2, 18.3, 18.4, 18.5
     */
    fun uploadProfilePhoto(bitmap: Bitmap, fileName: String) {
        viewModelScope.launch {
            _photoUploadState.value = UiState.Loading
            
            try {
                // Compress image using ImageUtils
                val compressedBytes = ImageUtils.compressImage(bitmap)
                
                // Upload the compressed photo
                uploadProfilePhoto(compressedBytes, fileName)
            } catch (e: IllegalStateException) {
                // Image too large after compression
                _photoUploadState.value = UiState.Error("Image file too large")
            } catch (e: Exception) {
                _photoUploadState.value = UiState.Error(
                    e.localizedMessage ?: "Failed to compress image"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading
            val result = authRepository.logout()
            result.fold(
                onSuccess = {
                    _logoutState.value = UiState.Success(Unit)
                },
                onFailure = { e ->
                    _logoutState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to logout"
                    )
                }
            )
        }
    }

    fun resetUpdateState() {
        _updateState.value = UiState.Empty
    }

    fun resetPhotoUploadState() {
        _photoUploadState.value = UiState.Empty
    }
}
