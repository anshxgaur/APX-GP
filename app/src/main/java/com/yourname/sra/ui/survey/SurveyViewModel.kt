package com.yourname.sra.ui.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.Survey
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.SurveyRepository
import com.yourname.sra.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // Current form location state
    var currentLatitude: Double? = null
    var currentLongitude: Double? = null
    var uploadedPhotoUrl: String? = null

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

    fun uploadPhoto(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _photoUploadState.value = UiState.Loading
            val result = surveyRepository.uploadPhoto(imageBytes, fileName)
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

    fun setLocation(lat: Double, lng: Double) {
        currentLatitude = lat
        currentLongitude = lng
    }

    fun resetSubmitState() {
        _submitState.value = UiState.Empty
    }
}
