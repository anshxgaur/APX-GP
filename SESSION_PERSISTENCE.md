# Session Persistence Implementation

## Overview
This document describes how session persistence is implemented in the APX-GP Android application using Supabase GoTrue automatic session management.

## Architecture

### Automatic Session Persistence
The Supabase GoTrue Auth module automatically handles session persistence using Android SharedPreferences. When a user logs in:
1. GoTrue stores the session token securely in SharedPreferences
2. On app restart, GoTrue automatically restores the session
3. The session is validated against the Supabase backend
4. No manual session management is required

### Components

#### 1. SupabaseClientProvider
**Location**: `data/remote/SupabaseClientProvider.kt`

**Responsibility**: Initializes Supabase client with Auth module that handles automatic session persistence.

```kotlin
val client: SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth) // Handles automatic session persistence
    install(Postgrest)
    install(Storage)
    install(Realtime)
}
```

#### 2. AuthRepository
**Location**: `data/repository/AuthRepository.kt`

**Responsibility**: Provides session check and monitoring methods.

**Key Methods**:
- `isLoggedIn()`: Checks if current session exists (used on app startup)
- `getCurrentUserId()`: Returns authenticated user's ID
- `observeSessionStatus()`: Flow that emits session state changes

```kotlin
fun isLoggedIn(): Boolean {
    return supabaseClient.auth.currentSessionOrNull() != null
}

fun observeSessionStatus(): Flow<Boolean> {
    return supabaseClient.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> true
            is SessionStatus.NotAuthenticated -> false
            is SessionStatus.LoadingFromStorage -> true
            is SessionStatus.NetworkError -> true
        }
    }
}
```

#### 3. SplashFragment
**Location**: `ui/auth/SplashFragment.kt`

**Responsibility**: Initial session check and routing on app startup.

**Flow**:
1. Display splash screen for 1.5 seconds
2. Call `viewModel.checkSession()` which uses `authRepository.isLoggedIn()`
3. Navigate to Dashboard if session exists
4. Navigate to Login if no session exists

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    kotlinx.coroutines.delay(1500)
    viewModel.checkSession()
}

viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.sessionState.collect { state ->
            when (state) {
                is UiState.Success -> {
                    if (state.data) {
                        // Logged in → Dashboard
                        findNavController().navigate(
                            R.id.action_splashFragment_to_dashboardFragment
                        )
                    } else {
                        // Not logged in → Login
                        findNavController().navigate(
                            R.id.action_splashFragment_to_loginFragment
                        )
                    }
                }
                is UiState.Error -> {
                    findNavController().navigate(
                        R.id.action_splashFragment_to_loginFragment
                    )
                }
                else -> { /* Loading — wait */ }
            }
        }
    }
}
```

#### 4. MainActivity
**Location**: `MainActivity.kt`

**Responsibility**: Monitor session expiration during app usage.

**Session Expiration Handling**:
- Observes `authViewModel.isSessionActive` flow
- Tracks last known session state with `lastSessionActive` flag
- When session becomes inactive from active state, redirects to login
- Avoids redirecting during app startup (handled by SplashFragment)
- Clears back stack when redirecting to prevent back navigation

```kotlin
private var lastSessionActive = true

private fun observeSessionStatus() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            authViewModel.isSessionActive.collect { isActive ->
                if (!isActive && lastSessionActive) {
                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination != R.id.loginFragment &&
                        currentDestination != R.id.signupFragment &&
                        currentDestination != R.id.splashFragment) {
                        // Session expired - redirect to login
                        navController.navigate(R.id.loginFragment) {
                            popUpTo(R.id.nav_graph) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
                lastSessionActive = isActive
            }
        }
    }
}
```

## Requirements Fulfillment

### Requirement 3.1: Check for Existing Valid Session on App Startup
**Implementation**: SplashFragment checks session via `authRepository.isLoggedIn()` after 1.5s delay

**Code**: 
- `SplashFragment.onViewCreated()` → `viewModel.checkSession()`
- `AuthViewModel.checkSession()` → `authRepository.isLoggedIn()`
- `AuthRepository.isLoggedIn()` → `supabaseClient.auth.currentSessionOrNull() != null`

### Requirement 3.2: Navigate to Dashboard When Valid Session Exists
**Implementation**: SplashFragment navigates to DashboardFragment when `sessionState` emits `Success(true)`

**Code**: 
```kotlin
if (state.data) {
    findNavController().navigate(
        R.id.action_splashFragment_to_dashboardFragment
    )
}
```

### Requirement 3.3: Navigate to Login When No Valid Session Exists
**Implementation**: SplashFragment navigates to LoginFragment when `sessionState` emits `Success(false)` or `Error`

**Code**: 
```kotlin
} else {
    findNavController().navigate(
        R.id.action_splashFragment_to_loginFragment
    )
}
```

### Requirement 3.4: Redirect to Login When Session Expires During App Usage
**Implementation**: MainActivity observes `authViewModel.isSessionActive` flow and redirects when session becomes inactive

**Code**: 
- `AuthRepository.observeSessionStatus()` monitors `supabaseClient.auth.sessionStatus`
- `AuthViewModel.isSessionActive` exposes the flow
- `MainActivity.observeSessionStatus()` reacts to state changes and navigates to login

## Session State Transitions

### App Startup Flow
```
App Launch
    ↓
SplashFragment displays
    ↓
Wait 1.5 seconds
    ↓
Check session (AuthRepository.isLoggedIn())
    ↓
    ├─→ Session exists → Navigate to Dashboard
    └─→ No session → Navigate to Login
```

### Login Flow
```
User enters credentials
    ↓
AuthRepository.login()
    ↓
Supabase Auth validates and creates session
    ↓
Session stored in SharedPreferences (automatic)
    ↓
Navigate to Dashboard
```

### Session Expiration Flow
```
User actively using app
    ↓
Session expires on server
    ↓
SessionStatus changes to NotAuthenticated
    ↓
AuthRepository.observeSessionStatus() emits false
    ↓
MainActivity.observeSessionStatus() detects change
    ↓
Check if user on non-auth screen
    ↓
Navigate to Login and clear back stack
```

### Logout Flow
```
User clicks logout
    ↓
AuthRepository.logout()
    ↓
Supabase Auth signs out
    ↓
Session cleared from SharedPreferences (automatic)
    ↓
Navigate to Login and clear back stack
```

## Testing Scenarios

### Scenario 1: First Time User
1. Install and launch app
2. SplashFragment displays
3. No session found
4. Navigate to LoginFragment

### Scenario 2: Returning User with Valid Session
1. Launch app (user previously logged in)
2. SplashFragment displays
3. GoTrue restores session from SharedPreferences
4. Session is valid
5. Navigate to DashboardFragment

### Scenario 3: Returning User with Expired Session
1. Launch app (session expired)
2. SplashFragment displays
3. GoTrue attempts to restore session
4. Session is invalid/expired
5. Navigate to LoginFragment

### Scenario 4: Session Expires During App Usage
1. User actively using app
2. Session expires on server (e.g., token timeout)
3. SessionStatus changes to NotAuthenticated
4. MainActivity detects change
5. Redirect to LoginFragment
6. User must log in again

### Scenario 5: User Logs Out
1. User clicks logout in ProfileFragment
2. AuthRepository.logout() called
3. Session cleared
4. Navigate to LoginFragment
5. Back stack cleared (cannot navigate back)

## Security Considerations

### Secure Storage
- Sessions are stored in Android SharedPreferences
- SharedPreferences is sandboxed per app (not accessible to other apps)
- Session tokens are encrypted in transit (HTTPS)
- No sensitive data logged to console

### Token Refresh
- Supabase GoTrue automatically refreshes access tokens
- Refresh happens transparently in the background
- No user action required for token refresh
- If refresh fails, session becomes invalid and user is logged out

### Network Error Handling
- Network errors do NOT trigger logout
- `SessionStatus.NetworkError` is treated as "session still valid"
- Prevents false logouts due to temporary network issues
- User must explicitly logout or session must expire on server

## Configuration

### Build Configuration
Session persistence requires no additional configuration. It works automatically when the Auth module is installed:

```kotlin
val client: SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth) // This is all that's needed!
}
```

### Dependencies
Session persistence is provided by the Supabase Kotlin SDK:
- `io.github.jan-tennert.supabase:gotrue-kt` - Auth module
- `io.github.jan-tennert.supabase:postgrest-kt` - Database operations
- `io.github.jan-tennert.supabase:storage-kt` - File uploads
- `io.github.jan-tennert.supabase:realtime-kt` - Live subscriptions

## Troubleshooting

### Issue: User not staying logged in
**Cause**: Auth module not installed or SharedPreferences cleared

**Solution**: 
1. Verify Auth module is installed in SupabaseClientProvider
2. Check app is not clearing SharedPreferences on startup
3. Verify credentials in local.properties are correct

### Issue: User logged out unexpectedly
**Cause**: Session expired on server or token refresh failed

**Solution**: 
1. Check server-side session timeout settings
2. Verify network connectivity during token refresh
3. Review Supabase dashboard for auth logs

### Issue: Session check fails on startup
**Cause**: Network issue or invalid stored session

**Solution**: 
1. App correctly redirects to login (expected behavior)
2. User can log in again to create new session
3. Check network connectivity

## Summary

Session persistence in APX-GP is fully automatic thanks to Supabase GoTrue:
- ✅ No manual session storage required
- ✅ No manual token refresh logic needed
- ✅ Secure storage in Android SharedPreferences
- ✅ Automatic restoration on app restart
- ✅ Session expiration monitoring during usage
- ✅ Clean navigation flows for all scenarios

All requirements (3.1, 3.2, 3.3, 3.4) are fulfilled through:
1. SplashFragment for startup session check
2. MainActivity for session expiration monitoring
3. Supabase GoTrue for automatic persistence
