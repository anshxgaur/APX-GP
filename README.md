# 🌿 Smart Resource Allocation

**Data-Driven Volunteer Coordination for Social Impact**

A production-ready Kotlin Android application for NGO volunteer coordination. Volunteers receive tasks, submit GPS-tagged field surveys, track their impact, and receive real-time notifications — all backed by Supabase.

---

## 📱 Screenshots & Features

### 🔐 Authentication
- Email/password sign-up and login via Supabase Auth
- Session persistence — auto-login on app restart
- Animated splash screen

### 📊 Dashboard
- Personalized greeting with current date
- Impact stats: tasks completed & hours contributed
- Active task card with urgency indicator
- Top 3 urgent open tasks
- Recent surveys summary
- Pull-to-refresh

### 📋 Task Management
- **Open Tasks**: Browse and accept available tasks
- **Ongoing Tasks**: View your current assignments, add field notes
- **Completed Tasks**: Review past work with completion notes
- Tab-based navigation (Open / Ongoing / Completed)
- Urgency color system (Critical → Minor)
- Offline cache with Room DB fallback

### 📝 Field Surveys
- Category dropdown (Food Shortage, Medical Need, etc.)
- Severity slider (1–5)
- People affected counter
- GPS auto-capture with one-tap refresh
- Photo attachment with Supabase Storage upload
- Full form validation

### 🔔 Notifications
- Real-time notification list
- Unread indicators with dot badges
- Tap to mark as read
- Type badges (general, task_assigned, etc.)

### 👤 Profile
- Editable profile (name, phone, area)
- Skill chips selection
- Availability dropdown
- Photo upload
- Impact statistics
- Logout with confirmation

---

## 🏗️ Tech Stack

| Component          | Technology                              |
| ---                | ---                                     |
| **Language**       | Kotlin                                  |
| **Architecture**   | MVVM (ViewModel + StateFlow)            |
| **DI**             | Hilt (Dagger)                           |
| **Backend**        | Supabase (PostgreSQL + Auth + Storage)  |
| **Network**        | Ktor Android Client                     |
| **Local DB**       | Room Database (offline cache)           |
| **Navigation**     | Jetpack Navigation + Safe Args          |
| **Location**       | Google Play Services Location           |
| **Images**         | Glide                                   |
| **UI**             | Material Design 3 + ViewBinding         |
| **Animations**     | Lottie                                  |
| **Serialization**  | Kotlinx Serialization                   |
| **Async**          | Kotlin Coroutines + Flow                |
| **Min SDK**        | 26 (Android 8.0)                        |
| **Target SDK**     | 34 (Android 14)                         |
| **JDK**            | 17                                      |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Panda (2024.1+) or newer
- JDK 17
- A Supabase account ([supabase.com](https://supabase.com))
- A Google Maps API key (optional, for maps features)

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/SmartResourceAllocation.git
cd SmartResourceAllocation
```

### 2. Set Up Supabase

1. Create a new project at [app.supabase.com](https://app.supabase.com)
2. Go to **SQL Editor** and run the contents of [`supabase_setup.sql`](supabase_setup.sql)
3. This creates all tables, RLS policies, indexes, and storage buckets

### 3. Configure API Keys

Create a `local.properties` file in the project root (or copy from `local.properties.example`):

```properties
# Supabase (from your project dashboard → Settings → API)
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOi...your-anon-key

# Google Maps (optional)
MAPS_API_KEY=AIzaSy...your-maps-key
```

### 4. Build & Run
```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and click **Run**.

---

## 📁 Project Structure

```
app/src/main/java/com/yourname/sra/
├── SRAApplication.kt              # Hilt Application class
├── MainActivity.kt                # Single Activity with NavController
│
├── data/
│   ├── model/
│   │   └── Models.kt              # Volunteer, Survey, Task, AppNotification, DashboardData
│   ├── local/
│   │   ├── TaskEntity.kt          # Room entity + converters
│   │   ├── TaskDao.kt             # Room DAO
│   │   └── AppDatabase.kt         # Room database
│   ├── remote/
│   │   └── SupabaseClientProvider.kt  # Supabase client singleton
│   └── repository/
│       ├── AuthRepository.kt      # Sign up, login, logout, session check
│       ├── TaskRepository.kt      # CRUD tasks + Room cache
│       ├── SurveyRepository.kt    # Submit/fetch surveys + photo upload
│       ├── ProfileRepository.kt   # Get/update profile + photo upload
│       └── NotificationRepository.kt  # Fetch/mark-read notifications
│
├── di/
│   └── AppModule.kt               # Hilt dependency injection module
│
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt       # Login, signup, session state
│   │   ├── SplashFragment.kt      # Splash → Dashboard or Login
│   │   ├── LoginFragment.kt       # Email/password login
│   │   └── SignupFragment.kt      # Registration with skills/availability
│   ├── dashboard/
│   │   ├── DashboardViewModel.kt  # Aggregated dashboard data
│   │   └── DashboardFragment.kt   # Main dashboard screen
│   ├── tasks/
│   │   ├── TaskViewModel.kt       # Task list/detail/accept/complete
│   │   ├── TaskListFragment.kt    # Tabbed task list
│   │   ├── TaskDetailFragment.kt  # Task detail with actions
│   │   └── TaskAdapter.kt         # RecyclerView adapter
│   ├── survey/
│   │   ├── SurveyViewModel.kt     # Survey form + photo upload
│   │   └── SurveyFragment.kt      # Survey submission form
│   ├── profile/
│   │   ├── ProfileViewModel.kt    # Profile CRUD + logout
│   │   └── ProfileFragment.kt     # Editable profile screen
│   └── notifications/
│       ├── NotificationViewModel.kt   # Notification list + unread count
│       ├── NotificationFragment.kt    # Notification screen
│       └── NotificationAdapter.kt     # RecyclerView adapter
│
└── utils/
    ├── UiState.kt                 # Sealed class: Loading, Success, Error, Empty
    ├── LocationHelper.kt          # GPS location wrapper
    ├── Extensions.kt              # View, String, Date, UI extensions
    └── Constants.kt               # App-wide constants
```

---

## 🎨 Design System

### Color Palette
| Name       | Hex       | Usage                       |
| ---        | ---       | ---                         |
| Primary    | `#2D6A4F` | Main brand, buttons, links  |
| Accent     | `#52B788` | Secondary actions, success   |
| Background | `#F0F4F8` | Screen backgrounds           |
| Text       | `#1A1A2E` | Primary text                 |

### Urgency Colors
| Level    | Color     | Code      |
| ---      | ---       | ---       |
| Critical | 🔴 Red    | `#D62828` |
| High     | 🟠 Orange | `#F77F00` |
| Moderate | 🟡 Yellow | `#FCBF49` |
| Low      | 🟢 Green  | `#52B788` |
| Minor    | ⚪ Gray   | `#ADB5BD` |

---

## 🔒 Security

- **Row Level Security (RLS)** enabled on all Supabase tables
- Volunteers can only read/modify their own data
- Tasks are readable by all authenticated users
- Auth tokens managed by Supabase SDK
- API keys stored in `local.properties` (not committed to git)

---

## 📄 License

This project is licensed under the MIT License.