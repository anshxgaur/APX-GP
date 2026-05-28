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

    suspend fun getNotifications(volunteerId: String): Result<List<AppNotification>> {
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
