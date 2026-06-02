package com.yourname.sra.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Volunteer(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "volunteer",
    val area: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val skills: List<String> = emptyList(),
    val availability: String = "flexible",
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    @SerialName("total_tasks_completed") val totalTasksCompleted: Int = 0,
    @SerialName("total_hours") val totalHours: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
) {
    val isAdmin: Boolean get() = role == "admin"
    val isSurveyor: Boolean get() = role == "surveyor"
}

@Serializable
data class Survey(
    val id: String = "",
    @SerialName("volunteer_id") val volunteerId: String = "",
    val category: String = "",
    val severity: Int = 1,
    @SerialName("people_affected") val peopleAffected: Int = 0,
    val description: String = "",
    @SerialName("location_name") val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val status: String = "pending",
    @SerialName("admin_notes") val adminNotes: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class Task(
    val id: String = "",
    @SerialName("survey_id") val surveyId: String? = null,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val urgency: Int = 1,
    @SerialName("location_name") val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("required_skills") val requiredSkills: List<String> = emptyList(),
    @SerialName("estimated_hours") val estimatedHours: Int = 1,
    val status: String = "open",
    @SerialName("assigned_volunteer") val assignedVolunteer: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("completion_note") val completionNote: String? = null,
    @SerialName("field_notes") val fieldNotes: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class TaskUpdate(
    val id: String = "",
    @SerialName("task_id") val taskId: String = "",
    @SerialName("volunteer_id") val volunteerId: String = "",
    @SerialName("update_text") val updateText: String = "",
    val status: String = "ongoing",
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class AreaRiskScore(
    val id: String = "",
    @SerialName("area_name") val areaName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerialName("risk_score") val riskScore: Float = 0f,
    @SerialName("risk_level") val riskLevel: String = "low",
    @SerialName("contributing_factors") val contributingFactors: List<String> = emptyList(),
    @SerialName("calculated_at") val calculatedAt: String = ""
)

@Serializable
data class AppNotification(
    val id: String = "",
    @SerialName("volunteer_id") val volunteerId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("task_id") val taskId: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

data class DashboardData(
    val volunteer: Volunteer,
    val ongoingTask: Task?,
    val openTasks: List<Task>,
    val recentSurveys: List<Survey>,
    val unreadNotificationCount: Int
)
