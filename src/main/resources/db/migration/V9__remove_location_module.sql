DROP TABLE IF EXISTS public.location_blocks;
DROP TABLE IF EXISTS public.user_locations;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'user_settings'
    ) THEN
        ALTER TABLE public.user_settings
            DROP COLUMN IF EXISTS location_sharing_enabled;
    END IF;
END
$$;
