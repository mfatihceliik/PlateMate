CREATE TABLE IF NOT EXISTS public.plate_review_moderation_events (
    id BIGSERIAL PRIMARY KEY,
    plate_review_id BIGINT NOT NULL,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    actor_user_id BIGINT,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_plate_review_moderation_events_review
        FOREIGN KEY (plate_review_id) REFERENCES public.plate_reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_plate_review_moderation_events_actor
        FOREIGN KEY (actor_user_id) REFERENCES public.users (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_prme_review_created_at
    ON public.plate_review_moderation_events (plate_review_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_prme_to_status_created_at
    ON public.plate_review_moderation_events (to_status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_prme_actor_created_at
    ON public.plate_review_moderation_events (actor_user_id, created_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS ux_prme_backfill_once
    ON public.plate_review_moderation_events (plate_review_id)
    WHERE action_type = 'BACKFILL_SNAPSHOT';

INSERT INTO public.plate_review_moderation_events (
    plate_review_id,
    from_status,
    to_status,
    action_type,
    actor_user_id,
    reason,
    created_at
)
SELECT
    r.id,
    NULL,
    r.status,
    'BACKFILL_SNAPSHOT',
    NULL,
    'INITIAL_SNAPSHOT',
    COALESCE(r.updated_at, r.created_at, NOW())
FROM public.plate_reviews r
WHERE NOT EXISTS (
    SELECT 1
    FROM public.plate_review_moderation_events e
    WHERE e.plate_review_id = r.id
      AND e.action_type = 'BACKFILL_SNAPSHOT'
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plates'
    ) THEN
        ALTER TABLE public.plates DROP COLUMN IF EXISTS created_by_user_id;
        ALTER TABLE public.plates DROP COLUMN IF EXISTS report_count;
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
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_user_id_created_at
            ON public.plate_reviews (user_id, created_at DESC);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_user_id_status
            ON public.plate_reviews (user_id, status);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_status_created_at
            ON public.plate_reviews (status, created_at DESC);
    END IF;
END
$$;
