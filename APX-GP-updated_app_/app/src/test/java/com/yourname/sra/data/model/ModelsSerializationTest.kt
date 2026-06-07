package com.yourname.sra.data.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for data model serialization/deserialization.
 * Validates: Requirements 4.1, 4.2, 4.3, 5.4, 7.5, 12.2, 14.2, 31.2
 */
class ModelsSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `Volunteer model serialization should map snake_case fields correctly`() {
        // Given
        val volunteer = Volunteer(
            id = "vol-123",
            fullName = "John Doe",
            email = "john@example.com",
            phone = "+1234567890",
            role = "volunteer",
            area = "Downtown",
            latitude = 34.0522,
            longitude = -118.2437,
            skills = listOf("first_aid", "search_rescue"),
            availability = "weekends",
            profilePhotoUrl = "https://example.com/photo.jpg",
            totalTasksCompleted = 5,
            totalHours = 20,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-02T00:00:00Z"
        )

        // When
        val jsonString = json.encodeToString(volunteer)
        val deserialized = json.decodeFromString<Volunteer>(jsonString)

        // Then
        assert(jsonString.contains("\"full_name\":\"John Doe\""))
        assert(jsonString.contains("\"profile_photo_url\":\"https://example.com/photo.jpg\""))
        assert(jsonString.contains("\"total_tasks_completed\":5"))
        assert(jsonString.contains("\"total_hours\":20"))
        assert(jsonString.contains("\"created_at\":\"2024-01-01T00:00:00Z\""))
        assert(jsonString.contains("\"updated_at\":\"2024-01-02T00:00:00Z\""))
        assertEquals(volunteer, deserialized)
    }

    @Test
    fun `Survey model serialization should map snake_case fields correctly`() {
        // Given
        val survey = Survey(
            id = "survey-123",
            volunteerId = "vol-123",
            category = "flood",
            severity = 4,
            peopleAffected = 150,
            description = "Severe flooding in residential area",
            locationName = "Oak Street",
            latitude = 34.0522,
            longitude = -118.2437,
            photoUrl = "https://example.com/survey.jpg",
            status = "pending",
            adminNotes = "Requires immediate attention",
            createdAt = "2024-01-01T12:00:00Z"
        )

        // When
        val jsonString = json.encodeToString(survey)
        val deserialized = json.decodeFromString<Survey>(jsonString)

        // Then
        assert(jsonString.contains("\"volunteer_id\":\"vol-123\""))
        assert(jsonString.contains("\"people_affected\":150"))
        assert(jsonString.contains("\"location_name\":\"Oak Street\""))
        assert(jsonString.contains("\"photo_url\":\"https://example.com/survey.jpg\""))
        assert(jsonString.contains("\"admin_notes\":\"Requires immediate attention\""))
        assert(jsonString.contains("\"created_at\":\"2024-01-01T12:00:00Z\""))
        assertEquals(survey, deserialized)
    }

    @Test
    fun `Task model serialization should map snake_case fields correctly`() {
        // Given
        val task = Task(
            id = "task-123",
            surveyId = "survey-123",
            title = "Distribute Emergency Supplies",
            description = "Deliver water and food to affected families",
            category = "relief",
            urgency = 5,
            locationName = "Oak Street",
            latitude = 34.0522,
            longitude = -118.2437,
            requiredSkills = listOf("driving", "organization"),
            estimatedHours = 4,
            status = "ongoing",
            assignedVolunteer = "vol-123",
            createdBy = "admin-001",
            startedAt = "2024-01-01T13:00:00Z",
            completedAt = null,
            completionNote = null,
            fieldNotes = "Team of 3 volunteers deployed",
            createdAt = "2024-01-01T12:00:00Z",
            updatedAt = "2024-01-01T13:00:00Z"
        )

        // When
        val jsonString = json.encodeToString(task)
        val deserialized = json.decodeFromString<Task>(jsonString)

        // Then
        assert(jsonString.contains("\"survey_id\":\"survey-123\""))
        assert(jsonString.contains("\"location_name\":\"Oak Street\""))
        assert(jsonString.contains("\"required_skills\":[\"driving\",\"organization\"]"))
        assert(jsonString.contains("\"estimated_hours\":4"))
        assert(jsonString.contains("\"assigned_volunteer\":\"vol-123\""))
        assert(jsonString.contains("\"created_by\":\"admin-001\""))
        assert(jsonString.contains("\"started_at\":\"2024-01-01T13:00:00Z\""))
        assert(jsonString.contains("\"field_notes\":\"Team of 3 volunteers deployed\""))
        assert(jsonString.contains("\"created_at\":\"2024-01-01T12:00:00Z\""))
        assert(jsonString.contains("\"updated_at\":\"2024-01-01T13:00:00Z\""))
        assertEquals(task, deserialized)
    }

    @Test
    fun `TaskUpdate model serialization should map snake_case fields correctly`() {
        // Given
        val taskUpdate = TaskUpdate(
            id = "update-123",
            taskId = "task-123",
            volunteerId = "vol-123",
            updateText = "Delivered supplies to 20 families",
            status = "ongoing",
            photoUrl = "https://example.com/update.jpg",
            createdAt = "2024-01-01T14:00:00Z"
        )

        // When
        val jsonString = json.encodeToString(taskUpdate)
        val deserialized = json.decodeFromString<TaskUpdate>(jsonString)

        // Then
        assert(jsonString.contains("\"task_id\":\"task-123\""))
        assert(jsonString.contains("\"volunteer_id\":\"vol-123\""))
        assert(jsonString.contains("\"update_text\":\"Delivered supplies to 20 families\""))
        assert(jsonString.contains("\"photo_url\":\"https://example.com/update.jpg\""))
        assert(jsonString.contains("\"created_at\":\"2024-01-01T14:00:00Z\""))
        assertEquals(taskUpdate, deserialized)
    }

    @Test
    fun `AreaRiskScore model serialization should map snake_case fields correctly`() {
        // Given
        val riskScore = AreaRiskScore(
            id = "risk-123",
            areaName = "Downtown District",
            latitude = 34.0522,
            longitude = -118.2437,
            riskScore = 7.5f,
            riskLevel = "high",
            contributingFactors = listOf("high_survey_density", "recent_flood_reports", "vulnerable_population"),
            calculatedAt = "2024-01-01T15:00:00Z"
        )

        // When
        val jsonString = json.encodeToString(riskScore)
        val deserialized = json.decodeFromString<AreaRiskScore>(jsonString)

        // Then
        assert(jsonString.contains("\"area_name\":\"Downtown District\""))
        assert(jsonString.contains("\"risk_score\":7.5"))
        assert(jsonString.contains("\"risk_level\":\"high\""))
        assert(jsonString.contains("\"contributing_factors\":[\"high_survey_density\",\"recent_flood_reports\",\"vulnerable_population\"]"))
        assert(jsonString.contains("\"calculated_at\":\"2024-01-01T15:00:00Z\""))
        assertEquals(riskScore, deserialized)
    }

    @Test
    fun `AppNotification model serialization should map snake_case fields correctly`() {
        // Given
        val notification = AppNotification(
            id = "notif-123",
            volunteerId = "vol-123",
            title = "New Task Assigned",
            message = "You have been assigned to distribute emergency supplies",
            type = "task_assigned",
            isRead = false,
            taskId = "task-123",
            createdAt = "2024-01-01T16:00:00Z"
        )

        // When
        val jsonString = json.encodeToString(notification)
        val deserialized = json.decodeFromString<AppNotification>(jsonString)

        // Then
        assert(jsonString.contains("\"volunteer_id\":\"vol-123\""))
        assert(jsonString.contains("\"is_read\":false"))
        assert(jsonString.contains("\"task_id\":\"task-123\""))
        assert(jsonString.contains("\"created_at\":\"2024-01-01T16:00:00Z\""))
        assertEquals(notification, deserialized)
    }

    @Test
    fun `Volunteer model deserialization should handle null optional fields`() {
        // Given - JSON with null optional fields
        val jsonString = """
            {
                "id": "vol-123",
                "full_name": "Jane Doe",
                "email": "jane@example.com",
                "phone": "+9876543210",
                "role": "volunteer",
                "area": "Uptown",
                "latitude": null,
                "longitude": null,
                "skills": [],
                "availability": "flexible",
                "profile_photo_url": null,
                "total_tasks_completed": 0,
                "total_hours": 0,
                "created_at": "2024-01-01T00:00:00Z",
                "updated_at": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()

        // When
        val volunteer = json.decodeFromString<Volunteer>(jsonString)

        // Then
        assertNotNull(volunteer)
        assertEquals("vol-123", volunteer.id)
        assertEquals("Jane Doe", volunteer.fullName)
        assertEquals(null, volunteer.latitude)
        assertEquals(null, volunteer.longitude)
        assertEquals(null, volunteer.profilePhotoUrl)
    }

    @Test
    fun `Survey model deserialization should handle null optional fields`() {
        // Given - JSON with null optional fields
        val jsonString = """
            {
                "id": "survey-456",
                "volunteer_id": "vol-456",
                "category": "medical",
                "severity": 3,
                "people_affected": 50,
                "description": "Medical emergency",
                "location_name": "Central Hospital",
                "latitude": null,
                "longitude": null,
                "photo_url": null,
                "status": "approved",
                "admin_notes": null,
                "created_at": "2024-01-02T00:00:00Z"
            }
        """.trimIndent()

        // When
        val survey = json.decodeFromString<Survey>(jsonString)

        // Then
        assertNotNull(survey)
        assertEquals("survey-456", survey.id)
        assertEquals(null, survey.latitude)
        assertEquals(null, survey.longitude)
        assertEquals(null, survey.photoUrl)
        assertEquals(null, survey.adminNotes)
    }

    @Test
    fun `Task model deserialization should handle null optional fields`() {
        // Given - JSON with null optional fields
        val jsonString = """
            {
                "id": "task-456",
                "survey_id": null,
                "title": "Open Task",
                "description": "New task without assignment",
                "category": "assessment",
                "urgency": 3,
                "location_name": "City Center",
                "latitude": null,
                "longitude": null,
                "required_skills": [],
                "estimated_hours": 2,
                "status": "open",
                "assigned_volunteer": null,
                "created_by": null,
                "started_at": null,
                "completed_at": null,
                "completion_note": null,
                "field_notes": null,
                "created_at": "2024-01-02T00:00:00Z",
                "updated_at": "2024-01-02T00:00:00Z"
            }
        """.trimIndent()

        // When
        val task = json.decodeFromString<Task>(jsonString)

        // Then
        assertNotNull(task)
        assertEquals("task-456", task.id)
        assertEquals(null, task.surveyId)
        assertEquals(null, task.latitude)
        assertEquals(null, task.longitude)
        assertEquals(null, task.assignedVolunteer)
        assertEquals(null, task.createdBy)
        assertEquals(null, task.startedAt)
        assertEquals(null, task.completedAt)
        assertEquals(null, task.completionNote)
        assertEquals(null, task.fieldNotes)
    }

    @Test
    fun `DashboardData should aggregate all required data`() {
        // Given
        val volunteer = Volunteer(
            id = "vol-123",
            fullName = "Test User",
            email = "test@example.com",
            phone = "+1234567890",
            role = "volunteer",
            area = "Downtown",
            skills = listOf("first_aid"),
            availability = "weekends",
            totalTasksCompleted = 10,
            totalHours = 40,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        val ongoingTask = Task(
            id = "task-ongoing",
            title = "Current Task",
            description = "Task in progress",
            category = "relief",
            urgency = 4,
            locationName = "Main Street",
            estimatedHours = 3,
            status = "ongoing",
            assignedVolunteer = "vol-123",
            createdAt = "2024-01-01T12:00:00Z",
            updatedAt = "2024-01-01T12:00:00Z"
        )

        val openTasks = listOf(
            Task(id = "task-1", title = "Task 1", description = "Open task 1", category = "assessment",
                urgency = 5, locationName = "Location 1", estimatedHours = 2, status = "open",
                createdAt = "2024-01-01T10:00:00Z", updatedAt = "2024-01-01T10:00:00Z"),
            Task(id = "task-2", title = "Task 2", description = "Open task 2", category = "relief",
                urgency = 3, locationName = "Location 2", estimatedHours = 4, status = "open",
                createdAt = "2024-01-01T11:00:00Z", updatedAt = "2024-01-01T11:00:00Z")
        )

        val recentSurveys = listOf(
            Survey(id = "survey-1", volunteerId = "vol-123", category = "flood", severity = 4,
                peopleAffected = 100, description = "Survey 1", locationName = "Area 1",
                status = "approved", createdAt = "2024-01-01T09:00:00Z"),
            Survey(id = "survey-2", volunteerId = "vol-123", category = "medical", severity = 3,
                peopleAffected = 50, description = "Survey 2", locationName = "Area 2",
                status = "pending", createdAt = "2024-01-01T08:00:00Z")
        )

        // When
        val dashboardData = DashboardData(
            volunteer = volunteer,
            ongoingTask = ongoingTask,
            openTasks = openTasks,
            recentSurveys = recentSurveys,
            unreadNotificationCount = 3
        )

        // Then
        assertNotNull(dashboardData)
        assertEquals(volunteer, dashboardData.volunteer)
        assertEquals(ongoingTask, dashboardData.ongoingTask)
        assertEquals(2, dashboardData.openTasks.size)
        assertEquals(2, dashboardData.recentSurveys.size)
        assertEquals(3, dashboardData.unreadNotificationCount)
    }

    @Test
    fun `Volunteer helper properties should return correct values`() {
        // Given
        val adminVolunteer = Volunteer(id = "1", email = "admin@test.com", role = "admin",
            fullName = "Admin", phone = "123", area = "Test", createdAt = "", updatedAt = "")
        val surveyorVolunteer = Volunteer(id = "2", email = "surveyor@test.com", role = "surveyor",
            fullName = "Surveyor", phone = "456", area = "Test", createdAt = "", updatedAt = "")
        val regularVolunteer = Volunteer(id = "3", email = "volunteer@test.com", role = "volunteer",
            fullName = "Volunteer", phone = "789", area = "Test", createdAt = "", updatedAt = "")

        // Then
        assert(adminVolunteer.isAdmin)
        assert(!adminVolunteer.isSurveyor)
        assert(!surveyorVolunteer.isAdmin)
        assert(surveyorVolunteer.isSurveyor)
        assert(!regularVolunteer.isAdmin)
        assert(!regularVolunteer.isSurveyor)
    }
}
