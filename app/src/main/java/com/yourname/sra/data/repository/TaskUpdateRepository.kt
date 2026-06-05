package com.yourname.sra.data.repository

import android.util.Log
import com.yourname.sra.data.model.TaskUpdate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing task progress updates.
 * Handles creating task updates, fetching update history, uploading update photos,
 * and subscribing to real-time changes.
 * 
 * Requirements: 24.1, 24.2, 24.3, 25.1, 25.2, 25.3, 25.4, 26.1, 26.2, 26.3
 */
@Singleton
class TaskUpdateRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    /**
     * Creates a new task update record.
     * 
     * @param taskUpdate The task update to create
     * @return Result.success(Unit) on success, Result.failure(exception) on failure
     * 
     * Requirements: 24.1, 24.2, 24.3
     */
    suspend fun createTaskUpdate(taskUpdate: TaskUpdate): Result<Unit> {
        return try {
            supabaseClient.postgrest.from("task_updates").insert(
                mapOf(
                    "task_id" to taskUpdate.taskId,
                    "volunteer_id" to taskUpdate.volunteerId,
                    "update_text" to taskUpdate.updateText,
                    "status" to taskUpdate.status,
                    "photo_url" to taskUpdate.photoUrl
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create task update", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches all task updates for a given task, ordered by created_at descending.
     * 
     * @param taskId The task ID to fetch updates for
     * @return Result.success(List<TaskUpdate>) on success, Result.failure(exception) on failure
     * 
     * Requirements: 25.1, 25.2, 25.3, 25.4
     */
    suspend fun getTaskUpdates(taskId: String): Result<List<TaskUpdate>> {
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
            Log.e(TAG, "Failed to fetch task updates for task $taskId", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a photo to the survey-photos bucket for a task update.
     * 
     * @param userId The user ID for the storage path
     * @param imageBytes The image data as byte array
     * @param fileName The file name for the uploaded photo
     * @return Result.success(String) with public URL on success, Result.failure(exception) on failure
     * 
     * Requirements: 24.1, 24.2
     */
    suspend fun uploadUpdatePhoto(userId: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = supabaseClient.storage.from("survey-photos")
            val path = "$userId/$fileName"
            bucket.upload(path, imageBytes, upsert = true)
            val publicUrl = bucket.publicUrl(path)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload update photo", e)
            Result.failure(e)
        }
    }

    /**
     * Subscribes to real-time changes on the task_updates table.
     * Returns a Flow that emits PostgresAction events when task updates are inserted,
     * updated, or deleted.
     * 
     * @return Flow<PostgresAction> emitting real-time database changes
     * 
     * Requirements: 26.1, 26.2, 26.3
     */
    fun subscribeTaskUpdateChanges(): Flow<PostgresAction> {
        val channel = supabaseClient.channel("task-updates-changes")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "task_updates"
        }
    }

    companion object {
        private const val TAG = "TaskUpdateRepository"
    }
}
