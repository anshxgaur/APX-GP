# Smart Resource Allocation — Complete Production Architecture Guide
### Data-Driven Volunteer Coordination for Social Impact
**Version 1.0 | For a Team of 6 | GitHub Student Pack Constrained**

---

## TABLE OF CONTENTS

1. [Project Overview & Refined Scope](#1-project-overview)
2. [System Architecture (High-Level)](#2-system-architecture)
3. [Tech Stack — Every Layer](#3-tech-stack)
4. [Team Role Allocation](#4-team-roles)
5. [Database Design](#5-database-design)
6. [Backend API Design](#6-backend-api)
7. [Admin Web App — Full Specification](#7-admin-web-app)
8. [Volunteer Mobile App — Full Specification](#8-volunteer-mobile-app)
9. [AI/ML Pipeline — Survey Intelligence & Heatmap Engine](#9-aiml-pipeline)
10. [Authentication & Security](#10-authentication--security)
11. [DevOps, CI/CD & Infrastructure](#11-devops-cicd--infrastructure)
12. [Data Pipelines & Event Streaming](#12-data-pipelines)
13. [Notifications & Real-Time System](#13-notifications--real-time)
14. [Failure Modes & Resilience Design](#14-failure-modes--resilience)
15. [Phase-by-Phase Development Roadmap](#15-development-roadmap)
16. [Future Modifications & Scale-Up Plan](#16-future-modifications)
17. [Hidden Details Every Team Misses](#17-hidden-details)
18. [Cost & GitHub Student Pack Resource Map](#18-cost--resource-map)

---

## 1. PROJECT OVERVIEW

### What You're Really Building

Three interconnected systems:

```
┌─────────────────────┐     ┌──────────────────────────┐     ┌──────────────────────┐
│   VOLUNTEER APP     │────▶│    BACKEND + AI ENGINE   │────▶│   ADMIN WEB PORTAL   │
│  (React Native)     │◀────│  (Node.js + Python ML)   │◀────│   (React.js + Maps)  │
│                     │     │                          │     │                      │
│ • Survey intake     │     │ • Auth, API, DB           │     │ • Heatmap dashboard  │
│ • Task view         │     │ • AI insight extraction   │     │ • Task assignment    │
│ • Location push     │     │ • Heatmap computation     │     │ • Volunteer mgmt     │
│ • Offline support   │     │ • Notification engine     │     │ • Reports/analytics  │
└─────────────────────┘     └──────────────────────────┘     └──────────────────────┘
```

### Refined Scope Additions (Architect Recommendations)

| Original Feature | Recommended Addition | Why |
|---|---|---|
| Surveys only | + Field photo uploads | Visual proof increases data quality |
| Admin assigns tasks | + Auto-suggest assignments via AI | Reduces admin workload |
| Heatmap view | + Trend heatmap (time-series) | Shows if problem is growing/shrinking |
| Volunteer task view | + Volunteer availability self-reporting | Better scheduling |
| No offline | + Offline survey submission | Field areas often have poor connectivity |
| No audit trail | + Full activity log for all admin actions | Accountability in NGO context |
| No analytics | + CSV/PDF report export | Donors, stakeholders need reports |

---

## 2. SYSTEM ARCHITECTURE

### Complete Architecture Diagram (Text)

```
                        ┌───────────────────────────────────────────────────────┐
                        │                   CLOUD INFRASTRUCTURE                │
                        │                  (Railway + Supabase)                 │
                        │                                                       │
   ┌──────────────┐     │  ┌──────────────┐    ┌──────────────┐                │
   │ ADMIN BROWSER│────▶│  │  React Admin │    │  Cloudflare  │                │
   │  (Desktop/   │◀────│  │  (Vite/SPA)  │    │   CDN/Pages  │                │
   │   Tablet)    │     │  └──────┬───────┘    └──────────────┘                │
   └──────────────┘     │         │                                             │
                        │         ▼                                             │
   ┌──────────────┐     │  ┌──────────────────────────────────────────────┐    │
   │  VOLUNTEER   │     │  │              API GATEWAY (Express.js)         │    │
   │  MOBILE APP  │────▶│  │          Rate limiting, JWT validation        │    │
   │(React Native)│◀────│  │          Request routing, CORS, Logging       │    │
   └──────────────┘     │  └──────────────┬───────────────────────────────┘    │
                        │                 │                                     │
                        │    ┌────────────┼────────────┐                       │
                        │    ▼            ▼            ▼                       │
                        │ ┌──────┐   ┌──────┐   ┌──────────┐                  │
                        │ │Auth  │   │Core  │   │AI/ML     │                  │
                        │ │Service│  │API   │   │Service   │                  │
                        │ │(JWT/ │   │(CRUD)│   │(Python/  │                  │
                        │ │OAuth)│   │      │   │FastAPI)  │                  │
                        │ └──┬───┘   └──┬───┘   └────┬─────┘                  │
                        │    │          │             │                        │
                        │    └──────────┼─────────────┘                       │
                        │               ▼                                     │
                        │  ┌────────────────────────────────────┐             │
                        │  │         Supabase (PostgreSQL)       │             │
                        │  │  + Row Level Security + Realtime    │             │
                        │  │  + Storage (photos/attachments)     │             │
                        │  └────────────────────────────────────┘             │
                        │                                                      │
                        │  ┌────────────┐   ┌────────────┐  ┌──────────────┐  │
                        │  │  Redis     │   │  BullMQ    │  │  Resend      │  │
                        │  │  (Cache)   │   │  (Queues)  │  │  (Email)     │  │
                        │  └────────────┘   └────────────┘  └──────────────┘  │
                        │                                                      │
                        │  ┌─────────────────────────────────────────────┐    │
                        │  │       Firebase Cloud Messaging (FCM)         │    │
                        │  │       Push Notifications to Mobile           │    │
                        │  └─────────────────────────────────────────────┘    │
                        └───────────────────────────────────────────────────────┘
```

### Data Flow — Survey to Heatmap

```
Volunteer fills survey (Mobile App)
          │
          ▼
Survey data saved locally (SQLite) ──offline──┐
          │ online                             │
          ▼                                   │
POST /api/surveys ──────────────────────────◀─┘
          │
          ▼
Survey stored in PostgreSQL (raw)
          │
          ▼
BullMQ job created: "process-survey"
          │
          ▼
Python ML Worker picks up job:
  • NLP: extract key issues, severity scores
  • Geocode GPS coordinates
  • Update heatmap_cells table
  • Tag categories (water/food/health/etc.)
          │
          ▼
Supabase Realtime event emitted
          │
    ┌─────┴──────┐
    ▼            ▼
Admin Dashboard  Heatmap recomputed
updates live     and served via API
```

---

## 3. TECH STACK

### Frontend — Admin Web Portal

| Layer | Technology | Reason |
|---|---|---|
| Framework | **React 18 + Vite** | Fast dev, HMR, small bundle |
| Language | **TypeScript** | Type safety, fewer runtime bugs |
| UI Library | **shadcn/ui + Tailwind CSS** | Production-quality, accessible components |
| Maps/Heatmap | **Mapbox GL JS** | Free tier generous, best heatmap layer support |
| Charts | **Recharts** | Lightweight, composable |
| State | **Zustand** | Simpler than Redux, works well with async |
| Data fetching | **TanStack Query (React Query)** | Cache, background refresh, optimistic UI |
| Forms | **React Hook Form + Zod** | Validation + schema typing |
| Auth | **Supabase Auth client** | Matches backend |
| Table | **TanStack Table** | Virtualized, sortable, filterable |
| Export | **jsPDF + xlsx** | Client-side PDF/Excel export |

### Frontend — Volunteer Mobile App

| Layer | Technology | Reason |
|---|---|---|
| Framework | **React Native (Expo SDK 51+)** | Single codebase iOS+Android, free OTA updates |
| Language | **TypeScript** | Consistency with web codebase |
| Navigation | **Expo Router (file-based)** | Modern, typed routing |
| UI | **NativeWind + React Native Paper** | Tailwind-like styling on native |
| Maps | **React Native Maps + Expo Location** | OS-native map rendering |
| Offline DB | **WatermelonDB** | High-performance local SQLite, sync-ready |
| Camera | **Expo Camera + ImagePicker** | Photo capture for field evidence |
| State | **Zustand** | Same as web — team familiarity |
| Push Notifs | **Expo Notifications + FCM** | Cross-platform push |
| Auth | **Supabase Auth + SecureStore** | JWT stored securely on device |
| Forms | **React Hook Form** | Same as web |

### Backend — Core API

| Layer | Technology | Reason |
|---|---|---|
| Runtime | **Node.js 20 LTS** | Stable, large ecosystem |
| Framework | **Express.js** | Simple, well-understood |
| Language | **TypeScript** | Type safety across full stack |
| ORM | **Prisma** | Type-safe queries, migration system |
| Database | **PostgreSQL 15 via Supabase** | Free, managed, has Realtime built-in |
| Auth | **Supabase Auth** | Handles JWT, refresh tokens, OAuth |
| Cache | **Redis via Upstash** | Serverless Redis, free tier |
| Queue | **BullMQ** | Job queue on top of Redis |
| Validation | **Zod** | Schema validation shared with frontend |
| Logging | **Pino** | Fast, structured JSON logs |
| API Docs | **Swagger/OpenAPI** | Auto-generated, keeps team in sync |

### Backend — AI/ML Service

| Layer | Technology | Reason |
|---|---|---|
| Runtime | **Python 3.11** | ML ecosystem |
| Framework | **FastAPI** | Async, automatic OpenAPI docs |
| NLP | **spaCy + HuggingFace Transformers** | Issue extraction, classification |
| Sentiment | **VADER + distilbert-base** | Survey response severity scoring |
| Geocoding | **Geopy + Nominatim** | Convert addresses to GPS, free |
| Heatmap Math | **NumPy + SciPy (KDE)** | Kernel Density Estimation for heatmap |
| Task Queue | **Celery + Redis** | Async ML processing |
| Data | **Pandas** | Data processing |
| Model Serving | **ONNX Runtime** | Fast inference for deployed models |

### Infrastructure & DevOps

| Layer | Technology | Reason |
|---|---|---|
| Hosting (API) | **Railway** | GitHub Student Pack free credits |
| Hosting (Web) | **Cloudflare Pages** | Free, global CDN, fast |
| Database | **Supabase** | Free tier, managed Postgres + Realtime |
| Storage | **Supabase Storage** | Photo/file uploads, free tier |
| Redis | **Upstash** | Serverless Redis, free tier |
| Email | **Resend** | 3000 free emails/month |
| Push Notifs | **Firebase FCM** | Free push notifications |
| Mobile OTA | **Expo EAS** | Free tier OTA updates |
| CI/CD | **GitHub Actions** | Free with student pack |
| Monitoring | **Better Stack (Logtail)** | Free log ingestion + uptime |
| Error Tracking | **Sentry** | Free tier, both web and mobile |
| Secret Management | **Railway Env Vars + GitHub Secrets** | Secure, free |
| API Rate Limiting | **express-rate-limit + Redis** | Prevent abuse |

---

## 4. TEAM ROLES

### Recommended Split (6 people)

```
┌─────────────────────────────────────────────────────────────────┐
│ Person 1 — Frontend Web Lead                                     │
│   Admin portal (React), Heatmap (Mapbox), Dashboard UI          │
│   Also: Design system, component library                         │
├─────────────────────────────────────────────────────────────────┤
│ Person 2 — Mobile App Lead                                       │
│   React Native/Expo app, Offline sync, Survey UI                │
│   Also: Push notification integration                            │
├─────────────────────────────────────────────────────────────────┤
│ Person 3 — Backend Lead                                          │
│   Express API, Auth, Prisma ORM, REST endpoints                  │
│   Also: Supabase Realtime, API documentation                    │
├─────────────────────────────────────────────────────────────────┤
│ Person 4 — Database & Data Engineer                              │
│   Schema design, migrations, indexes, RLS policies               │
│   Also: BullMQ job queues, data pipelines, Redis                │
├─────────────────────────────────────────────────────────────────┤
│ Person 5 — AI/ML Engineer                                        │
│   FastAPI ML service, NLP survey processing, heatmap algo        │
│   Also: Model training, ONNX export, insight extraction          │
├─────────────────────────────────────────────────────────────────┤
│ Person 6 — DevOps + QA Lead                                      │
│   CI/CD pipelines, Railway deploy, monitoring, Sentry            │
│   Also: Integration testing, load testing, security audit        │
└─────────────────────────────────────────────────────────────────┘
```

**Shared Responsibilities (All 6):**
- Weekly code review rotation
- API contract definition (Persons 1,2,3 together first week)
- Writing tests for your own module

---

## 5. DATABASE DESIGN

### Complete Schema

```sql
-- ============================================
-- USERS (Managed by Supabase Auth — extended)
-- ============================================
CREATE TABLE profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email TEXT UNIQUE NOT NULL,
  full_name TEXT NOT NULL,
  phone TEXT,
  role TEXT NOT NULL CHECK (role IN ('admin', 'volunteer', 'super_admin')),
  avatar_url TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- ORGANIZATIONS (NGOs/Social Groups)
-- ============================================
CREATE TABLE organizations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  description TEXT,
  logo_url TEXT,
  address TEXT,
  contact_email TEXT,
  created_by UUID REFERENCES profiles(id),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- VOLUNTEERS (extended profile)
-- ============================================
CREATE TABLE volunteers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  profile_id UUID UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,
  org_id UUID REFERENCES organizations(id),
  skills TEXT[],               -- ['medical', 'driving', 'teaching']
  languages TEXT[],            -- ['hindi', 'english']
  availability JSONB,          -- {"mon": ["09:00","17:00"], ...}
  current_location GEOGRAPHY(POINT, 4326),
  location_updated_at TIMESTAMPTZ,
  total_tasks_completed INT DEFAULT 0,
  rating DECIMAL(3,2),
  is_available BOOLEAN DEFAULT TRUE,
  emergency_contact TEXT,
  device_token TEXT,           -- FCM push token
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- SURVEY TEMPLATES (Admin-created)
-- ============================================
CREATE TABLE survey_templates (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID REFERENCES organizations(id),
  title TEXT NOT NULL,
  description TEXT,
  fields JSONB NOT NULL,        -- Dynamic field definitions
  category TEXT NOT NULL CHECK (category IN (
    'water', 'food', 'health', 'shelter', 'education', 'safety', 'infrastructure', 'other'
  )),
  is_active BOOLEAN DEFAULT TRUE,
  version INT DEFAULT 1,
  created_by UUID REFERENCES profiles(id),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- fields JSONB structure example:
-- [
--   {"id": "q1", "type": "text", "label": "Describe the issue", "required": true},
--   {"id": "q2", "type": "select", "label": "Severity", "options": ["low","medium","high","critical"], "required": true},
--   {"id": "q3", "type": "number", "label": "Approx people affected", "required": false},
--   {"id": "q4", "type": "photo", "label": "Upload photo evidence", "required": false},
--   {"id": "q5", "type": "location", "label": "Mark location on map", "required": true}
-- ]

-- ============================================
-- SURVEY RESPONSES
-- ============================================
CREATE TABLE survey_responses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_id UUID REFERENCES survey_templates(id),
  volunteer_id UUID REFERENCES volunteers(id),
  org_id UUID REFERENCES organizations(id),
  responses JSONB NOT NULL,    -- {"q1": "broken pipe", "q2": "high", ...}
  location GEOGRAPHY(POINT, 4326) NOT NULL,
  location_address TEXT,
  photos TEXT[],               -- Supabase storage URLs
  submitted_at TIMESTAMPTZ DEFAULT NOW(),
  sync_status TEXT DEFAULT 'synced' CHECK (sync_status IN ('synced', 'pending', 'failed')),
  device_submitted_at TIMESTAMPTZ,  -- when volunteer actually submitted (offline case)
  
  -- AI processed fields
  ai_processed BOOLEAN DEFAULT FALSE,
  ai_severity_score DECIMAL(4,2),    -- 0.0 to 10.0
  ai_category TEXT,
  ai_extracted_issues TEXT[],        -- ['broken_pipe', 'no_water_access']
  ai_sentiment TEXT,                 -- 'positive'|'negative'|'neutral'
  ai_summary TEXT,
  processed_at TIMESTAMPTZ
);

-- ============================================
-- HEATMAP CELLS (Pre-computed grid)
-- ============================================
CREATE TABLE heatmap_cells (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID REFERENCES organizations(id),
  geohash TEXT NOT NULL,          -- Geohash for grid cell identifier
  lat_center DECIMAL(9,6),
  lng_center DECIMAL(9,6),
  category TEXT NOT NULL,
  intensity DECIMAL(5,2) DEFAULT 0,   -- 0-100 scale
  response_count INT DEFAULT 0,
  avg_severity DECIMAL(4,2),
  last_updated TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(org_id, geohash, category)
);

CREATE INDEX idx_heatmap_geohash ON heatmap_cells(geohash);
CREATE INDEX idx_heatmap_category ON heatmap_cells(category);

-- ============================================
-- TASKS
-- ============================================
CREATE TABLE tasks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id UUID REFERENCES organizations(id),
  created_by UUID REFERENCES profiles(id),
  title TEXT NOT NULL,
  description TEXT,
  category TEXT NOT NULL,
  priority TEXT NOT NULL CHECK (priority IN ('low', 'medium', 'high', 'critical')),
  status TEXT NOT NULL DEFAULT 'open' CHECK (status IN (
    'open', 'assigned', 'in_progress', 'completed', 'cancelled', 'on_hold'
  )),
  location GEOGRAPHY(POINT, 4326),
  location_address TEXT,
  radius_meters INT DEFAULT 500,      -- Task coverage radius
  required_skills TEXT[],
  estimated_hours DECIMAL(4,1),
  deadline TIMESTAMPTZ,
  
  -- Linked data
  survey_response_ids UUID[],         -- Which surveys triggered this task
  
  -- Completion
  completed_at TIMESTAMPTZ,
  completion_notes TEXT,
  completion_photos TEXT[],
  
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- TASK ASSIGNMENTS
-- ============================================
CREATE TABLE task_assignments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
  volunteer_id UUID REFERENCES volunteers(id),
  assigned_by UUID REFERENCES profiles(id),
  assigned_at TIMESTAMPTZ DEFAULT NOW(),
  accepted_at TIMESTAMPTZ,
  declined_at TIMESTAMPTZ,
  decline_reason TEXT,
  started_at TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  status TEXT DEFAULT 'pending' CHECK (status IN (
    'pending', 'accepted', 'declined', 'in_progress', 'completed'
  )),
  volunteer_notes TEXT,
  admin_feedback TEXT,
  rating INT CHECK (rating BETWEEN 1 AND 5)
);

-- ============================================
-- NOTIFICATIONS
-- ============================================
CREATE TABLE notifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  recipient_id UUID REFERENCES profiles(id),
  org_id UUID REFERENCES organizations(id),
  type TEXT NOT NULL,             -- 'task_assigned', 'task_updated', 'survey_processed', etc.
  title TEXT NOT NULL,
  body TEXT NOT NULL,
  data JSONB,                     -- Extra payload for deep linking
  is_read BOOLEAN DEFAULT FALSE,
  push_sent BOOLEAN DEFAULT FALSE,
  push_sent_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- AUDIT LOG (ALL admin actions)
-- ============================================
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_id UUID REFERENCES profiles(id),
  org_id UUID REFERENCES organizations(id),
  action TEXT NOT NULL,
  resource_type TEXT NOT NULL,
  resource_id UUID,
  old_values JSONB,
  new_values JSONB,
  ip_address INET,
  user_agent TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- ROW LEVEL SECURITY (CRITICAL for multi-org)
-- ============================================
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE survey_responses ENABLE ROW LEVEL SECURITY;

-- Volunteers only see their org's data
CREATE POLICY "volunteers_own_org" ON tasks
  FOR SELECT USING (
    org_id IN (
      SELECT org_id FROM volunteers WHERE profile_id = auth.uid()
    )
  );

-- Admins see all data for their org
CREATE POLICY "admins_own_org" ON tasks
  FOR ALL USING (
    org_id IN (
      SELECT o.id FROM organizations o
      JOIN profiles p ON p.id = auth.uid()
      WHERE p.role IN ('admin', 'super_admin')
    )
  );
```

### Key Indexes for Performance

```sql
-- Survey queries
CREATE INDEX idx_surveys_org ON survey_responses(org_id);
CREATE INDEX idx_surveys_volunteer ON survey_responses(volunteer_id);
CREATE INDEX idx_surveys_location ON survey_responses USING GIST(location);
CREATE INDEX idx_surveys_submitted ON survey_responses(submitted_at DESC);
CREATE INDEX idx_surveys_unprocessed ON survey_responses(ai_processed) WHERE ai_processed = FALSE;

-- Task queries
CREATE INDEX idx_tasks_org_status ON tasks(org_id, status);
CREATE INDEX idx_tasks_location ON tasks USING GIST(location);
CREATE INDEX idx_tasks_priority ON tasks(priority, status);

-- Volunteer location
CREATE INDEX idx_volunteers_location ON volunteers USING GIST(current_location);
```

---

## 6. BACKEND API

### API Structure

```
/api/v1
├── /auth
│   ├── POST /register          (admin registration)
│   ├── POST /login             (admin login)
│   ├── POST /logout
│   ├── POST /refresh-token
│   ├── POST /forgot-password
│   └── POST /reset-password
│
├── /volunteers                 (admin-only routes)
│   ├── GET  /                  list all volunteers
│   ├── GET  /:id               volunteer detail
│   ├── POST /invite            invite volunteer by email
│   ├── PUT  /:id/status        activate/deactivate
│   └── GET  /:id/tasks         volunteer's task history
│
├── /volunteer-app              (mobile app routes)
│   ├── POST /auth/login        volunteer login
│   ├── GET  /profile           own profile
│   ├── PUT  /profile           update profile/availability
│   ├── PUT  /location          update GPS location
│   ├── GET  /tasks             assigned tasks
│   ├── PUT  /tasks/:id/accept  accept a task
│   ├── PUT  /tasks/:id/decline decline with reason
│   ├── PUT  /tasks/:id/start   mark in progress
│   ├── PUT  /tasks/:id/complete complete with notes/photos
│   └── GET  /notifications     own notifications
│
├── /surveys
│   ├── GET  /templates         get active templates (mobile)
│   ├── POST /                  submit survey response (mobile)
│   ├── GET  /                  list responses (admin)
│   ├── GET  /:id               response detail
│   └── GET  /export            CSV export (admin)
│
├── /tasks
│   ├── GET  /                  list tasks (admin)
│   ├── POST /                  create task
│   ├── GET  /:id               task detail
│   ├── PUT  /:id               update task
│   ├── DELETE /:id             cancel task
│   ├── POST /:id/assign        assign to volunteer(s)
│   └── GET  /suggestions       AI-suggested assignments
│
├── /heatmap
│   ├── GET  /                  heatmap cells (with filters)
│   ├── GET  /summary           category breakdown
│   └── GET  /trends            time-series trend data
│
├── /analytics
│   ├── GET  /overview          dashboard stats
│   ├── GET  /volunteers        volunteer performance
│   ├── GET  /tasks             task completion stats
│   └── GET  /surveys           survey volume stats
│
├── /organizations
│   ├── GET  /current           current org details
│   └── PUT  /current           update org settings
│
└── /admin
    ├── GET  /audit-logs        all admin actions
    ├── GET  /survey-templates  manage templates
    ├── POST /survey-templates  create template
    └── PUT  /survey-templates/:id update template
```

### Middleware Stack

```typescript
// Order matters — this is the exact chain:
app.use(helmet())               // Security headers
app.use(cors(corsOptions))      // CORS with whitelist
app.use(compression())          // Gzip responses
app.use(express.json({ limit: '10mb' }))  // Body parsing
app.use(requestLogger)          // Pino request logging
app.use(rateLimiter)            // Redis-based rate limiting
app.use(requestId)              // Add X-Request-ID header
// Route-specific:
router.use(authenticate)        // JWT verification
router.use(authorize(roles))    // Role-based access control
router.use(auditLogger)         // Log all state-changing actions
```

---

## 7. ADMIN WEB APP

### Page Structure

```
/app (protected — requires admin login)
│
├── /dashboard          ← Landing page after login
│   ├── Stats cards (active volunteers, open tasks, surveys today)
│   ├── Heatmap (main view — full width)
│   ├── Recent surveys feed
│   └── AI-generated daily summary
│
├── /heatmap            ← Dedicated heatmap view
│   ├── Full-screen Mapbox map
│   ├── Category filter (water/food/health/etc.)
│   ├── Date range picker
│   ├── Intensity legend
│   ├── Click cell → see survey responses from that area
│   └── Time-lapse slider (see how hotspots evolved)
│
├── /volunteers
│   ├── /                List view (table + map pins)
│   ├── /:id             Volunteer profile + task history
│   └── /invite          Send invite email
│
├── /tasks
│   ├── /                Kanban board (open/assigned/in_progress/done)
│   ├── /create          Create new task
│   ├── /:id             Task detail + assignment panel
│   └── /map             Tasks shown on map
│
├── /surveys
│   ├── /                Response list (filterable)
│   ├── /:id             Response detail with AI insights
│   ├── /templates       Manage survey forms
│   └── /analytics       Survey data charts
│
├── /analytics           ← Reports & exports
│   ├── Volunteer performance
│   ├── Task completion rates
│   └── Export (CSV/PDF)
│
└── /settings
    ├── Organization profile
    ├── Admin user management
    ├── Survey template builder
    └── Notification preferences
```

### Heatmap Implementation Details

```javascript
// Mapbox GL JS Heatmap Layer Configuration
map.addLayer({
  id: 'heatmap-layer',
  type: 'heatmap',
  source: 'heatmap-data',  // GeoJSON from /api/v1/heatmap
  paint: {
    'heatmap-weight': [
      'interpolate', ['linear'],
      ['get', 'intensity'],
      0, 0,
      10, 1
    ],
    'heatmap-intensity': [
      'interpolate', ['linear'],
      ['zoom'],
      0, 1,
      15, 3
    ],
    'heatmap-color': [
      'interpolate', ['linear'],
      ['heatmap-density'],
      0, 'rgba(33,102,172,0)',
      0.2, 'rgb(103,169,207)',
      0.4, 'rgb(253,219,199)',
      0.6, 'rgb(239,138,98)',
      1, 'rgb(178,24,43)'    // Red = most critical
    ],
    'heatmap-radius': ['interpolate', ['linear'], ['zoom'], 0, 2, 15, 30],
    'heatmap-opacity': 0.8
  }
});

// Category-specific color schemes:
const CATEGORY_COLORS = {
  water:          'rgb(0, 119, 190)',
  food:           'rgb(231, 141, 32)',
  health:         'rgb(218, 59, 59)',
  shelter:        'rgb(101, 78, 163)',
  education:      'rgb(52, 168, 83)',
  safety:         'rgb(255, 87, 34)',
  infrastructure: 'rgb(117, 117, 117)'
};
```

### Key Admin Features — Implementation Notes

**Task Assignment Panel (Smart Matching)**
```
When admin opens "Assign Volunteers" for a task:
1. Show volunteers sorted by:
   - Distance from task location (nearest first)
   - Skill match score (required skills vs volunteer skills)
   - Current availability
   - Past performance rating
   - Current task load (don't overload)
2. AI suggestion badge: "Recommended" label on top 3
3. Multi-select for group tasks
4. One-click assign → push notification sent immediately
```

**Survey Response Detail View**
```
Shows:
- Raw responses (all fields)
- AI extracted issues (badges)
- Severity score (visual gauge)
- AI summary paragraph
- Photo evidence (gallery)
- Map showing exact location
- "Create Task from This Survey" button
- Similar surveys in same area
```

---

## 8. VOLUNTEER MOBILE APP

### Screen Structure

```
App Entry
│
├── Onboarding (first time only)
│   ├── Welcome slides
│   ├── App permissions request (Location, Camera, Notifications)
│   └── Language selection
│
├── Auth Stack
│   ├── Login screen
│   ├── Forgot password
│   └── OTP verification
│
└── Main Tab Navigator (after login)
    │
    ├── Tab: Home
    │   ├── Greeting + availability toggle
    │   ├── Active tasks summary
    │   ├── Community map (mini heatmap — view only)
    │   └── Announcements from admin
    │
    ├── Tab: My Tasks
    │   ├── Task list (pending/active/completed)
    │   ├── Task detail
    │   │   ├── Map with directions
    │   │   ├── Accept/Decline buttons
    │   │   ├── "Start Task" → "Complete Task" flow
    │   │   └── Completion form (notes + photo upload)
    │   └── Task history
    │
    ├── Tab: Survey
    │   ├── Available survey templates
    │   ├── Survey form (dynamic, multi-step)
    │   │   ├── Text, select, number inputs
    │   │   ├── Photo capture
    │   │   ├── GPS auto-capture + manual map pin
    │   │   └── Offline queue indicator
    │   └── Submitted surveys list
    │
    ├── Tab: Map
    │   ├── Full heatmap (read-only, same data as admin)
    │   ├── My location
    │   ├── Nearby active tasks
    │   └── Pinch/zoom/filter by category
    │
    └── Tab: Profile
        ├── Personal info
        ├── Skills management
        ├── Availability calendar
        ├── Notification settings
        └── Logout
```

### Offline-First Architecture (Critical for NGO Context)

```
Survey Submission Flow:

User submits survey
        │
        ▼
Save to WatermelonDB (local SQLite) immediately
Show "Submitted (pending sync)" to user
        │
        ▼
NetInfo checks connectivity
    ├── Online: POST to API immediately
    │           Update local status to 'synced'
    └── Offline: Add to sync queue
                 Background sync when connected
                 (expo-background-fetch)
                 Show sync count badge in UI
```

```typescript
// WatermelonDB sync configuration
const syncOptions = {
  pullChanges: async ({ lastPulledAt }) => {
    const response = await api.get(`/sync/pull?last_pulled_at=${lastPulledAt}`);
    return response.data;  // { changes: {...}, timestamp: ... }
  },
  pushChanges: async ({ changes }) => {
    await api.post('/sync/push', { changes });
  },
  migrationsEnabledAtVersion: 1,
};
```

### Volunteer Authentication Flow

```
1. Admin sends invite email to volunteer (via admin panel)
2. Email contains magic link / invite code
3. Volunteer opens app → enters invite code OR clicks link
4. App shows registration form:
   - Name (pre-filled from invite)
   - Phone number
   - Password setup
   - Skills selection (chips)
   - Languages spoken
   - Emergency contact
5. OTP verification on phone number
6. Profile created → lands on Home tab
7. JWT stored in Expo SecureStore (encrypted)
8. Auto-refresh token on app resume
9. Biometric unlock option (FaceID/Fingerprint) via expo-local-authentication
```

---

## 9. AI/ML PIPELINE

### Survey Intelligence Engine

```python
# FastAPI ML Service — main processing flow

from fastapi import FastAPI, BackgroundTasks
from transformers import pipeline
import spacy
import numpy as np
from scipy.stats import gaussian_kde

app = FastAPI()
nlp = spacy.load("en_core_web_sm")
classifier = pipeline("text-classification", model="distilbert-base-uncased-finetuned-sst-2-english")

# ============================================
# STEP 1: Issue Extraction (spaCy NER + rules)
# ============================================
def extract_issues(text: str) -> list[str]:
    doc = nlp(text.lower())
    
    ISSUE_KEYWORDS = {
        'water': ['water', 'pipe', 'tap', 'flood', 'drought', 'contamination'],
        'health': ['sick', 'hospital', 'medicine', 'disease', 'infection'],
        'food': ['hunger', 'food', 'nutrition', 'malnutrition', 'ration'],
        'shelter': ['homeless', 'shelter', 'roof', 'damaged', 'house'],
        'infrastructure': ['road', 'bridge', 'electricity', 'power', 'pothole'],
        'safety': ['violence', 'accident', 'crime', 'unsafe', 'danger'],
        'education': ['school', 'children', 'learning', 'teacher', 'education']
    }
    
    found = []
    for category, keywords in ISSUE_KEYWORDS.items():
        if any(kw in text.lower() for kw in keywords):
            found.append(category)
    return found

# ============================================
# STEP 2: Severity Scoring
# ============================================
def compute_severity(responses: dict, self_reported_severity: str) -> float:
    # Base from self-report
    severity_map = {'low': 2.0, 'medium': 5.0, 'high': 7.5, 'critical': 10.0}
    base = severity_map.get(self_reported_severity, 5.0)
    
    # Text sentiment modifier
    all_text = ' '.join(str(v) for v in responses.values() if isinstance(v, str))
    if all_text:
        sentiment = classifier(all_text[:512])[0]
        if sentiment['label'] == 'NEGATIVE':
            base = min(10.0, base * (1 + sentiment['score'] * 0.3))
    
    # People affected modifier
    affected = responses.get('people_affected', 0)
    if affected > 1000: base = min(10.0, base * 1.2)
    elif affected > 100: base = min(10.0, base * 1.1)
    
    return round(base, 2)

# ============================================
# STEP 3: Heatmap Cell Update (KDE-based)
# ============================================
def update_heatmap(org_id: str, lat: float, lng: float,
                   category: str, severity: float):
    # Fetch nearby responses (within ~5km)
    nearby = db.query_nearby_responses(org_id, lat, lng, radius_km=5)
    
    if len(nearby) < 3:
        intensity = severity * 10  # sparse data — use raw score
    else:
        lats = [r.lat for r in nearby]
        lngs = [r.lng for r in nearby]
        severities = [r.severity for r in nearby]
        
        # Kernel Density Estimation weighted by severity
        kde = gaussian_kde([lats, lngs], weights=severities, bw_method=0.1)
        intensity = float(kde.evaluate([[lat], [lng]])[0])
        intensity = normalize_to_100(intensity)
    
    # Compute geohash for cell
    geohash = encode_geohash(lat, lng, precision=6)
    
    db.upsert_heatmap_cell({
        'org_id': org_id,
        'geohash': geohash,
        'category': category,
        'intensity': intensity,
        'lat_center': lat,
        'lng_center': lng
    })

# ============================================
# STEP 4: AI Summary Generation
# ============================================
def generate_summary(responses: dict, issues: list, severity: float) -> str:
    # Template-based (no LLM needed, reduces cost)
    severity_label = 'critical' if severity >= 8 else 'high' if severity >= 6 else 'moderate'
    issues_str = ', '.join(issues) if issues else 'general community'
    
    return (f"This report indicates a {severity_label}-severity {issues_str} issue "
            f"affecting the local community. "
            f"Immediate attention {'is strongly recommended' if severity >= 7 else 'may be warranted'}.")
```

### Task Auto-Suggestion Algorithm

```python
def suggest_volunteers_for_task(task: Task, volunteers: list[Volunteer]) -> list[dict]:
    scores = []
    
    for v in volunteers:
        if not v.is_available:
            continue
        
        score = 0.0
        
        # 1. Skill match (40% weight)
        skill_overlap = len(set(task.required_skills) & set(v.skills))
        skill_score = skill_overlap / max(len(task.required_skills), 1)
        score += skill_score * 40
        
        # 2. Distance (30% weight) — closer = better
        dist_km = haversine(task.location, v.current_location)
        dist_score = max(0, 1 - (dist_km / 50))  # 0 score at 50km
        score += dist_score * 30
        
        # 3. Current workload (15% weight)
        active_tasks = count_active_tasks(v.id)
        load_score = max(0, 1 - (active_tasks / 3))  # penalize >3 active
        score += load_score * 15
        
        # 4. Historical performance (15% weight)
        perf_score = (v.rating or 3.0) / 5.0
        score += perf_score * 15
        
        scores.append({'volunteer': v, 'score': round(score, 2)})
    
    return sorted(scores, key=lambda x: x['score'], reverse=True)[:10]
```

---

## 10. AUTHENTICATION & SECURITY

### Full Auth Flow

```
ADMIN WEB:
─────────
Registration:
  Admin fills form → POST /api/v1/auth/register
  → Supabase creates auth.users entry
  → Profile created in profiles table with role='admin'
  → Verification email sent (Resend)
  → Admin confirms email → can login

Login:
  POST /api/v1/auth/login → Supabase signInWithPassword()
  → Returns { access_token (15min), refresh_token (7 days) }
  → Stored in httpOnly cookie (admin web) — NOT localStorage
  → Auto-refresh via Supabase client before expiry

VOLUNTEER MOBILE:
─────────────────
Onboarding via Invite:
  Admin invites volunteer email from dashboard
  → Supabase sends invite email with OTP link
  → Volunteer opens app, enters email + OTP
  → Sets password on first login
  → Profile created with role='volunteer'

Login:
  POST to Supabase Auth
  → access_token stored in Expo SecureStore (AES-256 encrypted)
  → Biometric re-auth option after 24h
  → Refresh token in SecureStore
```

### Security Checklist

```
API Security:
✓ Helmet.js (all security headers)
✓ Rate limiting: 100 req/min per IP, 1000 req/min per auth'd user
✓ JWT expiry: 15 minutes access, 7 days refresh
✓ JWT stored in httpOnly cookie (web) / SecureStore (mobile)
✓ CORS: explicit whitelist of allowed origins
✓ SQL injection prevention: Prisma parameterized queries only
✓ XSS prevention: input sanitization + CSP headers
✓ File uploads: type whitelist (JPEG, PNG, PDF only), size limit 10MB
✓ Supabase Storage: signed URLs (expire in 1 hour), no public buckets
✓ Row Level Security: enforced at DB level for multi-tenant isolation
✓ Audit log: all admin mutations logged with IP + user-agent
✓ Environment secrets: never in code, always in env vars
✓ HTTPS only: enforced via Cloudflare (admin web) + Railway (API)
✓ Volunteer location data: encrypted at rest, access-controlled by RLS

Mobile Security:
✓ Certificate pinning for API calls (expo-ssl-pinning)
✓ Root/jailbreak detection (expo-device)
✓ Biometric auth support
✓ No sensitive data in AsyncStorage (use SecureStore)
✓ App background screenshot prevention (sensitive screens)
```

---

## 11. DEVOPS, CI/CD & INFRASTRUCTURE

### GitHub Actions Pipeline

```yaml
# .github/workflows/deploy.yml

name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  # ─── TEST ───────────────────────────────────
  test-backend:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci
      - run: npm run test:unit
      - run: npm run test:integration
      - run: npm run lint
      - run: npm run type-check

  test-ml:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with: { python-version: '3.11' }
      - run: pip install -r requirements.txt
      - run: pytest tests/ -v --cov=app

  # ─── BUILD ──────────────────────────────────
  build-admin:
    needs: [test-backend]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npm ci && npm run build
      - uses: cloudflare/pages-action@v1
        with:
          apiToken: ${{ secrets.CF_API_TOKEN }}
          accountId: ${{ secrets.CF_ACCOUNT_ID }}
          projectName: 'sra-admin'
          directory: 'dist'

  # ─── DEPLOY ─────────────────────────────────
  deploy-backend:
    needs: [test-backend]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: railwayapp/railway-actions@v2
        with:
          service: backend-api
          token: ${{ secrets.RAILWAY_TOKEN }}

  deploy-ml:
    needs: [test-ml]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: railwayapp/railway-actions@v2
        with:
          service: ml-service
          token: ${{ secrets.RAILWAY_TOKEN }}
```

### Environment Configuration

```bash
# Backend API — .env structure (NEVER commit this)
NODE_ENV=production
PORT=3000

# Supabase
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
SUPABASE_SERVICE_ROLE_KEY=eyJ...   # NEVER expose to frontend

# Database (direct connection for Prisma migrations)
DATABASE_URL=postgresql://postgres:pass@db.xxx.supabase.co:5432/postgres

# Redis
UPSTASH_REDIS_REST_URL=https://xxx.upstash.io
UPSTASH_REDIS_REST_TOKEN=xxx

# Email
RESEND_API_KEY=re_xxx

# Firebase (push notifications)
FIREBASE_PROJECT_ID=xxx
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n..."
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxx@xxx.iam.gserviceaccount.com

# Mapbox (admin web — public key is OK)
VITE_MAPBOX_TOKEN=pk.xxx

# ML Service
ML_SERVICE_URL=https://ml-service.railway.internal
ML_API_KEY=xxx   # Internal service-to-service auth

# Sentry
SENTRY_DSN=https://xxx@xxx.ingest.sentry.io/xxx

# App URLs
ADMIN_URL=https://admin.yourproject.com
API_URL=https://api.yourproject.com
```

### Railway Service Architecture

```
Railway Project: "smart-resource-allocation"
│
├── Service: backend-api (Node.js)
│   ├── Start: node dist/server.js
│   ├── Health: GET /health
│   ├── Scale: 1 instance (free tier)
│   └── Env: [all backend env vars]
│
├── Service: ml-service (Python)
│   ├── Start: uvicorn app.main:app --host 0.0.0.0 --port 8000
│   ├── Health: GET /health
│   ├── Scale: 1 instance (free tier)
│   └── Env: [ML env vars]
│
└── Service: worker (BullMQ + Celery)
    ├── Start: node dist/worker.js
    ├── No HTTP — queue consumer only
    └── Env: [same as backend-api]
```

---

## 12. DATA PIPELINES

### Survey Processing Pipeline

```
[Mobile App] → POST /api/surveys
      ↓
[Express API] validates, stores raw to DB
      ↓
Creates BullMQ job: { jobName: 'process-survey', data: { surveyId } }
      ↓
[Worker Service] picks up job
      ↓
Calls ML Service: POST /internal/analyze-survey
      ↓
[ML Service] runs:
  1. extract_issues(responses)
  2. compute_severity(responses)
  3. generate_summary(responses)
  4. update_heatmap(location, severity)
      ↓
Updates survey_responses with AI fields
Updates heatmap_cells table
      ↓
Emits Supabase Realtime event → Admin dashboard updates
      ↓
Checks if severity >= 7.0:
  → Creates "recommended_task" entry
  → Notifies admin via push/email: "New critical survey in [area]"
```

### Heatmap Recomputation Pipeline

```
Trigger: Every 5 minutes (cron) OR after each survey batch
      ↓
BullMQ cron job: 'recompute-heatmap'
      ↓
Query all survey_responses from last 30 days per org
Group by category + geohash
Run KDE (Kernel Density Estimation) per category
Compute normalized intensity scores
Upsert heatmap_cells table (UPSERT ON CONFLICT)
      ↓
Invalidate Redis cache: DEL heatmap:{org_id}:*
      ↓
Supabase Realtime broadcasts update to connected admin dashboards
```

### Sync Pipeline (Mobile ↔ Backend)

```
Pull (admin changes → mobile):
  Every 60 seconds in background:
  GET /sync/pull?last_pulled_at={timestamp}
  Returns: new tasks, heatmap updates, notifications
  WatermelonDB updates local store
  React state updates → UI re-renders

Push (mobile → backend):
  Immediate: survey submissions, task status updates, location updates
  Queued: anything that failed (retry with exponential backoff)
  
Conflict resolution:
  Server wins for tasks (admin is source of truth)
  Client wins for survey responses (volunteer data is source)
  Merge: profile updates (last-write-wins with timestamp check)
```

---

## 13. NOTIFICATIONS & REAL-TIME

### Notification Types Matrix

| Event | Admin Notification | Volunteer Notification |
|---|---|---|
| New survey submitted | Email + in-app bell | — |
| Critical survey (severity ≥ 8) | Push + email + in-app (urgent) | — |
| Task created & assigned | — | Push + in-app |
| Volunteer accepted task | In-app update | Confirmation in-app |
| Volunteer completed task | Push + in-app | — |
| Volunteer declined task | In-app | — |
| New volunteer registered | Email | Welcome email + in-app |
| Heatmap spike detected | In-app alert | — |
| Daily digest | Email (morning) | — |

### Real-Time Implementation

```typescript
// Admin Web: Supabase Realtime subscription
const channel = supabase
  .channel('admin-realtime')
  .on('postgres_changes', {
    event: 'INSERT',
    schema: 'public',
    table: 'survey_responses',
    filter: `org_id=eq.${orgId}`
  }, (payload) => {
    queryClient.invalidateQueries(['surveys']);
    showToast(`New survey from ${payload.new.volunteer_name}`);
  })
  .on('postgres_changes', {
    event: 'UPDATE',
    schema: 'public',
    table: 'heatmap_cells',
    filter: `org_id=eq.${orgId}`
  }, () => {
    queryClient.invalidateQueries(['heatmap']);  // Triggers map re-render
  })
  .subscribe();

// Mobile: Firebase Cloud Messaging
Notifications.addNotificationReceivedListener((notification) => {
  const { type, taskId } = notification.request.content.data;
  if (type === 'task_assigned') {
    store.addPendingTask(taskId);
    showLocalNotification(notification);
  }
});
```

---

## 14. FAILURE MODES & RESILIENCE

### Failure Scenarios & Mitigations

```
FAILURE: Database goes down
├── Impact: All API calls fail
├── Detection: Railway health check, Sentry alert within 30s
├── Mitigation: Supabase has automatic failover (managed)
├── User experience: App shows "Service temporarily unavailable"
└── Recovery: Auto-reconnects, queued writes replay on restore

FAILURE: ML Service crashes
├── Impact: Surveys stored but not processed
├── Detection: BullMQ job failures → alert after 5 consecutive fails
├── Mitigation: Failed jobs retry 3 times with exponential backoff
│              Queue jobs persist in Redis — no data loss
├── User experience: Surveys shown as "Processing..." in admin
└── Recovery: ML service restarts, processes backlog automatically

FAILURE: Volunteer has no internet (offline survey)
├── Impact: Survey can't be sent
├── Mitigation: WatermelonDB stores locally, syncs when online
│              Background sync every 5 min when connected
│              Sync queue visible in app UI with count badge
├── User experience: "Saved offline, will sync automatically"
└── Recovery: Transparent to user — just syncs when connected

FAILURE: Push notifications not delivered (FCM failure)
├── Impact: Volunteer doesn't see new task
├── Mitigation: In-app polling every 60s as backup
│              Unread badge on notifications tab
│              SMS fallback option via Twilio (future)
└── Recovery: Volunteer sees it next time they open app

FAILURE: Heatmap data is stale
├── Impact: Admin makes decisions on old data
├── Mitigation: "Last updated" timestamp shown on every heatmap tile
│              Auto-recompute every 5 minutes (cron job)
│              Manual "Refresh" button in admin UI
└── Recovery: Trigger manual recompute from admin settings

FAILURE: Admin accidentally assigns wrong volunteer
├── Impact: Wrong person gets task
├── Mitigation: 5-minute undo window after assignment
│              Full audit log of all assignments
│              Volunteer sees assignment but can decline with reason
└── Recovery: Admin can reassign at any time from task detail

FAILURE: Survey responses contain PII (names, addresses in free text)
├── Impact: Privacy violation
├── Mitigation: App shows privacy notice before each survey
│              Backend scans for common PII patterns (regex)
│              Flags for admin review if PII detected
└── Recovery: Admin can redact specific fields in UI

FAILURE: Railway free tier exhausts credits
├── Impact: Backend goes offline
├── Mitigation: Cost monitoring dashboard in Railway
│              Alert at 80% credit usage
│              Cloudflare Pages (admin web) is always free
│              Supabase DB stays up independently
└── Recovery: Upgrade to paid plan, or migrate to Render free tier
```

### Retry Strategies

```typescript
// BullMQ job retry configuration
const queue = new Queue('survey-processing', {
  defaultJobOptions: {
    attempts: 3,
    backoff: {
      type: 'exponential',
      delay: 2000,      // 2s, 4s, 8s
    },
    removeOnComplete: 100,  // Keep last 100 completed
    removeOnFail: 500,      // Keep last 500 failed for debugging
  }
});

// API call retry (mobile app)
const apiCall = async (fn: () => Promise<any>, maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (err) {
      if (i === maxRetries - 1) throw err;
      await sleep(Math.pow(2, i) * 1000);  // Exponential backoff
    }
  }
};
```

---

## 15. DEVELOPMENT ROADMAP

### Phase 0: Foundation (Week 1–2)
**All team members together**

```
□ Create GitHub organization, private mono-repo
□ Define API contracts (OpenAPI spec) — ALL agree before coding
□ Set up Supabase project, create DB schema, enable RLS
□ Set up Railway project, create services
□ Configure GitHub Actions (basic lint + test CI)
□ Set up Sentry (web + mobile projects)
□ Create shared Zod schemas package (used by both backend + frontend)
□ Admin web: scaffold with Vite + shadcn/ui
□ Mobile: scaffold with Expo + EAS
□ Backend: scaffold with Express + Prisma + TypeScript
□ ML: scaffold FastAPI + basic health check
□ Daily standup (15 min, async via GitHub Discussions)
```

### Phase 1: Auth & Core Data (Week 3–4)
```
□ Supabase Auth integration (backend)
□ Admin registration + login flow (web)
□ Volunteer invite + onboarding flow (mobile)
□ JWT middleware (backend)
□ RLS policies tested end-to-end
□ Basic volunteer CRUD (backend + admin web)
□ Basic profile management (mobile app)
□ Location permission + GPS tracking (mobile)
```

### Phase 2: Surveys (Week 5–6)
```
□ Survey template builder UI (admin web)
□ Dynamic survey form renderer (mobile)
□ Survey submission API
□ Photo upload to Supabase Storage
□ Offline queue (WatermelonDB setup)
□ Sync logic (push pending surveys when online)
□ Admin survey response list view
□ Basic ML pipeline stub (save survey, flag for processing)
```

### Phase 3: Heatmap & Intelligence (Week 7–8)
```
□ Mapbox integration (admin web)
□ Heatmap layer rendering from API data
□ NLP issue extraction (ML service)
□ Severity scoring algorithm
□ Heatmap computation (KDE-based)
□ BullMQ pipeline: survey → ML → heatmap
□ Supabase Realtime → admin dashboard live updates
□ Heatmap view in volunteer mobile app (read-only)
□ Category filters on heatmap
```

### Phase 4: Task Management (Week 9–10)
```
□ Task creation UI (admin web)
□ Task list + Kanban board
□ Task assignment panel with volunteer suggestions
□ Task notification → volunteer (FCM push)
□ Task accept/decline flow (mobile)
□ Task execution flow: start → complete
□ Task completion with photo proof (mobile)
□ Task map view (admin + mobile)
□ Admin task analytics
```

### Phase 5: Polish & Production Hardening (Week 11–12)
```
□ Audit log UI (admin)
□ CSV/PDF export
□ Performance testing (k6 load testing)
□ Security audit (OWASP checklist)
□ Accessibility audit (WCAG 2.1 AA)
□ Error boundaries everywhere
□ Complete Sentry integration (all errors tracked)
□ Uptime monitoring (Better Stack)
□ App Store submission prep (iOS + Android)
□ Documentation (API docs, deployment runbook)
□ Demo data seeding script
```

---

## 16. FUTURE MODIFICATIONS

### Short-term Additions (Post v1.0, 1-3 months)

```
1. SMS/WhatsApp Survey Collection
   Volunteers submit surveys via WhatsApp (Twilio API)
   Parser extracts structured data from natural language
   Useful for volunteers without smartphones

2. Multi-language Support (i18n)
   Survey forms in Hindi, Tamil, Bengali, etc.
   Admin portal English-first, add Hindi
   react-i18next (web) + expo-localization (mobile)

3. Volunteer Availability Scheduler
   Calendar UI for volunteers to block out unavailability
   Admin sees "available now" filter when assigning tasks
   Reminder notifications for upcoming tasks

4. Advanced Analytics Dashboard
   Predictive heatmap (where will issues likely appear next week?)
   Volunteer efficiency scores
   Seasonal trend analysis
   Donor-ready PDF reports with impact metrics

5. Photo AI Analysis
   Automatically classify photo evidence
   Detect severity from images (cracked pipes, crowded shelters)
   Flag inappropriate content
```

### Medium-term (3-6 months)

```
6. Multi-Organization Support
   Super-admin can manage multiple NGOs
   Cross-org resource sharing (when disaster strikes)
   Inter-org heatmap comparison (city-wide view)

7. Task Auto-Assignment (Full AI)
   Remove admin bottleneck — AI assigns based on volunteer profiles
   Admin only reviews + approves (or overrides)
   Learning model improves from feedback

8. Volunteer Gamification
   Points, badges, leaderboards per org
   "Volunteer of the Month" recognition
   Certificate generation for volunteer hours

9. Beneficiary Feedback Loop
   QR code at intervention site
   Beneficiaries rate if help actually arrived
   Closes feedback loop from survey → task → impact

10. Government Integration
    API to submit critical issues to municipal complaint portals
    Auto-generate reports in government-required formats
```

### Long-term (6-18 months)

```
11. Native LLM Integration
    Fine-tuned local model for survey analysis
    Chat interface for admin: "Show me water issues in Zone 3"
    Volunteer chatbot for guidance in the field

12. IoT Sensor Integration
    Water quality sensors → auto-trigger surveys
    Environmental monitors → heatmap data from hardware

13. Federated Multi-City Deployment
    Each city runs their own instance
    Shared infrastructure model
    National dashboard for government oversight

14. Impact Measurement Platform
    Before/after photo comparison (AI)
    Economic impact estimation
    UN SDG alignment scoring
```

---

## 17. HIDDEN DETAILS EVERY TEAM MISSES

### Things That Will Break You If Ignored

```
1. TIMEZONE HANDLING
   Problem: Volunteers submit surveys in IST, server logs UTC,
            admin reports are off by 5:30 hours
   Fix: Store ALL timestamps as UTC in DB
        Convert to local timezone only in UI
        Use date-fns-tz (web) and dayjs with timezone (mobile)

2. GPS ACCURACY ON CHEAP ANDROID PHONES
   Problem: GPS accuracy on budget Androids is ±50-200 meters
            Heatmap pins appear wrong
   Fix: Wait for accuracy < 50m before accepting location
        Show accuracy indicator in survey form
        Allow manual pin placement as override

3. LARGE PHOTO UPLOADS ON SLOW MOBILE DATA
   Problem: 5MB photo uploads fail on 2G connections
   Fix: Compress photos client-side (Expo ImageManipulator)
        Reduce to max 1MB, 1280px max dimension before upload
        Show upload progress bar, allow cancel + retry

4. JWT TOKEN EXPIRY DURING LONG SURVEY
   Problem: Volunteer spends 20 minutes on survey
            Access token expires (15 min)
            Submission fails with 401
   Fix: Intercept 401 responses, refresh token silently
        Retry original request with new token
        Local draft save every 30 seconds

5. SUPABASE REALTIME CONNECTION LIMITS
   Problem: Free tier has max 200 concurrent connections
            100 admins online = connection exhaustion
   Fix: Multiplex subscriptions per client
        Only subscribe when dashboard is in foreground
        Implement polling fallback if WS fails

6. PRISMA MIGRATION IN PRODUCTION
   Problem: Running prisma migrate deploy on Railway can
            lock tables during migration, causing downtime
   Fix: Always use additive migrations (never remove columns)
        Run migrations during off-peak hours
        Test migrations on staging first (create staging env early)
        Use --create-only flag to review before applying

7. SUPABASE STORAGE SIGNED URL EXPIRY
   Problem: Admin views survey photo, URL expires in 1 hour
            Next day photo shows broken image
   Fix: Never store signed URLs in DB — store only the path
        Generate signed URLs on-demand in the API response
        Cache signed URLs in Redis for 55 minutes

8. REACT NATIVE MAPS PERFORMANCE
   Problem: 500+ heatmap points on map → app freezes
   Fix: Cluster nearby points at low zoom levels
        Use react-native-maps clustering library
        Only render visible viewport, not all points

9. OFFLINE CONFLICT RESOLUTION
   Problem: Volunteer submits survey offline → admin creates
            task based on old data → survey syncs → duplicates
   Fix: UUID generated on device (not server) — no conflicts
        Created_at from device, server_received_at separately
        Idempotency key on all POST requests

10. EXPO EAS BUILD SECRETS
    Problem: Accidentally committing API keys in app config
    Fix: Use EAS secrets (eas secret:create)
         NEVER put secrets in app.json or app.config.js
         Use process.env with eas.json configuration only

11. ANDROID BACKGROUND LOCATION
    Problem: Android kills background processes aggressively
             Volunteer location goes stale when app is backgrounded
    Fix: Use expo-task-manager + expo-location background mode
         Request FOREGROUND + BACKGROUND location permissions
         Show persistent notification while tracking (required)
         Implement battery-efficient tracking (update every 5 min)

12. MAPBOX STYLE LICENSE
    Problem: Default Mapbox style requires Mapbox attribution
             Some orgs want a custom branded map
    Fix: Keep Mapbox attribution (required by TOS)
         Use Mapbox Studio to create a custom style (free)
         Consider OpenStreetMap + MapTiler for zero-cost fallback

13. MULTI-TENANT DATA ISOLATION
    Problem: Bug in RLS policy exposes Org A data to Org B
    Fix: Write integration tests that verify RLS
         Create test users from different orgs
         Assert cross-org queries return empty
         Never bypass RLS with service role key in user-facing APIs
```

---

## 18. COST & RESOURCE MAP

### GitHub Student Pack + Free Tiers

| Service | Free Tier Limits | Your Usage | Risk |
|---|---|---|---|
| **Railway** | $5/month credit (Student Pack: more) | ~$3-4/month | Low |
| **Supabase** | 500MB DB, 1GB storage, 50k MAU | Should fit | Low |
| **Cloudflare Pages** | Unlimited deploys, unlimited bandwidth | Free | Zero |
| **Upstash Redis** | 10k commands/day | ~5k/day | Low |
| **Resend** | 3,000 emails/month | ~200-500/month | Low |
| **Firebase FCM** | Free (no limits on push) | Free | Zero |
| **Expo EAS** | 30 builds/month free | ~10-20/month | Low |
| **GitHub Actions** | 2000 min/month free | ~500-800/month | Low |
| **Better Stack** | 10 monitors, 30 days retention | Fine | Low |
| **Sentry** | 5k errors/month | Fine for dev | Low |
| **Mapbox** | 50k map loads/month | Fine for pilot | Low |
| **Nominatim** | Rate limited (1 req/sec) | Use caching | Medium |

**Total Monthly Cost: ~$3-5 USD** (covered by Railway student credits)

### When You Outgrow Free Tiers

```
Scale trigger: 500+ active volunteers OR >100 surveys/day
Action plan:
  - Supabase Pro: $25/month (8GB DB, more storage)
  - Railway: $20/month (more compute)
  - Upstash: $10/month (higher Redis limits)
Total at scale: ~$55/month → apply for NGO grants for infrastructure
```

---

## APPENDIX A: Mono-Repo Structure

```
/smart-resource-allocation
│
├── /apps
│   ├── /admin-web          (React + Vite)
│   ├── /mobile             (Expo React Native)
│   └── /storybook          (component docs)
│
├── /services
│   ├── /api                (Express.js backend)
│   ├── /ml                 (FastAPI Python)
│   └── /worker             (BullMQ workers)
│
├── /packages
│   ├── /shared-types       (TypeScript types shared across apps)
│   ├── /shared-schemas     (Zod validation schemas)
│   └── /shared-utils       (date helpers, geo utils, etc.)
│
├── /infra
│   ├── /prisma             (DB schema + migrations)
│   └── /scripts            (seed scripts, deployment helpers)
│
├── /docs
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── CONTRIBUTING.md
│   └── RUNBOOK.md          (incident response procedures)
│
├── .github/
│   └── /workflows          (CI/CD pipelines)
│
├── package.json            (Turborepo root)
└── turbo.json              (build pipeline config)
```

## APPENDIX B: Week 1 Checklist (Do This First)

```
Day 1:
□ All 6 people added to GitHub organization
□ Mono-repo created with Turborepo
□ Branch protection rules on main (require 1 review)
□ Supabase project created, connection strings shared via vault
□ Railway project created

Day 2:
□ Finalize API spec (OpenAPI YAML) — ALL 6 review and sign off
□ DB schema reviewed and approved by all
□ Auth flow diagrammed and agreed upon
□ Figma workspace created for UI designs

Day 3-5:
□ Each person scaffolds their area (see Phase 0)
□ First CI pipeline passes (even with empty tests)
□ First Railway deployment (even just health check endpoint)
□ First Expo app runs on simulator
□ Daily 15-min async standup starts on GitHub Discussions
```

---

*This document represents a complete production-grade architecture for the Smart Resource Allocation platform. Follow each phase strictly, never skip security steps, and treat the "Hidden Details" section as the difference between a prototype and a production system.*

*Built for GitHub Student Pack constraints. Estimated to support up to 500 volunteers and 50 admin users on free tiers before scaling costs are needed.*
