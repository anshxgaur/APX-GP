package com.yourname.sra.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.Task
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.ProfileRepository
import com.yourname.sra.data.repository.TaskRepository
import com.yourname.sra.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _tasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Loading)
    val tasksState: StateFlow<UiState<List<Task>>> = _tasksState.asStateFlow()

    private val _taskDetailState = MutableStateFlow<UiState<Task>>(UiState.Loading)
    val taskDetailState: StateFlow<UiState<Task>> = _taskDetailState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    private val _isCached = MutableStateFlow(false)
    val isCached: StateFlow<Boolean> = _isCached.asStateFlow()

    fun loadTasks(status: String) {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading
            _isCached.value = false

            val userId = authRepository.getCurrentUserId()

            val result = if (status == "open") {
                taskRepository.getOpenTasks()
            } else {
                if (userId == null) {
                    _tasksState.value = UiState.Error("User not logged in")
                    return@launch
                }
                taskRepository.getMyTasks(userId, status)
            }

            result.fold(
                onSuccess = { tasks ->
                    if (tasks.isEmpty()) {
                        _tasksState.value = UiState.Empty
                    } else {
                        _tasksState.value = UiState.Success(tasks)
                    }
                },
                onFailure = { e ->
                    _tasksState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load tasks"
                    )
                }
            )
        }
    }

    fun loadTaskDetail(taskId: String) {
        viewModelScope.launch {
            _taskDetailState.value = UiState.Loading
            val result = taskRepository.getTaskById(taskId)
            result.fold(
                onSuccess = { task ->
                    _taskDetailState.value = UiState.Success(task)
                },
                onFailure = { e ->
                    _taskDetailState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load task details"
                    )
                }
            )
        }
    }

    fun acceptTask(taskId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _actionState.value = UiState.Error("User not logged in")
                return@launch
            }

            val result = taskRepository.acceptTask(taskId, userId)
            result.fold(
                onSuccess = {
                    _actionState.value = UiState.Success("accepted")
                    loadTaskDetail(taskId) // Refresh detail
                },
                onFailure = { e ->
                    _actionState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to accept task"
                    )
                }
            )
        }
    }

    fun completeTask(taskId: String, note: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()

            val result = taskRepository.completeTask(taskId, note)
            result.fold(
                onSuccess = {
                    // Increment tasks completed count
                    userId?.let { profileRepository.incrementTasksCompleted(it) }
                    _actionState.value = UiState.Success("completed")
                    loadTaskDetail(taskId) // Refresh detail
                },
                onFailure = { e ->
                    _actionState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to complete task"
                    )
                }
            )
        }
    }

    fun updateFieldNotes(taskId: String, notes: String) {
        viewModelScope.launch {
            taskRepository.updateFieldNotes(taskId, notes)
        }
    }

    fun resetActionState() {
        _actionState.value = UiState.Empty
    }
}
