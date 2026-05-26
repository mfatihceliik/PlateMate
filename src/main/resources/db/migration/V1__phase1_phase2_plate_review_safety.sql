DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plates'
    ) THEN
        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS status VARCHAR(32);
        UPDATE public.plates SET status = 'ACTIVE' WHERE status IS NULL;
        ALTER TABLE public.plates ALTER COLUMN status SET DEFAULT 'ACTIVE';
        ALTER TABLE public.plates ALTER COLUMN status SET NOT NULL;

        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;

        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS report_count INTEGER;
        UPDATE public.plates SET report_count = 0 WHERE report_count IS NULL;
        ALTER TABLE public.plates ALTER COLUMN report_count SET DEFAULT 0;
        ALTER TABLE public.plates ALTER COLUMN report_count SET NOT NULL;

        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS hidden_reason VARCHAR(500);
        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
        UPDATE public.plates SET created_at = NOW() WHERE created_at IS NULL;
        ALTER TABLE public.plates ALTER COLUMN created_at SET NOT NULL;

        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
        UPDATE public.plates SET updated_at = NOW() WHERE updated_at IS NULL;
        ALTER TABLE public.plates ALTER COLUMN updated_at SET NOT NULL;

        CREATE UNIQUE INDEX IF NOT EXISTS ux_plates_plate_code ON public.plates (plate_code);
        CREATE INDEX IF NOT EXISTS idx_plates_status ON public.plates (status);
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
        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS status VARCHAR(32);
        UPDATE public.plate_reviews SET status = 'APPROVED' WHERE status IS NULL;
        ALTER TABLE public.plate_reviews ALTER COLUMN status SET DEFAULT 'APPROVED';
        ALTER TABLE public.plate_reviews ALTER COLUMN status SET NOT NULL;

        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(255);

        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS report_count INTEGER;
        UPDATE public.plate_reviews SET report_count = 0 WHERE report_count IS NULL;
        ALTER TABLE public.plate_reviews ALTER COLUMN report_count SET DEFAULT 0;
        ALTER TABLE public.plate_reviews ALTER COLUMN report_count SET NOT NULL;

        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS user_accepted_responsibility BOOLEAN;
        UPDATE public.plate_reviews
        SET user_accepted_responsibility = false
        WHERE user_accepted_responsibility IS NULL;
        ALTER TABLE public.plate_reviews ALTER COLUMN user_accepted_responsibility SET DEFAULT false;
        ALTER TABLE public.plate_reviews ALTER COLUMN user_accepted_responsibility SET NOT NULL;

        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS user_accepted_responsibility_at TIMESTAMP;
        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS ip_hash VARCHAR(64);
        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS user_agent_hash VARCHAR(64);
        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
        UPDATE public.plate_reviews SET created_at = NOW() WHERE created_at IS NULL;
        ALTER TABLE public.plate_reviews ALTER COLUMN created_at SET NOT NULL;

        ALTER TABLE public.plate_reviews ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
        UPDATE public.plate_reviews SET updated_at = NOW() WHERE updated_at IS NULL;
        ALTER TABLE public.plate_reviews ALTER COLUMN updated_at SET NOT NULL;

        CREATE INDEX IF NOT EXISTS idx_plate_reviews_status ON public.plate_reviews (status);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_plate_id_status ON public.plate_reviews (plate_id, status);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_created_at ON public.plate_reviews (created_at);
        CREATE INDEX IF NOT EXISTS idx_plate_reviews_plate_id_created_at ON public.plate_reviews (plate_id, created_at);
    END IF;
END
$$;
