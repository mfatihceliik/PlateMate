CREATE TABLE IF NOT EXISTS public.plate_review_statuses (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.plate_review_statuses (id, code, label, sort_order, active) VALUES
    (1, 'PENDING_REVIEW', 'Pending Review', 1, TRUE),
    (2, 'APPROVED', 'Approved', 2, TRUE),
    (3, 'REJECTED', 'Rejected', 3, TRUE),
    (4, 'REMOVED_BY_USER', 'Removed By User', 4, TRUE),
    (5, 'REMOVED_BY_MODERATOR', 'Removed By Moderator', 5, TRUE),
    (6, 'REMOVED_BY_LEGAL_REQUEST', 'Removed By Legal Request', 6, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'plate_reviews'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_reviews'
              AND column_name = 'status_id'
        ) THEN
            ALTER TABLE public.plate_reviews ADD COLUMN status_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_reviews'
              AND column_name = 'status'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plate_reviews
                WHERE status IS NOT NULL
                  AND status NOT IN (
                      'PENDING_REVIEW',
                      'APPROVED',
                      'REJECTED',
                      'REMOVED_BY_USER',
                      'REMOVED_BY_MODERATOR',
                      'REMOVED_BY_LEGAL_REQUEST'
                  )
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plate_reviews.status values detected. Migration aborted.';
            END IF;

            UPDATE public.plate_reviews
            SET status_id = CASE status
                WHEN 'PENDING_REVIEW' THEN 1
                WHEN 'APPROVED' THEN 2
                WHEN 'REJECTED' THEN 3
                WHEN 'REMOVED_BY_USER' THEN 4
                WHEN 'REMOVED_BY_MODERATOR' THEN 5
                WHEN 'REMOVED_BY_LEGAL_REQUEST' THEN 6
                ELSE status_id
            END
            WHERE status_id IS NULL;
        END IF;

        UPDATE public.plate_reviews
        SET status_id = 1
        WHERE status_id IS NULL;

        ALTER TABLE public.plate_reviews
            ALTER COLUMN status_id SET NOT NULL;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_plate_reviews_status_id'
              AND conrelid = 'public.plate_reviews'::regclass
        ) THEN
            ALTER TABLE public.plate_reviews
                ADD CONSTRAINT fk_plate_reviews_status_id
                FOREIGN KEY (status_id) REFERENCES public.plate_review_statuses (id);
        END IF;

        DROP INDEX IF EXISTS public.idx_plate_reviews_user_id_status;
        DROP INDEX IF EXISTS public.idx_plate_reviews_status_created_at;
        DROP INDEX IF EXISTS public.idx_plate_reviews_status;
        DROP INDEX IF EXISTS public.idx_plate_reviews_status_created;

        CREATE INDEX IF NOT EXISTS idx_plate_reviews_status_id
            ON public.plate_reviews (status_id);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_status_id_created_at
            ON public.plate_reviews (status_id, created_at DESC);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_plate_id_status_id
            ON public.plate_reviews (plate_id, status_id);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_user_id_status_id
            ON public.plate_reviews (user_id, status_id);

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_reviews'
              AND column_name = 'status'
        ) THEN
            ALTER TABLE public.plate_reviews DROP COLUMN status;
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'plate_review_moderation_events'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_review_moderation_events'
              AND column_name = 'from_status_id'
        ) THEN
            ALTER TABLE public.plate_review_moderation_events ADD COLUMN from_status_id BIGINT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_review_moderation_events'
              AND column_name = 'to_status_id'
        ) THEN
            ALTER TABLE public.plate_review_moderation_events ADD COLUMN to_status_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_review_moderation_events'
              AND column_name = 'to_status'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plate_review_moderation_events
                WHERE to_status IS NULL
                   OR to_status NOT IN (
                       'PENDING_REVIEW',
                       'APPROVED',
                       'REJECTED',
                       'REMOVED_BY_USER',
                       'REMOVED_BY_MODERATOR',
                       'REMOVED_BY_LEGAL_REQUEST'
                   )
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plate_review_moderation_events.to_status values detected. Migration aborted.';
            END IF;

            IF EXISTS (
                SELECT 1
                FROM public.plate_review_moderation_events
                WHERE from_status IS NOT NULL
                  AND from_status NOT IN (
                      'PENDING_REVIEW',
                      'APPROVED',
                      'REJECTED',
                      'REMOVED_BY_USER',
                      'REMOVED_BY_MODERATOR',
                      'REMOVED_BY_LEGAL_REQUEST'
                  )
            ) THEN
                RAISE EXCEPTION 'Unknown legacy plate_review_moderation_events.from_status values detected. Migration aborted.';
            END IF;

            UPDATE public.plate_review_moderation_events
            SET from_status_id = CASE from_status
                WHEN 'PENDING_REVIEW' THEN 1
                WHEN 'APPROVED' THEN 2
                WHEN 'REJECTED' THEN 3
                WHEN 'REMOVED_BY_USER' THEN 4
                WHEN 'REMOVED_BY_MODERATOR' THEN 5
                WHEN 'REMOVED_BY_LEGAL_REQUEST' THEN 6
                ELSE from_status_id
            END
            WHERE from_status_id IS NULL
              AND from_status IS NOT NULL;

            UPDATE public.plate_review_moderation_events
            SET to_status_id = CASE to_status
                WHEN 'PENDING_REVIEW' THEN 1
                WHEN 'APPROVED' THEN 2
                WHEN 'REJECTED' THEN 3
                WHEN 'REMOVED_BY_USER' THEN 4
                WHEN 'REMOVED_BY_MODERATOR' THEN 5
                WHEN 'REMOVED_BY_LEGAL_REQUEST' THEN 6
                ELSE to_status_id
            END
            WHERE to_status_id IS NULL;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM public.plate_review_moderation_events
            WHERE to_status_id IS NULL
        ) THEN
            RAISE EXCEPTION 'plate_review_moderation_events.to_status_id contains NULL values. Migration aborted.';
        END IF;

        ALTER TABLE public.plate_review_moderation_events
            ALTER COLUMN to_status_id SET NOT NULL;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_prme_from_status_id'
              AND conrelid = 'public.plate_review_moderation_events'::regclass
        ) THEN
            ALTER TABLE public.plate_review_moderation_events
                ADD CONSTRAINT fk_prme_from_status_id
                FOREIGN KEY (from_status_id) REFERENCES public.plate_review_statuses (id);
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_prme_to_status_id'
              AND conrelid = 'public.plate_review_moderation_events'::regclass
        ) THEN
            ALTER TABLE public.plate_review_moderation_events
                ADD CONSTRAINT fk_prme_to_status_id
                FOREIGN KEY (to_status_id) REFERENCES public.plate_review_statuses (id);
        END IF;

        DROP INDEX IF EXISTS public.idx_prme_to_status_created_at;
        CREATE INDEX IF NOT EXISTS idx_prme_to_status_id_created_at
            ON public.plate_review_moderation_events (to_status_id, created_at DESC);

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_review_moderation_events'
              AND column_name = 'from_status'
        ) THEN
            ALTER TABLE public.plate_review_moderation_events DROP COLUMN from_status;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'plate_review_moderation_events'
              AND column_name = 'to_status'
        ) THEN
            ALTER TABLE public.plate_review_moderation_events DROP COLUMN to_status;
        END IF;
    END IF;
END
$$;
