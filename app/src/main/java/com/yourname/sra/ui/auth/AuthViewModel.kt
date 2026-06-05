package com.yourname.sra.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val signUpState: StateFlow<UiState<Unit>> = _signUpState.asStateFlow()

    private val _sessionState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val sessionState: StateFlow<UiState<Boolean>> = _sessionState.asStateFlow()

    // Observe session status to handle expiration during app usage
    val isSessionActive: StateFlow<Boolean> = authRepository.observeSessionStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun checkSession() {
        viewModelScope.launch {
            try {
                val isLoggedIn = authRepository.isLoggedIn()
                _sessionState.value = UiState.Success(isLoggedIn)
            } catch (e: Exception) {
                _sessionState.value = UiState.Success(false)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    _loginState.value = UiState.Success(Unit)
                },
                onFailure = { exception ->
                    _loginState.value = UiState.Error(
                        exception.localizedMessage ?: "Login failed. Please try again."
                    )
                }
            )
        }
    }

    fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        area: String,
        skills: List<String>,
        availability: String
    ) {
        viewModelScope.launch {
            _signUpState.value = UiState.Loading
            val result = authRepository.signUp(
                email = email,
                password = password,
                fullName = fullName,
                phone = phone,
                area = area,
                skills = skills,
                availability = availability
            )
            result.fold(
                onSuccess = {
                    _signUpState.value = UiState.Success(Unit)
                },
                onFailure = { exception ->
                    _signUpState.value = UiState.Error(
                        exception.localizedMessage ?: "Registration failed. Please try again."
                    )
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = authRepository.logout()
            result.fold(
                onSuccess = {
                    _loginState.value = UiState.Success(Unit)
                },
                onFailure = { exception ->
                    _loginState.value = UiState.Error(
                        exception.localizedMessage ?: "Logout failed. Please try again."
                    )
                }
            )
        }
    }

    fun resetLoginState() {
        _loginState.value = UiState.Empty
    }

    fun resetSignUpState() {
        _signUpState.value = UiState.Empty
    }
}
