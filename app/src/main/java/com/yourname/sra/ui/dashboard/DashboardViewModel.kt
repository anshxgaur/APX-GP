package com.yourname.sra.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.DashboardData
import com.yourname.sra.data.model.Task
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.NotificationRepository
import com.yourname.sra.data.repository.ProfileRepository
import com.yourname.sra.data.repository.SurveyRepository
import com.yourname.sra.data.repository.TaskRepository
import com.yourname.sra.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val taskRepository: TaskRepository,
    private val surveyRepository: SurveyRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val dashboardState: StateFlow<UiState<DashboardData>> = _dashboardState.asStateFlow()

    private val _acceptTaskState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val acceptTaskState: StateFlow<UiState<Unit>> = _acceptTaskState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            try {
                val userId = authRepository.getCurrentUserId()
                    ?: throw Exception("User not logged in")

                // Load profile
                val profileResult = profileRepository.getProfile(userId)
                val volunteer = profileResult.getOrThrow()

                // Load ongoing task
                val myTasksResult = taskRepository.getMyTasks(userId, "ongoing")
                val ongoingTask = myTasksResult.getOrNull()?.firstOrNull()

                // Load open tasks (top 3 by urgency)
                val openTasksResult = taskRepository.getOpenTasks()
                val openTasks = openTasksResult.getOrNull()?.take(3) ?: emptyList()

                // Load recent surveys
                val surveysResult = surveyRepository.getRecentSurveys(userId, 2)
                val recentSurveys = surveysResult.getOrNull() ?: emptyList()

                // Load unread notification count
                val unreadResult = notificationRepository.getUnreadCount(userId)
                val unreadCount = unreadResult.getOrNull() ?: 0

                val dashboardData = DashboardData(
                    volunteer = volunteer,
                    ongoingTask = ongoingTask,
                    openTasks = openTasks,
                    recentSurveys = recentSurveys,
                    unreadNotificationCount = unreadCount
                )

                _dashboardState.value = UiState.Success(dashboardData)
            } catch (e: Exception) {
                _dashboardState.value = UiState.Error(
                    e.localizedMessage ?: "Failed to load dashboard data"
                )
            }
        }
    }

    fun acceptTask(task: Task) {
        viewModelScope.launch {
            _acceptTaskState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _acceptTaskState.value = UiState.Error("User not logged in")
                return@launch
            }

            val result = taskRepository.acceptTask(task.id, userId)
            result.fold(
                onSuccess = {
                    _acceptTaskState.value = UiState.Success(Unit)
                    loadDashboard() // Refresh dashboard
                },
                onFailure = { e ->
                    _acceptTaskState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to accept task"
                    )
                }
            )
        }
    }

    fun resetAcceptState() {
        _acceptTaskState.value = UiState.Empty
    }
}
