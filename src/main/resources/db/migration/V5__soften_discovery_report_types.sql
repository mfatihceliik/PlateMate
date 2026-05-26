DO $$
DECLARE
    v_has_plate_report_types BOOLEAN;
    v_has_plate_reports BOOLEAN;
    v_traffic_type_id BIGINT;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'plate_report_types'
    ) INTO v_has_plate_report_types;

    IF NOT v_has_plate_report_types THEN
        RETURN;
    END IF;

    INSERT INTO public.plate_report_types (
        code,
        label,
        description,
        icon_key,
        severity,
        color_hex,
        weight,
        sort_order,
        active,
        created_at,
        updated_at
    )
    SELECT
        'TRAFFIC_RULE_VIOLATION',
        'Trafik Kurali Ihlali',
        'Trafik kurali ihlaline iliskin davranis bildirildi',
        'traffic_rule_violation',
        'RED',
        '#E53935',
        4,
        COALESCE((SELECT MAX(sort_order) + 1 FROM public.plate_report_types), 1),
        TRUE,
        NOW(),
        NOW()
    WHERE NOT EXISTS (
        SELECT 1
        FROM public.plate_report_types
        WHERE code = 'TRAFFIC_RULE_VIOLATION'
    );

    SELECT id
    INTO v_traffic_type_id
    FROM public.plate_report_types
    WHERE code = 'TRAFFIC_RULE_VIOLATION'
    ORDER BY id
    LIMIT 1;

    SELECT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'plate_reports'
    ) INTO v_has_plate_reports;

    IF v_has_plate_reports AND v_traffic_type_id IS NOT NULL THEN
        WITH legacy_rows AS (
            SELECT r.*
            FROM public.plate_reports r
            JOIN public.plate_report_types t ON t.id = r.report_type_id
            WHERE t.code IN ('HIT_AND_RUN', 'AGGRESSIVE_DRIVER')
        ),
        aggregated AS (
            SELECT
                plate_id,
                user_id,
                bool_or(active) AS active,
                min(first_reported_at) AS first_reported_at,
                max(last_reported_at) AS last_reported_at,
                min(created_at) AS created_at,
                max(updated_at) AS updated_at,
                max(deactivated_at) AS deactivated_at
            FROM legacy_rows
            GROUP BY plate_id, user_id
        )
        INSERT INTO public.plate_reports (
            plate_id,
            user_id,
            report_type_id,
            active,
            first_reported_at,
            last_reported_at,
            deactivated_at,
            created_at,
            updated_at
        )
        SELECT
            a.plate_id,
            a.user_id,
            v_traffic_type_id,
            a.active,
            a.first_reported_at,
            a.last_reported_at,
            CASE WHEN a.active THEN NULL ELSE a.deactivated_at END,
            a.created_at,
            a.updated_at
        FROM aggregated a
        ON CONFLICT (plate_id, user_id, report_type_id)
        DO UPDATE
        SET active = (public.plate_reports.active OR EXCLUDED.active),
            first_reported_at = LEAST(public.plate_reports.first_reported_at, EXCLUDED.first_reported_at),
            last_reported_at = GREATEST(public.plate_reports.last_reported_at, EXCLUDED.last_reported_at),
            created_at = LEAST(public.plate_reports.created_at, EXCLUDED.created_at),
            updated_at = GREATEST(public.plate_reports.updated_at, EXCLUDED.updated_at),
            deactivated_at = CASE
                WHEN (public.plate_reports.active OR EXCLUDED.active) THEN NULL
                WHEN public.plate_reports.deactivated_at IS NULL THEN EXCLUDED.deactivated_at
                WHEN EXCLUDED.deactivated_at IS NULL THEN public.plate_reports.deactivated_at
                ELSE GREATEST(public.plate_reports.deactivated_at, EXCLUDED.deactivated_at)
            END;

        DELETE FROM public.plate_reports r
        USING public.plate_report_types t
        WHERE r.report_type_id = t.id
          AND t.code IN ('HIT_AND_RUN', 'AGGRESSIVE_DRIVER');
    END IF;

    DELETE FROM public.plate_report_types
    WHERE code IN ('HIT_AND_RUN', 'AGGRESSIVE_DRIVER');
END
$$;
