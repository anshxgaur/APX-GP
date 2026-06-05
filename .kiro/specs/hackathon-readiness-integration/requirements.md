# Requirements Document

## Introduction

This document specifies the requirements for making the APX-GP (Smart Resource Allocation) Android application fully functional and hackathon-ready. The application connects volunteers with disaster relief tasks using Supabase backend services. The backend is complete and verified with authentication, database tables, RLS policies, realtime subscriptions, and storage buckets configured. This specification focuses on finalizing the Android app integration to ensure all features work end-to-end without mock data.

## Glossary

- **Application**: The APX-GP Android mobile application
- **Supabase_Client**: The initialized Supabase SDK client with Auth, Postgrest, Storage, and Realtime modules
- **Auth_System**: The Supabase authentication system handling user signup, login, logout, and session management
- **Repository_Layer**: Data access layer that communicates with Supabase backend services
- **UI_Layer**: Presentation layer including Fragments, ViewModels, and Compose components
- **Local_Properties**: Configuration file containing Supabase credentials (URL and anon key) excluded from version control
- **ML_Bridge**: Data integration layer enabling machine learning systems to read survey data and write risk scores
- **Realtime_Subscription**: Live data synchronization mechanism that notifies the app when database records change
- **Storage_Bucket**: Supabase storage container for user-uploaded images (survey-photos, profile-photos)
- **Session_Persistence**: Mechanism to maintain user authentication state across app restarts

## Requirements

### Requirement 1: Secure Credential Management

**User Story:** As a developer, I want credentials stored securely outside the codebase, so that sensitive keys are not exposed in version control

#### Acceptance Criteria

1. THE Application SHALL read SUPABASE_URL from local.properties BuildConfig at runtime
2. THE Application SHALL read SUPABASE_ANON_KEY from local.properties BuildConfig at runtime
3. THE Application SHALL fail initialization with a descriptive error WHEN local.properties is missing or incomplete
4. THE Application SHALL NOT contain hardcoded Supabase credentials in any source file
5. THE Application SHALL NOT log sensitive credentials (SUPABASE_ANON_KEY, user passwords, tokens) to system output

### Requirement 2: Authentication Flow Completion

**User Story:** As a volunteer, I want to create an account and log in, so that I can access the application features

#### Acceptance Criteria

1. WHEN valid email, password, and profile data are provided, THE Auth_System SHALL create a new user account and volunteer profile record
2. WHEN signup succeeds, THE Application SHALL automatically log in the user and navigate to the dashboard
3. WHEN signup fails due to duplicate email, THE Application SHALL display error message "Email already registered"
4. WHEN valid credentials are provided on login, THE Auth_System SHALL authenticate the user and establish a session
5. WHEN login succeeds, THE Application SHALL navigate to the dashboard with user profile loaded
6. WHEN login fails due to invalid credentials, THE Application SHALL display error message "Invalid email or password"
7. WHEN the user invokes logout, THE Auth_System SHALL terminate the session and clear authentication state
8. WHEN logout succeeds, THE Application SHALL navigate to the login screen

### Requirement 3: Session Persistence

**User Story:** As a volunteer, I want to remain logged in after closing the app, so that I don't need to re-enter credentials every time

#### Acceptance Criteria

1. WHEN the Application starts, THE Auth_System SHALL check for an existing valid session
2. WHEN a valid session exists, THE Application SHALL navigate directly to the dashboard
3. WHEN no valid session exists, THE Application SHALL navigate to the login screen
4. WHEN a session expires during app usage, THE Application SHALL redirect the user to the login screen

### Requirement 4: Profile Data Retrieval

**User Story:** As a volunteer, I want my profile information displayed correctly, so that I can verify my account details

#### Acceptance Criteria

1. WHEN the dashboard loads, THE Repository_Layer SHALL fetch the volunteer profile using the authenticated user ID
2. WHEN the profile screen loads, THE Repository_Layer SHALL fetch the volunteer profile using the authenticated user ID
3. WHEN profile retrieval succeeds, THE UI_Layer SHALL display full name, email, phone, area, skills, availability, total tasks completed, and total hours
4. WHEN profile retrieval fails, THE UI_Layer SHALL display error message "Failed to load profile"

### Requirement 5: Survey Submission Integration

**User Story:** As a volunteer, I want to submit field surveys with photos and GPS coordinates, so that disaster data is recorded in the system

#### Acceptance Criteria

1. WHEN the user captures or selects a photo, THE Application SHALL compress the image to under 2MB
2. WHEN a photo is ready for upload, THE Repository_Layer SHALL upload the image bytes to the survey-photos Storage_Bucket with path "{userId}/{timestamp}.jpg"
3. WHEN photo upload succeeds, THE Repository_Layer SHALL return the public URL of the uploaded image
4. WHEN the user completes the survey form with valid data, THE Repository_Layer SHALL insert a new record in the surveys table including category, severity, people_affected, description, location_name, latitude, longitude, photo_url, and volunteer_id
5. WHEN survey submission succeeds, THE UI_Layer SHALL display success message "Survey submitted successfully" and clear the form
6. WHEN survey submission fails, THE UI_Layer SHALL display error message "Failed to submit survey"
7. WHEN GPS coordinates are unavailable, THE UI_Layer SHALL display error message "GPS coordinates not captured"

### Requirement 6: Survey History Retrieval

**User Story:** As a volunteer, I want to view my previously submitted surveys, so that I can track my contributions

#### Acceptance Criteria

1. WHEN the dashboard loads, THE Repository_Layer SHALL fetch the 2 most recent surveys for the logged-in user ordered by created_at descending
2. WHEN the survey history screen loads, THE Repository_Layer SHALL fetch all surveys for the logged-in user ordered by created_at descending
3. WHEN surveys exist, THE UI_Layer SHALL display category, severity, location_name, status, and created_at for each survey
4. WHEN no surveys exist, THE UI_Layer SHALL display message "No surveys submitted yet"
5. WHEN survey retrieval fails, THE UI_Layer SHALL display error message "Failed to load surveys"

### Requirement 7: Task List Retrieval

**User Story:** As a volunteer, I want to see available tasks and my assigned tasks, so that I can choose work to perform

#### Acceptance Criteria

1. WHEN the dashboard loads, THE Repository_Layer SHALL fetch the top 3 open tasks ordered by urgency descending
2. WHEN the task list screen loads with filter "open", THE Repository_Layer SHALL fetch all open tasks ordered by urgency descending
3. WHEN the task list screen loads with filter "ongoing", THE Repository_Layer SHALL fetch all ongoing tasks assigned to the logged-in user ordered by urgency descending
4. WHEN the task list screen loads with filter "completed", THE Repository_Layer SHALL fetch all completed tasks assigned to the logged-in user ordered by completed_at descending
5. WHEN tasks exist, THE UI_Layer SHALL display title, category, urgency, location_name, and estimated_hours for each task
6. WHEN no tasks exist for the filter, THE UI_Layer SHALL display message "No {filter} tasks available"
7. WHEN task retrieval fails and cached data exists, THE Repository_Layer SHALL return cached tasks from Room database
8. WHEN task retrieval fails and no cached data exists, THE UI_Layer SHALL display error message "Failed to load tasks"

### Requirement 8: Task Detail Retrieval

**User Story:** As a volunteer, I want to view complete task details, so that I understand the work requirements

#### Acceptance Criteria

1. WHEN the user navigates to a task detail screen, THE Repository_Layer SHALL fetch the complete task record by task ID
2. WHEN task detail retrieval succeeds, THE UI_Layer SHALL display title, description, category, urgency, location_name, latitude, longitude, required_skills, estimated_hours, status, assigned_volunteer, field_notes, and completion_note
3. WHEN task detail retrieval fails and cached data exists, THE Repository_Layer SHALL return the cached task from Room database
4. WHEN task detail retrieval fails and no cached data exists, THE UI_Layer SHALL display error message "Failed to load task details"

### Requirement 9: Task Acceptance

**User Story:** As a volunteer, I want to accept an open task, so that I can begin working on it

#### Acceptance Criteria

1. WHEN the user accepts an open task, THE Repository_Layer SHALL update the task record setting status to "ongoing", assigned_volunteer to the user ID, started_at to the current timestamp, and updated_at to the current timestamp
2. WHEN task acceptance succeeds, THE UI_Layer SHALL display success message "Task accepted" and refresh the task detail view
3. WHEN task acceptance succeeds, THE Repository_Layer SHALL refresh the dashboard data
4. WHEN task acceptance fails, THE UI_Layer SHALL display error message "Failed to accept task"

### Requirement 10: Task Completion

**User Story:** As a volunteer, I want to mark a task as completed with notes, so that my work is recorded

#### Acceptance Criteria

1. WHEN the user completes an ongoing task with a completion note, THE Repository_Layer SHALL update the task record setting status to "completed", completed_at to the current timestamp, completion_note to the provided text, and updated_at to the current timestamp
2. WHEN task completion succeeds, THE Repository_Layer SHALL call increment_tasks_completed RPC function for the user
3. WHEN task completion succeeds, THE UI_Layer SHALL display success message "Task completed" and refresh the task detail view
4. WHEN task completion fails, THE UI_Layer SHALL display error message "Failed to complete task"

### Requirement 11: Task Field Notes Update

**User Story:** As a volunteer, I want to add field notes during task execution, so that I can document observations

#### Acceptance Criteria

1. WHEN the user saves field notes for an ongoing task, THE Repository_Layer SHALL update the task record setting field_notes to the provided text and updated_at to the current timestamp
2. WHEN field notes update succeeds, THE UI_Layer SHALL display success message "Notes saved"
3. WHEN field notes update fails, THE UI_Layer SHALL display error message "Failed to save notes"

### Requirement 12: Notification Retrieval

**User Story:** As a volunteer, I want to see my notifications, so that I stay informed about system events

#### Acceptance Criteria

1. WHEN the notification screen loads, THE Repository_Layer SHALL fetch all notifications for the logged-in user ordered by created_at descending
2. WHEN notifications exist, THE UI_Layer SHALL display title, message, type, is_read status, and created_at for each notification
3. WHEN no notifications exist, THE UI_Layer SHALL display message "No notifications yet"
4. WHEN notification retrieval fails, THE UI_Layer SHALL display error message "Failed to load notifications"

### Requirement 13: Notification Read Status

**User Story:** As a volunteer, I want to mark notifications as read, so that I can track which messages I've reviewed

#### Acceptance Criteria

1. WHEN the user opens a notification, THE Repository_Layer SHALL update the notification record setting is_read to true
2. WHEN mark as read succeeds, THE Repository_Layer SHALL refresh the notification list
3. WHEN the dashboard loads, THE Repository_Layer SHALL fetch the count of unread notifications for the logged-in user
4. WHEN unread count is greater than 0, THE UI_Layer SHALL display a badge with the count on the notification icon

### Requirement 14: Risk Score Data Retrieval for Dashboard

**User Story:** As a volunteer, I want to see area risk scores on a map, so that I understand which locations need attention

#### Acceptance Criteria

1. WHEN the risk dashboard screen loads, THE Repository_Layer SHALL fetch all risk score records ordered by risk_score descending
2. WHEN risk scores exist, THE UI_Layer SHALL display markers on a map at each (latitude, longitude) position with color coding based on risk_level: low=green, medium=yellow, high=orange, critical=red
3. WHEN a risk score marker is tapped, THE UI_Layer SHALL display area_name, risk_score, risk_level, and contributing_factors
4. WHEN no risk scores exist, THE UI_Layer SHALL display message "No risk scores calculated yet"
5. WHEN risk score retrieval fails, THE UI_Layer SHALL display error message "Failed to load risk scores"

### Requirement 15: ML Bridge Survey Data Export

**User Story:** As an ML engineer, I want to fetch all survey data for processing, so that I can calculate risk scores

#### Acceptance Criteria

1. WHEN fetchSurveyDataForML is invoked, THE Repository_Layer SHALL query all records from the surveys table
2. WHEN survey data retrieval succeeds, THE Repository_Layer SHALL return a list of maps containing id, volunteer_id, category, severity, people_affected, description, location_name, latitude, longitude, photo_url, status, admin_notes, and created_at
3. WHEN survey data retrieval fails, THE Repository_Layer SHALL return a failure result with exception details

### Requirement 16: ML Bridge Risk Score Import

**User Story:** As an ML engineer, I want to save calculated risk scores to the database, so that the app can display them

#### Acceptance Criteria

1. WHEN saveRiskScore is invoked with valid AreaRiskScore data, THE Repository_Layer SHALL insert a new record in the area_risk_scores table with area_name, latitude, longitude, risk_score, risk_level, contributing_factors, and calculated_at
2. WHEN saveRiskScores is invoked with a list of valid AreaRiskScore data, THE Repository_Layer SHALL batch insert all records in the area_risk_scores table
3. WHEN risk_score is not between 0 and 10, THE Repository_Layer SHALL reject the save and throw IllegalArgumentException "Risk score must be between 0 and 10"
4. WHEN latitude is not between -90 and 90, THE Repository_Layer SHALL reject the save and throw IllegalArgumentException "Invalid latitude"
5. WHEN longitude is not between -180 and 180, THE Repository_Layer SHALL reject the save and throw IllegalArgumentException "Invalid longitude"
6. WHEN risk_level is not one of "low", "medium", "high", "critical", THE Repository_Layer SHALL reject the save and throw IllegalArgumentException "Invalid risk level"

### Requirement 17: ML Bridge Old Score Cleanup

**User Story:** As an ML engineer, I want to clear previous risk calculations before recalculation, so that stale data is removed

#### Acceptance Criteria

1. WHEN clearOldScores is invoked, THE Repository_Layer SHALL call the clear_risk_scores RPC function
2. WHEN clearOldScores succeeds, THE Repository_Layer SHALL return a success result
3. WHEN clearOldScores fails, THE Repository_Layer SHALL return a failure result with exception details

### Requirement 18: Profile Photo Upload

**User Story:** As a volunteer, I want to upload a profile photo, so that my account is personalized

#### Acceptance Criteria

1. WHEN the user selects a profile photo, THE Application SHALL compress the image to under 2MB
2. WHEN a photo is ready for upload, THE Repository_Layer SHALL upload the image bytes to the profile-photos Storage_Bucket with path "{userId}/{timestamp}.jpg"
3. WHEN photo upload succeeds, THE Repository_Layer SHALL return the public URL of the uploaded image
4. WHEN photo upload succeeds, THE Repository_Layer SHALL update the volunteer record setting profile_photo_url to the returned URL
5. WHEN photo upload or profile update fails, THE UI_Layer SHALL display error message "Failed to upload photo"

### Requirement 19: Profile Update

**User Story:** As a volunteer, I want to update my profile information, so that my details remain current

#### Acceptance Criteria

1. WHEN the user submits updated profile data, THE Repository_Layer SHALL update the volunteer record setting full_name, phone, area, skills, availability, and updated_at to the current timestamp
2. WHEN profile update succeeds, THE UI_Layer SHALL display success message "Profile updated" and refresh the profile view
3. WHEN profile update fails, THE UI_Layer SHALL display error message "Failed to update profile"

### Requirement 20: Realtime Subscription for Tasks

**User Story:** As a volunteer, I want task updates to appear immediately without refreshing, so that I see changes in real-time

#### Acceptance Criteria

1. WHEN the task list screen is active, THE Repository_Layer SHALL subscribe to changes on the tasks table via Realtime channel
2. WHEN a task record is inserted, updated, or deleted, THE Realtime_Subscription SHALL emit a PostgresAction event
3. WHEN a PostgresAction event is received, THE UI_Layer SHALL refresh the task list automatically

### Requirement 21: Realtime Subscription for Notifications

**User Story:** As a volunteer, I want new notifications to appear immediately, so that I receive timely updates

#### Acceptance Criteria

1. WHEN the notification screen is active, THE Repository_Layer SHALL subscribe to changes on the notifications table via Realtime channel
2. WHEN a notification record is inserted, THE Realtime_Subscription SHALL emit a PostgresAction event
3. WHEN a PostgresAction event is received, THE UI_Layer SHALL refresh the notification list automatically

### Requirement 22: Realtime Subscription for Surveys

**User Story:** As a system, I want survey changes to be observable, so that admin interfaces can react to new submissions

#### Acceptance Criteria

1. WHEN subscribeSurveyChanges is invoked, THE Repository_Layer SHALL return a Flow emitting PostgresAction events from the surveys table Realtime channel
2. WHEN a survey record is inserted, updated, or deleted, THE Realtime_Subscription SHALL emit a PostgresAction event

### Requirement 23: Realtime Subscription for Risk Scores

**User Story:** As a volunteer, I want risk score updates to appear immediately on the map, so that I see the latest data

#### Acceptance Criteria

1. WHEN the risk dashboard screen is active, THE Repository_Layer SHALL subscribe to changes on the area_risk_scores table via Realtime channel
2. WHEN a risk score record is inserted, updated, or deleted, THE Realtime_Subscription SHALL emit a PostgresAction event
3. WHEN a PostgresAction event is received, THE UI_Layer SHALL refresh the map markers automatically

### Requirement 24: Task Update Creation

**User Story:** As a volunteer, I want to post progress updates on ongoing tasks, so that stakeholders are informed

#### Acceptance Criteria

1. WHEN the user submits a task update with text and optional photo, THE Repository_Layer SHALL insert a new record in the task_updates table with task_id, volunteer_id, update_text, status, and photo_url
2. WHEN task update creation succeeds, THE UI_Layer SHALL display success message "Update posted" and refresh the task detail view
3. WHEN task update creation fails, THE UI_Layer SHALL display error message "Failed to post update"

### Requirement 25: Task Update Retrieval

**User Story:** As a volunteer, I want to see all updates for a task, so that I can review progress history

#### Acceptance Criteria

1. WHEN the task detail screen loads, THE Repository_Layer SHALL fetch all task updates for the given task_id ordered by created_at descending
2. WHEN task updates exist, THE UI_Layer SHALL display volunteer_id, update_text, status, photo_url, and created_at for each update
3. WHEN no task updates exist, THE UI_Layer SHALL display message "No updates yet"
4. WHEN task update retrieval fails, THE UI_Layer SHALL display error message "Failed to load updates"

### Requirement 26: Realtime Subscription for Task Updates

**User Story:** As a volunteer, I want to see new task updates immediately, so that I stay current on progress

#### Acceptance Criteria

1. WHEN the task detail screen is active, THE Repository_Layer SHALL subscribe to changes on the task_updates table via Realtime channel
2. WHEN a task update record is inserted, THE Realtime_Subscription SHALL emit a PostgresAction event
3. WHEN a PostgresAction event is received, THE UI_Layer SHALL refresh the task updates list automatically

### Requirement 27: Error Handling for Network Failures

**User Story:** As a volunteer, I want meaningful error messages when network failures occur, so that I understand what went wrong

#### Acceptance Criteria

1. WHEN a network request fails due to connectivity issues, THE Repository_Layer SHALL return a failure result with exception message containing "network" or "connection"
2. WHEN a network failure occurs, THE UI_Layer SHALL display error message "Network error. Please check your connection."
3. WHEN a network request times out, THE UI_Layer SHALL display error message "Request timed out. Please try again."

### Requirement 28: Data Validation on Survey Submission

**User Story:** As a system, I want survey data validated before submission, so that invalid data is rejected

#### Acceptance Criteria

1. WHEN submitSurvey is invoked with severity not between 1 and 5, THE Repository_Layer SHALL throw IllegalArgumentException "Severity must be between 1 and 5"
2. WHEN submitSurvey is invoked with latitude not between -90 and 90, THE Repository_Layer SHALL throw IllegalArgumentException "Invalid latitude"
3. WHEN submitSurvey is invoked with longitude not between -180 and 180, THE Repository_Layer SHALL throw IllegalArgumentException "Invalid longitude"
4. WHEN submitSurvey is invoked with empty category, THE UI_Layer SHALL display validation error "Category is required"
5. WHEN submitSurvey is invoked with empty description, THE UI_Layer SHALL display validation error "Description is required"
6. WHEN submitSurvey is invoked with empty location_name, THE UI_Layer SHALL display validation error "Location name is required"

### Requirement 29: Image Compression for Uploads

**User Story:** As a user, I want photos compressed before upload, so that uploads complete quickly and don't exceed storage limits

#### Acceptance Criteria

1. WHEN an image is selected for upload, THE Application SHALL compress the image to a maximum of 2MB file size
2. WHEN an image is smaller than 2MB, THE Application SHALL NOT compress the image further
3. WHEN an image is larger than 2MB, THE Application SHALL reduce quality until file size is under 2MB
4. WHEN an image cannot be compressed below 2MB, THE Application SHALL display error message "Image file too large"

### Requirement 30: GPS Coordinate Capture

**User Story:** As a volunteer, I want GPS coordinates captured automatically when submitting surveys, so that locations are accurate

#### Acceptance Criteria

1. WHEN the survey form is opened, THE Application SHALL request the device's current GPS location
2. WHEN location permission is granted and GPS is available, THE Application SHALL capture latitude and longitude
3. WHEN location permission is denied, THE Application SHALL display message "Location permission required for survey submission"
4. WHEN GPS is unavailable after 10 seconds, THE Application SHALL display message "Unable to get GPS location. Please enable location services."
5. WHEN GPS coordinates are captured, THE UI_Layer SHALL display the location name or coordinates to the user for confirmation

### Requirement 31: Dashboard Data Aggregation

**User Story:** As a volunteer, I want the dashboard to show a summary of my activities, so that I can quickly assess my status

#### Acceptance Criteria

1. WHEN the dashboard loads, THE Repository_Layer SHALL aggregate data from profile, tasks, surveys, and notifications in a single DashboardData object
2. WHEN dashboard aggregation succeeds, THE UI_Layer SHALL display volunteer name, ongoing task title, count of open tasks, count of recent surveys, and unread notification count
3. WHEN any individual data fetch fails, THE Repository_Layer SHALL continue aggregating remaining data and log the failure
4. WHEN all data fetches fail, THE UI_Layer SHALL display error message "Failed to load dashboard data"

### Requirement 32: Task Caching with Room Database

**User Story:** As a volunteer, I want to see cached task data when offline, so that I can still browse previously loaded tasks

#### Acceptance Criteria

1. WHEN tasks are successfully fetched from Supabase, THE Repository_Layer SHALL save the tasks to Room database
2. WHEN a network request for tasks fails, THE Repository_Layer SHALL query the Room database for cached tasks matching the requested status
3. WHEN cached tasks exist, THE UI_Layer SHALL display the cached tasks with an indicator "Showing cached data"
4. WHEN no cached tasks exist and network fails, THE UI_Layer SHALL display error message "Failed to load tasks"

### Requirement 33: User Logout Session Cleanup

**User Story:** As a volunteer, I want all local session data cleared on logout, so that the next user cannot access my account

#### Acceptance Criteria

1. WHEN logout is invoked, THE Auth_System SHALL call Supabase signOut to terminate the remote session
2. WHEN logout succeeds, THE Application SHALL clear all cached user data from memory
3. WHEN logout succeeds, THE Application SHALL navigate to the login screen and remove all backstack entries
4. WHEN logout fails, THE UI_Layer SHALL display error message "Failed to logout" but still clear local session data

### Requirement 34: Null Safety for Optional Fields

**User Story:** As a developer, I want null checks on optional fields, so that the app doesn't crash when data is missing

#### Acceptance Criteria

1. WHEN profile_photo_url is null, THE UI_Layer SHALL display a default avatar placeholder
2. WHEN photo_url in surveys is null, THE UI_Layer SHALL NOT display an image component
3. WHEN assigned_volunteer in tasks is null, THE UI_Layer SHALL display "Unassigned"
4. WHEN field_notes or completion_note is null, THE UI_Layer SHALL display empty text or "No notes"
5. WHEN latitude or longitude is null, THE UI_Layer SHALL NOT attempt to display a map marker

### Requirement 35: Screen Navigation Integrity

**User Story:** As a user, I want navigation to work correctly, so that I can move between screens without errors

#### Acceptance Criteria

1. WHEN login succeeds, THE Application SHALL navigate from LoginFragment to DashboardFragment
2. WHEN logout succeeds, THE Application SHALL navigate from any screen to LoginFragment and clear the backstack
3. WHEN a task is tapped in the task list, THE Application SHALL navigate to TaskDetailFragment passing the task ID as an argument
4. WHEN the back button is pressed on DashboardFragment, THE Application SHALL NOT navigate back to LoginFragment
5. WHEN the back button is pressed on non-root screens, THE Application SHALL navigate back to the previous screen

### Requirement 36: Build Configuration Validation

**User Story:** As a developer, I want build failures when credentials are missing, so that deployment errors are caught early

#### Acceptance Criteria

1. WHEN local.properties is missing SUPABASE_URL, THE Application build SHALL fail with error "SUPABASE_URL not configured"
2. WHEN local.properties is missing SUPABASE_ANON_KEY, THE Application build SHALL fail with error "SUPABASE_ANON_KEY not configured"
3. WHEN SupabaseClientProvider is initialized with blank URL, THE Application SHALL throw IllegalArgumentException "SUPABASE_URL not configured. Add it to local.properties"
4. WHEN SupabaseClientProvider is initialized with blank anon key, THE Application SHALL throw IllegalArgumentException "SUPABASE_ANON_KEY not configured. Add it to local.properties"
