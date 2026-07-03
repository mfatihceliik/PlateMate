-- Online presence visibility preference (reciprocal): hide your own online status
-- and you also stop seeing others'. Defaults to visible to preserve current behavior.
ALTER TABLE user_settings ADD COLUMN online_visibility_enabled BOOLEAN NOT NULL DEFAULT TRUE;
