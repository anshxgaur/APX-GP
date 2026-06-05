package com.yourname.sra.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.Task
import com.yourname.sra.data.model.TaskUpdate
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.ProfileRepository
import com.yourname.sra.data.repository.TaskRepository
import com.yourname.sra.data.repository.TaskUpdateRepository
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
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val taskUpdateRepository: TaskUpdateRepository
) : ViewModel() {

    private val _tasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Loading)
    val tasksState: StateFlow<UiState<List<Task>>> = _tasksState.asStateFlow()

    private val _taskDetailState = MutableStateFlow<UiState<Task>>(UiState.Loading)
    val taskDetailState: StateFlow<UiState<Task>> = _taskDetailState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    private val _isCached = MutableStateFlow(false)
    val isCached: StateFlow<Boolean> = _isCached.asStateFlow()

    // Task updates state
    private val _taskUpdatesState = MutableStateFlow<UiState<List<TaskUpdate>>>(UiState.Loading)
    val taskUpdatesState: StateFlow<UiState<List<TaskUpdate>>> = _taskUpdatesState.asStateFlow()

    // Realtime subscription for task changes
    private val _taskChanges = MutableSharedFlow<PostgresAction>()
    val taskChanges: SharedFlow<PostgresAction> = _taskChanges.asSharedFlow()

    // Realtime subscription for task update changes
    private val _taskUpdateChanges = MutableSharedFlow<PostgresAction>()
    val taskUpdateChanges: SharedFlow<PostgresAction> = _taskUpdateChanges.asSharedFlow()

    // Current status filter for cached tasks observation
    private var currentStatusFilter: String? = null

    init {
        // Start realtime subscription to task changes
        viewModelScope.launch {
            taskRepository.subscribeTaskChanges().collect { action ->
                _taskChanges.emit(action)
            }
        }
        
        // Start realtime subscription to task update changes
        viewModelScope.launch {
            taskUpdateRepository.subscribeTaskUpdateChanges().collect { action ->
                _taskUpdateChanges.emit(action)
            }
        }
    }

    /**
     * Get open tasks from repository
     * Implements Requirement 7.2: Fetch all open tasks ordered by urgency descending
     */
    fun getOpenTasks() {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading
            _isCached.value = false
            currentStatusFilter = "open"

            val result = taskRepository.getOpenTasks()

            result.fold(
                onSuccess = { tasks ->
                    if (tasks.isEmpty()) {
                        _tasksState.value = UiState.Empty
                    } else {
                        _tasksState.value = UiState.Success(tasks)
                    }
                },
                onFailure = { e ->
                    // Show cached indicator if data came from cache
                    _isCached.value = true
                    _tasksState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load tasks"
                    )
                }
            )
        }
    }

    /**
     * Get user's tasks filtered by status
     * Implements Requirements 7.3, 7.4: Fetch ongoing and completed tasks for logged-in user
     */
    fun getMyTasks(status: String) {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading
            _isCached.value = false
            currentStatusFilter = status

            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _tasksState.value = UiState.Error("User not logged in")
                return@launch
            }

            val result = taskRepository.getMyTasks(userId, status)

            result.fold(
                onSuccess = { tasks ->
                    if (tasks.isEmpty()) {
                        _tasksState.value = UiState.Empty
                    } else {
                        _tasksState.value = UiState.Success(tasks)
                    }
                },
                onFailure = { e ->
                    // Show cached indicator if data came from cache
                    _isCached.value = true
                    _tasksState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load tasks"
                    )
                }
            )
        }
    }

    /**
     * Observe cached tasks from Room database for offline support
     * Implements Requirements 32.2, 32.3: Observe cached tasks flow from Room for offline support
     */
    fun observeCachedTasks(status: String) {
        viewModelScope.launch {
            currentStatusFilter = status
            taskRepository.getCachedTasksFlow(status).collect { cachedTasks ->
                if (_tasksState.value is UiState.Error || _tasksState.value is UiState.Empty) {
                    if (cachedTasks.isNotEmpty()) {
                        _isCached.value = true
                        _tasksState.value = UiState.Success(cachedTasks)
                    }
                }
            }
        }
    }

    /**
     * Get task detail by ID
     * Implements Requirement 8.1: Fetch complete task record by task ID
     */
    fun getTaskById(taskId: String) {
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

    /**
     * Load task detail - alias for getTaskById for backwards compatibility
     */
    fun loadTaskDetail(taskId: String) = getTaskById(taskId)

    /**
     * Accept an open task
     * Implements Requirement 9.1: Update task status to ongoing and assign to current user
     */
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
                    _actionState.value = UiState.Success("Task accepted")
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

    /**
     * Complete an ongoing task
     * Implements Requirement 10.1: Update task status to completed with completion note
     */
    fun completeTask(taskId: String, completionNote: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()

            val result = taskRepository.completeTask(taskId, completionNote)
            result.fold(
                onSuccess = {
                    // Increment tasks completed count for the user
                    userId?.let { profileRepository.incrementTasksCompleted(it) }
                    _actionState.value = UiState.Success("Task completed")
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

    /**
     * Update field notes for an ongoing task
     * Implements Requirement 11.1: Update task field_notes and updated_at timestamp
     */
    fun updateFieldNotes(taskId: String, notes: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            val result = taskRepository.updateFieldNotes(taskId, notes)
            result.fold(
                onSuccess = {
                    _actionState.value = UiState.Success("Notes saved")
                    loadTaskDetail(taskId) // Refresh detail
                },
                onFailure = { e ->
                    _actionState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to save notes"
                    )
                }
            )
        }
    }

    /**
     * Load task updates for a specific task
     * Implements Requirement 25.1, 25.2: Fetch all task updates for task_id ordered by created_at DESC
     */
    fun loadTaskUpdates(taskId: String) {
        viewModelScope.launch {
            _taskUpdatesState.value = UiState.Loading
            val result = taskUpdateRepository.getTaskUpdates(taskId)
            result.fold(
                onSuccess = { updates ->
                    if (updates.isEmpty()) {
                        _taskUpdatesState.value = UiState.Empty
                    } else {
                        _taskUpdatesState.value = UiState.Success(updates)
                    }
                },
                onFailure = { e ->
                    _taskUpdatesState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load updates"
                    )
                }
            )
        }
    }

    /**
     * Create a task update with optional photo
     * Implements Requirement 24.1, 24.2, 24.3: Create task update with text and optional photo
     */
    fun createTaskUpdate(taskId: String, updateText: String, photoUrl: String? = null) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _actionState.value = UiState.Error("User not logged in")
                return@launch
            }

            val taskUpdate = TaskUpdate(
                id = "", // Auto-generated by database
                taskId = taskId,
                volunteerId = userId,
                updateText = updateText,
                status = "ongoing", // Default status
                photoUrl = photoUrl,
                createdAt = "" // Auto-generated by database
            )

            val result = taskUpdateRepository.createTaskUpdate(taskUpdate)
            result.fold(
                onSuccess = {
                    _actionState.value = UiState.Success("Update posted")
                    loadTaskUpdates(taskId) // Refresh updates list
                },
                onFailure = { e ->
                    _actionState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to post update"
                    )
                }
            )
        }
    }

    /**
     * Upload photo for task update
     * Implements Requirement 24.1, 24.2: Upload photo with ImageUtils compression
     */
    suspend fun uploadUpdatePhoto(imageBytes: ByteArray, fileName: String): Result<String> {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            return Result.failure(IllegalStateException("User not logged in"))
        }
        return taskUpdateRepository.uploadUpdatePhoto(userId, imageBytes, fileName)
    }

    /**
     * Reset action state to empty
     */
    fun resetActionState() {
        _actionState.value = UiState.Empty
    }

    /**
     * Refresh tasks based on current filter
     * Used for realtime subscription updates
     */
    fun refreshCurrentTasks() {
        currentStatusFilter?.let { status ->
            if (status == "open") {
                getOpenTasks()
            } else {
                getMyTasks(status)
            }
        }
    }
}
