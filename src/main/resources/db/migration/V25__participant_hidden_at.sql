-- Supports per-user "delete conversation" (self-only, auto-revives when the other
-- participant sends a new message) without touching shared chat_rooms/chat_messages content.
ALTER TABLE public.participants ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP NULL;
