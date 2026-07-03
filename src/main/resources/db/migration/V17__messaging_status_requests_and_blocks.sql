-- Message delivery/read status (single tick / double tick / read), server-authoritative
ALTER TABLE chat_messages ADD COLUMN message_status VARCHAR(20) NOT NULL DEFAULT 'SENT';
ALTER TABLE chat_messages ADD COLUMN delivered_at TIMESTAMP;
ALTER TABLE chat_messages ADD COLUMN read_at TIMESTAMP;

-- Backfill existing read messages
UPDATE chat_messages SET message_status = 'READ', read_at = sent_at WHERE is_read = TRUE;

-- Message request / approval flow on 1-1 rooms (existing rooms stay ACCEPTED)
ALTER TABLE chat_rooms ADD COLUMN request_status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED';
ALTER TABLE chat_rooms ADD COLUMN initiator_id BIGINT REFERENCES users(id);

-- User blocking
CREATE TABLE user_blocks (
    id BIGSERIAL PRIMARY KEY,
    blocker_id BIGINT NOT NULL REFERENCES users(id),
    blocked_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_blocks_blocker_blocked UNIQUE (blocker_id, blocked_id),
    CONSTRAINT chk_user_blocks_no_self CHECK (blocker_id <> blocked_id)
);

CREATE INDEX idx_user_blocks_blocker_id ON user_blocks(blocker_id);
CREATE INDEX idx_user_blocks_blocked_id ON user_blocks(blocked_id);
