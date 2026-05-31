package com.yourname.sra.data.repository

import com.yourname.sra.data.model.AppNotification
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    private val isMockMode = com.yourname.sra.BuildConfig.SUPABASE_URL.contains("placeholder")

    private val mockNotificationsList = java.util.concurrent.CopyOnWriteArrayList<AppNotification>().apply {
        add(
            AppNotification(
                id = "mock-notif-1",
                volunteerId = "mock-volunteer-id",
                title = "New Task Assigned",
                message = "You have been assigned to: Deliver Emergency Medical Supplies.",
                type = "task_assigned",
                isRead = false,
                taskId = "mock-task-1",
                createdAt = "2026-05-30T10:05:00Z"
            )
        )
        add(
            AppNotification(
                id = "mock-notif-2",
                volunteerId = "mock-volunteer-id",
                title = "Welcome Volunteer!",
                message = "Thank you for registering with the Smart Resource Allocation system.",
                type = "general",
                isRead = true,
                createdAt = "2026-05-30T09:00:00Z"
            )
        )
    }

    suspend fun getNotifications(volunteerId: String): Result<List<AppNotification>> {
        if (isMockMode) {
            return Result.success(mockNotificationsList)
        }
        return try {
            val notifications = supabaseClient.postgrest.from("notifications")
                .select {
                    filter {
                        eq("volunteer_id", volunteerId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<AppNotification>()
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        if (isMockMode) {
            val index = mockNotificationsList.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                val notif = mockNotificationsList[index]
                mockNotificationsList[index] = notif.copy(isRead = true)
            }
            return Result.success(Unit)
        }
        return try {
            supabaseClient.postgrest.from("notifications").update(
                {
                    set("is_read", true)
                }
            ) {
                filter {
                    eq("id", notificationId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(volunteerId: String): Result<Int> {
        if (isMockMode) {
            return Result.success(mockNotificationsList.count { !it.isRead })
        }
        return try {
            val notifications = supabaseClient.postgrest.from("notifications")
                .select {
                    filter {
                        eq("volunteer_id", volunteerId)
                        eq("is_read", false)
                    }
                }
                .decodeList<AppNotification>()
            Result.success(notifications.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
