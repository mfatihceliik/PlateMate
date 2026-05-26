DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'plates'
    ) THEN
        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS report_count INTEGER;
        UPDATE public.plates SET report_count = 0 WHERE report_count IS NULL;
        ALTER TABLE public.plates ALTER COLUMN report_count SET DEFAULT 0;
        ALTER TABLE public.plates ALTER COLUMN report_count SET NOT NULL;

        ALTER TABLE public.plates ADD COLUMN IF NOT EXISTS status VARCHAR(32);
        UPDATE public.plates SET status = 'ACTIVE' WHERE status IS NULL;
        ALTER TABLE public.plates ALTER COLUMN status SET DEFAULT 'ACTIVE';
        ALTER TABLE public.plates ALTER COLUMN status SET NOT NULL;

        CREATE INDEX IF NOT EXISTS idx_plates_status ON public.plates (status);
    END IF;
END
$$;

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

        CREATE INDEX IF NOT EXISTS idx_users_active ON public.users (active);
    END IF;
END
$$;
