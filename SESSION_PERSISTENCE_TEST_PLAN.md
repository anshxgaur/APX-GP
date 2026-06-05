# Session Persistence Test Plan

## Test Cases for Requirements 3.1, 3.2, 3.3, 3.4

### Test Case 1: Session Check on App Startup - Valid Session (Req 3.1, 3.2)

**Objective**: Verify that when the app starts with a valid session, it navigates directly to the dashboard.

**Preconditions**:
- User has previously logged in
- Valid session exists in SharedPreferences
- Session has not expired

**Steps**:
1. Launch the application
2. Observe the splash screen
3. Wait for session check to complete

**Expected Results**:
- ✅ Splash screen displays for ~1.5 seconds
- ✅ `AuthRepository.isLoggedIn()` returns `true`
- ✅ App navigates directly to DashboardFragment
- ✅ User profile is loaded
- ✅ No login screen is shown

**Verification Points**:
- Check `AuthViewModel.sessionState` emits `UiState.Success(true)`
- Verify `supabaseClient.auth.currentSessionOrNull()` is not null
- Confirm navigation to `R.id.dashboardFragment`

---

### Test Case 2: Session Check on App Startup - No Session (Req 3.1, 3.3)

**Objective**: Verify that when the app starts without a session, it navigates to the login screen.

**Preconditions**:
- User has never logged in OR has logged out
- No session exists in SharedPreferences

**Steps**:
1. Launch the application
2. Observe the splash screen
3. Wait for session check to complete

**Expected Results**:
- ✅ Splash screen displays for ~1.5 seconds
- ✅ `AuthRepository.isLoggedIn()` returns `false`
- ✅ App navigates to LoginFragment
- ✅ Login form is displayed

**Verification Points**:
- Check `AuthViewModel.sessionState` emits `UiState.Success(false)`
- Verify `supabaseClient.auth.currentSessionOrNull()` is null
- Confirm navigation to `R.id.loginFragment`

---

### Test Case 3: Session Check on App Startup - Expired Session (Req 3.1, 3.3)

**Objective**: Verify that when the app starts with an expired session, it navigates to the login screen.

**Preconditions**:
- User previously logged in
- Session exists in SharedPreferences but has expired on server
- Token refresh fails due to expiration

**Steps**:
1. Wait for session to expire (or manually expire on Supabase dashboard)
2. Force close and relaunch the application
3. Observe the splash screen
4. Wait for session check to complete

**Expected Results**:
- ✅ Splash screen displays for ~1.5 seconds
- ✅ GoTrue attempts to restore session
- ✅ Session validation fails
- ✅ `AuthRepository.isLoggedIn()` returns `false`
- ✅ App navigates to LoginFragment

**Verification Points**:
- Check `AuthViewModel.sessionState` emits `UiState.Success(false)`
- Verify `supabaseClient.auth.currentSessionOrNull()` is null after validation
- Confirm navigation to `R.id.loginFragment`

---

### Test Case 4: Session Expiration During App Usage (Req 3.4)

**Objective**: Verify that when a session expires while the user is actively using the app, they are redirected to the login screen.

**Preconditions**:
- User is logged in
- User is on a non-auth screen (e.g., DashboardFragment)
- Session is valid at start

**Steps**:
1. Log in to the application
2. Navigate to Dashboard or any other screen
3. Wait for session to expire (or manually expire on Supabase dashboard)
4. Perform any action or wait for SessionStatus to emit NotAuthenticated

**Expected Results**:
- ✅ `AuthRepository.observeSessionStatus()` emits `false`
- ✅ `MainActivity.observeSessionStatus()` detects session expiration
- ✅ App navigates to LoginFragment
- ✅ Back stack is cleared (cannot navigate back to protected screens)
- ✅ User must log in again to access app

**Verification Points**:
- Check `SessionStatus` changes from `Authenticated` to `NotAuthenticated`
- Verify `authViewModel.isSessionActive` emits `false`
- Confirm `MainActivity` detects `!isActive && lastSessionActive`
- Verify navigation to `R.id.loginFragment` with back stack cleared
- Ensure user cannot press back button to return to dashboard

---

### Test Case 5: Session Persistence After App Restart

**Objective**: Verify that a valid session persists across app restarts.

**Preconditions**:
- User is logged in
- Session is valid

**Steps**:
1. Log in to the application
2. Navigate around the app
3. Force close the app (swipe away from recent apps)
4. Relaunch the app
5. Observe the session check

**Expected Results**:
- ✅ Session is restored from SharedPreferences
- ✅ App navigates directly to Dashboard
- ✅ No login required
- ✅ User profile is available immediately

**Verification Points**:
- Check `SessionStatus.LoadingFromStorage` emitted during restore
- Verify `SessionStatus.Authenticated` emitted after restore
- Confirm `AuthRepository.isLoggedIn()` returns `true`
- Ensure `AuthRepository.getCurrentUserId()` returns correct user ID

---

### Test Case 6: Session Persistence After Device Reboot

**Objective**: Verify that a valid session persists even after device reboot.

**Preconditions**:
- User is logged in
- Session is valid
- App is closed

**Steps**:
1. Log in to the application
2. Close the app
3. Reboot the device
4. Launch the app after reboot
5. Observe the session check

**Expected Results**:
- ✅ Session is restored from SharedPreferences
- ✅ App navigates directly to Dashboard
- ✅ No login required

**Verification Points**:
- SharedPreferences survived device reboot
- Session restored successfully
- User remains authenticated

---

### Test Case 7: Logout Clears Session

**Objective**: Verify that logging out properly clears the session.

**Preconditions**:
- User is logged in

**Steps**:
1. Log in to the application
2. Navigate to ProfileFragment
3. Click the logout button
4. Observe navigation

**Expected Results**:
- ✅ `AuthRepository.logout()` called
- ✅ `supabaseClient.auth.signOut()` executed
- ✅ Session cleared from SharedPreferences
- ✅ App navigates to LoginFragment
- ✅ Back stack is cleared (cannot navigate back)

**Verification Points**:
- Check `AuthRepository.logout()` returns `Result.success()`
- Verify `supabaseClient.auth.currentSessionOrNull()` is null after logout
- Confirm navigation to `R.id.loginFragment` with back stack cleared

---

### Test Case 8: Login Creates Session

**Objective**: Verify that logging in creates a persistent session.

**Preconditions**:
- User is on LoginFragment
- User has valid credentials

**Steps**:
1. Enter valid email and password
2. Click login button
3. Wait for authentication
4. Observe navigation
5. Force close and relaunch app

**Expected Results**:
- ✅ `AuthRepository.login()` succeeds
- ✅ Session created and stored in SharedPreferences
- ✅ App navigates to DashboardFragment
- ✅ On relaunch, session is restored and user stays logged in

**Verification Points**:
- Check `AuthRepository.login()` returns `Result.success()`
- Verify `supabaseClient.auth.currentSessionOrNull()` is not null
- Confirm navigation to `R.id.dashboardFragment`
- After relaunch, verify session persists

---

### Test Case 9: Network Error Does Not Log Out User

**Objective**: Verify that temporary network errors do not trigger logout.

**Preconditions**:
- User is logged in
- Session is valid

**Steps**:
1. Log in to the application
2. Disable network connectivity (airplane mode or Wi-Fi off)
3. Observe app behavior
4. Re-enable network connectivity

**Expected Results**:
- ✅ User remains logged in
- ✅ `SessionStatus.NetworkError` is emitted
- ✅ `AuthRepository.observeSessionStatus()` still emits `true`
- ✅ User NOT redirected to login
- ✅ App may show network error messages but does not log out
- ✅ When network returns, app continues normally

**Verification Points**:
- Check `SessionStatus.NetworkError` is treated as "session still valid"
- Verify `authViewModel.isSessionActive` remains `true` during network error
- Confirm user stays on current screen

---

### Test Case 10: Signup Creates Session

**Objective**: Verify that signing up creates a persistent session.

**Preconditions**:
- User is on SignupFragment
- User provides valid registration data

**Steps**:
1. Fill in signup form with valid data
2. Click signup button
3. Wait for registration and authentication
4. Observe navigation

**Expected Results**:
- ✅ `AuthRepository.signUp()` succeeds
- ✅ Auth user created
- ✅ Volunteer profile created
- ✅ User is automatically logged in
- ✅ Session stored in SharedPreferences
- ✅ App navigates to DashboardFragment

**Verification Points**:
- Check `AuthRepository.signUp()` returns `Result.success()`
- Verify `supabaseClient.auth.currentUserOrNull()` is not null
- Confirm volunteer profile created in database
- Verify navigation to `R.id.dashboardFragment`

---

### Test Case 11: Session Not Restored If User On Auth Screen

**Objective**: Verify that session expiration does not redirect if user already on auth screen.

**Preconditions**:
- User is on LoginFragment or SignupFragment
- Session expires (shouldn't happen but test edge case)

**Steps**:
1. Navigate to LoginFragment manually (or be logged out)
2. Let session expire or become invalid
3. Observe behavior

**Expected Results**:
- ✅ No navigation occurs (already on auth screen)
- ✅ MainActivity does not trigger redundant navigation
- ✅ User can log in normally

**Verification Points**:
- Check `MainActivity.observeSessionStatus()` condition
- Verify `currentDestination != R.id.loginFragment` prevents navigation
- Confirm no crash or navigation loop

---

### Test Case 12: Back Button Behavior After Session Expiration

**Objective**: Verify that back button does not navigate to protected screens after session expiration.

**Preconditions**:
- User was logged in and using app
- Session expired
- User redirected to LoginFragment

**Steps**:
1. User logged in and on DashboardFragment
2. Session expires
3. App redirects to LoginFragment
4. User presses back button

**Expected Results**:
- ✅ Back button exits app (no back stack entries)
- ✅ User cannot navigate back to DashboardFragment
- ✅ Back stack was cleared during navigation

**Verification Points**:
- Check navigation options in `MainActivity.observeSessionStatus()`
- Verify `popUpTo(R.id.nav_graph) { inclusive = true }` clears back stack
- Confirm `launchSingleTop = true` prevents duplicate login screens

---

## Manual Testing Procedure

### Setup
1. Configure `local.properties` with valid Supabase credentials
2. Build and install debug APK
3. Enable developer options on test device
4. Ensure network connectivity

### Test Execution Order
1. Run Test Case 2 (No Session) first - clean install
2. Run Test Case 8 (Login Creates Session)
3. Run Test Case 5 (App Restart) 
4. Run Test Case 6 (Device Reboot) if possible
5. Run Test Case 7 (Logout)
6. Run Test Case 10 (Signup)
7. Run Test Case 1 (Valid Session on Startup)
8. Run Test Case 9 (Network Error)
9. Run Test Case 4 (Session Expiration) - may require Supabase dashboard access
10. Run Test Case 3 (Expired Session on Startup)
11. Run Test Cases 11-12 (Edge cases)

### Tools Needed
- Android Studio Logcat
- Supabase Dashboard (for manual session expiration)
- Android Device or Emulator (API 26+)

### Success Criteria
- ✅ All test cases pass
- ✅ No crashes observed
- ✅ Session persists across app restarts
- ✅ Session expiration handled gracefully
- ✅ User experience is smooth and intuitive

## Automated Testing Considerations

### Unit Tests
Create unit tests for:
- `AuthRepository.isLoggedIn()`
- `AuthRepository.getCurrentUserId()`
- `AuthRepository.observeSessionStatus()` with mocked SessionStatus

### Integration Tests
Create integration tests for:
- Login flow creates session
- Logout flow clears session
- Session check returns correct value

### UI Tests
Create UI tests for:
- Splash navigation to Dashboard (valid session)
- Splash navigation to Login (no session)
- Session expiration redirects to Login

### Notes
Automated testing requires mocking Supabase client or using a test backend instance.

## Regression Testing
Run all test cases after:
- Supabase SDK version updates
- Navigation library updates
- Android OS version changes
- Code changes to auth flow

## Known Limitations
- Session expiration timing depends on Supabase server configuration
- Manual session expiration requires Supabase dashboard access
- Network error simulation requires device configuration changes

## Conclusion
This test plan covers all requirements (3.1, 3.2, 3.3, 3.4) for session persistence. All test cases should pass to ensure the implementation is correct and robust.
