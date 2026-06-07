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
    role TEXT NOT NULL DEFAULT 'volunteer' CHECK (role IN ('admin', 'volunteer', 'surveyor')),
    area TEXT DEFAULT '',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    skills JSONB DEFAULT '[]'::jsonb,
    availability TEXT DEFAULT 'flexible',
    profile_photo_url TEXT,
    total_tasks_completed INTEGER DEFAULT 0,
    total_hours INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════
-- 2. SURVEYS TABLE
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
-- 3. TASKS TABLE
-- ═══════════════════════════════════
CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    survey_id UUID REFERENCES surveys(id) ON DELETE SET NULL,
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
-- 4. TASK UPDATES TABLE
-- ═══════════════════════════════════
CREATE TABLE IF NOT EXISTS task_updates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    volunteer_id UUID NOT NULL REFERENCES volunteers(id) ON DELETE CASCADE,
    update_text TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'ongoing' CHECK (status IN ('ongoing', 'completed', 'blocked')),
    photo_url TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════
-- 5. NOTIFICATIONS TABLE
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
-- 6. AREA RISK SCORES TABLE (ML Output)
-- ═══════════════════════════════════
CREATE TABLE IF NOT EXISTS area_risk_scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    area_name TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    risk_score REAL NOT NULL DEFAULT 0.0 CHECK (risk_score BETWEEN 0.0 AND 10.0),
    risk_level TEXT NOT NULL DEFAULT 'low' CHECK (risk_level IN ('low', 'medium', 'high', 'critical')),
    contributing_factors JSONB DEFAULT '[]'::jsonb,
    calculated_at TIMESTAMPTZ DEFAULT now()
);

-- ═══════════════════════════════════
-- 7. INDEXES
-- ═══════════════════════════════════

-- Tasks
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned ON tasks(assigned_volunteer);
CREATE INDEX IF NOT EXISTS idx_tasks_urgency ON tasks(urgency DESC);
CREATE INDEX IF NOT EXISTS idx_tasks_survey ON tasks(survey_id);

-- Surveys
CREATE INDEX IF NOT EXISTS idx_surveys_volunteer ON surveys(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_surveys_status ON surveys(status);
CREATE INDEX IF NOT EXISTS idx_surveys_created_at ON surveys(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_surveys_location ON surveys(location_name);

-- Notifications
CREATE INDEX IF NOT EXISTS idx_notifications_volunteer ON notifications(volunteer_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(volunteer_id, is_read);

-- Task Updates
CREATE INDEX IF NOT EXISTS idx_task_updates_task ON task_updates(task_id);
CREATE INDEX IF NOT EXISTS idx_task_updates_volunteer ON task_updates(volunteer_id);

-- Area Risk Scores
CREATE INDEX IF NOT EXISTS idx_risk_scores_area ON area_risk_scores(area_name);
CREATE INDEX IF NOT EXISTS idx_risk_scores_score ON area_risk_scores(risk_score DESC);
CREATE INDEX IF NOT EXISTS idx_risk_scores_level ON area_risk_scores(risk_level);

-- Volunteers
CREATE INDEX IF NOT EXISTS idx_volunteers_role ON volunteers(role);

-- ═══════════════════════════════════
-- 8. ROW LEVEL SECURITY (RLS)
-- ═══════════════════════════════════

-- Helper function to check admin role WITHOUT triggering RLS on volunteers.
-- SECURITY DEFINER runs as the function owner (superuser), bypassing RLS
-- and avoiding infinite recursion when policies on `volunteers` need to
-- check the caller's role.
CREATE OR REPLACE FUNCTION is_admin()
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1 FROM volunteers WHERE id = auth.uid() AND role = 'admin'
    );
$$;

-- Enable RLS on all tables
ALTER TABLE volunteers ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE surveys ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE task_updates ENABLE ROW LEVEL SECURITY;
ALTER TABLE area_risk_scores ENABLE ROW LEVEL SECURITY;

-- ─── VOLUNTEERS ───
DROP POLICY IF EXISTS "Users can view own profile" ON volunteers;
CREATE POLICY "Users can view own profile"
    ON volunteers FOR SELECT
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Admins can view all profiles" ON volunteers;
CREATE POLICY "Admins can view all profiles"
    ON volunteers FOR SELECT
    USING (is_admin());

DROP POLICY IF EXISTS "Users can update own profile" ON volunteers;
CREATE POLICY "Users can update own profile"
    ON volunteers FOR UPDATE
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Users can insert own profile" ON volunteers;
CREATE POLICY "Users can insert own profile"
    ON volunteers FOR INSERT
    WITH CHECK (auth.uid() = id);

-- ─── TASKS ───
DROP POLICY IF EXISTS "Authenticated users can view all tasks" ON tasks;
CREATE POLICY "Authenticated users can view all tasks"
    ON tasks FOR SELECT
    TO authenticated
    USING (true);

DROP POLICY IF EXISTS "Admins can insert tasks" ON tasks;
CREATE POLICY "Admins can insert tasks"
    ON tasks FOR INSERT
    TO authenticated
    WITH CHECK (is_admin());

DROP POLICY IF EXISTS "Assigned volunteer can update own task" ON tasks;
CREATE POLICY "Assigned volunteer can update own task"
    ON tasks FOR UPDATE
    TO authenticated
    USING (assigned_volunteer = auth.uid());

DROP POLICY IF EXISTS "Any authenticated user can accept open task" ON tasks;
CREATE POLICY "Any authenticated user can accept open task"
    ON tasks FOR UPDATE
    TO authenticated
    USING (status = 'open' AND assigned_volunteer IS NULL);

DROP POLICY IF EXISTS "Admins can update any task" ON tasks;
CREATE POLICY "Admins can update any task"
    ON tasks FOR UPDATE
    TO authenticated
    USING (is_admin());

-- ─── SURVEYS ───
DROP POLICY IF EXISTS "Users can view own surveys" ON surveys;
CREATE POLICY "Users can view own surveys"
    ON surveys FOR SELECT
    USING (volunteer_id = auth.uid());

DROP POLICY IF EXISTS "Admins can view all surveys" ON surveys;
CREATE POLICY "Admins can view all surveys"
    ON surveys FOR SELECT
    USING (is_admin());

DROP POLICY IF EXISTS "Users can insert own surveys" ON surveys;
CREATE POLICY "Users can insert own surveys"
    ON surveys FOR INSERT
    WITH CHECK (volunteer_id = auth.uid());

DROP POLICY IF EXISTS "Admins can update any survey" ON surveys;
CREATE POLICY "Admins can update any survey"
    ON surveys FOR UPDATE
    TO authenticated
    USING (is_admin());

-- ─── TASK UPDATES ───
DROP POLICY IF EXISTS "Task participants can view updates" ON task_updates;
CREATE POLICY "Task participants can view updates"
    ON task_updates FOR SELECT
    TO authenticated
    USING (true);

DROP POLICY IF EXISTS "Volunteers can insert own updates" ON task_updates;
CREATE POLICY "Volunteers can insert own updates"
    ON task_updates FOR INSERT
    TO authenticated
    WITH CHECK (volunteer_id = auth.uid());

-- ─── NOTIFICATIONS ───
DROP POLICY IF EXISTS "Users can view own notifications" ON notifications;
CREATE POLICY "Users can view own notifications"
    ON notifications FOR SELECT
    USING (volunteer_id = auth.uid());

DROP POLICY IF EXISTS "Users can update own notifications" ON notifications;
CREATE POLICY "Users can update own notifications"
    ON notifications FOR UPDATE
    USING (volunteer_id = auth.uid());

DROP POLICY IF EXISTS "System can insert notifications" ON notifications;
CREATE POLICY "System can insert notifications"
    ON notifications FOR INSERT
    TO authenticated
    WITH CHECK (true);

-- ─── AREA RISK SCORES ───
DROP POLICY IF EXISTS "Anyone can view risk scores" ON area_risk_scores;
CREATE POLICY "Anyone can view risk scores"
    ON area_risk_scores FOR SELECT
    TO authenticated
    USING (true);

DROP POLICY IF EXISTS "Admins can insert risk scores" ON area_risk_scores;
CREATE POLICY "Admins can insert risk scores"
    ON area_risk_scores FOR INSERT
    TO authenticated
    WITH CHECK (is_admin());

DROP POLICY IF EXISTS "Admins can update risk scores" ON area_risk_scores;
CREATE POLICY "Admins can update risk scores"
    ON area_risk_scores FOR UPDATE
    TO authenticated
    USING (is_admin());

DROP POLICY IF EXISTS "Admins can delete risk scores" ON area_risk_scores;
CREATE POLICY "Admins can delete risk scores"
    ON area_risk_scores FOR DELETE
    TO authenticated
    USING (is_admin());

-- ═══════════════════════════════════
-- 9. STORAGE BUCKETS
-- ═══════════════════════════════════
INSERT INTO storage.buckets (id, name, public) VALUES ('profile-photos', 'profile-photos', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO storage.buckets (id, name, public) VALUES ('survey-photos', 'survey-photos', true)
ON CONFLICT (id) DO NOTHING;

-- Storage policies (path-scoped to user)
DROP POLICY IF EXISTS "Users can upload own profile photos" ON storage.objects;
CREATE POLICY "Users can upload own profile photos"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'profile-photos'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

DROP POLICY IF EXISTS "Anyone can view profile photos" ON storage.objects;
CREATE POLICY "Anyone can view profile photos"
    ON storage.objects FOR SELECT
    TO public
    USING (bucket_id = 'profile-photos');

DROP POLICY IF EXISTS "Users can upload own survey photos" ON storage.objects;
CREATE POLICY "Users can upload own survey photos"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'survey-photos'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

DROP POLICY IF EXISTS "Anyone can view survey photos" ON storage.objects;
CREATE POLICY "Anyone can view survey photos"
    ON storage.objects FOR SELECT
    TO public
    USING (bucket_id = 'survey-photos');

-- ═══════════════════════════════════
-- 10. HELPER FUNCTIONS (RPC)
-- ═══════════════════════════════════

-- Atomic increment for tasks completed (avoids race condition)
CREATE OR REPLACE FUNCTION increment_tasks_completed(user_id UUID)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE volunteers
    SET total_tasks_completed = total_tasks_completed + 1,
        updated_at = now()
    WHERE id = user_id;
END;
$$;

-- Atomic increment for total hours
CREATE OR REPLACE FUNCTION increment_total_hours(user_id UUID, hours INTEGER)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE volunteers
    SET total_hours = total_hours + hours,
        updated_at = now()
    WHERE id = user_id;
END;
$$;

-- Clear old risk scores before recalculation
CREATE OR REPLACE FUNCTION clear_risk_scores()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    DELETE FROM area_risk_scores;
END;
$$;

-- ═══════════════════════════════════
-- 11. REALTIME SUBSCRIPTIONS
-- ═══════════════════════════════════
-- Enable realtime for key tables via Supabase Dashboard > Database > Replication
-- Or run:
DO $$ BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE surveys;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
DO $$ BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE tasks;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
DO $$ BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE task_updates;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
DO $$ BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE notifications;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
DO $$ BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE area_risk_scores;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ═══════════════════════════════════
-- 12. SAMPLE DATA (optional)
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
