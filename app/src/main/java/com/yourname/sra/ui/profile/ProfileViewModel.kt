package com.yourname.sra.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.Volunteer
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.ProfileRepository
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

    fun uploadPhoto(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _photoUploadState.value = UiState.Loading
            val result = profileRepository.uploadProfilePhoto(imageBytes, fileName)
            result.fold(
                onSuccess = { url ->
                    _photoUploadState.value = UiState.Success(url)
                },
                onFailure = { e ->
                    _photoUploadState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to upload photo"
                    )
                }
            )
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
}
