-- PlateRemovalRequest entity moved from FK-based reason_id to a flat reason_code string (same
-- code-string convention as plate_removal_request_reasons.code / checkIfReasonExists validation),
-- but plate_removal_requests was never migrated to match — Hibernate validate fails on the missing
-- column. Backfill reason_code from the existing reason_id -> plate_removal_request_reasons.code
-- join; reason_id itself is left in place (unmapped by the entity now, harmless to keep).

ALTER TABLE public.plate_removal_requests ADD COLUMN IF NOT EXISTS reason_code VARCHAR(64);

UPDATE public.plate_removal_requests r
SET reason_code = pr.code
FROM public.plate_removal_request_reasons pr
WHERE r.reason_id = pr.id
  AND r.reason_code IS NULL;

UPDATE public.plate_removal_requests SET reason_code = 'OTHER' WHERE reason_code IS NULL;

ALTER TABLE public.plate_removal_requests ALTER COLUMN reason_code SET NOT NULL;
