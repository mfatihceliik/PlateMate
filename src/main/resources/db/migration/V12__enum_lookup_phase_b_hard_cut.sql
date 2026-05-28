-- Phase B: hard cut to lookup ids.
-- Removes legacy enum/string columns after Phase A backfill.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_subscriptions'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.user_subscriptions WHERE status_id IS NULL
        ) THEN
            RAISE EXCEPTION 'user_subscriptions.status_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.user_subscriptions
            ALTER COLUMN status_id SET NOT NULL;

        ALTER TABLE public.user_subscriptions
            DROP COLUMN IF EXISTS status;

        CREATE INDEX IF NOT EXISTS idx_user_subscriptions_status_id_created_at
            ON public.user_subscriptions (status_id, created_at DESC);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plates'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.plates WHERE status_id IS NULL
        ) THEN
            RAISE EXCEPTION 'plates.status_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.plates
            ALTER COLUMN status_id SET NOT NULL;

        ALTER TABLE public.plates
            DROP COLUMN IF EXISTS status;

        CREATE INDEX IF NOT EXISTS idx_plates_status_id
            ON public.plates (status_id);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'comment_reports'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.comment_reports WHERE reason_id IS NULL OR status_id IS NULL
        ) THEN
            RAISE EXCEPTION 'comment_reports.reason_id/status_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.comment_reports
            ALTER COLUMN reason_id SET NOT NULL;
        ALTER TABLE public.comment_reports
            ALTER COLUMN status_id SET NOT NULL;

        ALTER TABLE public.comment_reports
            DROP COLUMN IF EXISTS reason;
        ALTER TABLE public.comment_reports
            DROP COLUMN IF EXISTS status;

        DROP INDEX IF EXISTS public.idx_comment_reports_comment_status;
        DROP INDEX IF EXISTS public.idx_comment_reports_status_created_at;
        CREATE INDEX IF NOT EXISTS idx_comment_reports_comment_status_id
            ON public.comment_reports (comment_id, status_id);
        CREATE INDEX IF NOT EXISTS idx_comment_reports_status_id_created_at
            ON public.comment_reports (status_id, created_at DESC);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plate_removal_requests'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.plate_removal_requests WHERE reason_id IS NULL OR status_id IS NULL
        ) THEN
            RAISE EXCEPTION 'plate_removal_requests.reason_id/status_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.plate_removal_requests
            ALTER COLUMN reason_id SET NOT NULL;
        ALTER TABLE public.plate_removal_requests
            ALTER COLUMN status_id SET NOT NULL;

        ALTER TABLE public.plate_removal_requests
            DROP COLUMN IF EXISTS reason;
        ALTER TABLE public.plate_removal_requests
            DROP COLUMN IF EXISTS status;

        CREATE INDEX IF NOT EXISTS idx_plate_removal_requests_plate_status_id
            ON public.plate_removal_requests (plate_id, status_id);
        CREATE INDEX IF NOT EXISTS idx_plate_removal_requests_status_id_created_at
            ON public.plate_removal_requests (status_id, created_at DESC);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plate_report_types'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.plate_report_types WHERE severity_id IS NULL
        ) THEN
            RAISE EXCEPTION 'plate_report_types.severity_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.plate_report_types
            ALTER COLUMN severity_id SET NOT NULL;

        ALTER TABLE public.plate_report_types
            DROP COLUMN IF EXISTS severity;

        CREATE INDEX IF NOT EXISTS idx_plate_report_types_severity_id
            ON public.plate_report_types (severity_id);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'social_media_links'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.social_media_links WHERE platform_id IS NULL
        ) THEN
            RAISE EXCEPTION 'social_media_links.platform_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.social_media_links
            ALTER COLUMN platform_id SET NOT NULL;

        ALTER TABLE public.social_media_links
            DROP COLUMN IF EXISTS platform;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plate_review_moderation_events'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.plate_review_moderation_events WHERE action_type_id IS NULL
        ) THEN
            RAISE EXCEPTION 'plate_review_moderation_events.action_type_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.plate_review_moderation_events
            ALTER COLUMN action_type_id SET NOT NULL;

        DROP INDEX IF EXISTS public.ux_prme_backfill_once;
        CREATE UNIQUE INDEX IF NOT EXISTS ux_prme_backfill_once
            ON public.plate_review_moderation_events (plate_review_id)
            WHERE action_type_id = 7;

        ALTER TABLE public.plate_review_moderation_events
            DROP COLUMN IF EXISTS action_type;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_roles'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM public.user_roles WHERE code_id IS NULL
        ) THEN
            RAISE EXCEPTION 'user_roles.code_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.user_roles
            ALTER COLUMN code_id SET NOT NULL;

        ALTER TABLE public.user_roles
            DROP COLUMN IF EXISTS code;

        CREATE UNIQUE INDEX IF NOT EXISTS ux_user_roles_code_id
            ON public.user_roles (code_id);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'users'
    ) THEN
        ALTER TABLE public.users DROP COLUMN IF EXISTS premium_until;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_profiles'
    ) THEN
        ALTER TABLE public.user_profiles DROP COLUMN IF EXISTS driver_rating;
        ALTER TABLE public.user_profiles DROP COLUMN IF EXISTS review_count;
        ALTER TABLE public.user_profiles DROP COLUMN IF EXISTS total_rating_sum;
    END IF;
END
$$;
