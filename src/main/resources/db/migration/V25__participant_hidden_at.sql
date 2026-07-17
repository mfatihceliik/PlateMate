-- participants predates Flyway on some environments and was never created by a
-- migration; recreate its pre-this-migration shape here.
CREATE TABLE IF NOT EXISTS public.participants (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(id),
    chat_room_id BIGINT REFERENCES chat_rooms(id),
    joined_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_participants_user_chat_room UNIQUE (user_id, chat_room_id)
);

-- Supports per-user "delete conversation" (self-only, auto-revives when the other
-- participant sends a new message) without touching shared chat_rooms/chat_messages content.
ALTER TABLE public.participants ADD COLUMN IF NOT EXISTS hidden_at TIMESTAMP NULL;
