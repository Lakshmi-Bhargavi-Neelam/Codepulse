-- =====================================================================
-- CodePulse — PostgreSQL 16 Schema (Low-Level Design)
-- API Monitoring SaaS (UptimeRobot/Pingdom-style)
--
-- Scope: durable business data only.
-- Redis owns: current monitor status, consecutive-failure counters,
-- active-incident lookup, dashboard cache, and all short-lived tokens
-- (refresh / password-reset / email-verification). None of that is
-- modeled here.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Extensions
-- ---------------------------------------------------------------------
-- gen_random_uuid() ships in pgcrypto (or natively via PG 13+'s
-- gen_random_uuid() in pgcrypto — kept explicit for portability).
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------
-- ENUM types
-- Native Postgres enums are used instead of plain VARCHAR + CHECK
-- because these value sets are small, stable, and referenced
-- extremely often (every health check row, every incident, every
-- delivery). Enums are cheaper to store (4 bytes) and self-document
-- the allowed states at the schema level. Business rules that are
-- more likely to churn (e.g. failure thresholds, intervals) are kept
-- as plain columns with CHECK constraints instead.
-- ---------------------------------------------------------------------

CREATE TYPE project_member_role   AS ENUM ('OWNER', 'ADMIN', 'MEMBER');
CREATE TYPE invitation_status     AS ENUM ('PENDING', 'ACCEPTED');
CREATE TYPE http_method           AS ENUM ('GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS');
CREATE TYPE health_check_result   AS ENUM ('HEALTHY', 'DOWN', 'TIMEOUT', 'SLOW');
CREATE TYPE incident_status       AS ENUM ('ACTIVE', 'RESOLVED');
CREATE TYPE notification_channel_type AS ENUM ('EMAIL', 'SLACK', 'DISCORD', 'WEBHOOK');
CREATE TYPE delivery_status       AS ENUM ('PENDING', 'SENT', 'FAILED');

-- =====================================================================
-- 1. users
-- Authentication account. One row per human who can log in.
-- =====================================================================
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Login identifier. Normalized to lowercase at the application
    -- layer before insert so the UNIQUE constraint can't be bypassed
    -- by case variants (Foo@x.com vs foo@x.com).
    email               VARCHAR(255) NOT NULL,
    -- BCrypt/Argon2 hash — never plaintext, never reversible.
    password_hash       VARCHAR(255) NOT NULL,
    full_name           VARCHAR(150) NOT NULL,
    -- Set true once the user clicks the verification link. The token
    -- itself lives in Redis with a TTL; only the outcome is durable.
    email_verified      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_email_format CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$')
);

-- =====================================================================
-- 2. projects
-- A workspace/container owned by exactly one user. Monitors,
-- team members, and notification channels all hang off a project.
-- =====================================================================
CREATE TABLE projects (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- The creator/billing owner. Distinct from project_members, which
    -- tracks the full collaborator roster (including this owner).
    owner_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                VARCHAR(150) NOT NULL,
    description         TEXT,
    -- IANA tz name (e.g. 'Asia/Kolkata'), used to render incident/
    -- uptime timestamps and schedule reports in the owner's local time.
    timezone            VARCHAR(64) NOT NULL DEFAULT 'UTC',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================================
-- 3. project_members
-- Many-to-many join between users and projects, carrying role and
-- invitation lifecycle.
-- =====================================================================
CREATE TABLE project_members (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role                project_member_role NOT NULL DEFAULT 'MEMBER',
    status              invitation_status NOT NULL DEFAULT 'PENDING',
    -- Null while status = PENDING; stamped when the invite is accepted.
    joined_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- A user can only have one membership row per project (re-invites
    -- update the existing row rather than duplicating it).
    
    CONSTRAINT uq_project_members_project_user UNIQUE (project_id, user_id)
);

-- =====================================================================
-- 4. monitors
-- The thing being watched: an HTTP endpoint on a schedule, belonging
-- to exactly one project.
-- =====================================================================
CREATE TABLE monitors (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name                VARCHAR(150) NOT NULL,
    url                 TEXT NOT NULL,
    method              http_method NOT NULL DEFAULT 'GET',
    -- Minutes between checks. Constrained to the finite tier list the
    -- product offers, so a bad value can't silently create a
    -- pathological polling schedule.
    interval_minutes    SMALLINT NOT NULL DEFAULT 5,
    timeout_ms          INTEGER NOT NULL DEFAULT 30000,
    -- Number of consecutive failed checks required before an incident
    -- is opened (debounces flapping/transient blips).
    failure_threshold   SMALLINT NOT NULL DEFAULT 3,
    expected_status_code SMALLINT NOT NULL DEFAULT 200,
    is_enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    -- Denormalized scheduling pointer: the worker/scheduler queries
    -- "next_run_at <= now()" to pick due monitors instead of computing
    -- schedules on the fly. Updated after every executed check.
    next_run_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_monitors_interval CHECK (interval_minutes IN (1, 5, 15, 30, 60)),
    CONSTRAINT chk_monitors_expected_status_code CHECK (expected_status_code BETWEEN 100 AND 599)
);

-- =====================================================================
-- 5. health_checks
-- Append-only execution log: one row per check attempt against a
-- monitor. High write volume, time-series in nature.
-- =====================================================================
CREATE TABLE health_checks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    monitor_id          UUID NOT NULL REFERENCES monitors(id) ON DELETE CASCADE,
    checked_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Nullable: a connection-level failure (DNS/timeout/refused) may
    -- never receive an HTTP status code at all.
    status_code         SMALLINT,
    latency_ms          INTEGER,
    result              health_check_result NOT NULL,
    -- Populated for DOWN/TIMEOUT to aid debugging; left null on HEALTHY.
    error_message       TEXT,

    CONSTRAINT chk_health_checks_status_code CHECK (status_code IS NULL OR status_code BETWEEN 100 AND 599),
    CONSTRAINT chk_health_checks_latency_nonneg CHECK (latency_ms IS NULL OR latency_ms >= 0)
);

-- =====================================================================
-- 6. incidents
-- An outage window derived from a run of failing health checks on a
-- single monitor.
-- =====================================================================
CREATE TABLE incidents (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    monitor_id              UUID NOT NULL REFERENCES monitors(id) ON DELETE CASCADE,
    status                  incident_status NOT NULL DEFAULT 'ACTIVE',
    opened_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Null while status = ACTIVE; set on resolution.
    resolved_at             TIMESTAMPTZ,
    -- Denormalized for fast reporting (avoids recomputing
    -- resolved_at - opened_at in every uptime/SLA query). Filled in
    -- at resolution time.
    duration_seconds        INTEGER,
    -- Free-text postmortem note, filled in later by a team member;
    -- absent for auto-resolved/unattended incidents.
    root_cause              TEXT,
    -- Snapshot of monitors.failure_threshold at the moment this
    -- incident was opened, so later threshold edits don't rewrite
    -- history.
    consecutive_failures    SMALLINT NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_incidents_resolved_consistency CHECK (
        (status = 'ACTIVE'   AND resolved_at IS NULL) OR
        (status = 'RESOLVED' AND resolved_at IS NOT NULL)
    ),
    CONSTRAINT chk_incidents_duration_nonneg CHECK (duration_seconds IS NULL OR duration_seconds >= 0),
    CONSTRAINT chk_incidents_consecutive_failures_positive CHECK (consecutive_failures > 0)
);


-- =====================================================================
-- 7. notification_channels
-- A configured alert destination for a project (email address, Slack
-- webhook, Discord webhook, or generic webhook URL).
-- =====================================================================
CREATE TABLE notification_channels (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id          UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    type                notification_channel_type NOT NULL,
    -- Email address for EMAIL; webhook URL for SLACK/DISCORD/WEBHOOK.
    -- Kept as a single polymorphic column rather than one per type
    -- since exactly one is ever populated per row.
    destination         TEXT NOT NULL,
    is_enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================================
-- 8. notification_deliveries
-- One row per attempt to notify a channel about an incident. Tracks
-- the outbound delivery lifecycle independently of the incident
-- itself, since delivery can succeed/fail/retry on its own timeline.
-- =====================================================================
CREATE TABLE notification_deliveries (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id             UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    notification_channel_id UUID NOT NULL REFERENCES notification_channels(id) ON DELETE CASCADE,
    status                  delivery_status NOT NULL DEFAULT 'PENDING',
    -- Incremented on each retry; used to cap retry attempts at the
    -- application layer.
    attempt_count           SMALLINT NOT NULL DEFAULT 0,
    -- Null until status = SENT.
    sent_at                 TIMESTAMPTZ,
    -- Populated on FAILED (SMTP bounce, webhook non-2xx, timeout, etc).
    failure_reason          TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_notification_deliveries_attempt_count_nonneg CHECK (attempt_count >= 0),
    CONSTRAINT chk_notification_deliveries_sent_consistency CHECK (
        (status = 'SENT' AND sent_at IS NOT NULL) OR
        (status <> 'SENT')
    )
);

-- =====================================================================
-- INDEXES
-- =====================================================================

-- users -----------------------------------------------------------------
-- Login lookup by email is the hottest query on this table; the
-- UNIQUE constraint above already creates a btree index on email,
-- so no separate index is added here.

-- projects ---------------------------------------------------------------
-- "list my projects" / ownership checks on every authenticated request.
CREATE INDEX idx_projects_owner_id ON projects (owner_id);

-- project_members ---------------------------------------------------------
-- "list members of a project" (team page) — project_id first.
CREATE INDEX idx_project_members_project_id ON project_members (project_id);
-- "list projects a user belongs to" (dashboard project switcher).
CREATE INDEX idx_project_members_user_id ON project_members (user_id);
-- (project_id, user_id) is already covered by the UNIQUE constraint's
-- implicit index, which also satisfies the project_id-only lookup
-- above when project_id is the leading column — kept as a separate
-- explicit index here for clarity/independent tuning.

-- monitors -----------------------------------------------------------------
-- "list monitors for a project" (monitor list page).
CREATE INDEX idx_monitors_project_id ON monitors (project_id);
-- The scheduler's core query: "give me all enabled monitors due to
-- run now". Partial + composite index keeps this cheap even with
-- millions of disabled/paused monitors in the table.
CREATE INDEX idx_monitors_due_for_run ON monitors (next_run_at)
    WHERE is_enabled = TRUE;

-- health_checks --------------------------------------------------------------
-- Overwhelmingly the highest-volume table. Nearly all reads are
-- "recent history for monitor X", so (monitor_id, checked_at DESC)
-- is the primary access path — powers uptime charts, latency graphs,
-- and the failure-streak calculation.
CREATE INDEX idx_health_checks_monitor_id_checked_at
    ON health_checks (monitor_id, checked_at DESC);
-- Retention/cleanup jobs purge old rows by time regardless of monitor.
CREATE INDEX idx_health_checks_checked_at ON health_checks (checked_at);

-- incidents -------------------------------------------------------------------
-- Incident history/timeline for a monitor, most recent first.
CREATE INDEX idx_incidents_monitor_id_opened_at
    ON incidents (monitor_id, opened_at DESC);
-- "Is there already an open incident for this monitor?" / global
-- active-incidents dashboard. Partial index keeps it tiny since
-- ACTIVE rows are a small fraction of the table over time.
CREATE INDEX idx_incidents_active ON incidents (monitor_id)
    WHERE status = 'ACTIVE';

-- notification_channels ---------------------------------------------------------
-- "list channels for a project" (settings page) and the fan-out step
-- when an incident fires needs only the enabled ones.
CREATE INDEX idx_notification_channels_project_id ON notification_channels (project_id)
    WHERE is_enabled = TRUE;

-- notification_deliveries -----------------------------------------------------------
-- "delivery log for an incident" (incident detail page).
CREATE INDEX idx_notification_deliveries_incident_id ON notification_deliveries (incident_id);
-- "delivery history for a channel" (channel health/debug view).
CREATE INDEX idx_notification_deliveries_channel_id ON notification_deliveries (notification_channel_id);
-- Retry worker's queue query: find PENDING/FAILED deliveries to retry.
CREATE INDEX idx_notification_deliveries_status ON notification_deliveries (status)
    WHERE status IN ('PENDING', 'FAILED');

-- =====================================================================
-- SEED DATA FOR ENUMS
-- Native Postgres enum types are declared above via CREATE TYPE and
-- require no seed rows (unlike a lookup-table approach). This section
-- is included only for completeness/documentation of the allowed
-- values per enum, in case the team later migrates to lookup tables.
-- =====================================================================
-- project_member_role:        OWNER, ADMIN, MEMBER
-- invitation_status:          PENDING, ACCEPTED
-- http_method:                GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS
-- health_check_result:        HEALTHY, DOWN, TIMEOUT, SLOW
-- incident_status:            ACTIVE, RESOLVED
-- notification_channel_type:  EMAIL, SLACK, DISCORD, WEBHOOK
-- delivery_status:            PENDING, SENT, FAILED


-- Prevent duplicate monitors inside a project
ALTER TABLE monitors
ADD CONSTRAINT uq_project_monitor UNIQUE(project_id, url);

ALTER TABLE monitors
ADD CHECK (timeout_ms BETWEEN 1000 AND 30000);

-- ALTER TABLE monitors
-- ADD CHECK (failure_threshold BETWEEN 1 AND 10);

-- -- Prevent duplicate notification destinations
-- ALTER TABLE notification_channels
-- ADD CONSTRAINT uq_notification_destination
-- UNIQUE(project_id, channel_type, destination);

-- Only one ACTIVE incident per monitor
CREATE UNIQUE INDEX uq_active_incident
ON incidents(monitor_id)
WHERE status='ACTIVE';