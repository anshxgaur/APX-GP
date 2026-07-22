package com.yourname.sra.utils

object Constants {

    val SURVEY_CATEGORIES = listOf(
        "Food Shortage",
        "Medical Need",
        "Shelter Issue",
        "Education Gap",
        "Safety Concern",
        "Infrastructure",
        "Other"
    )

    val SKILL_OPTIONS = listOf(
        "Medical",
        "Education",
        "Construction",
        "Food Distribution",
        "Counseling",
        "Transportation",
        "IT Support",
        "Administration"
    )

    val AVAILABILITY_OPTIONS = listOf(
        "Flexible",
        "Weekdays Only",
        "Weekends Only",
        "Mornings Only",
        "Evenings Only"
    )

    object TaskStatus {
        const val OPEN = "open"
        const val ONGOING = "ongoing"
        const val COMPLETED = "completed"
        const val CANCELLED = "cancelled"
    }

    object SurveyStatus {
        const val PENDING = "pending"
        const val REVIEWED = "reviewed"
        const val ACTIONED = "actioned"
    }

    object NotificationType {
        const val GENERAL = "general"
        const val TASK_ASSIGNED = "task_assigned"
        const val TASK_COMPLETED = "task_completed"
        const val SURVEY_REVIEWED = "survey_reviewed"
    }
}
