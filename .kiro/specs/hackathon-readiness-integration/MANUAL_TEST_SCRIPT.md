# Manual Testing Script - APX-GP Android App
**Task 30: Final Integration and End-to-End Testing**  
**Version**: 1.0  
**Duration**: 90 minutes (complete), 45 minutes (critical only)

## Prerequisites

### Device Setup
- [ ] Android device with Android 8.0+ (API 26+)
- [ ] Location services enabled
- [ ] Camera permission available
- [ ] Storage permission available
- [ ] Stable WiFi/mobile data connection
- [ ] APK installed: `app/build/outputs/apk/debug/app-debug.apk`

### Backend Setup
- [ ] Supabase backend operational: https://svtlldsfpahjxrttdnkj.supabase.co
- [ ] Test user credentials prepared
- [ ] Sample data loaded (optional but recommended)

### Test Environment
- [ ] Clear app data before starting: Settings → Apps → APX-GP → Clear Data
- [ ] Device battery > 50%
- [ ] Screen timeout set to max

## Test Credentials

**Test User 1**:
- Email: `testuser1@example.com`
- Password: `TestPass123!`

**Test User 2** (for realtime testing):
- Email: `testuser2@example.com`
- Password: `TestPass123!`

---

## PHASE 1: AUTHENTICATION FLOW (15 mins) ⭐ CRITICAL

### Test 1.1: User Signup ✅ Requirements 2.1, 2.2
**Duration**: 3 minutes

**Steps**:
1. Launch app
2. Verify splash screen appears
3. Wait for navigation (should go to login if no session)
4. Tap "Sign Up" or navigate to signup screen
5. Enter the following:
   - Email: `testuser1@example.com`
   - Password: `TestPass123!`
   - Full Name: `Test User One`
   - Phone: `+1234567890`
   - Area: `Los Angeles`
   - Skills: Select 2-3 skills from list
   - Availability: `Weekdays`
6. Tap "Sign Up" button
7. Wait for processing

**Expected Results**:
- [ ] ✅ Signup succeeds
- [ ] ✅ User automatically logged in
- [ ] ✅ Navigates to dashboard
- [ ] ✅ Dashboard shows user name
- [ ] ✅ No crashes or errors

**If Fails**: Note error message and screenshot

---

### Test 1.2: User Logout ✅ Requirements 2.7, 2.8, 33, 35.2
**Duration**: 2 minutes

**Steps**:
1. From dashboard, tap menu icon
2. Tap "Logout" option
3. Confirm logout if prompted
4. Verify navigation to login screen
5. Press back button

**Expected Results**:
- [ ] ✅ Navigates to login screen
- [ ] ✅ Back button exits app (doesn't return to dashboard)
- [ ] ✅ No crashes

---

### Test 1.3: User Login ✅ Requirements 2.4, 2.5
**Duration**: 2 minutes

**Steps**:
1. Launch app (after logout from previous test)
2. Verify login screen appears
3. Enter credentials:
   - Email: `testuser1@example.com`
   - Password: `TestPass123!`
4. Tap "Login" button
5. Wait for authentication

**Expected Results**:
- [ ] ✅ Login succeeds
- [ ] ✅ Navigates to dashboard
- [ ] ✅ Profile data loaded correctly
- [ ] ✅ No crashes

---

### Test 1.4: Invalid Login ✅ Requirement 2.6
**Duration**: 2 minutes

**Steps**:
1. Logout again
2. Enter incorrect credentials:
   - Email: `testuser1@example.com`
   - Password: `WrongPassword123`
3. Tap "Login" button

**Expected Results**:
- [ ] ❌ Login fails
- [ ] ✅ Error message displayed: "Invalid email or password"
- [ ] ✅ Remains on login screen
- [ ] ✅ No crash

---

### Test 1.5: Session Persistence ✅ Requirements 3.1, 3.2
**Duration**: 3 minutes

**Steps**:
1. Login successfully (use correct credentials)
2. Close app completely (swipe from recent apps)
3. Wait 5 seconds
4. Reopen app
5. Observe behavior

**Expected Results**:
- [ ] ✅ Splash screen appears
- [ ] ✅ Directly navigates to dashboard (no login prompt)
- [ ] ✅ User data still loaded
- [ ] ✅ No crashes

**Status**: Phase 1 ⬜ PASS / ⬜ FAIL / ⬜ PARTIAL

---

## PHASE 2: SURVEY SUBMISSION (15 mins) ⭐ CRITICAL

### Test 2.1: GPS Coordinate Capture ✅ Requirements 30.1, 30.2, 30.5
**Duration**: 3 minutes

**Steps**:
1. From dashboard, navigate to "Submit Survey" or "New Survey"
2. Observe location permission request if first time
3. Grant location permission
4. Wait for GPS coordinates to load

**Expected Results**:
- [ ] ✅ Location permission prompt appears
- [ ] ✅ GPS coordinates captured (latitude/longitude shown)
- [ ] ✅ Location name or coordinates displayed
- [ ] ✅ No crash if GPS takes time

**If GPS fails**: Error message should show "Unable to get GPS location"

---

### Test 2.2: Survey with Photo Submission ✅ Requirements 5, 28, 29, 30
**Duration**: 8 minutes

**Steps**:
1. On survey form, fill in:
   - Category: Select "Flood" from dropdown
   - Severity: Select 4 (slider or picker)
   - People Affected: Enter `50`
   - Description: Enter "Test flood survey with photo"
   - Location Name: Enter "Downtown LA"
2. Tap "Add Photo" or camera icon
3. Choose "Camera" or "Gallery"
4. Select/capture a photo
5. Verify photo preview appears
6. Verify GPS coordinates are shown
7. Tap "Submit Survey"
