-- Drop unused english 'label' column from fixed Java Enum lookup tables.
-- These translations are now securely managed via messages_tr.properties and LocalizedEnumService in the application layer.

ALTER TABLE public.user_subscription_statuses DROP COLUMN label;
ALTER TABLE public.plate_statuses DROP COLUMN label;
ALTER TABLE public.comment_report_reasons DROP COLUMN label;
ALTER TABLE public.comment_report_statuses DROP COLUMN label;
ALTER TABLE public.plate_removal_request_reasons DROP COLUMN label;
ALTER TABLE public.plate_removal_request_statuses DROP COLUMN label;
ALTER TABLE public.plate_report_severities DROP COLUMN label;
ALTER TABLE public.plate_review_moderation_action_types DROP COLUMN label;
ALTER TABLE public.user_role_codes DROP COLUMN label;
ALTER TABLE public.friendship_request_statuses DROP COLUMN label;
ALTER TABLE public.plate_review_statuses DROP COLUMN label;
