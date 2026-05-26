DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'users'
    ) THEN
        ALTER TABLE public.users ADD COLUMN IF NOT EXISTS active BOOLEAN;
        UPDATE public.users SET active = TRUE WHERE active IS NULL;
        ALTER TABLE public.users ALTER COLUMN active SET DEFAULT TRUE;
        ALTER TABLE public.users ALTER COLUMN active SET NOT NULL;

        ALTER TABLE public.users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
        UPDATE public.users SET updated_at = NOW() WHERE updated_at IS NULL;
        ALTER TABLE public.users ALTER COLUMN updated_at SET NOT NULL;

        ALTER TABLE public.users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

        CREATE INDEX IF NOT EXISTS idx_users_active ON public.users (active);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plate_reviews'
    ) THEN
        ALTER TABLE public.plate_reviews
            ADD COLUMN IF NOT EXISTS responsibility_policy_version VARCHAR(32);
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS public.comment_reports (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    reporter_user_id BIGINT NOT NULL,
    reason VARCHAR(32) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    admin_note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    CONSTRAINT uk_comment_reports_comment_reporter UNIQUE (comment_id, reporter_user_id),
    CONSTRAINT fk_comment_reports_comment
        FOREIGN KEY (comment_id) REFERENCES public.plate_reviews (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_comment_reports_comment_id
    ON public.comment_reports (comment_id);
CREATE INDEX IF NOT EXISTS idx_comment_reports_status
    ON public.comment_reports (status);
CREATE INDEX IF NOT EXISTS idx_comment_reports_created_at
    ON public.comment_reports (created_at);

CREATE TABLE IF NOT EXISTS public.plate_removal_requests (
    id BIGSERIAL PRIMARY KEY,
    plate_id BIGINT NOT NULL,
    requester_user_id BIGINT,
    requester_email VARCHAR(255),
    reason VARCHAR(32) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    admin_note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    CONSTRAINT fk_plate_removal_requests_plate
        FOREIGN KEY (plate_id) REFERENCES public.plates (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_plate_removal_requests_plate_id
    ON public.plate_removal_requests (plate_id);
CREATE INDEX IF NOT EXISTS idx_plate_removal_requests_status
    ON public.plate_removal_requests (status);
CREATE INDEX IF NOT EXISTS idx_plate_removal_requests_created_at
    ON public.plate_removal_requests (created_at);

CREATE TABLE IF NOT EXISTS public.audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(128) NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL,
    ip_hash VARCHAR(64),
    user_agent_hash VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at
    ON public.audit_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action
    ON public.audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_target
    ON public.audit_logs (target_type, target_id);
