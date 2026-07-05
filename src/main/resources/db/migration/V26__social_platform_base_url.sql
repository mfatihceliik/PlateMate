-- V23/V24 added the platform catalog (icon_url + colors) but not a canonical profile-root URL.
-- The Android client now auto-detects which platform a pasted profile URL belongs to by matching
-- its host against this base_url, so the platform-URL mapping must live in the backend (not hardcoded
-- in the app). base_url is the profile-root of each platform, e.g. https://www.instagram.com/.
ALTER TABLE public.social_platforms
    ADD COLUMN IF NOT EXISTS base_url VARCHAR(500);

UPDATE public.social_platforms SET base_url = 'https://www.instagram.com/'   WHERE code = 'INSTAGRAM';
UPDATE public.social_platforms SET base_url = 'https://x.com/'               WHERE code = 'X';
UPDATE public.social_platforms SET base_url = 'https://www.facebook.com/'    WHERE code = 'FACEBOOK';
UPDATE public.social_platforms SET base_url = 'https://www.linkedin.com/in/' WHERE code = 'LINKEDIN';
UPDATE public.social_platforms SET base_url = 'https://github.com/'          WHERE code = 'GITHUB';
UPDATE public.social_platforms SET base_url = 'https://www.snapchat.com/add/' WHERE code = 'SNAPCHAT';
