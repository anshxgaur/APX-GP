package com.yourname.sra.ui.dashboard

import android.util.Log
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

    init {
        // Subscribe to task changes for auto-refresh
        viewModelScope.launch {
            try {
                taskRepository.subscribeTaskChanges().collect {
                    Log.d(TAG, "Task change detected, refreshing dashboard")
                    loadDashboard()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to task changes", e)
            }
        }

        // Subscribe to notification changes for auto-refresh
        viewModelScope.launch {
            try {
                notificationRepository.subscribeNotificationChanges().collect {
                    Log.d(TAG, "Notification change detected, refreshing dashboard")
                    loadDashboard()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to notification changes", e)
            }
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _dashboardState.value = UiState.Error("User not logged in")
                return@launch
            }

            // Track if any data fetch succeeds
            var successCount = 0
            var failureCount = 0

            // Fetch profile - this is critical for dashboard
            val profileResult = profileRepository.getProfile(userId)
            val volunteer = profileResult.getOrNull()
            if (profileResult.isSuccess) {
                successCount++
            } else {
                failureCount++
                Log.e(TAG, "Failed to load profile", profileResult.exceptionOrNull())
            }

            // Fetch ongoing task - non-critical, continue on failure
            val myTasksResult = taskRepository.getMyTasks(userId, "ongoing")
            val ongoingTask = myTasksResult.getOrNull()?.firstOrNull()
            if (myTasksResult.isSuccess) {
                successCount++
            } else {
                failureCount++
                Log.e(TAG, "Failed to load ongoing tasks", myTasksResult.exceptionOrNull())
            }

            // Fetch top 3 open tasks - non-critical, continue on failure
            val openTasksResult = taskRepository.getOpenTasks()
            val openTasks = openTasksResult.getOrNull()?.take(3) ?: emptyList()
            if (openTasksResult.isSuccess) {
                successCount++
            } else {
                failureCount++
                Log.e(TAG, "Failed to load open tasks", openTasksResult.exceptionOrNull())
            }

            // Fetch 2 recent surveys - non-critical, continue on failure
            val surveysResult = surveyRepository.getRecentSurveys(userId, 2)
            val recentSurveys = surveysResult.getOrNull() ?: emptyList()
            if (surveysResult.isSuccess) {
                successCount++
            } else {
                failureCount++
                Log.e(TAG, "Failed to load recent surveys", surveysResult.exceptionOrNull())
            }

            // Fetch unread notification count - non-critical, continue on failure
            val unreadResult = notificationRepository.getUnreadCount(userId)
            val unreadCount = unreadResult.getOrNull() ?: 0
            if (unreadResult.isSuccess) {
                successCount++
            } else {
                failureCount++
                Log.e(TAG, "Failed to load unread count", unreadResult.exceptionOrNull())
            }

            // Return error only if all fetches failed
            if (successCount == 0) {
                _dashboardState.value = UiState.Error("Failed to load dashboard data")
                return@launch
            }

            // If profile failed but others succeeded, still show error as profile is critical
            if (volunteer == null) {
                _dashboardState.value = UiState.Error("Failed to load profile data")
                return@launch
            }

            // Build dashboard data with whatever we successfully fetched
            val dashboardData = DashboardData(
                volunteer = volunteer,
                ongoingTask = ongoingTask,
                openTasks = openTasks,
                recentSurveys = recentSurveys,
                unreadNotificationCount = unreadCount
            )

            _dashboardState.value = UiState.Success(dashboardData)
            Log.d(TAG, "Dashboard loaded: $successCount succeeded, $failureCount failed")
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

    companion object {
        private const val TAG = "DashboardViewModel"
    }
}
