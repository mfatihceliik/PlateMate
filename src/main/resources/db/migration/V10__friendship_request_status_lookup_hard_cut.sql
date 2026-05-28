CREATE TABLE IF NOT EXISTS public.friendship_request_statuses (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(128) NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO public.friendship_request_statuses (id, code, label, sort_order, active) VALUES
    (1, 'REQUESTED', 'Requested', 1, TRUE),
    (2, 'ACCEPTED', 'Accepted', 2, TRUE),
    (3, 'REJECTED', 'Rejected', 3, TRUE),
    (4, 'REMOVED', 'Removed', 4, TRUE)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    active = EXCLUDED.active;

DO $$
DECLARE
    legacy_constraint_name TEXT;
    legacy_index_name TEXT;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'friendships'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'friendships'
              AND column_name = 'status_id'
        ) THEN
            ALTER TABLE public.friendships ADD COLUMN status_id BIGINT;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'friendships'
              AND column_name = 'status'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.friendships
                WHERE status IS NOT NULL
                  AND status NOT IN ('PENDING', 'REQUESTED', 'ACCEPTED', 'REJECTED', 'REMOVED')
            ) THEN
                RAISE EXCEPTION 'Unknown legacy friendships.status values detected. Migration aborted.';
            END IF;

            UPDATE public.friendships
            SET status_id = CASE status
                WHEN 'PENDING' THEN 1
                WHEN 'REQUESTED' THEN 1
                WHEN 'ACCEPTED' THEN 2
                WHEN 'REJECTED' THEN 3
                WHEN 'REMOVED' THEN 4
                ELSE status_id
            END
            WHERE status_id IS NULL;
        END IF;

        UPDATE public.friendships
        SET status_id = 1
        WHERE status_id IS NULL;

        ALTER TABLE public.friendships
            ALTER COLUMN status_id SET NOT NULL;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_friendships_status_id'
              AND conrelid = 'public.friendships'::regclass
        ) THEN
            ALTER TABLE public.friendships
                ADD CONSTRAINT fk_friendships_status_id
                FOREIGN KEY (status_id) REFERENCES public.friendship_request_statuses (id);
        END IF;

        FOR legacy_constraint_name IN
            SELECT c.conname
            FROM pg_constraint c
            WHERE c.conrelid = 'public.friendships'::regclass
              AND c.contype = 'u'
              AND (
                  pg_get_constraintdef(c.oid) LIKE '%(requester_id, addressee_id)%'
                  OR pg_get_constraintdef(c.oid) LIKE '%(addressee_id, requester_id)%'
              )
        LOOP
            EXECUTE format('ALTER TABLE public.friendships DROP CONSTRAINT %I', legacy_constraint_name);
        END LOOP;

        FOR legacy_index_name IN
            SELECT i.indexname
            FROM pg_indexes i
            WHERE i.schemaname = 'public'
              AND i.tablename = 'friendships'
              AND i.indexdef ILIKE 'CREATE UNIQUE INDEX%'
              AND (
                  i.indexdef ILIKE '%(requester_id, addressee_id)%'
                  OR i.indexdef ILIKE '%(addressee_id, requester_id)%'
              )
              AND i.indexname <> 'ux_friendships_pair_active'
        LOOP
            EXECUTE format('DROP INDEX IF EXISTS public.%I', legacy_index_name);
        END LOOP;

        IF EXISTS (
            SELECT 1
            FROM public.friendships
            WHERE status_id IN (1, 2)
            GROUP BY LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id)
            HAVING COUNT(*) > 1
        ) THEN
            RAISE EXCEPTION 'Duplicate active friendship rows found for a user pair. Manual cleanup is required before adding unique index.';
        END IF;

        CREATE INDEX IF NOT EXISTS idx_friendships_status_id
            ON public.friendships (status_id);

        CREATE INDEX IF NOT EXISTS idx_friendships_addressee_status_id
            ON public.friendships (addressee_id, status_id);

        CREATE INDEX IF NOT EXISTS idx_friendships_last_action_at
            ON public.friendships ((COALESCE(responded_at, created_at)) DESC);

        CREATE UNIQUE INDEX IF NOT EXISTS ux_friendships_pair_active
            ON public.friendships ((LEAST(requester_id, addressee_id)), (GREATEST(requester_id, addressee_id)))
            WHERE status_id IN (1, 2);

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'friendships'
              AND column_name = 'status'
        ) THEN
            ALTER TABLE public.friendships DROP COLUMN status;
        END IF;
    END IF;
END
$$;
