# Quick Start Testing Guide
**For Immediate Device Testing**

## Step 1: Install APK (5 minutes)

### Option A: Using ADB (Recommended)
```bash
# Connect your Android device via USB
# Enable USB Debugging on device: Settings → Developer Options → USB Debugging

# Install APK
C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe install app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Manual Installation
1. Copy `app/build/outputs/apk/debug/app-debug.apk` to your device
2. Open file manager on device
3. Tap the APK file
4. Allow "Install from Unknown Sources" if prompted
5. Tap "Install"

## Step 2: Grant Permissions (2 minutes)

When app prompts, grant:
- ✅ Location (for GPS in surveys)
- ✅ Camera (for photo capture)
- ✅ Storage (for photo selection)

## Step 3: Critical 5-Minute Smoke Test

### Test A: Authentication (2 mins)
1. Launch app
2. Tap "Sign Up"
3. Create account:
   - Email: `test@example.com`
   - Password: `Test123!`
   - Fill other fields
4. Should auto-login to dashboard
5. ✅ **PASS** if you see dashboard

### Test B: Survey Submission (2 mins)
1. Navigate to "New Survey"
2. Grant location permission
3. Wait for GPS coordinates
4. Fill minimal form:
   - Category: "Flood"
   - Severity: 3
   - Description: "Test"
   - Location: "Test Location"
5. Tap "Add Photo" → Select any image
6. Tap "Submit"
7. ✅ **PASS** if success message appears

### Test C: Task Management (1 min)
1. Navigate to "Tasks"
2. View task list
3. Tap any task
4. Tap "Accept Task"
5. ✅ **PASS** if status changes to "Ongoing"

## Step 4: If All Pass → ✅ APP WORKS!

You're ready for the hackathon! Proceed to:
- Prepare demo data
- Rehearse demo flow
- Test on presentation device

## Step 5: If Any Fail → Debug

### Common Issues:

**No GPS Coordinates**:
- Ensure location services enabled: Settings → Location → On
- Go outside or near window for better GPS signal
- Wait 10-15 seconds for initial GPS lock

**Photo Upload Fails**:
- Check internet connection
- Ensure storage permission granted
- Try smaller image file

**Login/Signup Fails**:
- Check internet connection
- Verify Supabase backend is operational
- Try different email address

**App Crashes**:
- Clear app data: Settings → Apps → APX-GP → Clear Data
- Reinstall APK
- Check device meets requirements (Android 8.0+)

**Network Error**:
- Verify WiFi/mobile data is on
- Check Supabase URL is accessible: https://svtlldsfpahjxrttdnkj.supabase.co
- Try different network

## Emergency Contact

If critical issues prevent testing:
1. Check `E2E_TEST_EXECUTION_RESULTS.md` for known limitations
2. Review `FINAL_INTEGRATION_TEST_REPORT.md` for troubleshooting
3. Refer to `E2E_TEST_PLAN.md` for detailed test procedures

## Test Credentials

**Primary Test User**:
- Email: `testuser1@example.com`
- Password: `TestPass123!`

**Secondary Test User** (for realtime testing):
- Email: `testuser2@example.com`
- Password: `TestPass123!`

---

**Quick Test Duration**: 5-10 minutes  
**Full Test Duration**: 45-90 minutes (see E2E_TEST_PLAN.md)  
**Demo Rehearsal**: 15-20 minutes (see FINAL_INTEGRATION_TEST_REPORT.md)
