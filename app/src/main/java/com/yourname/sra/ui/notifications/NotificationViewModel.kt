package com.yourname.sra.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.AppNotification
import com.yourname.sra.data.repository.AuthRepository
import com.yourname.sra.data.repository.NotificationRepository
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
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<UiState<List<AppNotification>>>(UiState.Loading)
    val notificationsState: StateFlow<UiState<List<AppNotification>>> = _notificationsState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _notificationChanges = MutableSharedFlow<PostgresAction>()
    val notificationChanges: SharedFlow<PostgresAction> = _notificationChanges.asSharedFlow()

    init {
        // Subscribe to realtime notification changes
        viewModelScope.launch {
            notificationRepository.subscribeNotificationChanges().collect { action ->
                _notificationChanges.emit(action)
                // Auto-refresh notifications on any change
                loadNotifications()
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = UiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _notificationsState.value = UiState.Error("User not logged in")
                return@launch
            }

            val result = notificationRepository.getNotifications(userId)
            result.fold(
                onSuccess = { notifications ->
                    if (notifications.isEmpty()) {
                        _notificationsState.value = UiState.Empty
                    } else {
                        _notificationsState.value = UiState.Success(notifications)
                    }
                    _unreadCount.value = notifications.count { !it.isRead }
                },
                onFailure = { e ->
                    _notificationsState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load notifications"
                    )
                }
            )
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val result = notificationRepository.markAsRead(notificationId)
            if (result.isSuccess) {
                loadNotifications() // Refresh list
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val result = notificationRepository.getUnreadCount(userId)
            result.onSuccess { count ->
                _unreadCount.value = count
            }
        }
    }
}
