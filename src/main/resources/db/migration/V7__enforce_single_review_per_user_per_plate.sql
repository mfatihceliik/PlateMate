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
            FROM pg_indexes
            WHERE schemaname = 'public'
              AND indexname = 'ux_plate_reviews_user_id_plate_id'
        ) THEN
            IF EXISTS (
                SELECT 1
                FROM public.plate_reviews
                GROUP BY user_id, plate_id
                HAVING COUNT(*) > 1
            ) THEN
                RAISE EXCEPTION
                    'Duplicate plate_reviews rows found for (user_id, plate_id). Manual cleanup is required before adding unique index.';
            END IF;

            CREATE UNIQUE INDEX ux_plate_reviews_user_id_plate_id
                ON public.plate_reviews (user_id, plate_id);
        END IF;
    END IF;
END
$$;
