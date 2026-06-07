package com.yourname.sra.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.yourname.sra.data.model.Task

@Entity(tableName = "tasks")
@TypeConverters(StringListConverter::class)
data class TaskEntity(
    @PrimaryKey val id: String,
    val surveyId: String? = null,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val urgency: Int = 1,
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val requiredSkills: List<String> = emptyList(),
    val estimatedHours: Int = 1,
    val status: String = "open",
    val assignedVolunteer: String? = null,
    val createdBy: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val completionNote: String? = null,
    val fieldNotes: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun toTask(): Task = Task(
        id = id,
        surveyId = surveyId,
        title = title,
        description = description,
        category = category,
        urgency = urgency,
        locationName = locationName,
        latitude = latitude,
        longitude = longitude,
        requiredSkills = requiredSkills,
        estimatedHours = estimatedHours,
        status = status,
        assignedVolunteer = assignedVolunteer,
        createdBy = createdBy,
        startedAt = startedAt,
        completedAt = completedAt,
        completionNote = completionNote,
        fieldNotes = fieldNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromTask(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            surveyId = task.surveyId,
            title = task.title,
            description = task.description,
            category = task.category,
            urgency = task.urgency,
            locationName = task.locationName,
            latitude = task.latitude,
            longitude = task.longitude,
            requiredSkills = task.requiredSkills,
            estimatedHours = task.estimatedHours,
            status = task.status,
            assignedVolunteer = task.assignedVolunteer,
            createdBy = task.createdBy,
            startedAt = task.startedAt,
            completedAt = task.completedAt,
            completionNote = task.completionNote,
            fieldNotes = task.fieldNotes,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )
    }
}

class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
