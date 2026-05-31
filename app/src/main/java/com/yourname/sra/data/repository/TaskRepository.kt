package com.yourname.sra.data.repository

import com.yourname.sra.data.local.TaskDao
import com.yourname.sra.data.local.TaskEntity
import com.yourname.sra.data.model.Task
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val taskDao: TaskDao
) {

    private val isMockMode = com.yourname.sra.BuildConfig.SUPABASE_URL.contains("placeholder")

    private val mockTasksList = java.util.concurrent.CopyOnWriteArrayList<Task>().apply {
        add(
            Task(
                id = "mock-task-1",
                title = "Deliver Emergency Medical Supplies",
                description = "Urgent delivery of first-aid kits and insulin to the Community Center clinic.",
                category = "Medical",
                urgency = 3,
                locationName = "Downtown Community Center",
                latitude = 37.7749,
                longitude = -122.4194,
                requiredSkills = listOf("First Aid", "Driving"),
                estimatedHours = 2,
                status = "ongoing",
                assignedVolunteer = "mock-volunteer-id",
                createdAt = "2026-05-30T10:00:00Z"
            )
        )
        add(
            Task(
                id = "mock-task-2",
                title = "Debris Clearance on Main St",
                description = "Clear fallen branches and minor debris blocking the north lane of Main St.",
                category = "Infrastructure",
                urgency = 2,
                locationName = "120 Main Street",
                latitude = 37.7849,
                longitude = -122.4294,
                requiredSkills = listOf("Physical Labor"),
                estimatedHours = 3,
                status = "open",
                createdAt = "2026-05-30T11:00:00Z"
            )
        )
        add(
            Task(
                id = "mock-task-3",
                title = "Elderly Care Food Delivery",
                description = "Distribute hot meals to elderly residents at the Silver Care Home.",
                category = "Food Distribution",
                urgency = 1,
                locationName = "Silver Care Home",
                latitude = 37.7649,
                longitude = -122.4094,
                requiredSkills = listOf("Empathy", "Driving"),
                estimatedHours = 1,
                status = "open",
                createdAt = "2026-05-30T12:00:00Z"
            )
        )
    }

    suspend fun getOpenTasks(): Result<List<Task>> {
        if (isMockMode) {
            return Result.success(mockTasksList.filter { it.status == "open" })
        }
        return try {
            val tasks = supabaseClient.postgrest.from("tasks")
                .select {
                    filter {
                        eq("status", "open")
                    }
                    order("urgency", Order.DESCENDING)
                }
                .decodeList<Task>()

            // Cache to Room
            taskDao.deleteByStatus("open")
            taskDao.insertAll(tasks.map { TaskEntity.fromTask(it) })

            Result.success(tasks)
        } catch (e: Exception) {
            // Fallback to cached data
            val cached = getCachedTasksByStatus("open")
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getMyTasks(volunteerId: String, status: String): Result<List<Task>> {
        if (isMockMode) {
            return Result.success(mockTasksList.filter { it.status == status && it.assignedVolunteer == volunteerId })
        }
        return try {
            val tasks = supabaseClient.postgrest.from("tasks")
                .select {
                    filter {
                        eq("status", status)
                        eq("assigned_volunteer", volunteerId)
                    }
                    order("urgency", Order.DESCENDING)
                }
                .decodeList<Task>()

            // Cache to Room
            val entities = tasks.map { TaskEntity.fromTask(it) }
            taskDao.insertAll(entities)

            Result.success(tasks)
        } catch (e: Exception) {
            val cached = getCachedTasksByStatusAndVolunteer(status, volunteerId)
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getTaskById(taskId: String): Result<Task> {
        if (isMockMode) {
            val task = mockTasksList.find { it.id == taskId }
            return if (task != null) Result.success(task) else Result.failure(Exception("Task not found"))
        }
        return try {
            val task = supabaseClient.postgrest.from("tasks")
                .select {
                    filter {
                        eq("id", taskId)
                    }
                    limit(1)
                }
                .decodeSingle<Task>()

            // Cache
            taskDao.insertAll(listOf(TaskEntity.fromTask(task)))

            Result.success(task)
        } catch (e: Exception) {
            val cached = taskDao.getById(taskId)
            if (cached != null) {
                Result.success(cached.toTask())
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun acceptTask(taskId: String, volunteerId: String): Result<Unit> {
        if (isMockMode) {
            val index = mockTasksList.indexOfFirst { it.id == taskId }
            if (index != -1) {
                val task = mockTasksList[index]
                mockTasksList[index] = task.copy(
                    status = "ongoing",
                    assignedVolunteer = volunteerId,
                    startedAt = Instant.now().toString()
                )
            }
            return Result.success(Unit)
        }
        return try {
            val now = Instant.now().toString()
            supabaseClient.postgrest.from("tasks").update(
                {
                    set("status", "ongoing")
                    set("assigned_volunteer", volunteerId)
                    set("started_at", now)
                    set("updated_at", now)
                }
            ) {
                filter {
                    eq("id", taskId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeTask(taskId: String, note: String): Result<Unit> {
        if (isMockMode) {
            val index = mockTasksList.indexOfFirst { it.id == taskId }
            if (index != -1) {
                val task = mockTasksList[index]
                mockTasksList[index] = task.copy(
                    status = "completed",
                    completedAt = Instant.now().toString(),
                    completionNote = note
                )
            }
            return Result.success(Unit)
        }
        return try {
            val now = Instant.now().toString()
            supabaseClient.postgrest.from("tasks").update(
                {
                    set("status", "completed")
                    set("completed_at", now)
                    set("completion_note", note)
                    set("updated_at", now)
                }
            ) {
                filter {
                    eq("id", taskId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFieldNotes(taskId: String, notes: String): Result<Unit> {
        if (isMockMode) {
            val index = mockTasksList.indexOfFirst { it.id == taskId }
            if (index != -1) {
                val task = mockTasksList[index]
                mockTasksList[index] = task.copy(fieldNotes = notes)
            }
            return Result.success(Unit)
        }
        return try {
            supabaseClient.postgrest.from("tasks").update(
                {
                    set("field_notes", notes)
                    set("updated_at", Instant.now().toString())
                }
            ) {
                filter {
                    eq("id", taskId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCachedTasksByStatus(status: String): List<Task> {
        return try {
            taskDao.getByStatus(status).first().map { it.toTask() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getCachedTasksByStatusAndVolunteer(
        status: String,
        volunteerId: String
    ): List<Task> {
        return try {
            taskDao.getByStatusAndVolunteer(status, volunteerId).first().map { it.toTask() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCachedTasksFlow(status: String): Flow<List<Task>> {
        return taskDao.getByStatus(status).map { entities ->
            entities.map { it.toTask() }
        }
    }
}
