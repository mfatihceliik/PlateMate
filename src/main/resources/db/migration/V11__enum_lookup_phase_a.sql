-- Phase A: compatibility layer for persisted enum -> lookup conversion.
-- Keeps legacy columns temporarily while introducing *_id lookup FKs.

CREATE TABLE IF NOT EXISTS public.user_subscription_statuses (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.user_subscription_statuses (id, code, label, sort_order, active) VALUES
    (1, 'PENDING', 'Pending', 1, TRUE),
    (2, 'ACTIVE', 'Active', 2, TRUE),
    (3, 'EXPIRED', 'Expired', 3, TRUE),
    (4, 'CANCELED', 'Canceled', 4, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.plate_statuses (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.plate_statuses (id, code, label, sort_order, active) VALUES
    (1, 'ACTIVE', 'Active', 1, TRUE),
    (2, 'PENDING', 'Pending', 2, TRUE),
    (3, 'HIDDEN_BY_REQUEST', 'Hidden By Request', 3, TRUE),
    (4, 'BLOCKED', 'Blocked', 4, TRUE),
    (5, 'DELETED', 'Deleted', 5, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.comment_report_reasons (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.comment_report_reasons (id, code, label, sort_order, active) VALUES
    (1, 'HATE_SPEECH', 'Hate Speech', 1, TRUE),
    (2, 'INSULT', 'Insult', 2, TRUE),
    (3, 'FALSE_INFORMATION', 'False Information', 3, TRUE),
    (4, 'PERSONAL_DATA', 'Personal Data', 4, TRUE),
    (5, 'THREAT', 'Threat', 5, TRUE),
    (6, 'SPAM', 'Spam', 6, TRUE),
    (7, 'OTHER', 'Other', 7, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.comment_report_statuses (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.comment_report_statuses (id, code, label, sort_order, active) VALUES
    (1, 'OPEN', 'Open', 1, TRUE),
    (2, 'REVIEWED', 'Reviewed', 2, TRUE),
    (3, 'ACCEPTED', 'Accepted', 3, TRUE),
    (4, 'REJECTED', 'Rejected', 4, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.plate_removal_request_reasons (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.plate_removal_request_reasons (id, code, label, sort_order, active) VALUES
    (1, 'PLATE_BELONGS_TO_ME', 'Plate Belongs To Me', 1, TRUE),
    (2, 'FALSE_INFORMATION', 'False Information', 2, TRUE),
    (3, 'PRIVACY_REQUEST', 'Privacy Request', 3, TRUE),
    (4, 'HARASSMENT', 'Harassment', 4, TRUE),
    (5, 'LEGAL_REQUEST', 'Legal Request', 5, TRUE),
    (6, 'OTHER', 'Other', 6, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.plate_removal_request_statuses (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.plate_removal_request_statuses (id, code, label, sort_order, active) VALUES
    (1, 'OPEN', 'Open', 1, TRUE),
    (2, 'IN_REVIEW', 'In Review', 2, TRUE),
    (3, 'ACCEPTED', 'Accepted', 3, TRUE),
    (4, 'REJECTED', 'Rejected', 4, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.plate_report_severities (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.plate_report_severities (id, code, label, sort_order, active) VALUES
    (1, 'RED', 'Red', 1, TRUE),
    (2, 'YELLOW', 'Yellow', 2, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.social_platforms (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.social_platforms (id, code, label, sort_order, active) VALUES
    (1, 'INSTAGRAM', 'Instagram', 1, TRUE),
    (2, 'X', 'X', 2, TRUE),
    (3, 'SNAPCHAT', 'Snapchat', 3, TRUE),
    (4, 'LINKEDIN', 'LinkedIn', 4, TRUE),
    (5, 'FACEBOOK', 'Facebook', 5, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.plate_review_moderation_action_types (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.plate_review_moderation_action_types (id, code, label, sort_order, active) VALUES
    (1, 'SUBMITTED_FOR_REVIEW', 'Submitted For Review', 1, TRUE),
    (2, 'APPROVED_BY_ADMIN', 'Approved By Admin', 2, TRUE),
    (3, 'REJECTED_BY_ADMIN', 'Rejected By Admin', 3, TRUE),
    (4, 'REMOVED_BY_MODERATOR', 'Removed By Moderator', 4, TRUE),
    (5, 'REMOVED_BY_USER', 'Removed By User', 5, TRUE),
    (6, 'AUTO_PENDING_BY_REPORT_THRESHOLD', 'Auto Pending By Report Threshold', 6, TRUE),
    (7, 'BACKFILL_SNAPSHOT', 'Backfill Snapshot', 7, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

CREATE TABLE IF NOT EXISTS public.user_role_codes (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.user_role_codes (id, code, label, sort_order, active) VALUES
    (1, 'NORMAL', 'Normal', 1, TRUE),
    (2, 'PREMIUM', 'Premium', 2, TRUE),
    (3, 'ADMIN', 'Admin', 3, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_subscriptions'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'user_subscriptions' AND column_name = 'status_id'
        ) THEN
            ALTER TABLE public.user_subscriptions ADD COLUMN status_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'user_subscriptions' AND column_name = 'status'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.user_subscriptions
                WHERE status IS NOT NULL
                  AND status NOT IN ('PENDING', 'ACTIVE', 'EXPIRED', 'CANCELED')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy user_subscriptions.status values detected. Migration aborted.';
            END IF;

            UPDATE public.user_subscriptions
            SET status_id = CASE status
                WHEN 'PENDING' THEN 1
                WHEN 'ACTIVE' THEN 2
                WHEN 'EXPIRED' THEN 3
                WHEN 'CANCELED' THEN 4
                ELSE status_id
            END
            WHERE status_id IS NULL;
        END IF;

        UPDATE public.user_subscriptions
        SET status_id = 1
        WHERE status_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_user_subscriptions_status_id'
              AND conrelid = 'public.user_subscriptions'::regclass
        ) THEN
            ALTER TABLE public.user_subscriptions
                ADD CONSTRAINT fk_user_subscriptions_status_id
                FOREIGN KEY (status_id) REFERENCES public.user_subscription_statuses (id);
        END IF;

        CREATE INDEX IF NOT EXISTS idx_user_subscriptions_status_id
            ON public.user_subscriptions (status_id);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'users'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'premium_until'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_subscriptions'
    ) THEN
        INSERT INTO public.user_subscriptions (
            user_id,
            status_id,
            purchased_days,
            started_at,
            expires_at,
            created_at,
            updated_at
        )
        SELECT
            u.id,
            2,
            GREATEST(1, CEIL(EXTRACT(EPOCH FROM (u.premium_until - NOW())) / 86400.0)::INT),
            NOW(),
            u.premium_until,
            NOW(),
            NOW()
        FROM public.users u
        WHERE u.premium_until IS NOT NULL
          AND u.premium_until > NOW()
          AND NOT EXISTS (
              SELECT 1 FROM public.user_subscriptions s WHERE s.user_id = u.id
          );
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plates'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plates' AND column_name = 'status_id'
        ) THEN
            ALTER TABLE public.plates ADD COLUMN status_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plates' AND column_name = 'status'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plates
                WHERE status IS NOT NULL
                  AND status NOT IN ('ACTIVE', 'PENDING', 'HIDDEN_BY_REQUEST', 'BLOCKED', 'DELETED')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plates.status values detected. Migration aborted.';
            END IF;

            UPDATE public.plates
            SET status_id = CASE status
                WHEN 'ACTIVE' THEN 1
                WHEN 'PENDING' THEN 2
                WHEN 'HIDDEN_BY_REQUEST' THEN 3
                WHEN 'BLOCKED' THEN 4
                WHEN 'DELETED' THEN 5
                ELSE status_id
            END
            WHERE status_id IS NULL;
        END IF;

        UPDATE public.plates SET status_id = 1 WHERE status_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_plates_status_id'
              AND conrelid = 'public.plates'::regclass
        ) THEN
            ALTER TABLE public.plates
                ADD CONSTRAINT fk_plates_status_id
                FOREIGN KEY (status_id) REFERENCES public.plate_statuses (id);
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'comment_reports'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'comment_reports' AND column_name = 'reason_id'
        ) THEN
            ALTER TABLE public.comment_reports ADD COLUMN reason_id BIGINT;
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'comment_reports' AND column_name = 'status_id'
        ) THEN
            ALTER TABLE public.comment_reports ADD COLUMN status_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'comment_reports' AND column_name = 'reason'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.comment_reports
                WHERE reason IS NOT NULL
                  AND reason NOT IN ('HATE_SPEECH', 'INSULT', 'FALSE_INFORMATION', 'PERSONAL_DATA', 'THREAT', 'SPAM', 'OTHER')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy comment_reports.reason values detected. Migration aborted.';
            END IF;

            UPDATE public.comment_reports
            SET reason_id = CASE reason
                WHEN 'HATE_SPEECH' THEN 1
                WHEN 'INSULT' THEN 2
                WHEN 'FALSE_INFORMATION' THEN 3
                WHEN 'PERSONAL_DATA' THEN 4
                WHEN 'THREAT' THEN 5
                WHEN 'SPAM' THEN 6
                WHEN 'OTHER' THEN 7
                ELSE reason_id
            END
            WHERE reason_id IS NULL;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'comment_reports' AND column_name = 'status'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.comment_reports
                WHERE status IS NOT NULL
                  AND status NOT IN ('OPEN', 'REVIEWED', 'ACCEPTED', 'REJECTED')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy comment_reports.status values detected. Migration aborted.';
            END IF;

            UPDATE public.comment_reports
            SET status_id = CASE status
                WHEN 'OPEN' THEN 1
                WHEN 'REVIEWED' THEN 2
                WHEN 'ACCEPTED' THEN 3
                WHEN 'REJECTED' THEN 4
                ELSE status_id
            END
            WHERE status_id IS NULL;
        END IF;

        UPDATE public.comment_reports SET reason_id = 7 WHERE reason_id IS NULL;
        UPDATE public.comment_reports SET status_id = 1 WHERE status_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_comment_reports_reason_id'
              AND conrelid = 'public.comment_reports'::regclass
        ) THEN
            ALTER TABLE public.comment_reports
                ADD CONSTRAINT fk_comment_reports_reason_id
                FOREIGN KEY (reason_id) REFERENCES public.comment_report_reasons (id);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_comment_reports_status_id'
              AND conrelid = 'public.comment_reports'::regclass
        ) THEN
            ALTER TABLE public.comment_reports
                ADD CONSTRAINT fk_comment_reports_status_id
                FOREIGN KEY (status_id) REFERENCES public.comment_report_statuses (id);
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plate_removal_requests'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_removal_requests' AND column_name = 'reason_id'
        ) THEN
            ALTER TABLE public.plate_removal_requests ADD COLUMN reason_id BIGINT;
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_removal_requests' AND column_name = 'status_id'
        ) THEN
            ALTER TABLE public.plate_removal_requests ADD COLUMN status_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_removal_requests' AND column_name = 'reason'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plate_removal_requests
                WHERE reason IS NOT NULL
                  AND reason NOT IN ('PLATE_BELONGS_TO_ME', 'FALSE_INFORMATION', 'PRIVACY_REQUEST', 'HARASSMENT', 'LEGAL_REQUEST', 'OTHER')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plate_removal_requests.reason values detected. Migration aborted.';
            END IF;

            UPDATE public.plate_removal_requests
            SET reason_id = CASE reason
                WHEN 'PLATE_BELONGS_TO_ME' THEN 1
                WHEN 'FALSE_INFORMATION' THEN 2
                WHEN 'PRIVACY_REQUEST' THEN 3
                WHEN 'HARASSMENT' THEN 4
                WHEN 'LEGAL_REQUEST' THEN 5
                WHEN 'OTHER' THEN 6
                ELSE reason_id
            END
            WHERE reason_id IS NULL;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_removal_requests' AND column_name = 'status'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plate_removal_requests
                WHERE status IS NOT NULL
                  AND status NOT IN ('OPEN', 'IN_REVIEW', 'ACCEPTED', 'REJECTED')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plate_removal_requests.status values detected. Migration aborted.';
            END IF;

            UPDATE public.plate_removal_requests
            SET status_id = CASE status
                WHEN 'OPEN' THEN 1
                WHEN 'IN_REVIEW' THEN 2
                WHEN 'ACCEPTED' THEN 3
                WHEN 'REJECTED' THEN 4
                ELSE status_id
            END
            WHERE status_id IS NULL;
        END IF;

        UPDATE public.plate_removal_requests SET reason_id = 6 WHERE reason_id IS NULL;
        UPDATE public.plate_removal_requests SET status_id = 1 WHERE status_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_plate_removal_requests_reason_id'
              AND conrelid = 'public.plate_removal_requests'::regclass
        ) THEN
            ALTER TABLE public.plate_removal_requests
                ADD CONSTRAINT fk_plate_removal_requests_reason_id
                FOREIGN KEY (reason_id) REFERENCES public.plate_removal_request_reasons (id);
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_plate_removal_requests_status_id'
              AND conrelid = 'public.plate_removal_requests'::regclass
        ) THEN
            ALTER TABLE public.plate_removal_requests
                ADD CONSTRAINT fk_plate_removal_requests_status_id
                FOREIGN KEY (status_id) REFERENCES public.plate_removal_request_statuses (id);
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plate_report_types'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_report_types' AND column_name = 'severity_id'
        ) THEN
            ALTER TABLE public.plate_report_types ADD COLUMN severity_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_report_types' AND column_name = 'severity'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plate_report_types
                WHERE severity IS NOT NULL
                  AND severity NOT IN ('RED', 'YELLOW')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plate_report_types.severity values detected. Migration aborted.';
            END IF;

            UPDATE public.plate_report_types
            SET severity_id = CASE severity
                WHEN 'RED' THEN 1
                WHEN 'YELLOW' THEN 2
                ELSE severity_id
            END
            WHERE severity_id IS NULL;
        END IF;

        UPDATE public.plate_report_types SET severity_id = 1 WHERE severity_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_plate_report_types_severity_id'
              AND conrelid = 'public.plate_report_types'::regclass
        ) THEN
            ALTER TABLE public.plate_report_types
                ADD CONSTRAINT fk_plate_report_types_severity_id
                FOREIGN KEY (severity_id) REFERENCES public.plate_report_severities (id);
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'social_media_links'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'social_media_links' AND column_name = 'platform_id'
        ) THEN
            ALTER TABLE public.social_media_links ADD COLUMN platform_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'social_media_links' AND column_name = 'platform'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.social_media_links
                WHERE platform IS NOT NULL
                  AND platform NOT IN ('INSTAGRAM', 'X', 'SNAPCHAT', 'LINKEDIN', 'FACEBOOK')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy social_media_links.platform values detected. Migration aborted.';
            END IF;

            UPDATE public.social_media_links
            SET platform_id = CASE platform
                WHEN 'INSTAGRAM' THEN 1
                WHEN 'X' THEN 2
                WHEN 'SNAPCHAT' THEN 3
                WHEN 'LINKEDIN' THEN 4
                WHEN 'FACEBOOK' THEN 5
                ELSE platform_id
            END
            WHERE platform_id IS NULL;
        END IF;

        UPDATE public.social_media_links SET platform_id = 1 WHERE platform_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_social_media_links_platform_id'
              AND conrelid = 'public.social_media_links'::regclass
        ) THEN
            ALTER TABLE public.social_media_links
                ADD CONSTRAINT fk_social_media_links_platform_id
                FOREIGN KEY (platform_id) REFERENCES public.social_platforms (id);
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plate_review_moderation_events'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_review_moderation_events' AND column_name = 'action_type_id'
        ) THEN
            ALTER TABLE public.plate_review_moderation_events ADD COLUMN action_type_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'plate_review_moderation_events' AND column_name = 'action_type'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plate_review_moderation_events
                WHERE action_type IS NULL
                   OR action_type NOT IN (
                       'SUBMITTED_FOR_REVIEW',
                       'APPROVED_BY_ADMIN',
                       'REJECTED_BY_ADMIN',
                       'REMOVED_BY_MODERATOR',
                       'REMOVED_BY_USER',
                       'AUTO_PENDING_BY_REPORT_THRESHOLD',
                       'BACKFILL_SNAPSHOT'
                   )
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plate_review_moderation_events.action_type values detected. Migration aborted.';
            END IF;

            UPDATE public.plate_review_moderation_events
            SET action_type_id = CASE action_type
                WHEN 'SUBMITTED_FOR_REVIEW' THEN 1
                WHEN 'APPROVED_BY_ADMIN' THEN 2
                WHEN 'REJECTED_BY_ADMIN' THEN 3
                WHEN 'REMOVED_BY_MODERATOR' THEN 4
                WHEN 'REMOVED_BY_USER' THEN 5
                WHEN 'AUTO_PENDING_BY_REPORT_THRESHOLD' THEN 6
                WHEN 'BACKFILL_SNAPSHOT' THEN 7
                ELSE action_type_id
            END
            WHERE action_type_id IS NULL;
        END IF;

        UPDATE public.plate_review_moderation_events SET action_type_id = 1 WHERE action_type_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_prme_action_type_id'
              AND conrelid = 'public.plate_review_moderation_events'::regclass
        ) THEN
            ALTER TABLE public.plate_review_moderation_events
                ADD CONSTRAINT fk_prme_action_type_id
                FOREIGN KEY (action_type_id) REFERENCES public.plate_review_moderation_action_types (id);
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_roles'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'user_roles' AND column_name = 'code_id'
        ) THEN
            ALTER TABLE public.user_roles ADD COLUMN code_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'user_roles' AND column_name = 'code'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.user_roles
                WHERE code IS NOT NULL
                  AND code NOT IN ('NORMAL', 'PREMIUM', 'ADMIN')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy user_roles.code values detected. Migration aborted.';
            END IF;

            UPDATE public.user_roles
            SET code_id = CASE code
                WHEN 'NORMAL' THEN 1
                WHEN 'PREMIUM' THEN 2
                WHEN 'ADMIN' THEN 3
                ELSE code_id
            END
            WHERE code_id IS NULL;
        END IF;

        UPDATE public.user_roles SET code_id = 1 WHERE code_id IS NULL;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'fk_user_roles_code_id'
              AND conrelid = 'public.user_roles'::regclass
        ) THEN
            ALTER TABLE public.user_roles
                ADD CONSTRAINT fk_user_roles_code_id
                FOREIGN KEY (code_id) REFERENCES public.user_role_codes (id);
        END IF;

        CREATE UNIQUE INDEX IF NOT EXISTS ux_user_roles_code_id ON public.user_roles (code_id);
    END IF;
END
$$;
