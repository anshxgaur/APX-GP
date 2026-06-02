package com.yourname.sra.data.repository

import com.yourname.sra.data.model.TaskUpdate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskUpdateRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun addUpdate(update: TaskUpdate): Result<Unit> {
        return try {
            supabaseClient.postgrest.from("task_updates").insert(
                mapOf(
                    "task_id" to update.taskId,
                    "volunteer_id" to update.volunteerId,
                    "update_text" to update.updateText,
                    "status" to update.status,
                    "photo_url" to update.photoUrl
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpdatesForTask(taskId: String): Result<List<TaskUpdate>> {
        return try {
            val updates = supabaseClient.postgrest.from("task_updates")
                .select {
                    filter {
                        eq("task_id", taskId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<TaskUpdate>()
            Result.success(updates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Realtime: subscribe to task update changes
    fun subscribeTaskUpdateChanges(): Flow<PostgresAction> {
        val channel = supabaseClient.channel("task-updates-changes")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "task_updates"
        }
    }
}
