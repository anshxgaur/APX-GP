package com.yourname.sra.ui.survey

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.Survey
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.SurveyRepository
import com.yourname.sra.utils.ImageUtils
import com.yourname.sra.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val surveyRepository: SurveyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    private val _photoUploadState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val photoUploadState: StateFlow<UiState<String>> = _photoUploadState.asStateFlow()

    private val _mySurveysState = MutableStateFlow<UiState<List<Survey>>>(UiState.Loading)
    val mySurveysState: StateFlow<UiState<List<Survey>>> = _mySurveysState.asStateFlow()

    // Realtime subscription for survey changes
    private val _surveyChanges = MutableSharedFlow<PostgresAction>()
    val surveyChanges: SharedFlow<PostgresAction> = _surveyChanges.asSharedFlow()

    // Current form location state
    var currentLatitude: Double? = null
    var currentLongitude: Double? = null
    var uploadedPhotoUrl: String? = null

    init {
        // Subscribe to realtime survey changes
        viewModelScope.launch {
            surveyRepository.subscribeSurveyChanges().collect { action ->
                _surveyChanges.emit(action)
            }
        }
    }

    /**
     * Submit survey with input validation.
     * 
     * Validates:
     * - User authentication
     * - GPS coordinates captured
     * - Category, description, location_name non-empty (validated in UI)
     * - Severity 1-5, lat/lng ranges (validated in repository)
     * 
     * **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 28.1, 28.2, 28.3, 28.4, 28.5, 28.6**
     */
    fun submitSurvey(
        category: String,
        severity: Int,
        peopleAffected: Int,
        description: String,
        locationName: String
    ) {
        viewModelScope.launch {
            _submitState.value = UiState.Loading

            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _submitState.value = UiState.Error("User not logged in")
                return@launch
            }

            if (currentLatitude == null || currentLongitude == null) {
                _submitState.value = UiState.Error("GPS coordinates not captured")
                return@launch
            }

            val survey = Survey(
                volunteerId = userId,
                category = category,
                severity = severity,
                peopleAffected = peopleAffected,
                description = description,
                locationName = locationName,
                latitude = currentLatitude,
                longitude = currentLongitude,
                photoUrl = uploadedPhotoUrl
            )

            val result = surveyRepository.submitSurvey(survey)
            result.fold(
                onSuccess = {
                    _submitState.value = UiState.Success(Unit)
                    // Reset form state
                    currentLatitude = null
                    currentLongitude = null
                    uploadedPhotoUrl = null
                },
                onFailure = { e ->
                    _submitState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to submit survey"
                    )
                }
            )
        }
    }

    /**
     * Upload photo with compression using ImageUtils.compressImage().
     * 
     * This method accepts a Bitmap, compresses it to under 2MB using ImageUtils,
     * then uploads it to the survey-photos storage bucket.
     * 
     * **Validates: Requirements 5.1, 29.1, 29.2, 29.3, 29.4**
     */
    fun uploadPhoto(bitmap: Bitmap, fileName: String) {
        viewModelScope.launch {
            _photoUploadState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _photoUploadState.value = UiState.Error("User not logged in")
                return@launch
            }

            try {
                // Compress image using ImageUtils
                val compressedBytes = ImageUtils.compressImage(bitmap)
                
                // Upload the compressed photo
                val result = surveyRepository.uploadPhoto(userId, compressedBytes, fileName)
                result.fold(
                    onSuccess = { url ->
                        uploadedPhotoUrl = url
                        _photoUploadState.value = UiState.Success(url)
                    },
                    onFailure = { e ->
                        _photoUploadState.value = UiState.Error(
                            e.localizedMessage ?: "Failed to upload photo"
                        )
                    }
                )
            } catch (e: Exception) {
                _photoUploadState.value = UiState.Error(
                    e.localizedMessage ?: "Failed to compress image"
                )
            }
        }
    }

    /**
     * Upload photo from raw bytes (deprecated - use uploadPhoto(Bitmap) instead).
     * 
     * This method is kept for backward compatibility but should be replaced
     * with uploadPhoto(Bitmap) to ensure compression is applied.
     */
    @Deprecated("Use uploadPhoto(Bitmap, String) to ensure image compression")
    fun uploadPhoto(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _photoUploadState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _photoUploadState.value = UiState.Error("User not logged in")
                return@launch
            }
            val result = surveyRepository.uploadPhoto(userId, imageBytes, fileName)
            result.fold(
                onSuccess = { url ->
                    uploadedPhotoUrl = url
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

    /**
     * Load all surveys submitted by the logged-in user.
     * 
     * Fetches surveys ordered by created_at descending.
     * Updates mySurveysState with Loading, Success, Error, or Empty states.
     * 
     * **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**
     */
    fun loadMySurveys() {
        viewModelScope.launch {
            _mySurveysState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _mySurveysState.value = UiState.Error("User not logged in")
                return@launch
            }

            val result = surveyRepository.getMySurveys(userId)
            result.fold(
                onSuccess = { surveys ->
                    if (surveys.isEmpty()) {
                        _mySurveysState.value = UiState.Empty
                    } else {
                        _mySurveysState.value = UiState.Success(surveys)
                    }
                },
                onFailure = { e ->
                    _mySurveysState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load surveys"
                    )
                }
            )
        }
    }

    /**
     * Set GPS location coordinates for the survey.
     * 
     * Called when GPS coordinates are successfully captured.
     * 
     * **Validates: Requirements 5.7, 30.1, 30.2, 30.5**
     */
    fun setLocation(lat: Double, lng: Double) {
        currentLatitude = lat
        currentLongitude = lng
    }

    /**
     * Reset submit state to Empty.
     * 
     * Called after handling success or error states in the UI.
     */
    fun resetSubmitState() {
        _submitState.value = UiState.Empty
    }
}
