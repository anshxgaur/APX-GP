# Implementation Plan: Hackathon-Readiness Integration

## Overview

This implementation plan transforms the APX-GP Android application from a prototype with mock data into a production-ready system with complete Supabase backend integration. The implementation follows a layered approach: first establishing core infrastructure (credentials, client, data models), then implementing the repository layer for data access, followed by UI integration and realtime features, and finally wiring everything together for end-to-end functionality.

## Tasks

- [x] 1. Set up build configuration and Supabase client initialization
  - [x] 1.1 Configure build system for secure credential management
    - Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties
    - Update app/build.gradle.kts to read credentials from local.properties and expose via BuildConfig
    - Add validation to fail build when credentials are missing
    - Update .gitignore to exclude local.properties
    - _Requirements: 1.1, 1.2, 1.3, 36.1, 36.2_
  
  - [x] 1.2 Implement SupabaseClientProvider singleton
    - Create data/remote/SupabaseClientProvider.kt as object singleton
    - Initialize SupabaseClient with BuildConfig credentials in init block
    - Install Auth, Postgrest, Storage, and Realtime modules
    - Validate credentials and throw IllegalArgumentException if blank
    - _Requirements: 1.1, 1.2, 1.3, 36.3, 36.4_
  
  - [x] 1.3 Update Hilt dependency injection module
    - Update di/AppModule.kt to provide SupabaseClient from SupabaseClientProvider
    - Add provideAppDatabase for Room database instance
    - Add provideTaskDao for offline task caching
    - _Requirements: 1.1, 1.2_

- [x] 2. Implement core data models and Room database
  - [x] 2.1 Define Kotlin data models with serialization
    - Update data/model/Models.kt with @Serializable annotation for all models
    - Add @SerialName annotations for snake_case field mapping
    - Implement Volunteer, Survey, Task, TaskUpdate, AreaRiskScore, AppNotification models
    - Create DashboardData aggregation model
    - _Requirements: 4.1, 4.2, 4.3, 5.4, 7.5, 12.2, 14.2, 31.2_
  
  - [x] 2.2 Create Room database entities and DAOs
    - Create data/local/TaskEntity.kt with Room annotations
    - Implement TaskEntity.fromTask() and toTask() conversion methods
    - Create data/local/TaskDao.kt with CRUD operations and Flow queries
    - Update data/local/AppDatabase.kt with TaskEntity
    - _Requirements: 7.7, 7.8, 32.1, 32.2, 32.3, 32.4_

- [x] 3. Implement Authentication Repository
  - [x] 3.1 Create AuthRepository with signup and login
    - Create data/repository/AuthRepository.kt with @Inject constructor
    - Implement signUp() to create auth user and volunteer profile record
    - Implement login() to authenticate and establish session
    - Implement logout() to clear session and navigate to login
    - Implement isLoggedIn() to check session existence
    - Implement getCurrentUserId() to return current user ID
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_
  
  - [x] 3.2 Implement session persistence logic
    - Leverage Supabase GoTrue automatic session persistence
    - Verify session check on app startup in SplashFragment
    - Handle session expiration during app usage
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [x] 3.3 Add authentication error handling
    - Map Supabase auth errors to user-friendly messages
    - Handle duplicate email error ("Email already registered")
    - Handle invalid credentials error ("Invalid email or password")
    - Handle network errors with appropriate messages
    - _Requirements: 2.3, 2.6, 27.1, 27.2, 27.3_

- [x] 4. Implement Profile Repository
  - [x] 4.1 Create ProfileRepository for profile operations
    - Create data/repository/ProfileRepository.kt with @Inject constructor
    - Implement getProfile() to fetch volunteer by ID
    - Implement updateProfile() to update volunteer fields
    - Implement uploadProfilePhoto() to upload image to profile-photos bucket
    - Implement updateProfilePhotoUrl() to update volunteer.profile_photo_url
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 18.2, 18.3, 18.4, 18.5, 19.1, 19.2, 19.3_

- [x] 5. Implement Survey Repository
  - [x] 5.1 Create SurveyRepository for survey operations
    - Create data/repository/SurveyRepository.kt with @Inject constructor
    - Implement submitSurvey() with input validation (severity 1-5, lat/lng ranges)
    - Implement getMySurveys() to fetch user's surveys
    - Implement getRecentSurveys() for dashboard with limit parameter
    - Implement getAllSurveys() for ML bridge data export
    - Implement uploadPhoto() to upload to survey-photos bucket
    - Implement subscribeSurveyChanges() returning Flow<PostgresAction>
    - _Requirements: 5.4, 5.5, 5.6, 5.7, 6.1, 6.2, 6.3, 6.4, 6.5, 15.1, 15.2, 15.3, 22.1, 22.2, 28.1, 28.2, 28.3_

- [x] 6. Implement Task Repository with caching
  - [x] 6.1 Create TaskRepository for task operations
    - Create data/repository/TaskRepository.kt with @Inject constructor (SupabaseClient + TaskDao)
    - Implement getOpenTasks() with Supabase fetch and Room caching fallback
    - Implement getMyTasks() with status filter and caching fallback
    - Implement getTaskById() with caching fallback
    - Implement acceptTask() to update task status to ongoing and assign volunteer
    - Implement completeTask() to update status to completed and call increment RPC
    - Implement updateFieldNotes() to save field notes
    - Implement subscribeTaskChanges() returning Flow<PostgresAction>
    - Implement getCachedTasksFlow() returning Flow from Room
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3, 9.4, 10.1, 10.2, 10.3, 10.4, 11.1, 11.2, 11.3, 20.1, 20.2, 20.3, 32.1, 32.2, 32.3, 32.4_

- [x] 7. Implement Task Update Repository
  - [x] 7.1 Create TaskUpdateRepository for progress updates
    - Create data/repository/TaskUpdateRepository.kt with @Inject constructor
    - Implement createTaskUpdate() to insert task update record
    - Implement getTaskUpdates() to fetch updates for a task ordered by created_at DESC
    - Implement uploadUpdatePhoto() to upload to survey-photos bucket
    - Implement subscribeTaskUpdateChanges() returning Flow<PostgresAction>
    - _Requirements: 24.1, 24.2, 24.3, 25.1, 25.2, 25.3, 25.4, 26.1, 26.2, 26.3_

- [x] 8. Implement Notification Repository
  - [x] 8.1 Create NotificationRepository for notification operations
    - Create data/repository/NotificationRepository.kt with @Inject constructor
    - Implement getNotifications() to fetch user notifications ordered by created_at DESC
    - Implement getUnreadCount() to count unread notifications
    - Implement markAsRead() to update is_read flag
    - Implement subscribeNotificationChanges() returning Flow<PostgresAction>
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 13.1, 13.2, 13.3, 13.4, 21.1, 21.2, 21.3_

- [x] 9. Implement Risk Score Repository
  - [x] 9.1 Create RiskScoreRepository for ML bridge integration
    - Create data/repository/RiskScoreRepository.kt with @Inject constructor
    - Implement getAllRiskScores() to fetch all risk scores ordered by risk_score DESC
    - Implement saveRiskScore() with validation (risk_score 0-10, lat/lng ranges, risk_level enum)
    - Implement saveRiskScores() for batch insert
    - Implement clearOldScores() to call clear_risk_scores RPC
    - Implement subscribeRiskScoreChanges() returning Flow<PostgresAction>
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 17.1, 17.2, 17.3, 23.1, 23.2, 23.3_

- [x] 10. Implement image compression utility
  - [x] 10.1 Create ImageUtils for photo compression
    - Create utils/ImageUtils.kt with compressImage() function
    - Implement progressive quality reduction algorithm (start at 100, reduce by 10)
    - Set maximum file size to 2MB (2,097,152 bytes)
    - Throw exception if image cannot be compressed below 2MB
    - _Requirements: 5.1, 18.1, 29.1, 29.2, 29.3, 29.4_

- [x] 11. Implement error handling utilities
  - [x] 11.1 Create error message mapping extension
    - Update utils/Extensions.kt with Throwable.toUserMessage() extension function
    - Map IOException and network errors to "Network error. Please check your connection."
    - Map SocketTimeoutException to "Request timed out. Please try again."
    - Map duplicate email error to "Email already registered"
    - Map invalid credentials to "Invalid email or password"
    - Provide generic fallback for unmapped errors
    - _Requirements: 2.3, 2.6, 27.1, 27.2, 27.3_

- [x] 12. Checkpoint - Verify repository layer implementation
  - Ensure all repository classes compile without errors
  - Verify all methods return Result<T> for proper error handling
  - Confirm Hilt injection is configured correctly
  - Ensure Room database and DAOs are properly set up
  - Ask the user if questions arise

- [x] 13. Update AuthViewModel to use AuthRepository
  - [x] 13.1 Integrate AuthRepository in AuthViewModel
    - Update ui/auth/AuthViewModel.kt to inject AuthRepository
    - Implement signUp() calling authRepository.signUp() with profile data
    - Implement login() calling authRepository.login()
    - Implement logout() calling authRepository.logout()
    - Add UiState sealed class for Loading, Success, Error states
    - Use viewModelScope for coroutine launches
    - Map repository results to UI states
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_

- [x] 14. Update SplashFragment for session persistence
  - [x] 14.1 Implement automatic session check on app start
    - Update ui/auth/SplashFragment.kt to check authRepository.isLoggedIn()
    - Navigate to DashboardFragment if session exists
    - Navigate to LoginFragment if no session
    - Handle session expiration during app lifecycle
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 15. Update LoginFragment and SignupFragment
  - [x] 15.1 Connect authentication UI to AuthViewModel
    - Update ui/auth/LoginFragment.kt to observe AuthViewModel.uiState
    - Show loading indicator during authentication
    - Display success/error messages using Snackbar or Toast
    - Navigate to DashboardFragment on successful login
    - Update ui/auth/SignupFragment.kt with same pattern for signup
    - Clear form on successful signup
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 35.1_

- [x] 16. Update ProfileViewModel and ProfileFragment
  - [x] 16.1 Integrate ProfileRepository in ProfileViewModel
    - Update ui/profile/ProfileViewModel.kt to inject ProfileRepository
    - Implement loadProfile() calling profileRepository.getProfile()
    - Implement updateProfile() calling profileRepository.updateProfile()
    - Implement uploadProfilePhoto() with ImageUtils.compressImage()
    - Handle profile photo upload and URL update flow
    - Add UiState management for loading, success, error
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 18.1, 18.2, 18.3, 18.4, 18.5, 19.1, 19.2, 19.3_
  
  - [x] 16.2 Update ProfileFragment UI
    - Update ui/profile/ProfileFragment.kt to observe ProfileViewModel states
    - Display full_name, email, phone, area, skills, availability, total_tasks_completed, total_hours
    - Show default avatar placeholder when profile_photo_url is null
    - Implement photo selection and upload flow
    - Display success/error messages for profile updates
    - _Requirements: 4.3, 4.4, 18.5, 19.2, 19.3, 34.1_

- [x] 17. Update SurveyViewModel and SurveyFragment
  - [x] 17.1 Integrate SurveyRepository in SurveyViewModel
    - Update ui/survey/SurveyViewModel.kt to inject SurveyRepository
    - Implement submitSurvey() with input validation
    - Implement uploadPhoto() with ImageUtils.compressImage()
    - Implement getMySurveys() for survey history
    - Add realtime subscription to survey changes
    - Add UiState management
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 6.1, 6.2, 6.3, 6.4, 6.5, 22.1, 22.2, 28.1, 28.2, 28.3, 28.4, 28.5, 28.6_
  
  - [x] 17.2 Update SurveyFragment UI with GPS capture
    - Update ui/survey/SurveyFragment.kt to observe SurveyViewModel states
    - Implement GPS location capture on form open
    - Request location permission and handle denial
    - Display location name or coordinates after capture
    - Show validation errors for empty fields
    - Display success message "Survey submitted successfully" and clear form
    - Handle photo selection, compression, and upload
    - Display error "GPS coordinates not captured" when GPS unavailable
    - _Requirements: 5.5, 5.6, 5.7, 28.4, 28.5, 28.6, 30.1, 30.2, 30.3, 30.4, 30.5_

- [x] 18. Update TaskViewModel and TaskListFragment
  - [x] 18.1 Integrate TaskRepository in TaskViewModel
    - Update ui/tasks/TaskViewModel.kt to inject TaskRepository
    - Implement getOpenTasks() calling taskRepository.getOpenTasks()
    - Implement getMyTasks() with status filter ("ongoing", "completed")
    - Implement getTaskById() for task details
    - Implement acceptTask() calling taskRepository.acceptTask()
    - Implement completeTask() calling taskRepository.completeTask()
    - Implement updateFieldNotes() calling taskRepository.updateFieldNotes()
    - Add realtime subscription to task changes with refresh-on-change strategy
    - Observe cached tasks flow from Room for offline support
    - Add UiState management
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3, 9.4, 10.1, 10.2, 10.3, 10.4, 11.1, 11.2, 11.3, 20.1, 20.2, 20.3, 32.2, 32.3_
  
  - [x] 18.2 Update TaskListFragment UI
    - Update ui/tasks/TaskListFragment.kt to observe TaskViewModel states
    - Display task list with title, category, urgency, location_name, estimated_hours
    - Implement filter switching (open, ongoing, completed)
    - Display "No {filter} tasks available" when list is empty
    - Display "Showing cached data" indicator when offline
    - Navigate to TaskDetailFragment on task item click with task ID argument
    - Subscribe to realtime changes in lifecycle-aware manner (repeatOnLifecycle STARTED)
    - _Requirements: 7.5, 7.6, 20.3, 32.3, 35.3_

- [x] 19. Update TaskDetailFragment for task operations
  - [x] 19.1 Update TaskDetailFragment UI and operations
    - Update ui/tasks/TaskDetailFragment.kt to observe TaskViewModel states
    - Display complete task details (title, description, category, urgency, location, skills, hours, status, field_notes, completion_note)
    - Display "Unassigned" when assigned_volunteer is null
    - Display "No notes" when field_notes or completion_note is null
    - Implement Accept Task button (visible for open tasks)
    - Implement Complete Task button with completion note input (visible for ongoing tasks)
    - Implement field notes editing and saving (visible for ongoing tasks)
    - Display task updates list ordered by created_at DESC
    - Display "No updates yet" when no updates exist
    - Subscribe to task_updates realtime changes with refresh strategy
    - Handle null photo_url by not displaying image component
    - _Requirements: 8.2, 8.3, 9.2, 9.3, 10.2, 10.3, 11.2, 11.3, 25.2, 25.3, 25.4, 26.1, 26.2, 26.3, 34.2, 34.4, 34.5_

- [x] 20. Implement task update posting functionality
  - [x] 20.1 Add task update creation to TaskDetailFragment
    - Inject TaskUpdateRepository in TaskViewModel
    - Implement createTaskUpdate() calling taskUpdateRepository.createTaskUpdate()
    - Implement uploadUpdatePhoto() with ImageUtils.compressImage()
    - Add UI for posting updates with text and optional photo
    - Display success message "Update posted" and refresh updates list
    - Display error message "Failed to post update"
    - _Requirements: 24.1, 24.2, 24.3, 25.1_

- [x] 21. Update NotificationViewModel and NotificationFragment
  - [x] 21.1 Integrate NotificationRepository in NotificationViewModel
    - Update ui/notifications/NotificationViewModel.kt to inject NotificationRepository
    - Implement getNotifications() calling notificationRepository.getNotifications()
    - Implement getUnreadCount() calling notificationRepository.getUnreadCount()
    - Implement markAsRead() calling notificationRepository.markAsRead()
    - Add realtime subscription to notification changes
    - Add UiState management
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 13.1, 13.2, 13.3, 21.1, 21.2, 21.3_
  
  - [x] 21.2 Update NotificationFragment UI
    - Update ui/notifications/NotificationFragment.kt to observe NotificationViewModel states
    - Display notifications with title, message, type, is_read status, created_at
    - Display "No notifications yet" when list is empty
    - Mark notification as read when tapped
    - Subscribe to realtime changes in lifecycle-aware manner
    - _Requirements: 12.2, 12.3, 12.4, 13.1, 13.2, 21.3_

- [x] 22. Implement dashboard data aggregation
  - [x] 22.1 Create DashboardViewModel with data aggregation
    - Update ui/dashboard/DashboardViewModel.kt to inject multiple repositories
    - Inject ProfileRepository, TaskRepository, SurveyRepository, NotificationRepository
    - Implement loadDashboard() to aggregate DashboardData
    - Fetch profile, ongoing task, top 3 open tasks, 2 recent surveys, unread count
    - Continue aggregating even if individual fetches fail (log failures)
    - Return error only if all fetches fail
    - Subscribe to task and notification realtime changes to auto-refresh
    - Add UiState management with DashboardData
    - _Requirements: 6.1, 7.1, 9.3, 13.3, 31.1, 31.2, 31.3, 31.4_
  
  - [x] 22.2 Update DashboardFragment UI
    - Update ui/dashboard/DashboardFragment.kt to observe DashboardViewModel states
    - Display volunteer name, ongoing task title (or "No active task"), open task count
    - Display recent surveys count and unread notification badge
    - Subscribe to realtime changes for auto-refresh
    - Navigate to TaskDetailFragment on ongoing task tap
    - Prevent back navigation to LoginFragment
    - _Requirements: 31.2, 31.3, 13.4, 35.4_

- [x] 23. Checkpoint - Verify UI integration
  - Test authentication flows (signup, login, logout, session persistence)
  - Test profile viewing and updating
  - Test survey submission with photo upload and GPS
  - Test task browsing, acceptance, completion, and field notes
  - Test notifications and mark as read
  - Test dashboard data aggregation and navigation
  - Ensure all tests pass, ask the user if questions arise


- [x] 24. Implement risk score dashboard (optional advanced feature)
  - [x] 24.1 Create RiskDashboardFragment and ViewModel
    - Create new Fragment ui/risk/RiskDashboardFragment.kt
    - Create RiskDashboardViewModel injecting RiskScoreRepository
    - Implement getAllRiskScores() calling riskScoreRepository.getAllRiskScores()
    - Subscribe to risk_score realtime changes
    - Add UiState management
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 23.1, 23.2, 23.3_
  
  - [x] 24.2 Implement map visualization for risk scores
    - Add Google Maps or Mapbox dependency to app/build.gradle.kts
    - Display map in RiskDashboardFragment
    - Render markers at (latitude, longitude) with color coding (low=green, medium=yellow, high=orange, critical=red)
    - Display area_name, risk_score, risk_level, contributing_factors on marker tap
    - Display "No risk scores calculated yet" when empty
    - Handle null latitude/longitude by skipping marker
    - _Requirements: 14.2, 14.3, 14.4, 14.5, 23.3, 34.5_

- [x] 25. Implement ML bridge integration endpoints
  - [x] 25.1 Create MLBridgeRepository for external ML systems
    - Create data/repository/MLBridgeRepository.kt as convenience wrapper
    - Implement fetchSurveyDataForML() calling surveyRepository.getAllSurveys()
    - Implement saveCalculatedRiskScores() calling riskScoreRepository.saveRiskScores()
    - Implement clearPreviousRiskScores() calling riskScoreRepository.clearOldScores()
    - Document ML workflow in code comments
    - _Requirements: 15.1, 15.2, 15.3, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 17.1, 17.2, 17.3_

- [x] 26. Implement logout session cleanup
  - [x] 26.1 Ensure proper logout and backstack management
    - Update AuthRepository.logout() to clear Supabase session
    - Update navigation graph to clear backstack on logout
    - Navigate to LoginFragment with popUpTo inclusive after logout
    - Clear any in-memory cached data on logout
    - Display error "Failed to logout" if remote logout fails but still clear local session
    - _Requirements: 2.7, 2.8, 33.1, 33.2, 33.3, 33.4, 35.2_


- [x] 27. Add comprehensive null safety checks across UI
  - [x] 27.1 Implement null safety for optional model fields
    - Review all Fragments for null field handling
    - Display default avatar when profile_photo_url is null
    - Hide image components when photo_url is null
    - Display "Unassigned" when assigned_volunteer is null
    - Display "No notes" or empty text when field_notes/completion_note is null
    - Skip map markers when latitude/longitude is null
    - _Requirements: 34.1, 34.2, 34.3, 34.4, 34.5_

- [x] 28. Implement navigation integrity and backstack management
  - [x] 28.1 Configure navigation graph for proper flow
    - Update res/navigation/nav_graph.xml
    - Set SplashFragment as start destination
    - Configure LoginFragment -> DashboardFragment transition
    - Configure DashboardFragment to prevent back to LoginFragment (popUpTo)
    - Configure TaskListFragment -> TaskDetailFragment with task ID argument
    - Configure logout action to clear backstack and return to LoginFragment
    - _Requirements: 35.1, 35.2, 35.3, 35.4, 35.5_

- [x] 29. Add security and logging considerations
  - [x] 29.1 Implement secure logging practices
    - Review all logging statements in codebase
    - Remove or redact logs containing SUPABASE_ANON_KEY
    - Remove logs containing user passwords or auth tokens
    - Ensure BuildConfig credentials are not logged to system output
    - Add proguard rules to obfuscate sensitive constants in release builds
    - _Requirements: 1.5_

- [x] 30. Final integration and end-to-end testing
  - [x] 30.1 Perform comprehensive end-to-end testing
    - Test complete signup -> login -> dashboard flow
    - Test profile photo upload and update
    - Test survey submission with GPS and photo
    - Test task acceptance, field notes, and completion
    - Test task updates with photos
    - Test notifications and realtime updates
    - Test offline task viewing with cached data
    - Test network error handling and user-friendly messages
    - Test logout and session cleanup
    - Verify no crashes during demo scenarios
    - _Requirements: All 36 requirements_
  
  - [x] 30.2 Verify success criteria
    - Verify all 36 requirements implemented and verified
    - Confirm authentication flows working end-to-end
    - Confirm real-time data synchronization operational
    - Confirm image uploads functional for surveys and profiles
    - Confirm offline task viewing with cached data
    - Confirm ML Bridge operational for risk score integration
    - Confirm zero crashes during demo scenarios
    - Confirm clean error handling with user-friendly messages
    - _Success Criteria: All 8 criteria met_

- [x] 31. Final checkpoint - Hackathon readiness verification
  - Run the application and verify all features work as expected
  - Test on physical device for GPS and camera functionality
  - Prepare demo scenarios for hackathon presentation
  - Document any known limitations or future enhancements
  - Ensure all tests pass, ask the user if questions arise

## Notes

- Tasks are organized in a logical dependency order: infrastructure → repositories → ViewModels → UI → integration
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at major milestones
- The implementation uses Kotlin for Android with Supabase Kotlin SDK
- Room database provides offline caching only for tasks (most critical for field work)
- Realtime subscriptions use refresh-on-change strategy for simplicity and reliability
- All repository operations use Result<T> for consistent error handling
- Image compression ensures uploads stay under 2MB limit
- GPS capture includes permission handling and timeout logic
- Navigation graph configured to prevent back navigation security issues
- ML Bridge provides data access layer for external ML systems
- Risk score dashboard is optional advanced feature (task 24)
- Security practices include credential validation, secure logging, and session cleanup

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "2.1"] },
    { "id": 1, "tasks": ["1.3", "2.2", "10.1", "11.1"] },
    { "id": 2, "tasks": ["3.1", "3.2", "4.1", "5.1", "6.1", "7.1", "8.1", "9.1"] },
    { "id": 3, "tasks": ["3.3", "13.1", "14.1"] },
    { "id": 4, "tasks": ["15.1", "16.1", "17.1", "18.1", "21.1", "22.1"] },
    { "id": 5, "tasks": ["16.2", "17.2", "18.2", "19.1", "20.1", "21.2", "22.2"] },
    { "id": 6, "tasks": ["24.1", "25.1", "26.1", "27.1", "28.1", "29.1"] },
    { "id": 7, "tasks": ["24.2", "30.1", "30.2"] }
  ]
}
```
