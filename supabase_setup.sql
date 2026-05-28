-- ═══════════════════════════════════════════════════════════════
-- SMART RESOURCE ALLOCATION - Supabase Database Setup
-- Run this SQL in your Supabase SQL Editor
-- ═══════════════════════════════════════════════════════════════

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ═══════════════════════════════════
-- 1. VOLUNTEERS TABLE
-- ═══════════════════════════════════
CREATE TABLE IF NOT EXISTS volunteers (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL DEFAULT '',
    email TEXT NOT NULL DEFAULT '',
    phone TEXT DEFAULT '',
    area TEXT DEFAULT '',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    skills JSONB DEFAULT '[]'::jsonb,
    availability TEXT DEFAULT 'flexible',
    profile_photo_url TEXT,
    total_tasks_completed INTEGER DEFAULT 0,
    total_hours INTEGER DEFAULT 0,
    is_admin BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════
-- 2. TASKS TABLE
-- ═══════════════════════════════════
CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    category TEXT NOT NULL,
    urgency INTEGER NOT NULL DEFAULT 1 CHECK (urgency BETWEEN 1 AND 5),
    location_name TEXT DEFAULT '',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    required_skills JSONB DEFAULT '[]'::jsonb,
    estimated_hours INTEGER DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('open', 'ongoing', 'completed', 'cancelled')),
    assigned_volunteer UUID REFERENCES volunteers(id),
    created_by UUID REFERENCES volunteers(id),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    completion_note TEXT,
    field_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════
-- 3. SURVEYS TABLE
-- ═══════════════════════════════════
CREATE TABLE IF NOT EXISTS surveys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    volunteer_id UUID NOT NULL REFERENCES volunteers(id) ON DELETE CASCADE,
    category TEXT NOT NULL,
    severity INTEGER NOT NULL DEFAULT 1 CHECK (severity BETWEEN 1 AND 5),
    people_affected INTEGER DEFAULT 0,
    description TEXT DEFAULT '',
    location_name TEXT DEFAULT '',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    photo_url TEXT,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'actioned')),
    admin_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════
-- 4. NOTIFICATIONS TABLE
-- ═══════════════════════════════════
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    volunteer_id UUID NOT NULL REFERENCES volunteers(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT DEFAULT 'general' CHECK (type IN ('general', 'task_assigned', 'task_completed', 'survey_reviewed')),
    is_read BOOLEAN DEFAULT false,
    task_id UUID REFERENCES tasks(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════
-- 5. INDEXES
-- ═══════════════════════════════════
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned ON tasks(assigned_volunteer);
CREATE INDEX IF NOT EXISTS idx_tasks_urgency ON tasks(urgency DESC);
CREATE INDEX IF NOT EXISTS idx_surveys_volunteer ON surveys(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_surveys_status ON surveys(status);
CREATE INDEX IF NOT EXISTS idx_notifications_volunteer ON notifications(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(volunteer_id, is_read);

-- ═══════════════════════════════════
-- 6. ROW LEVEL SECURITY (RLS)
-- ═══════════════════════════════════

-- Enable RLS on all tables
ALTER TABLE volunteers ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE surveys ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- VOLUNTEERS: Users can read/update their own profile; admins can read all
CREATE POLICY "Users can view own profile"
    ON volunteers FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON volunteers FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON volunteers FOR INSERT
    WITH CHECK (auth.uid() = id);

-- TASKS: All authenticated users can read tasks; only assigned volunteer can update
CREATE POLICY "Authenticated users can view all tasks"
    ON tasks FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Assigned volunteer can update task"
    ON tasks FOR UPDATE
    TO authenticated
    USING (assigned_volunteer = auth.uid() OR status = 'open');

-- SURVEYS: Users can view own surveys; can insert own surveys
CREATE POLICY "Users can view own surveys"
    ON surveys FOR SELECT
    USING (volunteer_id = auth.uid());

CREATE POLICY "Users can insert own surveys"
    ON surveys FOR INSERT
    WITH CHECK (volunteer_id = auth.uid());

-- NOTIFICATIONS: Users can view and update own notifications
CREATE POLICY "Users can view own notifications"
    ON notifications FOR SELECT
    USING (volunteer_id = auth.uid());

CREATE POLICY "Users can update own notifications"
    ON notifications FOR UPDATE
    USING (volunteer_id = auth.uid());

-- ═══════════════════════════════════
-- 7. STORAGE BUCKETS
-- ═══════════════════════════════════
-- Run these via the Supabase Dashboard > Storage or SQL:
INSERT INTO storage.buckets (id, name, public) VALUES ('profile-photos', 'profile-photos', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO storage.buckets (id, name, public) VALUES ('survey-photos', 'survey-photos', true)
ON CONFLICT (id) DO NOTHING;

-- Storage policies
CREATE POLICY "Authenticated users can upload profile photos"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'profile-photos');

CREATE POLICY "Anyone can view profile photos"
    ON storage.objects FOR SELECT
    TO public
    USING (bucket_id = 'profile-photos');

CREATE POLICY "Authenticated users can upload survey photos"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'survey-photos');

CREATE POLICY "Anyone can view survey photos"
    ON storage.objects FOR SELECT
    TO public
    USING (bucket_id = 'survey-photos');

-- ═══════════════════════════════════
-- 8. SAMPLE DATA (optional)
-- ═══════════════════════════════════

-- Insert sample tasks (after signing up at least one user)
-- Replace 'YOUR_USER_UUID' with an actual admin user UUID

/*
INSERT INTO tasks (title, description, category, urgency, location_name, latitude, longitude, required_skills, estimated_hours) VALUES
    ('Medical Camp Setup', 'Set up medical camp at community center for rural area health checkup', 'Medical', 5, 'Community Center, Sector 12', 28.7041, 77.1025, '["Medical", "Administration"]', 6),
    ('Food Distribution Drive', 'Distribute food packets to families in flood-affected areas', 'Food Distribution', 4, 'Riverside Colony', 28.6139, 77.2090, '["Food Distribution", "Transportation"]', 4),
    ('School Repair Work', 'Repair roof and walls of damaged government school', 'Construction', 3, 'Government School, Block C', 28.5355, 77.3910, '["Construction"]', 8),
    ('IT Training Session', 'Conduct basic computer literacy class for youth center', 'Education', 2, 'Youth Center, Main Road', 28.4595, 77.0266, '["IT Support", "Education"]', 3),
    ('Counseling Outreach', 'Mental health counseling session for disaster-affected families', 'Counseling', 4, 'Relief Camp Area', 28.6692, 77.4538, '["Counseling", "Medical"]', 5);
*/
